package com.talent.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.mybatisPlus.MyBatisBatchInsertHelper;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Finance;
import com.talent.interview.mapper.FeedbackMapper;
import com.talent.interview.mapper.FinanceMapper;
import com.talent.interview.service.FinanceService;
import com.talent.interview.utils.LambdaQueryBuilderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 财务表 Service 实现类
 */
@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    /**
     * 条件查询
     */
    @Override
    public List<Finance> queryList(Finance entity) {
        LambdaQueryWrapper<Finance> wrapper = LambdaQueryBuilderUtil.buildFinanceQueryWrapper(entity);
        List<Finance> financeList = financeMapper.selectList(wrapper);

        // 面试反馈
        List<Feedback>  feedbackList = feedbackMapper.selectList(null);

        // 循环查询总金额：总金额 = 计费人数 * 金额 + 额外金额 * 计费人数（在年龄区间内的人数）
        for (Finance finance : financeList) {

            // 保险处理：null转空，避免equals空指针
            Integer deptId = finance.getDeptId(); // 供应商ID
            Integer locationId = finance.getLocationId(); // 点位ID

            // ============ 过滤同供应商+点位+计费=Y ============
            List<Feedback> related = feedbackList.stream()
                    .filter(f ->
                            safeEqual(f.getDeptId(), deptId) &&
                                    safeEqual(f.getLocationId(), locationId) &&
                                    Constants.IS_DEL_Y.equalsIgnoreCase(safeStr(f.getIsBilling()))
                    )
                    .toList();

            // 计费人数
            int billingCount = related.size();

            // ============ 年龄区间统计 ============
            long extraCountLong = related.stream()
                    .filter(f -> {
                        Integer age = parseAgeSafe(f.getAge());
                        return age != null &&
                                finance.getAgeStart() != null &&
                                finance.getAgeEnd() != null &&
                                age >= finance.getAgeStart() &&
                                age <= finance.getAgeEnd();
                    })
                    .count();

            // 年龄区间人数
            int extraCount = Math.toIntExact(extraCountLong); // 若超int范围会异常通知你

            // ============ BigDecimal计算总金额 ============
            BigDecimal total = BigDecimal.ZERO;

            // 金额不等于null
            if (finance.getPrice() != null) {
                total = total.add(finance.getPrice().multiply(BigDecimal.valueOf(billingCount)));
            }
            // 额外金额不等于null
            if (finance.getExtraPrice() != null) {
                total = total.add(finance.getExtraPrice().multiply(BigDecimal.valueOf(extraCount)));
            }

            finance.setTotalPrice(total);
        }

        return financeList;
    }

    /** 安全equals，允许null，不报错 */
    private boolean safeEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /** 安全string处理，null→"" */
    private String safeStr(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }

    /**
     * 年龄字符串安全转换数字
     * 支持 null、空串、字母、含中文，如 "25岁", "30周岁"
     */
    private Integer parseAgeSafe(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) return null;

        // 提取其中的数字 "25岁" -> 25
        String number = ageStr.replaceAll("[^0-9]", "");
        if (number.isEmpty()) return null;

        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return null; // 不抛异常，安心返回null
        }
    }

    /**
     * 根据ID获取详细信息
     */
    @Override
    public AjaxResult selectById(Long id) {
        return AjaxResult.success(financeMapper.selectById(id));
    }

    /**
     * 保存或更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult saveOrUpdate(Finance entity) {
        if (entity.getId() == null) {
            entity.setCreateName(SecurityUtils.getLoginUser().getUser().getNickName());
            financeMapper.insert(entity);
        } else {
            MyBatisBatchInsertHelper.updateAllFieldsById(entity, financeMapper);
        }
        return AjaxResult.success(entity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult delete(Long[] ids) {
        List<Long> idList = Arrays.stream(ids).collect(Collectors.toList());

        // 删除数据
        financeMapper.deleteBatchIds(idList);

        return AjaxResult.success();
    }
}
