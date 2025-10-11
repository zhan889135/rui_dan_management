package com.talent.system.service.impl;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.UserConstants;
import com.talent.system.entity.*;
import com.talent.system.mapper.SysRoleDeptMapper;
import com.talent.system.mapper.SysRoleMapper;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.exception.ServiceException;
import com.talent.system.mapper.SysRoleMenuMapper;
import com.talent.system.mapper.SysUserRoleMapper;
import com.talent.system.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 角色 业务层处理
 * 
 * @author JamesRay
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     * 根据条件分页查询角色数据
     * 
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    public List<SysRole> selectRoleList(SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (role.getRoleId() != null && role.getRoleId() != 0) {
            wrapper.eq(SysRole::getRoleId, role.getRoleId());
        }

        if (StringUtils.isNotEmpty(role.getRoleName())) {
            wrapper.like(SysRole::getRoleName, role.getRoleName());
        }

        if (StringUtils.isNotEmpty(role.getRoleKey())) {
            wrapper.like(SysRole::getRoleKey, role.getRoleKey());
        }

        if (StringUtils.isNotEmpty(role.getStatus())) {
            wrapper.eq(SysRole::getStatus, role.getStatus());
        }

        // 时间范围过滤
        if (role.getParams() != null) {
            Object beginTime = role.getParams().get("beginTime");
            Object endTime = role.getParams().get("endTime");

            if (beginTime != null && StringUtils.isNotEmpty(beginTime.toString())) {
                wrapper.ge(SysRole::getCreateTime, beginTime.toString());
            }
            if (endTime != null && StringUtils.isNotEmpty(endTime.toString())) {
                wrapper.le(SysRole::getCreateTime, endTime.toString());
            }
        }

        // 排序
        wrapper.orderByAsc(SysRole::getRoleSort);

        return roleMapper.selectList(wrapper);
    }


    /**
     * 根据用户ID查询角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        // 1. 获取当前用户已分配的角色 ID 列表
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        Set<Long> assignedRoleIds = new HashSet<>();
        for (SysUserRole ur : userRoles) {
            if (ur.getRoleId() != null) {
                assignedRoleIds.add(ur.getRoleId());
            }
        }

        // 2. 查询所有角色（del_flag = 0）
        List<SysRole> allRoles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .orderByAsc(SysRole::getRoleSort)
        );

        // 3. 设置标记
        for (SysRole role : allRoles) {
            if (assignedRoleIds.contains(role.getRoleId())) {
                role.setFlag(true);
            }
        }

        return allRoles;
    }


    /**
     * 根据用户ID查询权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        // 1. 查询当前用户关联的角色 ID
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        List<Long> roleIds = new ArrayList<>();
        for (SysUserRole ur : userRoles) {
            if (ur.getRoleId() != null) {
                roleIds.add(ur.getRoleId());
            }
        }

        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        // 2. 查询角色 key，过滤 del_flag != 0 的
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getRoleId, roleIds)
        );

        // 3. 拆分 roleKey（支持逗号分隔多个权限），加入 Set
        Set<String> permsSet = new HashSet<>();
        for (SysRole role : roles) {
            if (role != null && StringUtils.isNotEmpty(role.getRoleKey())) {
                permsSet.addAll(Arrays.asList(role.getRoleKey().trim().split(",")));
            }
        }

        return permsSet;
    }


    /**
     * 查询所有角色
     * 
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return roleMapper.selectList(null);
    }

    /**
     * 根据用户ID获取角色选择框列表
     * 
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId) {
        // 第一步：从 sys_user_role 中查询该用户绑定的所有 roleId
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        // 第二步：提取 roleId 列表（可直接返回，也可用于下一步查详情）
        List<Long> roleIds = new ArrayList<>();
        for (SysUserRole ur : userRoles) {
            if (ur.getRoleId() != null) {
                roleIds.add(ur.getRoleId());
            }
        }

        return roleIds;
    }


    /**
     * 通过角色ID查询角色
     * 
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectById(roleId);
    }

    /**
     * 校验角色名称是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role) {
        Long roleId = (role.getRoleId() == null) ? -1L : role.getRoleId();

        SysRole info = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleName, role.getRoleName())
                        .last("limit 1")
        );

        if (info != null && !info.getRoleId().equals(roleId)) {
            return UserConstants.NOT_UNIQUE;
        }

        return UserConstants.UNIQUE;
    }


    /**
     * 校验角色权限是否唯一
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role) {
        Long roleId = (role.getRoleId() == null) ? -1L : role.getRoleId();

        SysRole info = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleKey, role.getRoleKey())
                        .last("limit 1")
        );

        return (info != null && !info.getRoleId().equals(roleId))
                ? UserConstants.NOT_UNIQUE
                : UserConstants.UNIQUE;
    }


    /**
     * 校验角色是否允许操作
     * 
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     * 
     * @param roleIds 角色id
     */
    @Override
    public void checkRoleDataScope(Long... roleIds)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            for (Long roleId : roleIds)
            {

                List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleId, roleId));

                if (StringUtils.isEmpty(roles))
                {
                    throw new ServiceException("没有权限访问角色数据！");
                }
            }
        }
    }

    /**
     * 通过角色ID查询角色使用数量
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId) {
        return userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
        );
    }


    /**
     * 新增保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRole(SysRole role)
    {
        // 新增角色信息
        roleMapper.insert(role);
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRole(SysRole role) {
        // 1. 更新角色（MyBatis-Plus 会自动忽略 null）
        role.setUpdateTime(new Date()); // 如果用了自动填充可省略
        int row = roleMapper.updateById(role);

        // 2. 删除旧的菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, role.getRoleId())
        );

        // 3. 插入新的菜单关联
        return row > 0 ? insertRoleMenu(role) : 0;
    }


    /**
     * 修改角色状态
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateById(role);
    }

    /**
     * 修改数据权限信息
     * 
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional
    public int authDataScope(SysRole role)
    {
        // 修改角色信息
        roleMapper.updateById(role);
        // 删除角色与部门关联
        roleDeptMapper.delete(
                new LambdaQueryWrapper<SysRoleDept>()
                        .eq(SysRoleDept::getRoleId, role.getRoleId())
        );
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(role);
    }

    /**
     * 新增角色菜单信息
     * 
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            for(SysRoleMenu sysRoleMenu : list){
                rows = roleMenuMapper.insert(sysRoleMenu);
            }
        }
        return rows;
    }

    /**
     * 新增角色部门信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            for(SysRoleDept sysRoleDept : list){
                rows = roleDeptMapper.insert(sysRoleDept);
            }
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int deleteRoleById(Long roleId) {
        // 1. 删除角色与菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );

        // 2. 删除角色与部门关联
        roleDeptMapper.delete(
                new LambdaQueryWrapper<SysRoleDept>()
                        .eq(SysRoleDept::getRoleId, roleId)
        );

        // 3. 更新角色为逻辑删除（del_flag = 'Y'）
        SysRole updateRole = new SysRole();
        updateRole.setRoleId(roleId);
        updateRole.setDelFlag("Y");

        return roleMapper.updateById(updateRole);
    }


    /**
     * 批量删除角色信息
     * 
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds) {
        for (Long roleId : roleIds) {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }

        // 1. 删除角色与菜单关联（批量）
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, Arrays.asList(roleIds))
        );

        // 2. 删除角色与部门关联（批量）
        roleDeptMapper.delete(
                new LambdaQueryWrapper<SysRoleDept>()
                        .in(SysRoleDept::getRoleId, Arrays.asList(roleIds))
        );

//        // 3. 批量逻辑删除角色（设置 del_flag = 'Y'）
//        List<SysRole> roleList = Arrays.stream(roleIds).map(roleId -> {
//            SysRole r = new SysRole();
//            r.setRoleId(roleId);
//            r.setDelFlag("Y");
//            return r;
//        }).collect(Collectors.toList());

        int rows = 0;
        for (Long roleId : roleIds) {
            rows = roleMapper.deleteById(roleId);
        }

        return rows;
    }


    /**
     * 取消授权用户角色
     * 
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole) {
        return userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userRole.getUserId())
                        .eq(SysUserRole::getRoleId, userRole.getRoleId())
        );
    }

    /**
     * 批量取消授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        return userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId)
                        .in(SysUserRole::getUserId, Arrays.asList(userIds))
        );
    }

    /**
     * 批量选择授权用户角色
     * 
     * @param roleId 角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        int rows = 0;
        for (Long userId : userIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            rows = userRoleMapper.insert(ur); // 逐个插入
        }
        return rows;
    }
}
