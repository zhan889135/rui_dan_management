package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告表 sys_notice
 */
@Data
@TableName("sys_notice")
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseEntity
{
    /** 公告ID */
    @TableId
    private Long noticeId;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型（1通知 2公告） */
    private String noticeType;

    /** 公告内容 */
    private String noticeContent;

    /** 公告状态（0正常 1关闭） */
    private String status;

    /** 供应商id */
    private Integer deptId;

    /** 供应商名称 */
    private String deptName;
}
