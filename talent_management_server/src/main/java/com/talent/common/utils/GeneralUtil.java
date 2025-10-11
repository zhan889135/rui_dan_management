package com.talent.common.utils;

import com.talent.common.utils.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 三个部门都能用到的通用工具类
 */
public class GeneralUtil {

    /**
     * 生成编号
     * 格式：年份 + 流水号（支持配置位数）
     * 示例：20250001
     *
     * @param existingNos   已有编号集合
     * @param serialLength  流水号长度（如 3、4）
     * @return 新编号
     */
    public static String generateNo(List<String> existingNos, int serialLength) {
        // 年份前缀
        String year = String.valueOf(LocalDate.now().getYear());

        // 期望总长度
        int expectedLength = year.length() + serialLength;

        int maxSerial = 0;

        if (existingNos != null) {
            for (String no : existingNos) {
                if (StringUtils.isNotBlank(no)
                        && no.startsWith(year)
                        && no.length() == expectedLength) {
                    String suffix = no.substring(year.length());
                    if (StringUtils.isNumeric(suffix)) {
                        int num = Integer.parseInt(suffix);
                        if (num > maxSerial) {
                            maxSerial = num;
                        }
                    }
                }
            }
        }

        int nextSerial = maxSerial + 1;
        String serialStr = String.format("%0" + serialLength + "d", nextSerial);

        return year + serialStr;
    }
}
