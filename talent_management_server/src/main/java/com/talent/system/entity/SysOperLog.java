package com.talent.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.talent.system.config.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 操作日志记录表 oper_log
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog
{
    /** 日志主键 */
    @TableId
    @Excel(name = "操作序号", cellType = Excel.ColumnType.NUMERIC)
    private Long operId;

    /** 操作模块 */
    @Excel(name = "操作模块")
    private String title;

    /** 业务类型（0浏览 1新增 2修改 3删除） */
    @Excel(name = "业务类型")
    private Integer businessType;

    /** 业务类型数组 */
    @TableField(exist = false)
    private Integer[] businessTypes;

    /** 请求方法 */
    @Excel(name = "请求方法")
    private String method;

    /** 请求方式 */
    @Excel(name = "请求方式")
    private String requestMethod;

    /** 操作类别（0其它 1后台用户 2手机端用户） */
    @Excel(name = "操作类别")
    private Integer operatorType;

    /** 操作人员 */
    @Excel(name = "操作人员")
    private String operName;

    /** 部门名称 */
    @Excel(name = "部门名称")
    private String deptName;

    /** 请求url */
    @Excel(name = "请求地址")
    private String operUrl;

    /** 操作地址 */
    @Excel(name = "操作地址")
    private String operIp;

    /** 操作地点 */
    @Excel(name = "操作地点")
    private String operLocation;

    /** 请求参数 */
    @Excel(name = "请求参数")
    private String operParam;

    /** 返回参数 */
    @Excel(name = "返回参数")
    private String jsonResult;

    /** 操作状态（0正常 1异常） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private Integer status;

    /** 错误消息 */
    @Excel(name = "错误消息")
    private String errorMsg;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;

    /** 消耗时间 */
    @Excel(name = "消耗时间", suffix = "毫秒")
    private Long costTime;

    /** 请求参数 */
    @TableField(exist = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> params;
}
