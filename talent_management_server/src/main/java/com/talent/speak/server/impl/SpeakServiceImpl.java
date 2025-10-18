package com.talent.speak.server.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Location;
import com.talent.interview.entity.Report;
import com.talent.interview.mapper.FeedbackMapper;
import com.talent.interview.mapper.LocationMapper;
import com.talent.interview.service.LocationService;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.speak.entity.Speak;
import com.talent.speak.server.SpeakService;
import com.talent.system.entity.SysDept;
import com.talent.system.entity.SysUser;
import com.talent.system.mapper.SysDeptMapper;
import com.talent.system.mapper.SysUserMapper;
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
    private SysUserMapper sysUserMapper;

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
     * 查询招聘人邀约总数（按创建人 userName → 昵称 nickName）
     */
    @Override
    public AjaxResult selectPeopleInvitationCount(Feedback entity) {
        // 1️⃣ 查询所有用户：构建 userName -> nickName 映射表
        List<SysUser> sysUserList = sysUserMapper.selectList(null);
        Map<String, String> userMap = sysUserList.stream()
                .filter(u -> u.getUserName() != null)
                .collect(Collectors.toMap(SysUser::getUserName, SysUser::getNickName, (v1, v2) -> v1));

        // 2️⃣ 查询指定日期、部门的 Feedback
        LambdaQueryWrapper<Feedback> wrapper;

        Integer deptLevel = SecurityUtils.getLoginUser().getDeptLevel();
        if(null != deptLevel && Constants.RET_CODE_1_NUM == deptLevel){
            wrapper = new LambdaQueryWrapper<>();
        }else{
            wrapper = DeptPermissionUtil.buildSubDeptScopeWrapper(Feedback::getSubDeptId);
        }

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

        // 3️⃣ 按 createBy（userName）分组统计数量
        List<Speak> speakList = buildSpeakListByUserName(
                feedbackList,
                Feedback::getCreateBy,                        // 分组字段：创建人 userName
                userMap                                       // userName → nickName
        );

        return AjaxResult.success(speakList);
    }

    /**
     * 将 Feedback 列表按 userName 分组统计，并映射成 nickName
     */
    private List<Speak> buildSpeakListByUserName(List<Feedback> feedbackList,
                                                 Function<Feedback, String> keyExtractor,
                                                 Map<String, String> userMap) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            return Collections.emptyList();
        }

        // 1️⃣ 分组计数
        Map<String, Long> countMap = feedbackList.stream()
                .map(fb -> {
                    String key = keyExtractor.apply(fb);
                    return key != null ? new AbstractMap.SimpleEntry<>(key, fb) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.counting()));

        if (countMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 2️⃣ 转换为 Speak 列表（userName→nickName）
        return countMap.entrySet().stream()
                .map(entry -> {
                    Speak speak = new Speak();
                    speak.setKey(entry.getKey()); // userName
                    speak.setName(userMap.getOrDefault(entry.getKey(), "未知招聘人")); // nickName
                    speak.setCount(String.valueOf(entry.getValue()));
                    return speak;
                })
                .sorted((s1, s2) -> Integer.compare(
                        Integer.parseInt(s2.getCount()),
                        Integer.parseInt(s1.getCount())))
                .collect(Collectors.toList());
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
        wrapper.like(StringUtils.isNotEmpty(entity.getLocationName()), Feedback::getLocationName, entity.getLocationName());
        wrapper.like(StringUtils.isNotEmpty(entity.getName()), Feedback::getName, entity.getName());
        wrapper.like(StringUtils.isNotEmpty(entity.getPhone()), Feedback::getPhone, entity.getPhone());
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

        // 先检查名称唯一性
        Integer count = feedbackMapper.selectCount(
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getPhone, entity.getPhone())
                        .ne(entity.getId() != null, Feedback::getId, entity.getId()) // 如果是更新，排除自己
        );

        if (count != null && count > 0) {
            return AjaxResult.error("手机号码已存在，请重新输入");
        }

        // 上找二级部门
        DeptPermissionUtil.DeptInfo deptInfo = DeptPermissionUtil.findSecondLevelDept(entity.getSubDeptId(), entity.getSubDeptName());
        entity.setCreateName(SecurityUtils.getLoginUser().getUser().getNickName());
        entity.setDeptId(deptInfo.getDeptId());
        entity.setDeptName(deptInfo.getDeptName());

        feedbackMapper.insert(entity);
        return AjaxResult.success(entity);
    }
}
