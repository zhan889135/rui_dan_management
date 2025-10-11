package com.talent.chart.service;

import com.talent.chart.entity.Invitation;
import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Report;

/**
 * 面试点位统计图
 */
public interface HomePageService {

    /**
     * 首页邀约量
     */
    AjaxResult invitationCount(Invitation entity);

    /**
     * 邀约量top5
     */
    AjaxResult invitationTop5();

    /**
     * 供应商公告
     */
    AjaxResult sysNoticeList();

    /**
     * 统计供应商明日报备点位人数
     */
    AjaxResult tomorrowReportCount(Report entity);
}

