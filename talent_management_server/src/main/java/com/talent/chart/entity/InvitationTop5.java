package com.talent.chart.entity;

import lombok.Data;

/**
 * TOP5 排行榜
 */
@Data
public class InvitationTop5 {

    private String deptName;     // 归属供应商

    private String createBy;     // 创建人

    private Integer weekCount;   // 本周邀约量

    private Integer monthCount;  // 本月邀约量

}
