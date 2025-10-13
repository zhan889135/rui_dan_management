package com.talent.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.DateUtils;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.mybatisPlus.MyBatisBatchInsertHelper;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Report;
import com.talent.interview.mapper.FeedbackMapper;
import com.talent.interview.mapper.ReportMapper;
import com.talent.interview.service.ReportService;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.interview.utils.LambdaQueryBuilderUtil;
import com.talent.system.entity.SysDictData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.talent.common.page.PageUtils.startPage;

/**
 * 面试报备 Service 实现类
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    /**
     * 条件查询（不分页）
     */
    @Override
    public List<Report> queryList(Report entity) {
        LambdaQueryWrapper<Report> wrapper = LambdaQueryBuilderUtil.buildReportQueryWrapper(entity);
        return reportMapper.selectList(wrapper);
    }

    /**
     * 条件查询（分页）
     */
    @Override
    public List<Report> queryPage(Report entity) {
        LambdaQueryWrapper<Report> wrapper = LambdaQueryBuilderUtil.buildReportQueryWrapper(entity);
        startPage();
        return reportMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取详细信息
     */
    @Override
    public AjaxResult selectById(Long id) {
        return AjaxResult.success(reportMapper.selectById(id));
    }

    /**
     * 保存或更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult saveOrUpdate(Report entity) {

        // 处理历史数据
        if(null == entity.getSubDeptId() && StringUtils.isEmpty(entity.getSubDeptName())){
            entity.setSubDeptId(entity.getDeptId());
            entity.setSubDeptName(entity.getDeptName());
        }
        // 上找二级部门
        DeptPermissionUtil.DeptInfo deptInfo = DeptPermissionUtil.findSecondLevelDept(entity.getSubDeptId(), entity.getSubDeptName());

        entity.setDeptId(deptInfo.getDeptId());
        entity.setDeptName(deptInfo.getDeptName());

        if (entity.getId() == null) {
            entity.setCreateName(SecurityUtils.getLoginUser().getUser().getNickName());
            reportMapper.insert(entity);
        } else {
            MyBatisBatchInsertHelper.updateAllFieldsById(entity, reportMapper);
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
        reportMapper.deleteBatchIds(idList);
        return AjaxResult.success();
    }

    /**
     * 人员到达现场,推送至面试反馈
     */
    @Override
    public AjaxResult personToFeedback(Report entity) {
        // 查询面试报备
        Report info = reportMapper.selectById(entity.getId());

        // 同步至面试反馈
        Feedback feedback = new Feedback();

        feedback.setLocationId(info.getLocationId());
        feedback.setLocationName(info.getLocationName());
        feedback.setDeptId(info.getDeptId());
        feedback.setDeptName(info.getDeptName());
        feedback.setSubDeptId(info.getSubDeptId());
        feedback.setSubDeptName(info.getSubDeptName());

        feedback.setName(info.getName());
        feedback.setSex(info.getSex());
        feedback.setPhone(info.getPhone());
        feedback.setAge(info.getAge());
        feedback.setEducation(info.getEducation());
        feedback.setInterviewDate(new Date());
        feedback.setRemark(info.getRemark());

        feedback.setCreateBy(info.getCreateBy());
        feedback.setCreateTime(info.getCreateTime());
        feedback.setUpdateBy(info.getUpdateBy());
        feedback.setUpdateTime(info.getUpdateTime());

        // 插入
        feedbackMapper.insert(feedback);

        // 强制更新创建人属性
        feedbackMapper.update(
                null,
                new LambdaUpdateWrapper<Feedback>()
                        .eq(Feedback::getId, feedback.getId())
                        .set(Feedback::getCreateBy, info.getCreateBy())
                        .set(Feedback::getCreateTime, info.getCreateTime())
        );

        return AjaxResult.success();
    }
}
