package com.talent.speak.server;

import com.talent.common.domain.AjaxResult;
import com.talent.interview.entity.Feedback;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 交流对接
 */
public interface SpeakService {

    /**
     * 查询供应商邀约总数
     */
    AjaxResult selectDeptInvitationCount(Feedback entity);

    /**
     * 查询招聘人邀约总数
     */
    AjaxResult selectPeopleInvitationCount(Feedback entity);

    /**
     * 查询邀约明细
     */
    AjaxResult selectInvitationInfo(Feedback entity);

    /**
     * 保存邀约信息
     */
    AjaxResult saveInvitationInfo(Feedback entity);
}
