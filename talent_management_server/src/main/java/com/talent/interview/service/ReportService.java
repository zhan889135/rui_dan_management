package com.talent.interview.service;

import com.talent.interview.entity.Report;
import com.talent.common.domain.AjaxResult;

import java.util.List;

/**
 * 面试报备 Service
 */
public interface ReportService {

    /**
     * 查询数据
     */
    List<Report> queryList(Report entity);
    List<Report> queryPage(Report entity);

    /**
     * 根据ID获取详细信息
     */
    AjaxResult selectById(Long id);

    /**
     * 保存或更新
     */
    AjaxResult saveOrUpdate(Report entity);

    /**
     * 删除
     */
    AjaxResult delete(Long[] ids);

    /**
     * 人员到达现场,推送至人员报备
     */
    AjaxResult personToFeedback(Report entity);
}
