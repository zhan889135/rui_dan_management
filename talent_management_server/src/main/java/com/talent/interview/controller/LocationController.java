package com.talent.interview.controller;

import com.talent.common.constant.BusinessType;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.common.page.TableDataInfo;
import com.talent.common.utils.poi.ExcelUtil;
import com.talent.interview.entity.Location;
import com.talent.interview.service.LocationService;
import com.talent.system.config.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 面试点位
 */
@RestController
@RequestMapping("/interview/location")
public class LocationController extends BaseController {

    @Autowired
    private LocationService service;

    /**
     * 查询全部（不做供应商数据权限，纯条件输入查询）为了公司名称下拉历史输入
     */
    @GetMapping("/allListNoDept")
    public AjaxResult allListNoDept(Location entity) {
        return success(service.allListNoDept(entity));
    }

    /**
     * 查询全部（默认带供应商权限）
     */
    @GetMapping("/allList")
    public AjaxResult allList(Location entity) {
        return success(service.queryList(entity));
    }

    /**
     * 条件查询（默认带供应商权限）
     */
    @GetMapping("/query")
    public TableDataInfo query(Location entity) {
        List<Location> list = service.queryPage(entity);
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
    @Log(title = "面试点位", businessType = BusinessType.UPDATE)
    public AjaxResult save(@RequestBody Location entity) {
        return service.saveOrUpdate(entity);
    }

    /**
     * 删除
     */
    @DeleteMapping(value = "/{ids}")
    @Log(title = "面试点位", businessType = BusinessType.DELETE)
    public AjaxResult delete(@PathVariable Long[] ids) {
        return service.delete(ids);
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    @Log(title = "面试点位", businessType = BusinessType.EXPORT)
    public void export(HttpServletResponse response, Location entity) {
        List<Location> list = service.queryList(entity);
        ExcelUtil<Location> util = new ExcelUtil<>(Location.class);
        util.exportExcel(response, list, "面试点位");
    }
}
