package com.talent.system.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户对象 sys_user
 */
@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity{

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId
    private Long userId;

    /** 部门ID */
    @Excel(name = "部门编号", type = Excel.Type.EXPORT)
    private Long deptId;

    /** 部门名称 */
    @Excel(name = "部门名称", type = Excel.Type.EXPORT)
    @TableField(exist = false)
    private String deptName;

    /** 部门等级(总部门-1；供应商-2；员工-3) */
    @TableField(exist = false)
    private Integer deptLevel;

    /** 登录账号 */
    @Excel(name = "登录账号", type = Excel.Type.EXPORT)
    private String userName;

    /** 用户昵称 */
    @Excel(name = "用户昵称", type = Excel.Type.EXPORT)
    private String nickName;

    /** 用户邮箱 */
    @Excel(name = "用户邮箱")
    private String email;

    /** 手机号码 */
    @Excel(name = "手机号码", cellType = Excel.ColumnType.TEXT, type = Excel.Type.EXPORT)
    private String phoneNumber;

    /** 用户性别 */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idNumber;

    /** 用户头像 */
    private String avatar;

    /** 密码 */
    private String password;

    /** 状态（0正常 1停用） */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用,2=未审核", type = Excel.Type.EXPORT)
    private String status;

    /** 最后登录IP */
    @Excel(name = "最后登录IP", type = Excel.Type.EXPORT)
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date loginDate;

    public SysUser() {}

    public SysUser(Long deptId, String nickName, String userName, String password, String phoneNumber)
    {
        this.deptId = deptId;
        this.nickName = nickName;
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isAdmin()
    {
        return isAdmin(this.userId);
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public static boolean isAdmin(Long userId)
    {
        return userId != null && 1L == userId;
    }

    /** 角色对象 */
    @TableField(exist = false)
    private List<SysRole> roles;

    /** 角色对象 */
    @TableField(exist = false)
    private int roleId;

    /** 角色组 */
    @TableField(exist = false)
    private Long[] roleIds;

    /** 岗位组 */
    @TableField(exist = false)
    private Long[] postIds;
}
