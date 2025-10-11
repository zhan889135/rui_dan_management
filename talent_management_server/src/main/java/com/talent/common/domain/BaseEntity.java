package com.talent.common.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Entity基类
 */
@Data
public class BaseEntity implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    /** 备注 */
    private String remark;

    /** 删除标志（N代表存在 Y代表删除） */
    @TableLogic
    @TableField(fill = FieldFill.INSERT) // 插入时填充
    private String delFlag;

    /** 创建者 */
    @TableField(fill = FieldFill.INSERT) // 插入时填充
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT) // 插入时填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时填充
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时填充
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 请求参数 */
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params;

    /** 搜索值 */
    @JsonIgnore
    @TableField(exist = false)
    private String searchValue;

    public Map<String, Object> getParams()
    {
        if (params == null)
        {
            params = new HashMap<>();
        }
        return params;
    }
}
