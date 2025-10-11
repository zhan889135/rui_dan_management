package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型表 sys_dict_type
 */
@Data
@TableName("sys_dict_type")
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity
{
    /** 字典主键 */
    @TableId
    @Excel(name = "字典主键", type = Excel.Type.EXPORT)
    private Long dictId;

    /** 字典名称 */
    @Excel(name = "字典名称", type = Excel.Type.EXPORT)
    private String dictName;

    /** 字典类型 */
    @Excel(name = "字典类型", type = Excel.Type.EXPORT)
    private String dictType;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", type = Excel.Type.EXPORT, readConverterExp = "0=正常,1=停用")
    private String status;
}
