package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * 角色表 sys_role
 */
@Data
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity
{
    /** 角色ID */
    @TableId
    @Excel(name = "角色ID", type = Excel.Type.EXPORT)
    private Long roleId;

    /** 角色名称 */
    @Excel(name = "角色名称", type = Excel.Type.EXPORT)
    private String roleName;

    /** 角色权限 */
    @Excel(name = "角色权限", type = Excel.Type.EXPORT)
    private String roleKey;

    /** 角色排序 */
    @Excel(name = "角色排序", type = Excel.Type.EXPORT)
    private Integer roleSort;

    /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限） */
    @Excel(name = "数据范围", type = Excel.Type.EXPORT, readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
    private String dataScope;

    /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
    private boolean menuCheckStrictly;

    /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
    private boolean deptCheckStrictly;

    /** 角色状态（0正常 1停用） */
    @Excel(name = "角色状态", type = Excel.Type.EXPORT, readConverterExp = "0=正常,1=停用")
    private String status;

    /** 用户是否存在此角色标识 默认不存在 */
    @TableField(exist = false)
    private boolean flag = false;

    /** 菜单组 */
    @TableField(exist = false)
    private Long[] menuIds;

    /** 部门组（数据权限） */

    @TableField(exist = false)
    private Long[] deptIds;

    /** 角色菜单权限 */
    @TableField(exist = false)
    private Set<String> permissions;

    public SysRole()
    {

    }

    public SysRole(Long roleId)
    {
        this.roleId = roleId;
    }

    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }
}
