package com.talent.chart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.chart.entity.Chart;
import com.talent.chart.entity.StatisticsQuery;
import com.talent.chart.service.StatisticsService;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Feedback;
import com.talent.interview.mapper.FeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计分析菜单
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    /**
     * 统计分析 - 供应商 计费人数
     */
    @Override
    public AjaxResult deptBillingCount(StatisticsQuery entity) {
        List<Feedback> feedbackList = selectList(entity);

        Chart chart = new Chart();

        if (feedbackList == null || feedbackList.isEmpty()) {
            return AjaxResult.success(chart);
        }

        // 使用 LinkedHashMap 保证插入顺序（可选，如果你希望按出现顺序展示）
        Map<String, Long> locationCountMap = feedbackList.stream()
                .filter(f -> f.getLocationId() != null && f.getLocationName() != null && Constants.IS_DEL_Y.equals(f.getIsBilling()))
                .collect(Collectors.groupingBy(
                        Feedback::getLocationName,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // 提取 xData 和 yData，确保长度一致
        String[] xData = locationCountMap.keySet().toArray(new String[0]);
        Integer[] yData = locationCountMap.values().stream()
                .map(Long::intValue)
                .toArray(Integer[]::new);

        chart.setX(xData);
        chart.setY(yData);

        return AjaxResult.success(chart);
    }

    /**
     * 统计分析 - 招聘员工 计费人数
     */
    @Override
    public AjaxResult personBillingCount(StatisticsQuery entity) {
        List<Feedback> feedbackList = selectList(entity);

        Chart chart = new Chart();

        if (feedbackList == null || feedbackList.isEmpty()) {
            return AjaxResult.success(chart);
        }

        // 过滤：必须有创建人信息，且 isBilling = 'Y'（计费）
        Map<String, Long> locationCountMap = feedbackList.stream()
                .filter(f -> f.getCreateBy() != null && f.getCreateName() != null && Constants.IS_DEL_Y.equals(f.getIsBilling()))
                .collect(Collectors.groupingBy(
                        Feedback::getCreateName,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // 按计费人数降序排序
        List<Map.Entry<String, Long>> sortedEntries = locationCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        // 提取排序后的 x 和 y
        String[] xData = sortedEntries.stream()
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
        Integer[] yData = sortedEntries.stream()
                .map(entry -> entry.getValue().intValue())
                .toArray(Integer[]::new);

        chart.setX(xData);
        chart.setY(yData);

        return AjaxResult.success(chart);
    }

    /**
     * 统计分析 - 通用面试反馈查询方法
     */
    public List<Feedback> selectList(StatisticsQuery entity){
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        // 精确匹配：点位ID
        wrapper.eq(null != entity.getLocationId(), Feedback::getLocationId, entity.getLocationId());
        // 精确匹配：供应商ID
        wrapper.eq(null != entity.getDeptId(), Feedback::getDeptId, entity.getDeptId());
        // 日期范围查询
        if (null != entity.getDateRange() && entity.getDateRange().length == 2) {
            String beginDate = entity.getDateRange()[0];
            String endDate = entity.getDateRange()[1];
            wrapper.between(Feedback::getInterviewDate, beginDate, endDate);
        }
        // 查询面试反馈
        return feedbackMapper.selectList(wrapper);
    }
}

