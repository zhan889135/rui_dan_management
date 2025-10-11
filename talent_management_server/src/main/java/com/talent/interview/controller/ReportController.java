package com.talent.interview.controller;

import com.talent.common.constant.BusinessType;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.common.page.TableDataInfo;
import com.talent.common.utils.poi.ExcelUtil;
import com.talent.interview.entity.Report;
import com.talent.interview.service.ReportService;
import com.talent.system.config.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 面试报备
 */
@RestController
@RequestMapping("/interview/report")
public class ReportController extends BaseController {

    @Autowired
    private ReportService service;

    /**
     * 查询全部
     */
    @GetMapping("/allList")
    public AjaxResult allList(Report entity) {
        return success(service.queryList(entity));
    }

    /**
     * 条件查询
     */
    @GetMapping("/query")
    public TableDataInfo queryPage(Report entity) {
        List<Report> list = service.queryPage(entity);
        return getDataTable(list);
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
    @Log(title = "面试报备", businessType = BusinessType.UPDATE)
    public AjaxResult save(@RequestBody Report entity) {
        return service.saveOrUpdate(entity);
    }

    /**
     * 删除
     */
    @DeleteMapping(value = "/{ids}")
    @Log(title = "面试报备", businessType = BusinessType.DELETE)
    public AjaxResult delete(@PathVariable Long[] ids) {
        return service.delete(ids);
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    @Log(title = "面试报备", businessType = BusinessType.EXPORT)
    public void export(HttpServletResponse response, Report entity) {
        List<Report> list = service.queryList(entity);
        ExcelUtil<Report> util = new ExcelUtil<>(Report.class);
        util.exportExcel(response, list, "面试报备");
    }
}
