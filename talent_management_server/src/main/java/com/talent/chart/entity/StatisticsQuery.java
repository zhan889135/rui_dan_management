package com.talent.chart.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 统计分析菜单 查询项
 */
@Data
public class StatisticsQuery {

    /** 点位 */
    private Integer locationId;

    /** 供应商 */
    private Integer deptId;

    /** 面试日期 */
    @TableField(exist = false)
    private String[] dateRange;
}
