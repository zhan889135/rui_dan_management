package com.talent.interview.controller;

import com.talent.common.constant.BusinessType;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.common.page.TableDataInfo;
import com.talent.common.utils.poi.ExcelUtil;
import com.talent.interview.entity.Requirement;
import com.talent.interview.service.RequirementService;
import com.talent.system.config.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 招聘需求
 */
@RestController
@RequestMapping("/interview/requirement")
public class RequirementController extends BaseController {

    @Autowired
    private RequirementService service;

    /**
     * 查询全部
     */
    @GetMapping("/allList")
    public AjaxResult allList(Requirement entity) {
        return success(service.query(entity));
    }

    /**
     * 条件查询
     */
    @GetMapping("/query")
    public TableDataInfo query(Requirement entity) {
        startPage();
        List<Requirement> list = service.query(entity);
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
    @Log(title = "招聘需求", businessType = BusinessType.UPDATE)
    public AjaxResult save(@RequestPart("entity") Requirement entity,
                           @RequestPart(value = "picture", required = false) MultipartFile picture) {
        return service.saveOrUpdate(entity, picture);
    }

    /**
     * 删除
     */
    @DeleteMapping(value = "/{ids}")
    @Log(title = "招聘需求", businessType = BusinessType.DELETE)
    public AjaxResult delete(@PathVariable Long[] ids) {
        return service.delete(ids);
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    @Log(title = "招聘需求", businessType = BusinessType.EXPORT)
    public void export(HttpServletResponse response, Requirement entity) {
        List<Requirement> list = service.query(entity);
        ExcelUtil<Requirement> util = new ExcelUtil<>(Requirement.class);
        util.exportExcel(response, list, "招聘需求");
    }

    /**
     * 获取图片
     */
    @GetMapping("/getItemPic/{id}")
    public void getItemPic(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {service.getItemPic(id, response);}

}
