package com.talent.chart.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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


    /** 招聘人 */
    private String[] createNameData;
    /** 硬性条件人数 */
    private Integer[] hardRequirementsData;
    /** 不合格人数 */
    private Integer[] unqualifiedData;
    /** 通过人数 */
    private Integer[] passedData;
    /** 总送人数 */
    private Integer[] totalData;
    /** 计费率 */
    private Double[] billingRateData;


    /** 底部的计费率列表 */
    private List<PointStat> billingTableData;

    // 内部静态类，用于封装每个点位的统计信息
    @Data
    public static class PointStat {
        private String locationName;           // 点位名称
        private int totalSent = 0;             // 总送人数
        private int hardRequirementNotMet = 0; // 硬性条件不符人数（hardRequirements = 'N'）
        private int remainingDenominator = 0;  // 剩余分母数 = totalSent - hardRequirementNotMet
        private int billedCount = 0;           // 计费人数（满足硬性且 isBilling='Y'）
        private BigDecimal billingRate = BigDecimal.ZERO; // 计费率（百分比，保留两位小数）

        public PointStat(String locationName) {
            this.locationName = locationName;
        }
    }
}
