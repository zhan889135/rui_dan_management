package com.talent.chart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.chart.entity.Chart;
import com.talent.chart.entity.StatisticsQuery;
import com.talent.chart.service.StatisticsService;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Feedback;
import com.talent.interview.mapper.FeedbackMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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

        // 用 TreeMap 保持 key 有序（按招聘人名称排序）
        Map<String, FeedbackStats> statsMap = new TreeMap<>();

        for (Feedback f : feedbackList) {

            // createName 为空的情况处理
            String createName = (f.getCreateName() == null || f.getCreateName().trim().isEmpty())
                    ? "未知招聘人"
                    : f.getCreateName().trim();

            // 获取统计对象
            FeedbackStats stats = statsMap.getOrDefault(createName, new FeedbackStats());

            // 1. 总人数
            stats.totalCount++;

            // 2. 硬性条件（不计费 + 硬性条件）
            if (Constants.IS_DEL_N.equals(f.getIsBilling()) && Constants.IS_DEL_Y.equals(f.getHardRequirements())) {
                stats.hardRequirementsCount++;
            }

            // 3. 不合格（不计费 + 非硬性条件）
            if (Constants.IS_DEL_N.equals(f.getIsBilling()) && Constants.IS_DEL_N.equals(f.getHardRequirements())) {
                stats.unqualifiedCount++;
            }

            // 4. 通过（计费=Y）
            if (Constants.IS_DEL_Y.equals(f.getIsBilling())) {
                stats.passedCount++;
            }

            statsMap.put(createName, stats);
        }

        int size = statsMap.size();

        // 创建固定长度数组，保证 echarts 不报错
        String[] createNameData = new String[size];
        Integer[] hardRequirementsData = new Integer[size];
        Integer[] unqualifiedData = new Integer[size];
        Integer[] passedData = new Integer[size];
        Integer[] totalData = new Integer[size];
        Double[] billingRateData = new Double[size];

        int i = 0;
        for (Map.Entry<String, FeedbackStats> entry : statsMap.entrySet()) {

            String key = entry.getKey();
            FeedbackStats s = entry.getValue();

            // 保证所有字段不会为 null
            int total = s.totalCount;
            int hard = s.hardRequirementsCount;
            int passed = s.passedCount;

            // 填入数组
            createNameData[i] = key;
            hardRequirementsData[i] = hard;
            unqualifiedData[i] = s.unqualifiedCount;
            passedData[i] = passed;
            totalData[i] = total;

            // 分母保护：不能 <= 0，否则 ECharts 报错
            int denominator = Math.max(1, total - hard);
            double rate = (double) passed / denominator;

            // 保留两位小数
            billingRateData[i] = new java.math.BigDecimal(rate * 100)
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .doubleValue();

            i++;
        }

        // 设置 Chart 结果
        chart.setCreateNameData(createNameData);
        chart.setHardRequirementsData(hardRequirementsData);
        chart.setUnqualifiedData(unqualifiedData);
        chart.setPassedData(passedData);
        chart.setTotalData(totalData);
        chart.setBillingRateData(billingRateData);

        return AjaxResult.success(chart);
    }

    /**
     * 反馈统计辅助类
     */
    private static class FeedbackStats {
        int totalCount = 0;          // 总人数
        int hardRequirementsCount = 0; // 硬性条件人数
        int unqualifiedCount = 0;    // 不合格人数
        int passedCount = 0;         // 通过人数
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

    /**
     * 统计分析 - 后加的计费率
     */
    @Override
    public AjaxResult rateCalculation(StatisticsQuery entity) {
        List<Feedback> feedbackList = selectList(entity);
        Chart chart = new Chart();

        if (feedbackList == null || feedbackList.isEmpty()) {
            return AjaxResult.success(chart);
        }

        // 使用 Map 按 locationName 分组统计
        Map<String, Chart.PointStat> statMap = new HashMap<>();

        for (Feedback feedback : feedbackList) {
            String locationName = feedback.getLocationName();
            if (locationName == null || locationName.trim().isEmpty()) {
                locationName = "未知点位";
            }

            //总送人数：      总人数，
            //硬性条件不符：   硬性条件=是，
            //剩余分母人数：   总送人数-是否硬性
            //计费人数：      是否计费=是
            //计费率：        计费人数/剩余分母人数
            statMap.putIfAbsent(locationName, new Chart.PointStat(locationName));

            Chart.PointStat stat = statMap.get(locationName);

            // 总送人数 +1
            stat.setTotalSent(stat.getTotalSent() + 1);

            // 硬性条件：Y
            if (Constants.IS_DEL_Y.equalsIgnoreCase(feedback.getHardRequirements())) {
                stat.setHardRequirementNotMet(stat.getHardRequirementNotMet() + 1);
            }

            // 是否计费：Y
            if (Constants.IS_DEL_Y.equalsIgnoreCase(feedback.getIsBilling())) {
                stat.setBilledCount(stat.getBilledCount() + 1);
            }
        }

        // 计算剩余分母和计费率
        List<Chart.PointStat> resultList = new ArrayList<>(statMap.values());
        for (Chart.PointStat stat : resultList) {
            // 剩余分母数 = 总送人数 - 硬性条件不符人数
            int remaining = stat.getTotalSent() - stat.getHardRequirementNotMet();

            stat.setRemainingDenominator(remaining);
            // 计费率 = billedCount / remainingDenominator （避免除零）
            if (remaining > 0) {
                BigDecimal rate = BigDecimal.valueOf(stat.getBilledCount())
                        .divide(BigDecimal.valueOf(remaining), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                stat.setBillingRate(rate);
            } else {
                stat.setBillingRate(BigDecimal.ZERO);
            }
        }

        // 设置到底部列表
        chart.setBillingTableData(resultList);

        return AjaxResult.success(chart);
    }
}

