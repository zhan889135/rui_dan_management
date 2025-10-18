package com.talent.interview.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 面试反馈
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_interview_feedback")
public class Feedback extends BaseEntity {

    /** 主键ID */
    @TableId
    @Excel(name = "ID", width = 10)
    private Integer id;

    /** 面试点位id */
    private Integer locationId;

    /** 面试点位 */
    @Excel(name = "面试点位", width = 35)
    private String locationName;

    /** 供应商id */
    private Integer deptId;

    /** 供应商名称 */
    @Excel(name = "归属供应商", width = 20)
    private String deptName;

    /** 分支id */
    private Integer subDeptId;

    /** 分支名称 */
    private String subDeptName;

    /** 姓名 */
    @Excel(name = "姓名", width = 10)
    private String name;

    /** 性别 */
    @Excel(name = "性别", width = 10)
    private String sex;

    /** 电话 */
    @Excel(name = "电话", width = 18)
    private String phone;

    /** 年龄 */
    @Excel(name = "年龄", width = 10)
    private String age;

    /** 学历 */
    @Excel(name = "学历", width = 10)
    private String education;

    /** 面试日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "面试日期", width = 15, dateFormat = "yyyy-MM-dd")
    private Date interviewDate;

    /** 反馈原因 */
    @Excel(name = "反馈原因", width = 50)
    private String reason;

    /** 硬性条件1Y-是；N-否 */
    @Excel(name = "硬性条件", readConverterExp = "Y=是,N=否", width = 10)
    private String hardRequirements;

    /** 是否计费1Y-是；N-否 */
    @Excel(name = "是否计费", readConverterExp = "Y=是,N=否", width = 10)
    private String isBilling;

    /** 反馈原因2 */
    private String reason2;

    /** 硬性条件2Y-是；N-否 */
    private String hardRequirements2;

    /** 是否计费2Y-是；N-否 */
    private String isBilling2;

    /** 总部门确认人 */
    private String level1Person;

    /** 总部门确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date level1Time;

    /** 供应商确认人 */
    private String level2Person;

    /** 供应商确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date level2Time;

    /** 创建者名称 */
    private String createName;

    /** 分为三个列表，deptLevel：1总部，能看所有的，2供应商，能看总部推送的，3员工，能看供应商推送的 */
    @TableField(exist = false)
    private Integer deptLevel;

    /** 查询条件：日期范围 */
    @TableField(exist = false)
    private String[] dateRange;
}

