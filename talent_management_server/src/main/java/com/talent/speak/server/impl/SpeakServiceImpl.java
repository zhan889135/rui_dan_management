package com.talent.speak.server.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.StringUtils;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Location;
import com.talent.interview.mapper.FeedbackMapper;
import com.talent.interview.mapper.LocationMapper;
import com.talent.interview.service.LocationService;
import com.talent.speak.entity.Speak;
import com.talent.speak.server.SpeakService;
import com.talent.system.entity.SysDept;
import com.talent.system.mapper.SysDeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 交流对接
 */
@Service
public class SpeakServiceImpl implements SpeakService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    /**
     * 查询供应商邀约总数
     */
    @Override
    public AjaxResult selectDeptInvitationCount(Feedback entity) {
        // 查询全部部门
        List<SysDept> sysDeptList = sysDeptMapper.selectList(null);

        // 查询指定日期的
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        // 日期改为范围查询
        if (entity.getInterviewDate() != null) {
            LocalDate localDate = entity.getInterviewDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            wrapper.between(Feedback::getInterviewDate,
                    localDate.atStartOfDay(),
                    localDate.plusDays(1).atStartOfDay());
        }
        List<Feedback> feedbackList = feedbackMapper.selectList(wrapper);

        // 部门 id -> 部门名称
        Map<Long, String> deptMap = sysDeptList.stream()
                .filter(d -> d.getDeptId() != null)
                .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName, (v1, v2) -> v1));

        List<Speak> speakList = buildSpeakList(
                feedbackList,
                fb -> fb.getDeptId() != null ? fb.getDeptId().longValue() : null,
                id -> deptMap.getOrDefault(id, "未知供应商")
        );

        return AjaxResult.success(speakList);
    }


    /**
     * 查询招聘人邀约总数
     */
    @Override
    public AjaxResult selectPeopleInvitationCount(Feedback entity) {
        // 查询全部部门
        List<SysDept> sysDeptList = sysDeptMapper.selectList(null);

        // 查询指定日期和部门的
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        // 日期改为范围查询
        if (entity.getInterviewDate() != null) {
            LocalDate localDate = entity.getInterviewDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            wrapper.between(Feedback::getInterviewDate,
                    localDate.atStartOfDay(),
                    localDate.plusDays(1).atStartOfDay());
        }
        wrapper.eq(entity.getDeptId() != null, Feedback::getDeptId, entity.getDeptId());
        List<Feedback> feedbackList = feedbackMapper.selectList(wrapper);

        // subDeptId -> 部门名称，这里也可以根据 sysDeptList 做映射
        Map<Long, String> deptMap = sysDeptList.stream()
                .filter(d -> d.getDeptId() != null)
                .collect(Collectors.toMap(SysDept::getDeptId, SysDept::getDeptName, (v1, v2) -> v1));

        List<Speak> speakList = buildSpeakList(
                feedbackList,
                fb -> fb.getSubDeptId() != null ? fb.getSubDeptId().longValue() : null,
                id -> deptMap.getOrDefault(id, "未知招聘人")
        );

        return AjaxResult.success(speakList);
    }

    /**
     * 将 Feedback 列表按指定字段分组，统计数量并转换为 Speak 列表
     * @param feedbackList  面试反馈数据
     * @param keyExtractor  分组字段（例如 Feedback::getDeptId 或 Feedback::getSubDeptId）
     * @param nameResolver  id 转换成名称的函数（例如 deptMap::get）
     */
    private List<Speak> buildSpeakList(List<Feedback> feedbackList,
                                       Function<Feedback, Long> keyExtractor,
                                       Function<Long, String> nameResolver) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 按指定字段分组统计数量
        Map<Long, Long> countMap = feedbackList.stream()
                .map(fb -> {
                    Long key = keyExtractor.apply(fb);
                    return key != null ? new AbstractMap.SimpleEntry<>(key, fb) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.counting()));

        if (countMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 转换为 Speak 列表
        return countMap.entrySet().stream()
                .map(entry -> {
                    Speak speak = new Speak();
                    speak.setKey(String.valueOf(entry.getKey())); // id
                    speak.setName(nameResolver.apply(entry.getKey())); // 名称
                    speak.setCount(String.valueOf(entry.getValue()));
                    return speak;
                })
                .sorted((s1, s2) -> {
                    int c1 = Integer.parseInt(Optional.ofNullable(s1.getCount()).orElse("0"));
                    int c2 = Integer.parseInt(Optional.ofNullable(s2.getCount()).orElse("0"));
                    return Integer.compare(c2, c1); // 大的在前面
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询邀约明细
     */
    @Override
    public AjaxResult selectInvitationInfo(Feedback entity) {
        // 查询日期为明天的
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();

        // 日期改为范围查询
        if (entity.getInterviewDate() != null) {
            LocalDate localDate = entity.getInterviewDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            wrapper.between(Feedback::getInterviewDate,
                    localDate.atStartOfDay(),
                    localDate.plusDays(1).atStartOfDay());
        }

        wrapper.eq(null != entity.getDeptId(), Feedback::getDeptId, entity.getDeptId());
        wrapper.eq(StringUtils.isNotEmpty(entity.getCreateBy()), Feedback::getCreateBy, entity.getCreateBy());
        wrapper.orderByDesc(Feedback::getCreateTime);
        // 面试反馈数据集
        List<Feedback> feedbackList = feedbackMapper.selectList(wrapper);

        return AjaxResult.success(feedbackList);
    }

    /**
     * 保存邀约信息
     */
    @Override
    public AjaxResult saveInvitationInfo(Feedback entity) {
        // 查询全部部门
        List<SysDept> sysDeptList = sysDeptMapper.selectList(null);

        // 子部门id
        Integer subDeptId = entity.getSubDeptId();
        if (subDeptId != null) {
            // 找到 subDeptId 对应的部门
            SysDept subDept = sysDeptList.stream()
                    .filter(d -> d.getDeptId().equals(Long.valueOf(subDeptId)))
                    .findFirst()
                    .orElse(null);

            if (subDept != null) {
                if (subDept.getDeptLevel() != null && subDept.getDeptLevel() != Constants.RET_CODE_3_NUM) {
                    // 不是三级 → 直接用它自己
                    entity.setDeptId(subDeptId);
                    entity.setDeptName(entity.getSubDeptName());
                } else {
                    // 是三级 → 向上找二级部门
                    SysDept current = subDept;
                    while (current != null && current.getDeptLevel() != null && current.getDeptLevel() != Constants.RET_CODE_2_NUM) {
                        Long parentId = current.getParentId();
                        if (parentId == null) break;
                        current = sysDeptList.stream()
                                .filter(d -> d.getDeptId().equals(parentId))
                                .findFirst()
                                .orElse(null);
                    }
                    // 找到二级部门
                    if (current != null && current.getDeptLevel() != null && current.getDeptLevel() == Constants.RET_CODE_2_NUM) {
                        entity.setDeptId(current.getDeptId().intValue());
                        entity.setDeptName(current.getDeptName());
                    }
                }
            }
        }

        feedbackMapper.insert(entity);
        return AjaxResult.success(entity);
    }
}
