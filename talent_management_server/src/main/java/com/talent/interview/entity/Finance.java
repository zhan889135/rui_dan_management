package com.talent.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.talent.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 财务表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_finance")
public class Finance extends BaseEntity {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 供应商id */
    private Integer deptId;

    /** 供应商名称 */
    private String deptName;

    /** 面试点位id */
    private Integer locationId;

    /** 面试点位名称 */
    private String locationName;

    /** 金额 */
    private BigDecimal price;

    /** 年龄起始 */
    private Integer ageStart;

    /** 年龄结束 */
    private Integer ageEnd;

    /** 额外金额 */
    private BigDecimal extraPrice;

    /** 创建者名称 */
    private String createName;

    /** 总金额 */
    @TableField(exist = false)
    private BigDecimal totalPrice;

    /** 查询条件：日期范围 */
    @TableField(exist = false)
    private String[] dateRange;
}
