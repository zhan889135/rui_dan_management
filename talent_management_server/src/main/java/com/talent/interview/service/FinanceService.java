package com.talent.interview.service;

import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Finance;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 财务表 Service
 */
public interface FinanceService {

    /**
     * 查询数据
     */
    List<Finance> queryList(Finance entity);

    /**
     * 根据ID获取详细信息
     */
    AjaxResult selectById(Long id);

    /**
     * 保存或更新
     */
    AjaxResult saveOrUpdate(Finance entity);

    /**
     * 删除
     */
    AjaxResult delete(Long[] ids);
}
