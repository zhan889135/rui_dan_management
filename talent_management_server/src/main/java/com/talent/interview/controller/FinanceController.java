package com.talent.interview.controller;

import com.talent.common.constant.BusinessType;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.common.page.PageUtils;
import com.talent.common.page.TableDataInfo;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Finance;
import com.talent.interview.service.FinanceService;
import com.talent.interview.utils.StatisticsUtil;
import com.talent.system.config.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 财务报表
 */
@RestController
@RequestMapping("/interview/finance")
public class FinanceController extends BaseController {

    @Autowired
    private FinanceService service;

    /**
     * 条件查询
     */
    @GetMapping("/query")
    public TableDataInfo query(Finance entity) {
        List<Finance> fullList = service.queryList(entity); // 包含手动分页后的结果
        // 调用工具类查询总数：
        Map<String, String> map = StatisticsUtil.calculateFinanceMoney(fullList);
        List<Finance> pageList = PageUtils.manualSubList(fullList);   // 手动分页子集
        return getDataTable(pageList, fullList.size(), map); // 这里传原始总数
    }

    /**
     * 根据ID获取详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult selectById(@PathVariable Long id) {
        return service.selectById(id);
    }

    /**
     * 保存或更新
     */
    @PostMapping("/save")
    @Log(title = "财务报表", businessType = BusinessType.UPDATE)
    public AjaxResult save(@RequestBody Finance entity) {
        return service.saveOrUpdate(entity);
    }

    /**
     * 删除
     */
    @DeleteMapping(value = "/{ids}")
    @Log(title = "财务报表", businessType = BusinessType.DELETE)
    public AjaxResult delete(@PathVariable Long[] ids) {
        return service.delete(ids);
    }
}
