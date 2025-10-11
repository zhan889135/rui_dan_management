package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位表 sys_post
 */
@Data
@TableName("sys_post")
@EqualsAndHashCode(callSuper = true)
public class SysPost extends BaseEntity
{
    /** 岗位编号 */
    @TableId
    @Excel(name = "岗位编号", type = Excel.Type.EXPORT)
    private Long postId;

    /** 岗位编码 */
    @Excel(name = "岗位编码", type = Excel.Type.EXPORT)
    private String postCode;

    /** 岗位名称 */
    @Excel(name = "岗位名称", type = Excel.Type.EXPORT)
    private String postName;

    /** 岗位排序 */
    @Excel(name = "岗位排序", type = Excel.Type.EXPORT)
    private Integer postSort;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", type = Excel.Type.EXPORT, readConverterExp = "0=正常,1=停用")
    private String status;

    /** 用户是否存在此岗位标识 默认不存在 */
    @TableField(exist = false)
    private boolean flag = false;
}
