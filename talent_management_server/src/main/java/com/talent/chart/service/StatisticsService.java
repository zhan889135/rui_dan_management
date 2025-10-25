package com.talent.chart.service;

import com.talent.chart.entity.StatisticsQuery;
import com.talent.common.domain.AjaxResult;

/**
 * 统计分析菜单
 */
public interface StatisticsService {

    /**
     * 统计分析 - 供应商 计费人数
     */
    AjaxResult deptBillingCount(StatisticsQuery entity);

    /**
     * 统计分析 - 招聘员工 计费人数
     */
    AjaxResult personBillingCount(StatisticsQuery entity);
}

