package com.talent.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.system.entity.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单表 数据层
 */
public interface SysMenuMapper extends BaseMapper<SysMenu>
{

    @Select({
            "<script>",
            "select distinct m.menu_id, m.parent_id, m.menu_name, m.path, m.component, m.query, m.route_name, m.visible,",
            "m.status, ifnull(m.perms,'') as perms, m.is_frame, m.is_cache, m.menu_type, m.icon, m.order_num, m.create_time",
            "from sys_menu m",
            "left join sys_role_menu rm on m.menu_id = rm.menu_id",
            "left join sys_user_role ur on rm.role_id = ur.role_id",
            "left join sys_role ro on ur.role_id = ro.role_id",
            "where ur.user_id = #{userId}",
            "<if test='menuName != null and menuName != \"\"'>",
            "AND m.menu_name like concat('%', #{menuName}, '%')",
            "</if>",
            "<if test='visible != null and visible != \"\"'>",
            "AND m.visible = #{visible}",
            "</if>",
            "<if test='status != null and status != \"\"'>",
            "AND m.status = #{status}",
            "</if>",
            "order by m.parent_id, m.order_num",
            "</script>"
    })
    List<SysMenu> selectMenuListByUserId(@Param("menuName") String menuName,
                                         @Param("visible") String visible,
                                         @Param("status") String status,
                                         @Param("userId") Long userId);

    @Select("""
        select distinct m.menu_id, m.parent_id, m.menu_name, m.path, m.component, m.query,
               m.route_name, m.visible, m.status, ifnull(m.perms,'') as perms,
               m.is_frame, m.is_cache, m.menu_type, m.icon, m.order_num, m.create_time
        from sys_menu m
             left join sys_role_menu rm on m.menu_id = rm.menu_id
             left join sys_user_role ur on rm.role_id = ur.role_id
             left join sys_role ro on ur.role_id = ro.role_id
             left join sys_user u on ur.user_id = u.user_id
        where u.user_id = #{userId}
          and m.menu_type in ('M', 'C')
          and m.status = 0
          and ro.status = 0
        order by m.parent_id, m.order_num
    """)
    List<SysMenu> selectMenuTreeByUserId(@Param("userId") Long userId);

//    /**
//     * 查询系统菜单列表
//     *
//     * @param menu 菜单信息
//     * @return 菜单列表
//     */
//    public List<SysMenu> selectMenuList(SysMenu menu);
//
//    /**
//     * 根据用户所有权限
//     *
//     * @return 权限列表
//     */
//    public List<String> selectMenuPerms();
//
//    /**
//     * 根据用户查询系统菜单列表
//     *
//     * @param menu 菜单信息
//     * @return 菜单列表
//     */
//    public List<SysMenu> selectMenuListByUserId(SysMenu menu);
//
    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
//    public List<String> selectMenuPermsByRoleId(Long roleId);
//
//    /**
//     * 根据用户ID查询权限
//     *
//     * @param userId 用户ID
//     * @return 权限列表
//     */
//    public List<String> selectMenuPermsByUserId(Long userId);
//
//    /**
//     * 根据用户ID查询菜单
//     *
//     * @return 菜单列表
//     */
//    public List<SysMenu> selectMenuTreeAll();
//
//    /**
//     * 根据用户ID查询菜单
//     *
//     * @param userId 用户ID
//     * @return 菜单列表
//     */
//    public List<SysMenu> selectMenuTreeByUserId(Long userId);
//
//    /**
//     * 根据角色ID查询菜单树信息
//     *
//     * @param roleId 角色ID
//     * @param menuCheckStrictly 菜单树选择项是否关联显示
//     * @return 选中菜单列表
//     */
//    public List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);
//
//    /**
//     * 根据菜单ID查询信息
//     *
//     * @param menuId 菜单ID
//     * @return 菜单信息
//     */
//    public SysMenu selectMenuById(Long menuId);
//
//    /**
//     * 是否存在菜单子节点
//     *
//     * @param menuId 菜单ID
//     * @return 结果
//     */
//    public int hasChildByMenuId(Long menuId);
//
//    /**
//     * 新增菜单信息
//     *
//     * @param menu 菜单信息
//     * @return 结果
//     */
//    public int insertMenu(SysMenu menu);
//
//    /**
//     * 修改菜单信息
//     *
//     * @param menu 菜单信息
//     * @return 结果
//     */
//    public int updateMenu(SysMenu menu);
//
//    /**
//     * 删除菜单管理信息
//     *
//     * @param menuId 菜单ID
//     * @return 结果
//     */
//    public int deleteMenuById(Long menuId);
//
//    /**
//     * 校验菜单名称是否唯一
//     *
//     * @param menuName 菜单名称
//     * @param parentId 父菜单ID
//     * @return 结果
//     */
//    public SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId);
}
