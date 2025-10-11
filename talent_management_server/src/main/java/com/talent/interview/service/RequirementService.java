package com.talent.interview.service;

import com.talent.interview.entity.Requirement;
import com.talent.common.domain.AjaxResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 招聘需求 Service
 */
public interface RequirementService {

    /**
     * 查询全部
     */
    List<Requirement> query(Requirement entity);

    /**
     * 根据ID获取详细信息
     */
    AjaxResult selectById(Long id);

    /**
     * 保存或更新
     */
    AjaxResult saveOrUpdate(Requirement entity, MultipartFile picture);

    /**
     * 删除
     */
    AjaxResult delete(Long[] ids);

    /**
     * 获取图片
     */
    void getItemPic(Integer id, HttpServletResponse response);
}
