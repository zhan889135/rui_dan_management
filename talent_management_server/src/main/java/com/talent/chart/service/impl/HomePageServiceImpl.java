package com.talent.chart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.chart.entity.Chart;
import com.talent.chart.entity.InvitationTop5;
import com.talent.chart.entity.Invitation;
import com.talent.chart.service.HomePageService;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Location;
import com.talent.interview.entity.Report;
import com.talent.interview.mapper.FeedbackMapper;
import com.talent.interview.mapper.LocationMapper;
import com.talent.interview.mapper.ReportMapper;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.interview.utils.LambdaQueryBuilderUtil;
import com.talent.system.entity.SysNotice;
import com.talent.system.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试点位统计图
 */
@Service
public class HomePageServiceImpl implements HomePageService {

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private SysNoticeMapper noticeMapper;

    /**
     * 首页邀约量
     */
    @Override
    public AjaxResult invitationCount(Invitation entity) {
        Location location = new Location();
        location.setDeptName(entity.getDeptName());
        location.setName(entity.getName());
        location.setDateRange(entity.getDateRange());
        // 1. 查询面试点位
        LambdaQueryWrapper<Location> locationQueryWrapper = LambdaQueryBuilderUtil.buildLocationQueryWrapper(location);
        List<Location> locationList = locationMapper.selectList(locationQueryWrapper);

        if (locationList.isEmpty()) {
            return AjaxResult.success(Collections.emptyList());
        }

        // 2. 查询面试报备
        LambdaQueryWrapper<Report> reportQueryWrapper = LambdaQueryBuilderUtil.buildReportQueryWrapper(null);
        List<Report> reportList = reportMapper.selectList(reportQueryWrapper);

        // 3. 报备按 locationId 分组统计
        Map<Integer, Long> reportCountMap = reportList.stream()
                .collect(Collectors.groupingBy(Report::getLocationId, Collectors.counting()));

        // 4. 转换成 LocationChart
        List<Invitation> charts = locationList.stream().map(loc -> {
            Invitation chart = new Invitation();
            chart.setDeptName(loc.getDeptName());
            chart.setCompanyName(loc.getCompanyName());
            chart.setName(loc.getName());
            chart.setDate(loc.getDate());
            chart.setStatus(loc.getStatus());
            chart.setRemark(loc.getRemark());

            // 需求量
            int demand = loc.getDemand() == null ? 0 : loc.getDemand();
            chart.setDemand(demand);

            // 邀约量
            int invitationCount = reportCountMap.getOrDefault(loc.getId(), 0L).intValue();
            chart.setInvitationCount(invitationCount);

            // 剩余可邀约（可能为负数）
            chart.setRemainingCount(demand - invitationCount);

            return chart;
        }).collect(Collectors.toList());

        // 5. 内存排序（只对计算字段起作用）
        if (StringUtils.isNotBlank(entity.getSortField()) && StringUtils.isNotBlank(entity.getSortOrder())) {
            Comparator<Invitation> comparator = null;

            switch (entity.getSortField()) {
                case "demand" ->
                        comparator = Comparator.comparing(Invitation::getDemand, Comparator.nullsLast(Integer::compareTo));
                case "invitationCount" ->
                        comparator = Comparator.comparing(Invitation::getInvitationCount, Comparator.nullsLast(Integer::compareTo));
                default -> {}
            }

            if (comparator != null) {
                if ("descending".equals(entity.getSortOrder())) {
                    comparator = comparator.reversed();
                }
                charts = charts.stream().sorted(comparator).collect(Collectors.toList());
            }
        }

        return AjaxResult.success(charts);
    }

    /**
     * 邀约量 TOP5 （按供应商+招聘人分组）
     */
    @Override
    public AjaxResult invitationTop5() {
        LambdaQueryWrapper<Feedback> wrapper;
        // 部门等级，如果是最高级，就不用拼接数据权限了，直接显示全部
        Integer deptLevel = SecurityUtils.getLoginUser().getDeptLevel();
        if(null != deptLevel && Constants.RET_CODE_1_NUM == deptLevel){
            wrapper = new LambdaQueryWrapper<>();
        }else{
            wrapper = DeptPermissionUtil.buildSubDeptScopeWrapper(Feedback::getSubDeptId);
        }

        // 查询所有面试反馈
        List<Feedback> feedbackList = feedbackMapper.selectList(wrapper);
        if (feedbackList.isEmpty()) return AjaxResult.success(Collections.emptyList());

        // 时间范围：本周、本月
        LocalDate now = LocalDate.now();
//        // 周起始：本周周一
//        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
//        // 周结束：本周周日
//        LocalDate weekEnd = weekStart.plusDays(6);

        // 上个月的 1 号
        LocalDate monthStart = now.minusMonths(1).withDayOfMonth(1);

        // 上个月的最后一天
        LocalDate monthEnd = now.minusMonths(1).withDayOfMonth(now.minusMonths(1).lengthOfMonth());

        // 分组统计：key=deptId+createBy + 先过滤掉没有面试日期的
        List<Feedback> validReports = feedbackList.stream()
                .filter(r -> r.getInterviewDate() != null)
                .toList();

        // 定义结果 Map
        Map<String, InvitationTop5> grouped = new HashMap<>();

        for (Feedback r : validReports) {
            // 只统计是否计费为Y的；
            if(StringUtils.isNotEmpty(r.getIsBilling()) && r.getIsBilling().equals(Constants.IS_DEL_Y)){
                String key = r.getSubDeptId() + "_" + r.getCreateBy();

                // 如果不存在就初始化
                InvitationTop5 dto = grouped.get(key);
                if (dto == null) {
                    dto = initDto(r, monthStart, monthEnd);
                    grouped.put(key, dto);
                } else {
                    // 已存在则合并
                    mergeDto(dto, initDto(r, monthStart, monthEnd));
                }
            }
        }

        // 取本月邀约量前5
        List<InvitationTop5> top5 = grouped.values().stream()
                .sorted(Comparator.comparing(InvitationTop5::getMonthCount).reversed())
                .limit(5)
                .toList();

        return AjaxResult.success(top5);
    }

    /** 初始化单个统计对象 */
    private InvitationTop5 initDto(Feedback r,
                                   LocalDate monthStart, LocalDate monthEnd) {
        LocalDate date;

        if (r.getInterviewDate() instanceof java.sql.Date) {
            // 直接安全转换
            date = ((java.sql.Date) r.getInterviewDate()).toLocalDate();
        } else {
            // 保底方案
            date = r.getInterviewDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        InvitationTop5 dto = new InvitationTop5();
        dto.setDeptName(r.getDeptName());
        dto.setCreateBy(r.getCreateBy());
//        dto.setWeekCount(isInWeek(date, weekStart, weekEnd) ? 1 : 0);
        dto.setMonthCount(isInMonth(date, monthStart, monthEnd) ? 1 : 0);
        return dto;
    }


    /**
     * 合并两个统计对象
     */
    private void mergeDto(InvitationTop5 a, InvitationTop5 b) {
        a.setMonthCount(a.getMonthCount() + b.getMonthCount());
    }

    /** 判断是否在本月 */
    private boolean isInMonth(LocalDate date, LocalDate monthStart, LocalDate monthEnd) {
        return !date.isBefore(monthStart) && !date.isAfter(monthEnd);
    }

    /**
     * 供应商公告
     */
    @Override
    public AjaxResult sysNoticeList() {
        LambdaQueryWrapper<SysNotice> wrapper = DeptPermissionUtil.buildDeptScopeWrapper(SysNotice::getDeptId);
        wrapper.eq(SysNotice::getStatus, Constants.RET_CODE_0);
        List<SysNotice> sysNoticeList = noticeMapper.selectList(wrapper);
        return AjaxResult.success(sysNoticeList);
    }

    /**
     * 统计供应商明日报备点位人数
     */
    @Override
    public AjaxResult tomorrowReportCount(Report entity) {

        LambdaQueryWrapper<Report> wrapper = LambdaQueryBuilderUtil.buildReportQueryWrapper(entity);

        // 1. 查询报备数据
        List<Report> reportList = reportMapper.selectList(wrapper);

        // 2. 按 locationId 分组统计，同时保留样本记录
        Map<Integer, List<Report>> grouped = reportList.stream()
                .filter(r -> r.getLocationId() != null)
                .collect(Collectors.groupingBy(Report::getLocationId));

        // 3. 转换为 List<Chart>
        List<Chart> result = grouped.entrySet().stream()
                .map(entry -> {
                    Integer locationId = entry.getKey();
                    List<Report> reports = entry.getValue();
                    long count = reports.size();

                    Chart chart = new Chart();
                    chart.setKey(String.valueOf(locationId));                // id
                    chart.setLabel(reports.get(0).getLocationName());        // 显示用的名称
                    chart.setValue(String.valueOf(count));                   // 人数
                    return chart;
                })
                .toList();

        return AjaxResult.success(result);
    }
}

