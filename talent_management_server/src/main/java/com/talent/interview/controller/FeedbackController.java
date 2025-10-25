package com.talent.interview.controller;

import com.talent.common.constant.BusinessType;
import com.talent.common.constant.Constants;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.common.page.PageUtils;
import com.talent.common.page.TableDataInfo;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.poi.ExcelUtil;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.FeedbackDept3Excel;
import com.talent.interview.entity.Location;
import com.talent.interview.service.FeedbackService;
import com.talent.interview.utils.StatisticsUtil;
import com.talent.system.config.annotation.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 面试反馈
 */
@RestController
@RequestMapping("/interview/feedback")
public class FeedbackController extends BaseController {

    @Autowired
    private FeedbackService service;

    /**
     * 条件查询
     */
    @GetMapping("/query")
    public TableDataInfo query(Feedback entity) {
        List<Feedback> fullList = service.queryList(entity); // 包含手动分页后的结果
        // 调用工具类查询总数：
        Map<String, String> map = StatisticsUtil.calculateFeedbackStats(fullList);
        List<Feedback> pageList = PageUtils.manualSubList(fullList);   // 手动分页子集
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
    @Log(title = "面试反馈", businessType = BusinessType.UPDATE)
    public AjaxResult save(@RequestBody Feedback entity) {
        return service.saveOrUpdate(entity);
    }

    /**
     * 删除
     */
    @DeleteMapping(value = "/{ids}")
    @Log(title = "面试反馈", businessType = BusinessType.DELETE)
    public AjaxResult delete(@PathVariable Long[] ids) {
        return service.delete(ids);
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    @Log(title = "面试反馈", businessType = BusinessType.EXPORT)
    public void export(HttpServletResponse response, Feedback entity) {
        List<Feedback> list = service.queryList(entity);

        // 如果等于三级，我还要到处单独的Excel实体类
        if (entity.getDeptLevel() == Constants.RET_CODE_3_NUM) {
            List<FeedbackDept3Excel> feedbackDept3Excels = list.stream()
                    .map(f -> {
                        FeedbackDept3Excel t = new FeedbackDept3Excel();
                        BeanUtils.copyProperties(f, t);
                        return t;
                    })
                    .toList();

            ExcelUtil<FeedbackDept3Excel> util = new ExcelUtil<>(FeedbackDept3Excel.class);
            util.exportExcel(response, feedbackDept3Excels, "面试反馈");
        } else {
            ExcelUtil<Feedback> util = new ExcelUtil<>(Feedback.class);
            util.exportExcel(response, list, "面试反馈");
        }
    }

    /**
     * 导入面试反馈
     */
    @PostMapping("/importData")
    @Log(title = "导入面试反馈", businessType = BusinessType.IMPORT)
    public AjaxResult importData(HttpServletResponse response, MultipartFile file) throws Exception {
        return service.importData(response, file);
    }

    /**
     * 二级一键推送数据
     */
    @GetMapping("/secondPushData")
    public AjaxResult secondPushData(Feedback entity) {
        return service.secondPushData(entity);
    }


    /**
     * 校验手机号是否存在
     */
    @PostMapping("/verifyIsExist")
    public AjaxResult verifyIsExist(@RequestBody Feedback entity) {
        return service.verifyIsExist(entity);
    }
}
