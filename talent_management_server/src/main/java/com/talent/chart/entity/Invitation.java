package com.talent.chart.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 邀约量
 */
@Data
public class Invitation {

    /** 供应商名称 */
    private String deptName;

    /** 公司名称 */
    private String companyName;

    /** 面试点位名称 */
    private String name;

    /** 需求量 */
    private Integer demand;

    /** 邀约量 */
    private Integer invitationCount;

    /** 剩余可邀约 */
    private Integer remainingCount;

    /** 面试日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    /** 面试日期 */
    @TableField(exist = false)
    private String[] dateRange;

    /** 备注 */
    private String remark;

    /** 招聘状态Y-正在招聘，N停止招聘 */
    private String status;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** 排序字段 */
    private String sortField;

    /** 排序方式 */
    private String sortOrder;
}
