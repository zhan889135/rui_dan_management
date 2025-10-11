package com.talent.interview.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.talent.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 文件管理
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_file")
public class FileEntity extends BaseEntity {

    /** 主键 */
    @TableId
    private Integer id;

    /** 模块 */
    private String module;

    /** 关联项目 */
    private Integer parentId;

    /** 序号 */
    private Integer orderNum;

    /** 名称 */
    private String fileName;

    /** 路径 */
    private String filePath;

    /** 上传人 */
    private String uploadName;

    /** 上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date uploadTime;
}
