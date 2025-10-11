package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门表 sys_dept
 */
@Data
@TableName("sys_dept")
@EqualsAndHashCode(callSuper = true)
public class SysDept extends BaseEntity
{
    /** 部门ID */
    @TableId
    @Excel(name = "部门编号", type = Excel.Type.EXPORT)
    private Long deptId;

    /** 父部门ID */
    private Long parentId;

    /** 祖级列表 */
    private String ancestors;

    /** 部门等级(总部门-1；供应商-2；员工及其他-3) */
    private Integer deptLevel;

    /** 供应商名称 */
    @Excel(name = "供应商名称", type = Excel.Type.EXPORT)
    private String deptName;

    /** 经验值统计(面试反馈中，是否计费为Y的) */
    private Integer exp;

    /** 交付力统计(面试反馈中，是否计费为Y||N) */
    private Integer deliveryPower;

    /** 显示顺序 */
    @Excel(name = "显示顺序", type = Excel.Type.EXPORT)
    private Integer orderNum;

    /** 负责人 */
    @Excel(name = "负责人", type = Excel.Type.EXPORT)
    private String leader;

    /** 联系电话 */
    @Excel(name = "联系电话", type = Excel.Type.EXPORT)
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱", type = Excel.Type.EXPORT)
    private String email;

    /** 部门状态:0正常,1停用 */
    @Excel(name = "部门状态", readConverterExp = "0=正常,1=停用", type = Excel.Type.EXPORT)
    private String status;

    /** 删除标志（N代表存在 Y代表删除） */
    @TableLogic
    private String delFlag;

    /** 父部门名称 */
    @TableField(exist = false)
    private String parentName;
    
    /** 子部门 */
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<SysDept>();

    /** 面试点位 */
    @TableField(exist = false)
    private String locationName;

    /** 点位列表 */
//    @TableField(exist = false)
//    private List<SysLocation> sysLocationList = new ArrayList<>();
}
