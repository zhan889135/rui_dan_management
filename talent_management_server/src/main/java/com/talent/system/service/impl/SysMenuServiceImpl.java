package com.talent.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.constant.UserConstants;
import com.talent.system.entity.*;
import com.talent.common.domain.tree.TreeSelect;
import com.talent.common.domain.vo.MetaVo;
import com.talent.common.domain.vo.RouterVo;
import com.talent.system.mapper.SysMenuMapper;
import com.talent.system.mapper.SysRoleMapper;
import com.talent.system.service.ISysMenuService;
import com.talent.common.utils.StringUtils;
import com.talent.system.mapper.SysRoleMenuMapper;
import com.talent.system.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单 业务层处理
 * 
 * @author JamesRay
 */
@Service
public class SysMenuServiceImpl implements ISysMenuService
{
    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;


    /**
     * 根据用户查询系统菜单列表
     * 
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuList(Long userId)
    {
        return selectMenuList(new SysMenu(), userId);
    }

    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId) {
        if (SysUser.isAdmin(userId)) {
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();

            if (StringUtils.isNotEmpty(menu.getMenuName())) {
                wrapper.like(SysMenu::getMenuName, menu.getMenuName());
            }
            if (StringUtils.isNotEmpty(menu.getVisible())) {
                wrapper.eq(SysMenu::getVisible, menu.getVisible());
            }
            if (StringUtils.isNotEmpty(menu.getStatus())) {
                wrapper.eq(SysMenu::getStatus, menu.getStatus());
            }

            wrapper.orderByAsc(SysMenu::getParentId, SysMenu::getOrderNum);
            return menuMapper.selectList(wrapper);
        } else {
            return menuMapper.selectMenuListByUserId(
                    menu.getMenuName(),
                    menu.getVisible(),
                    menu.getStatus(),
                    userId
            );
        }
    }

    /**
     * 查询系统菜单列表
     * 
     * @param menu 菜单信息
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuList(SysMenu menu)
    {
        LambdaQueryWrapper<SysMenu> lambdaQueryWrapper = new LambdaQueryWrapper<SysMenu>()
                .like(StringUtils.isNotNull(menu.getMenuName()), SysMenu::getMenuName, menu.getMenuName())
                .eq(StringUtils.isNotNull(menu.getStatus()), SysMenu::getStatus, menu.getStatus())
                .orderByAsc(SysMenu::getOrderNum);

        return menuMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId)
    {
        List<String> perms = getMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms)
        {
            if (StringUtils.isNotEmpty(perm))
            {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }
    public List<String> getMenuPermsByUserId(Long userId) {
        List<String> perms = new ArrayList<>();

        // 查询用户角色
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

        if (roleIds.isEmpty()) return perms;

        // 查询启用角色
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, "0")
                        .in(SysRole::getRoleId, roleIds)
        );
        roleIds.clear();
        for (SysRole role : roles) {
            roleIds.add(role.getRoleId());
        }

        if (roleIds.isEmpty()) return perms;

        // 查询角色对应菜单
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        );
        List<Long> menuIds = new ArrayList<>();
        for (SysRoleMenu rm : roleMenus) {
            if (rm.getMenuId() != null) {
                menuIds.add(rm.getMenuId());
            }
        }

        if (menuIds.isEmpty()) return perms;

        // 查询菜单权限
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, "0")
                        .in(SysMenu::getMenuId, menuIds)
                        .select(SysMenu::getPerms)
        );
        for (SysMenu m : menus) {
            String p = m.getPerms();
            if (p != null && !perms.contains(p)) {
                perms.add(p);
            }
        }

        return perms;
    }

    /**
     * 根据角色ID查询权限
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId)
    {
        List<String> perms = getMenuPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms)
        {
            if (StringUtils.isNotEmpty(perm))
            {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    public List<String> getMenuPermsByRoleId(Long roleId) {
        // 第一步：查找该角色绑定的菜单ID列表
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );

        List<Long> menuIdList = new ArrayList<>();
        for (SysRoleMenu rm : roleMenus) {
            if (rm.getMenuId() != null && !menuIdList.contains(rm.getMenuId())) {
                menuIdList.add(rm.getMenuId());
            }
        }

        // 如果菜单为空，直接返回空集合
        if (menuIdList.isEmpty()) {
            return new ArrayList<>();
        }

        // 第二步：根据菜单ID列表，查询权限标识（只查启用状态的）
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, "0")
                        .in(SysMenu::getMenuId, menuIdList)
                        .select(SysMenu::getPerms)
        );

        List<String> permsList = new ArrayList<>();
        for (SysMenu m : menus) {
            if (m.getPerms() != null && !permsList.contains(m.getPerms())) {
                permsList.add(m.getPerms());
            }
        }

        return permsList;
    }


    /**
     * 根据用户ID查询菜单
     * 
     * @param userId 用户名称
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId)
    {
        List<SysMenu> menus = null;
        if (SysUser.isAdmin(userId))
        {
            menus = menuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .in(SysMenu::getMenuType, "M", "C")
                            .eq(SysMenu::getStatus, 0)
                            .orderByAsc(SysMenu::getParentId)
                            .orderByAsc(SysMenu::getOrderNum)
            );
        }
        else
        {
            menus = menuMapper.selectMenuTreeByUserId(userId);
        }
        return getChildPerms(menus, 0);
    }

    /**
     * 根据角色ID查询菜单树信息
     * 
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        boolean menuCheckStrictly = role.isMenuCheckStrictly();

        // 第一步：先查出所有菜单 ID
        List<Long> allMenuIds = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .inSql(SysMenu::getMenuId,
                                "select menu_id from sys_role_menu where role_id = " + roleId
                        )
        ).stream().map(SysMenu::getMenuId).collect(Collectors.toList());

        // menuCheckStrictly = true 时，剔除 parent_id
        if (menuCheckStrictly) {
            List<Long> parentIds = menuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .inSql(SysMenu::getMenuId,
                                    "select menu_id from sys_role_menu where role_id = " + roleId
                            )
            ).stream().map(SysMenu::getParentId).distinct().collect(Collectors.toList());

            allMenuIds.removeAll(parentIds);
        }

        return allMenuIds;
    }


    /**
     * 构建前端路由所需要的菜单
     * 
     * @param menus 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus)
    {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus)
        {
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
            List<SysMenu> cMenus = menu.getChildren();
            if (StringUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType()))
            {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            }
            else if (isMenuFrame(menu))
            {
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(getRouteName(menu.getRouteName(), menu.getPath()));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            else if (menu.getParentId().intValue() == 0 && isInnerLink(menu))
            {
                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                String routerPath = innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(UserConstants.INNER_LINK);
                children.setName(getRouteName(menu.getRouteName(), routerPath));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端所需要树结构
     */
    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus)
    {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        List<Long> tempList = menus.stream().map(SysMenu::getMenuId).collect(Collectors.toList());
        for (Iterator<SysMenu> iterator = menus.iterator(); iterator.hasNext();)
        {
            SysMenu menu = (SysMenu) iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(menu.getParentId()))
            {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus)
    {
        List<SysMenu> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 根据菜单ID查询信息
     */
    @Override
    public SysMenu selectMenuById(Long menuId)
    {
        return menuMapper.selectById(menuId);
    }

    /**
     * 是否存在菜单子节点
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        int count = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, menuId)
        );
        return count > 0;
    }


    /**
     * 查询菜单使用数量
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        int count = roleMenuMapper.selectCount(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getMenuId, menuId)
        );
        return count > 0;
    }


    /**
     * 新增保存菜单信息
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public int insertMenu(SysMenu menu)
    {
        return menuMapper.insert(menu);
    }

    /**
     * 修改保存菜单信息
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public int updateMenu(SysMenu menu)
    {
        return menuMapper.updateById(menu);
    }

    /**
     * 删除菜单管理信息
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public int deleteMenuById(Long menuId)
    {
        return menuMapper.deleteById(menuId);
    }

    /**
     * 校验菜单名称是否唯一
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenu menu) {
        Long menuId = StringUtils.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();

        SysMenu info = menuMapper.selectOne(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getMenuName, menu.getMenuName())
                        .eq(SysMenu::getParentId, menu.getParentId())
                        .last("limit 1")
        );

        if (StringUtils.isNotNull(info) && !info.getMenuId().equals(menuId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }


    /**
     * 获取路由名称
     * 
     * @param menu 菜单信息
     * @return 路由名称
     */
    public String getRouteName(SysMenu menu)
    {
        // 非外链并且是一级目录（类型为目录）
        if (isMenuFrame(menu))
        {
            return StringUtils.EMPTY;
        }
        return getRouteName(menu.getRouteName(), menu.getPath());
    }

    /**
     * 获取路由名称，如没有配置路由名称则取路由地址
     * 
     * @param path 路由地址
     * @return 路由名称（驼峰格式）
     */
    public String getRouteName(String name, String path)
    {
        String routerName = StringUtils.isNotEmpty(name) ? name : path;
        return StringUtils.capitalize(routerName);
    }

    /**
     * 获取路由地址
     * 
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu)
    {
        String routerPath = menu.getPath();
        // 内链打开外网方式
        if (menu.getParentId().intValue() != 0 && isInnerLink(menu))
        {
            routerPath = innerLinkReplaceEach(routerPath);
        }
        // 非外链并且是一级目录（类型为目录）
        if (0 == menu.getParentId().intValue() && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame()))
        {
            routerPath = "/" + menu.getPath();
        }
        // 非外链并且是一级目录（类型为菜单）
        else if (isMenuFrame(menu))
        {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     * 
     * @param menu 菜单信息
     * @return 组件信息
     */
    public String getComponent(SysMenu menu)
    {
        String component = UserConstants.LAYOUT;
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu))
        {
            component = menu.getComponent();
        }
        else if (StringUtils.isEmpty(menu.getComponent()) && menu.getParentId().intValue() != 0 && isInnerLink(menu))
        {
            component = UserConstants.INNER_LINK;
        }
        else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu))
        {
            component = UserConstants.PARENT_VIEW;
        }
        return component;
    }

    /**
     * 是否为菜单内部跳转
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isMenuFrame(SysMenu menu)
    {
        return menu.getParentId().intValue() == 0 && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && menu.getIsFrame().equals(UserConstants.NO_FRAME);
    }

    /**
     * 是否为内链组件
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isInnerLink(SysMenu menu)
    {
        return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.ishttp(menu.getPath());
    }

    /**
     * 是否为parent_view组件
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isParentView(SysMenu menu)
    {
        return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 根据父节点的ID获取所有子节点
     * 
     * @param list 分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId)
    {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext();)
        {
            SysMenu t = (SysMenu) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId)
            {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     * 
     * @param list 分类表
     * @param t 子节点
     */
    private void recursionFn(List<SysMenu> list, SysMenu t)
    {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t)
    {
        List<SysMenu> tlist = new ArrayList<SysMenu>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext())
        {
            SysMenu n = (SysMenu) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t)
    {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 内链域名特殊字符替换
     * 
     * @return 替换后的内链域名
     */
    public String innerLinkReplaceEach(String path)
    {
        return StringUtils.replaceEach(path, new String[] { Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":" },
                new String[] { "", "", "", "/", "/" });
    }
}
