package com.talent.interview.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 招聘需求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_requirement")
public class Requirement extends BaseEntity {

    /** 主键ID */
    @TableId
    private Integer id;

    /** logo地址 */
    private String logoPath;

    /** 排序 */
    private Integer orderNum;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 排序方向（asc 或 desc），前端传值 */
    @TableField(exist = false)
    private String orderDirection;
}
