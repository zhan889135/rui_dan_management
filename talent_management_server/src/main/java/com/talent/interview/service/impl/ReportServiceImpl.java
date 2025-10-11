package com.talent.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.mybatisPlus.MyBatisBatchInsertHelper;
import com.talent.interview.entity.Report;
import com.talent.interview.mapper.ReportMapper;
import com.talent.interview.service.ReportService;
import com.talent.interview.utils.LambdaQueryBuilderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
}
