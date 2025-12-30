package com.talent.interview.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 统计类型的工具类
 */
public class StatisticsUtil {

    /**
     * 新增总数
     * 数据源：刨除掉今天，最近的一天的总数
     * 总送人数求和；
     * 硬性条件选是求和；
     * 计费选是求和；
     * 未出总数:硬性条件和计费条件为空的求和
     * @param fullList 面试反馈集合
     * @return 返回Map
     */
    public static Map<String, String> calculateFeedbackStats(List<Feedback> fullList) {
        Map<String, String> map = Map.of(
                "totalCount", "0",
                "hardRequirementsYesCount", "0",
                "isBillingYesCount", "0",
                "bothNullCount", "0"
        );
        if (fullList == null || fullList.isEmpty()) {
            return map;
        }

        // Step 1: 过滤掉 interviewDate 为 null 的数据，并转换为 LocalDate
        List<Feedback> validList = fullList.stream()
                .filter(f -> f.getInterviewDate() != null)
                .toList();

        if (validList.isEmpty()) {
            return map;
        }

        // Step 2: 找出所有 interviewDate 中 **严格小于今天** 的最近一天
        LocalDate today = LocalDate.now();
        Optional<LocalDate> nearestDateOpt = validList.stream()
                .map(f -> f.getInterviewDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .filter(date -> date.isBefore(today)) // ⬅️ 关键修改：排除今天，只取之前
                .max(LocalDate::compareTo); // 最大的即最近的

        if (nearestDateOpt.isEmpty()) {
            return map;
        }

        LocalDate nearestDate = nearestDateOpt.get();

        // Step 3: 筛选出这一天的所有数据
        List<Feedback> nearestDayList = validList.stream()
                .filter(f -> {
                    LocalDate date = f.getInterviewDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return date.equals(nearestDate);
                })
                .toList();

        // 总送人数求和；
        long totalCount = nearestDayList.size();

        // 获取登录人部门等级
        Integer deptLevel = SecurityUtils.getLoginUser().getDeptLevel();

        // 根据部门等级，选择字段获取方式
        Function<Feedback, String> hardReqGetter;
        Function<Feedback, String> billingGetter;

        // 一、二级 ：硬性条件、是否计费
        if (deptLevel != null && (Constants.RET_CODE_1_NUM == deptLevel || Constants.RET_CODE_2_NUM == deptLevel)) {
            hardReqGetter = Feedback::getHardRequirements;
            billingGetter = Feedback::getIsBilling;
        }
        // 三级 ：硬性条件2、是否计费2
        else {
            hardReqGetter = Feedback::getHardRequirements2;
            billingGetter = Feedback::getIsBilling2;
        }

        // 硬性条件选是
        long hardRequirementsYesCount = nearestDayList.stream()
                .map(hardReqGetter)
                .filter(Constants.IS_DEL_Y::equalsIgnoreCase)
                .count();

        // 是否计费选是
        long isBillingYesCount = nearestDayList.stream()
                .map(billingGetter)
                .filter(Constants.IS_DEL_Y::equalsIgnoreCase)
                .count();

        // 未出总数（两个字段都为空）
        long bothNullCount = nearestDayList.stream()
                .filter(f -> StringUtils.isEmpty(hardReqGetter.apply(f))
                        && StringUtils.isEmpty(billingGetter.apply(f)))
                .count();

        // 返回结果
        return Map.of(
                "totalCount", String.valueOf(totalCount),
                "hardRequirementsYesCount", String.valueOf(hardRequirementsYesCount),
                "isBillingYesCount", String.valueOf(isBillingYesCount),
                "bothNullCount", String.valueOf(bothNullCount)
        );
    }


    /**
     * 查询财务总金额
     */
    public static Map<String, String> calculateFinanceMoney(List<Finance> fullList) {
        Map<String, String> map = Map.of("totalMoney", "0");
        if (fullList == null || fullList.isEmpty()) {
            return map;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Finance finance : fullList) {
            BigDecimal price = finance.getTotalPrice();
            if (price != null) {
                total = total.add(price);
            }
        }

        // 使用 toPlainString() 避免科学计数法，且保留所有小数位
        return Map.of("totalMoney", total.toPlainString());
    }

}
