package com.talent.system.service.impl;

import javax.validation.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.talent.common.constant.Constants;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.system.entity.*;
import com.talent.system.mapper.*;
import com.talent.system.service.ISysUserService;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.bean.BeanValidators;
import com.talent.common.constant.UserConstants;
import com.talent.common.utils.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 * 
 * @author JamesRay
 */
@Slf4j
@Service
public class SysUserServiceImpl implements ISysUserService
{
    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

//    @Autowired
//    private ISysConfigService configService;

//    @Autowired
//    private ISysDeptService deptService;

    @Autowired
    protected Validator validator;

    // 导入默认密码
    @Value("${user.password.default}")
    private String defaultPassword;

    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUser> selectUserList(SysUser user)
    {
        // 部门排序显示
        // 103 → 经理办
        // 107 → 综合部
        // 106 → 销售部
        // 104 → 研发中心
        // 105 → 生产工程部
        List<Long> deptSortOrder = Arrays.asList(103L, 107L, 106L, 104L, 105L);

        LambdaQueryWrapper<SysUser> lambdaQueryWrapper = new LambdaQueryWrapper<SysUser>()
                .ne(SysUser::getUserName, "admin")
                .like(StringUtils.isNotNull(user.getUserName()), SysUser::getUserName, user.getUserName())
                .like(StringUtils.isNotNull(user.getPhoneNumber()), SysUser::getPhoneNumber, user.getPhoneNumber())
                .eq(StringUtils.isNotNull(user.getStatus()), SysUser::getStatus, user.getStatus())
                .and(StringUtils.isNotNull(user.getDeptId()), wrapper -> wrapper
                        .eq(SysUser::getDeptId, user.getDeptId())
                        .or()
                        .inSql(SysUser::getDeptId,
                                "SELECT t.dept_id FROM sys_dept t WHERE FIND_IN_SET(" + user.getDeptId() + ", t.ancestors) AND t.del_flag = 'N'")
                ).last("ORDER BY FIELD(dept_id, " + String.join(",",
                        deptSortOrder.stream().map(String::valueOf).collect(Collectors.toList())) + ")");
        // 时间范围
        if (user.getParams() != null) {
            Object beginTime = user.getParams().get("beginTime");
            Object endTime = user.getParams().get("endTime");

            if (beginTime != null && StringUtils.isNotEmpty(beginTime.toString())) {
                lambdaQueryWrapper.ge(SysUser::getCreateTime, beginTime.toString());
            }

            if (endTime != null && StringUtils.isNotEmpty(endTime.toString())) {
                lambdaQueryWrapper.le(SysUser::getCreateTime, endTime.toString());
            }
        }

        List<SysUser> sysUserList = userMapper.selectList(lambdaQueryWrapper);
        fillDeptName(sysUserList);

        return sysUserList;
    }

    public void fillDeptName(List<SysUser> userList) {
        // 判空：用户列表为空，直接跳出
        if (CollectionUtils.isEmpty(userList)) {
            return;
        }

        // 提取所有非空 deptId
        Set<Long> deptIds = userList.stream()
                .map(SysUser::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 判空：没有有效的 deptId，直接返回
        if (CollectionUtils.isEmpty(deptIds)) {
            return;
        }

        // 查询部门映射表
        Map<Long, String> deptMap = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .in(SysDept::getDeptId, deptIds)
        ).stream().collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName));

        // 设置用户的部门名称
        userList.forEach(u -> u.setDeptName(deptMap.get(u.getDeptId())));
    }



    /**
     * 根据条件分页查询已分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUser> selectAllocatedList(SysUser user)
    {
        return userMapper.selectAllocatedList(user);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUser> selectUnallocatedList(SysUser user)
    {
        return userMapper.selectUnallocatedList(user);
    }

    /**
     * 通过用户名查询用户
     * 
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName)
    {
        return Optional.ofNullable(
                userMapper.selectOne(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getUserName, userName)
                                .last("LIMIT 1")
        )).orElse(new SysUser());
    }

    /**
     * 通过用户ID查询用户
     */
    @Override
    public SysUser selectUserById(Long userId)
    {
        return userMapper.selectById(userId);
    }

    /**
     * 查询用户所属角色组
     * 
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(Long userId)
    {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) return "";

        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleId, roleIds));

        StringBuffer idsStr = new StringBuffer();
        for (SysRole role : roles)
        {
            idsStr.append(role.getRoleName()).append(",");
        }
        if (StringUtils.isNotEmpty(idsStr.toString()))
        {
            return idsStr.substring(0, idsStr.length() - 1);
        }
        return idsStr.toString();
    }

    /**
     * 查询用户所属岗位组
     * 
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(Long userId) {
        // 查询岗位 ID 列表
        List<Long> postIds = userPostMapper.selectList(
                        new LambdaQueryWrapper<SysUserPost>()
                                .eq(SysUserPost::getUserId, userId)
                ).stream()
                .map(SysUserPost::getPostId)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return "";
        }

        // 根据岗位 ID 查询岗位信息
        List<SysPost> posts = postMapper.selectList(
                new LambdaQueryWrapper<SysPost>()
                        .in(SysPost::getPostId, postIds)
        );

        // 拼接岗位名称
        return posts.stream()
                .map(SysPost::getPostName)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(","));
    }


    /**
     * 校验用户名称是否唯一
     */
    @Override
    public boolean checkUserNameUnique(SysUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        SysUser info = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserName, user.getUserName())
                        .last("LIMIT 1")
        );
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     * @return
     */
    @Override
    public boolean checkPhoneUnique(SysUser user) {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();

        SysUser info = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhoneNumber, user.getPhoneNumber())
                        .last("limit 1")
        );

        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }


    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     * @return
     */
//    @Override
//    public boolean checkEmailUnique(SysUser user)
//    {
//        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
//        SysUser info = userMapper.checkEmailUnique(user.getEmail());
//        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
//        {
//            return UserConstants.NOT_UNIQUE;
//        }
//        return UserConstants.UNIQUE;
//    }

    /**
     * 校验用户是否允许操作
     */
    @Override
    public void checkUserAllowed(SysUser user)
    {
        if (StringUtils.isNotNull(user.getUserId()) && user.getUserId() == Constants.RET_CODE_1_NUM)
        {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 新增保存用户信息
     */
    @Override
    @Transactional
    public int insertUser(SysUser user){
        // 新增用户信息
        int rows = userMapper.insert(user);
        // 新增用户岗位关联
        insertUserPost(user);
        // 新增用户与角色管理
        insertUserRole(user.getUserId(), user.getRoleIds());
        return rows;
    }

    /**
     * 新增用户岗位信息
     *
     * @param user 用户对象
     */
    public void insertUserPost(SysUser user)
    {
        Long[] posts = user.getPostIds();
        if (StringUtils.isNotEmpty(posts))
        {
            // 新增用户与岗位管理
            for (Long postId : posts)
            {
                SysUserPost up = new SysUserPost();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                userPostMapper.insert(up);
            }
        }
    }

    /**
     * 新增用户角色信息
     */
    public void insertUserRole(Long userId, Long[] roleIds)
    {
        if (StringUtils.isNotEmpty(roleIds))
        {
            // 新增用户与角色管理
            for (Long roleId : roleIds)
            {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    /**
     * 注册用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
//    @Override
//    public boolean registerUser(SysUser user)
//    {
//        return userMapper.insertUser(user) > 0;
//    }

    /**
     * 修改保存用户信息
     */
    @Override
    @Transactional
    public int updateUser(SysUser user)
    {
        Long userId = user.getUserId();

        // 1. 删除用户与角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        // 2. 删除用户与岗位关联
        userPostMapper.delete(
                new LambdaQueryWrapper<SysUserPost>()
                        .eq(SysUserPost::getUserId, userId)
        );

        // 新增用户与角色管理
        insertUserRole(user.getUserId(), user.getRoleIds());
        // 新增用户与岗位管理
        insertUserPost(user);

        return userMapper.updateById(user);
    }

    /**
     * 用户授权角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional
    public void insertUserAuth(Long userId, Long[] roleIds)
    {
        userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );
        insertUserRole(userId, roleIds);
    }

    /**
     * 修改用户状态
     */
    @Override
    public int updateUserStatus(SysUser user)
    {
        return userMapper.updateById(user);
    }

    /**
     * 修改用户基本信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user)
    {
        return userMapper.updateById(user);
    }

    /**
     * 修改用户头像
     * 
     * @param userName 用户名
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public int updateUserAvatar(String userName, String avatar)
    {
        return userMapper.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getUserName, userName)
                        .eq(SysUser::getDelFlag, Constants.IS_DEL_N)
                        .set(SysUser::getAvatar, avatar)
        );
    }

    /**
     * 重置用户密码
     */
    @Override
    public int resetPwd(SysUser user)
    {
        return userMapper.updateById(user);
    }

    /**
     * 重置用户密码
     * 
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password)
    {
        return userMapper.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getUserName, userName)
                        .eq(SysUser::getDelFlag, Constants.IS_DEL_N)
                        .set(SysUser::getPassword, password)
        );
    }

    /**
     * 新增用户角色信息
     * 
     * @param user 用户对象
     */
//    public void insertUserRole(SysUser user)
//    {
//        this.insertUserRole(user.getUserId(), user.getRoleIds());
//    }

    /**
     * 新增用户岗位信息
     * 
     * @param user 用户对象
     */
//    public void insertUserPost(SysUser user)
//    {
//        Long[] posts = user.getPostIds();
//        if (StringUtils.isNotEmpty(posts))
//        {
//            // 新增用户与岗位管理
//            List<SysUserPost> list = new ArrayList<SysUserPost>(posts.length);
//            for (Long postId : posts)
//            {
//                SysUserPost up = new SysUserPost();
//                up.setUserId(user.getUserId());
//                up.setPostId(postId);
//                list.add(up);
//            }
//            userPostMapper.batchUserPost(list);
//        }
//    }

    /**
     * 新增用户角色信息
     * 
     * @param userId 用户ID
     * @param roleIds 角色组
     */
//    public void insertUserRole(Long userId, Long[] roleIds)
//    {
//        if (StringUtils.isNotEmpty(roleIds))
//        {
//            // 新增用户与角色管理
//            List<SysUserRole> list = new ArrayList<SysUserRole>(roleIds.length);
//            for (Long roleId : roleIds)
//            {
//                SysUserRole ur = new SysUserRole();
//                ur.setUserId(userId);
//                ur.setRoleId(roleId);
//                list.add(ur);
//            }
//            userRoleMapper.batchUserRole(list);
//        }
//    }

    /**
     * 通过用户ID删除用户
     * 
     * @param userId 用户ID
     * @return 结果
     */
//    @Override
//    @Transactional
//    public int deleteUserById(Long userId)
//    {
//        // 删除用户与角色关联
//        return userMapper.deleteById(userId);
//    }

    /**
     * 批量删除用户信息
     */
    @Override
    @Transactional
    public int deleteUserByIds(Long[] userIds) {
        if(StringUtils.isNull(userIds)) return Constants.RET_CODE_0_NUM;
        // 判空 + 判是否包含超级管理员
        if (Arrays.stream(userIds)
                .filter(Objects::nonNull)
                .anyMatch(id -> id.equals((long) Constants.RET_CODE_1_NUM))) {
            throw new ServiceException("不允许操作超级管理员用户");
        }

        return userMapper.update(
                null,
                new LambdaUpdateWrapper<SysUser>()
                        .in(SysUser::getUserId, Arrays.asList(userIds))
                        .set(SysUser::getDelFlag, Constants.IS_DEL_Y)
        );
    }

    /**
     * 导入用户数据
     */
    @Override
    public String importUser(List<SysUserImport> userList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(userList) || userList.size() == 0)
        {
            throw new ServiceException("导入用户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (SysUserImport user : userList)
        {
            try
            {
                if(StringUtils.isNotNull(user.getDeptId())){
                    SysDept sysDept = deptMapper.selectById(user.getDeptId());
                    if (StringUtils.isNull(sysDept)){
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、部门编号不存在");
                        continue;
                    }
                }else{
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、部门编号不能为空");
                    continue;
                }

                if(StringUtils.isEmpty(user.getNickName()) || StringUtils.isNull(user.getNickName())){
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、用户昵称不能为空");
                    continue;
                }
                if(StringUtils.isEmpty(user.getUserName()) || StringUtils.isNull(user.getUserName())){
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、登录账号不能为空");
                    continue;
                }
                if(StringUtils.isNotEmpty(user.getPassword()) && user.getPassword().length() < 6){
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、登录账号的：").append(user.getUserName()).append("，用户密码不能小于六位");
                    continue;
                }

                // 验证是否存在这个用户
                SysUser u = userMapper.selectOne(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getUserName, user.getUserName())
                                .last("LIMIT 1")
                );
                if (StringUtils.isNull(u))
                {
                    BeanValidators.validateWithException(validator, user);
                    // 默认密码123456
                    String passWord = StringUtils.isNotEmpty(user.getPassword()) ? user.getPassword(): defaultPassword;
                    passWord = SecurityUtils.encryptPassword(passWord);
                    u = new SysUser(user.getDeptId(), user.getNickName(), user.getUserName(), passWord, user.getPhoneNumber());
                    userMapper.insert(u);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、登录账号 ").append(user.getUserName()).append(" 导入成功");
                }
                else if (isUpdateSupport)
                {
                    BeanValidators.validateWithException(validator, user);
                    checkUserAllowed(u);
                    u.setDeptId(user.getDeptId());
                    u.setNickName(user.getNickName());
                    u.setUserName(user.getUserName());
                    String passWord = StringUtils.isNotEmpty(user.getPassword()) ? SecurityUtils.encryptPassword(user.getPassword()): u.getPassword();
                    u.setPassword(passWord);
                    u.setPhoneNumber(user.getPhoneNumber());
                    userMapper.updateById(u);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、登录账号 ").append(user.getUserName()).append(" 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>").append(failureNum).append("、登录账号 ").append(user.getUserName()).append(" 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、登录账号 " + user.getUserName() + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    @Override
    public List<SysUser> selectUserKvList(SysUser user) {

        return userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .select(
                                SysUser::getUserId,
                                SysUser::getDeptId,
                                SysUser::getUserName,
                                SysUser::getNickName,
                                SysUser::getPhoneNumber,
                                SysUser::getCreateTime
                        )
                        .and(StringUtils.isNotNull(user.getDeptId()), wrapper -> wrapper
                                .eq(SysUser::getDeptId, user.getDeptId())
                                .or()
                                .inSql(SysUser::getDeptId,
                                        "SELECT t.dept_id FROM sys_dept t WHERE FIND_IN_SET(" + user.getDeptId() + ", t.ancestors) AND t.del_flag = 'N'")
                        ).ne(SysUser::getUserName, "admin")
                        .ne(SysUser::getStatus, Constants.RET_CODE_1)
        );
    }

    /**
     * 查询自己部门下的用户，包含当前部门
     */
    @Override
    public List<SysUser> listUserKvSubDept(SysUser user) {
        // 获取当前部门及子部门 ID
        List<Long> deptIds = DeptPermissionUtil.getDeptAndChildrenIds(SecurityUtils.getDeptId());

        return userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .select(
                                SysUser::getUserId,
                                SysUser::getDeptId,
                                SysUser::getUserName,
                                SysUser::getNickName,
                                SysUser::getPhoneNumber,
                                SysUser::getCreateTime
                        )
                        // 新增：仅限本部门及子部门
                        .in(SysUser::getDeptId, deptIds)
                        // 原有条件：指定部门或其祖先包含该部门
                        .and(StringUtils.isNotNull(user.getDeptId()), wrapper -> wrapper
                                .eq(SysUser::getDeptId, user.getDeptId())
                                .or()
                                .inSql(SysUser::getDeptId,
                                        "SELECT t.dept_id FROM sys_dept t WHERE FIND_IN_SET(" + user.getDeptId() + ", t.ancestors) AND t.del_flag = 'N'")
                        )
                        // 排除 admin 用户
                        .ne(SysUser::getUserName, "admin")
                        // 排除状态为 '1' 的用户（假设 Constants.RET_CODE_1 表示禁用状态）
                        .ne(SysUser::getStatus, Constants.RET_CODE_1)
        );
    }
}
