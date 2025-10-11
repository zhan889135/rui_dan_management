package com.talent.system.controller;

import com.talent.common.constant.BusinessType;
import com.talent.common.constant.Constants;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Location;
import com.talent.system.config.annotation.Log;
import com.talent.system.entity.SysDept;
import com.talent.system.service.ISysDeptService;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.poi.ExcelUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 部门信息
 * 
 * @author JamesRay
 */
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController
{
    @Autowired
    private ISysDeptService deptService;

    /**
     * 获取部门列表
     */
    @GetMapping("/list")
    public AjaxResult list(SysDept dept)
    {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return success(depts);
    }

    /**
     * 查询部门列表（排除节点）
     */
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId)
    {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return success(depts);
    }

    /**
     * 根据部门编号获取详细信息
     */
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId)
    {
//        deptService.checkDeptDataScope(deptId);
        return success(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysDept dept)
    {
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysDept dept)
    {
        Long deptId = dept.getDeptId();
//        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        else if (dept.getParentId().equals(deptId))
        {
            return error("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        }
        else if (StringUtils.equals(Constants.IS_DEL_Y, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0)
        {
            return error("该部门包含未停用的子部门！");
        }
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId)
    {
        if (deptService.hasChildByDeptId(deptId))
        {
            return warn("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId))
        {
            return warn("部门存在用户,不允许删除");
        }
//        deptService.checkDeptDataScope(deptId);
        return toAjax(deptService.deleteDeptById(deptId));
    }

    /**
     * 状态修改
     */
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysDept dept)
    {
        return toAjax(deptService.updateDeptStatus(dept));
    }

    @Log(title = "部门管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysDept dept)
    {
        List<SysDept> depts = deptService.selectDeptList(dept);
        ExcelUtil<SysDept> util = new ExcelUtil<>(SysDept.class);
        util.exportExcel(response, depts, "用户数据");
    }


    /**
     * 查询全部供应商，多级的按照/拼接
     * 微兴
     * 微兴/招聘一部
     * 微兴/招聘二部
     * 微兴/招聘三部
     * 佳新
     * 佳新/综合部门
     * 佳新/综合部门/招投标组
     * ......
     */
    @GetMapping("/selectLevel2DeptName")
    public AjaxResult selectLevel2DeptName(Location entity) {
        return deptService.selectLevel2DeptName(entity);
    }

    /**
     * 查询当前供应商存在的点位
     */
//    @GetMapping("/selectSysLocationByDeptId")
//    public AjaxResult selectSysLocationByDeptId(SysLocation entity) {
//        return deptService.selectSysLocationByDeptId(entity);
//    }
}
