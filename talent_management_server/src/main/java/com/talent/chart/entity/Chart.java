package com.talent.chart.entity;

import lombok.Data;

/**
 * key:value实体类
 */
@Data
public class Chart {

    /** id */
    private String key;

    /** 显示 */
    private String label;

    /** value */
    private String value;

    /** 统计图 X / Y 坐标 */
    private String[] x;
    private Integer[] y;
}
