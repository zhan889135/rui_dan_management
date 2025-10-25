package com.talent.chart.controller;

import com.talent.chart.entity.StatisticsQuery;
import com.talent.chart.service.StatisticsService;
import com.talent.common.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计分析菜单
 */
@RestController
@RequestMapping("/chart/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService service;

    /**
     * 统计分析 - 供应商 计费人数
     */
    @GetMapping("/deptBillingCount")
    public AjaxResult deptBillingCount(StatisticsQuery entity) { return service.deptBillingCount(entity); }

    /**
     * 统计分析 - 招聘员工 计费人数
     */
    @GetMapping("/personBillingCount")
    public AjaxResult personBillingCount(StatisticsQuery entity) { return service.personBillingCount(entity); }
}
