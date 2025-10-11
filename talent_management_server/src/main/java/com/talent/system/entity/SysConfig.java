package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.common.domain.BaseEntity;
import com.talent.system.config.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 参数配置表 sys_config
 */
@Data
@TableName("sys_config")
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 参数主键 */
    @TableId
    @Excel(name = "参数主键", type = Excel.Type.EXPORT)
    private Long configId;

    /** 参数名称 */
    @Excel(name = "参数名称", type = Excel.Type.EXPORT)
    private String configName;

    /** 参数键名 */
    @Excel(name = "参数键名", type = Excel.Type.EXPORT)
    private String configKey;

    /** 参数键值 */
    @Excel(name = "参数键值", type = Excel.Type.EXPORT)
    private String configValue;

    /** 系统内置（Y是 N否） */
    @Excel(name = "系统内置", type = Excel.Type.EXPORT, readConverterExp = "Y=是,N=否")
    private String configType;
}
