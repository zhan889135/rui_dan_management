package com.talent.interview.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.talent.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 面试报备
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_interview_report")
public class Report extends BaseEntity {

    /** 主键ID */
    @TableId
    private Integer id;

    /** 供应商id */
    private Integer deptId;

    /** 供应商名称 */
    private String deptName;

    /** 分支id */
    private Integer subDeptId;

    /** 分支名称 */
    private String subDeptName;

    /** 面试点位id */
    private Integer locationId;

    /** 面试点位名称 */
    private String locationName;

    /** 姓名 */
    private String name;

    /** 性别 */
    private String sex;

    /** 联系电话 */
    private String phone;

    /** 年龄 */
    private String age;

    /** 学历 */
    private String education;

    /** 面试日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date interviewDate;

    /** 面试时间 */
    private String interviewTime;

    /** 过往经历与收入 */
    private String experience;

    /** 创建者名称 */
    private String createName;

    /** 查询条件：日期范围 */
    @TableField(exist = false)
    private String[] dateRange;
}
