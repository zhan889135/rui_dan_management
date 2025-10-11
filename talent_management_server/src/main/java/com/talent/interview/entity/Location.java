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
 * 面试点位
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_interview_location")
public class Location extends BaseEntity {

    /** 主键ID */
    @TableId
    private Integer id;

    /** 供应商id */
    private String deptId;

    /** 供应商名称 */
    private String deptName;

    /** 公司名称 */
    private String companyName;

    /** 面试点位名称 */
    private String name;

    /** 面试日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    /** 面试日期 */
    @TableField(exist = false)
    private String[] dateRange;

    /** 需求量 */
    private Integer demand;

    /** 招聘状态Y-正在招聘，N停止招聘 */
    private String status;

    /** 详情 */
    private String details;
}
