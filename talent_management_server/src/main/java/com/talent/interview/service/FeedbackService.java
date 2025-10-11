package com.talent.interview.service;

import com.talent.interview.entity.Feedback;
import com.talent.common.domain.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 面试反馈 Service
 */
public interface FeedbackService {

    /**
     * 查询数据
     */
    List<Feedback> queryList(Feedback entity);
    List<Feedback> queryPage(Feedback entity);

    /**
     * 根据ID获取详细信息
     */
    AjaxResult selectById(Long id);

    /**
     * 保存或更新
     */
    AjaxResult saveOrUpdate(Feedback entity);

    /**
     * 删除
     */
    AjaxResult delete(Long[] ids);

    /**
     * 导入面试反馈
     */
    AjaxResult importData(HttpServletResponse response, MultipartFile file);

    /**
     * 二级一键推送数据
     */
    AjaxResult secondPushData(Feedback entity);
}
