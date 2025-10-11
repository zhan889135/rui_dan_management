package com.talent.speak.controller;

import com.talent.common.constant.BusinessType;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Report;
import com.talent.speak.server.SpeakService;
import com.talent.system.config.annotation.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 交流对接
 */
@RestController
@RequestMapping("/interview/speak")
public class SpeakController extends BaseController {

    @Autowired
    private SpeakService service;

    /**
     * 查询供应商邀约总数
     */
    @GetMapping("/selectDeptInvitationCount")
    public AjaxResult selectDeptInvitationCount(Feedback entity) {
        return service.selectDeptInvitationCount(entity);
    }

    /**
     * 查询员工邀约总数
     */
    @GetMapping("/selectPeopleInvitationCount")
    public AjaxResult selectPeopleInvitationCount(Feedback entity) {
        return service.selectPeopleInvitationCount(entity);
    }

    /**
     * 查询邀约明细
     */
    @GetMapping("/selectInvitationInfo")
    public AjaxResult selectInvitationInfo(Feedback entity) {
        return service.selectInvitationInfo(entity);
    }

    /**
     * 保存邀约信息
     */
    @PostMapping("/saveInvitationInfo")
    public AjaxResult saveInvitationInfo(@RequestBody Feedback entity) {
        return service.saveInvitationInfo(entity);
    }
}
