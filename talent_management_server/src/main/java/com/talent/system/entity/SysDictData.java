package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据表 sys_dict_data
 */
@Data
@TableName("sys_dict_data")
@EqualsAndHashCode(callSuper = true)
public class SysDictData extends BaseEntity
{
    /** 字典编码 */
    @TableId
    @Excel(name = "字典编码", type = Excel.Type.EXPORT)
    private Long dictCode;

    /** 字典排序 */
    @Excel(name = "字典排序", type = Excel.Type.EXPORT)
    private Long dictSort;

    /** 字典标签 */
    @Excel(name = "字典标签", type = Excel.Type.EXPORT)
    private String dictLabel;

    /** 字典键值 */
    @Excel(name = "字典键值", type = Excel.Type.EXPORT)
    private String dictValue;

    /** 字典类型 */
    @Excel(name = "字典类型", type = Excel.Type.EXPORT)
    private String dictType;

    /** 样式属性（其他样式扩展） */
    @Excel(name = "样式属性", type = Excel.Type.EXPORT)
    private String cssClass;

    /** 表格字典样式 */
    @Excel(name = "表格字典样式", type = Excel.Type.EXPORT)
    private String listClass;

    /** 是否默认（Y是 N否） */
    @Excel(name = "是否默认", type = Excel.Type.EXPORT, readConverterExp = "Y=是,N=否")
    private String isDefault;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", type = Excel.Type.EXPORT, readConverterExp = "0=正常,1=停用")
    private String status;
}
