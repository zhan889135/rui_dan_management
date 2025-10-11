package com.talent.chart.controller;

import com.talent.chart.entity.Invitation;
import com.talent.chart.service.HomePageService;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 面试点位统计图
 */
@RestController
@RequestMapping("/chart/homePage")
public class HomePageController extends BaseController {

    @Autowired
    private HomePageService homePageService;

    /**
     * 首页邀约量
     */
    @GetMapping("/invitationCount")
    public AjaxResult invitationCount(Invitation entity) { return homePageService.invitationCount(entity); }

    /**
     * 邀约量top5
     */
    @GetMapping("/invitationTop5")
    public AjaxResult invitationTop5() { return homePageService.invitationTop5(); }

    /**
     * 供应商公告
     */
    @GetMapping("/sysNoticeList")
    public AjaxResult sysNoticeList() { return homePageService.sysNoticeList(); }



    /**
     * 统计供应商明日报备点位人数
     */
    @GetMapping("/tomorrowReportCount")
    public AjaxResult tomorrowReportCount(Report entity) { return homePageService.tomorrowReportCount(entity); }
}
