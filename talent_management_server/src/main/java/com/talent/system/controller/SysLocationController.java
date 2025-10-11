//package com.talent.system.controller;
//
//import com.talent.common.constant.BusinessType;
//import com.talent.common.controller.BaseController;
//import com.talent.common.domain.AjaxResult;
//import com.talent.system.config.annotation.Log;
//import com.talent.system.entity.SysLocation;
//import com.talent.system.service.SysLocationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 供应商面试点位
// */
//@RestController
//@RequestMapping("/sys/location")
//public class SysLocationController extends BaseController {
//
//    @Autowired
//    private SysLocationService service;
//
//    /**
//     * 查询全部
//     */
//    @GetMapping("/allList")
//    public AjaxResult allList(SysLocation entity) {
//        return success(service.queryList(entity));
//    }
//
//    /**
//     * 根据ID获取详细信息
//     */
//    @GetMapping(value = "/{id}")
//    public AjaxResult selectById(@PathVariable Long id) {
//        return service.selectById(id);
//    }
//
//    /**
//     * 保存或更新
//     */
//    @PostMapping("/save")
//    @Log(title = "面试点位", businessType = BusinessType.UPDATE)
//    public AjaxResult save(@RequestBody SysLocation entity) {
//        return service.saveOrUpdate(entity);
//    }
//
//    /**
//     * 删除
//     */
//    @DeleteMapping(value = "/{ids}")
//    @Log(title = "面试点位", businessType = BusinessType.DELETE)
//    public AjaxResult delete(@PathVariable Long[] ids) {
//        return service.delete(ids);
//    }
//
//}
