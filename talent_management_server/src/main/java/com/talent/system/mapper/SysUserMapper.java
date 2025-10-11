package com.talent.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户表 数据层
 */
public interface SysUserMapper extends BaseMapper<SysUser>{
    @Select({
            "<script>",
            "select distinct u.user_id, u.dept_id, u.user_name, u.nick_name, u.email, u.phone_number, u.status, u.create_time",
            "from sys_user u",
            "left join sys_dept d on u.dept_id = d.dept_id",
            "left join sys_user_role ur on u.user_id = ur.user_id",
            "left join sys_role r on r.role_id = ur.role_id",
            "where u.del_flag = 'N' and r.role_id = #{user.roleId}",
            "<if test='user.userName != null and user.userName != \"\"'>",
            "  AND u.user_name like concat('%', #{user.userName}, '%')",
            "</if>",
            "<if test='user.phoneNumber != null and user.phoneNumber != \"\"'>",
            "  AND u.phone_number like concat('%', #{user.phoneNumber}, '%')",
            "</if>",
            "${user.params.dataScope}",
            "</script>"
    })
    List<SysUser> selectAllocatedList(@Param("user") SysUser user);

    @Select({
            "<script>",
            "select distinct u.user_id, u.dept_id, u.user_name, u.nick_name, u.email, u.phone_number, u.status, u.create_time",
            "from sys_user u",
            "left join sys_dept d on u.dept_id = d.dept_id",
            "left join sys_user_role ur on u.user_id = ur.user_id",
            "left join sys_role r on r.role_id = ur.role_id",
            "where u.del_flag = 'N'",
            "and (r.role_id != #{user.roleId} or r.role_id is null)",
            "and u.user_id not in (",
            "  select ur.user_id from sys_user_role ur where ur.role_id = #{user.roleId}",
            ")",
            "<if test='user.userName != null and user.userName != \"\"'>",
            "  AND u.user_name like concat('%', #{user.userName}, '%')",
            "</if>",
            "<if test='user.phoneNumber != null and user.phoneNumber != \"\"'>",
            "  AND u.phone_number like concat('%', #{user.phoneNumber}, '%')",
            "</if>",
            "${user.params.dataScope}",
            "</script>"
    })
    List<SysUser> selectUnallocatedList(@Param("user") SysUser user);

}
