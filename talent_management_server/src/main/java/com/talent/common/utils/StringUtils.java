package com.talent.common.utils;

import com.talent.common.constant.Constants;
import com.talent.common.utils.text.StrFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * 字符串工具类
 *
 * @author JamesRay
 */
@Slf4j
public class StringUtils extends org.apache.commons.lang3.StringUtils
{
    /** 空字符串 */
    private static final String NULLSTR = "";

    /**
     * * 判断一个对象数组是否为空
     *
     * @param objects 要判断的对象数组
     ** @return true：为空 false：非空
     */
    public static boolean isEmpty(Object[] objects)
    {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * 判断一个字符串是否为空串
     *
     * @param str String
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(String str)
    {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }
    /**
     * * 判断一个对象数组是否非空
     *
     * @param objects 要判断的对象数组
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Object[] objects)
    {
        return !isEmpty(objects);
    }

    public static boolean isNotEmpty(Map<?, ?> map)
    {
        return !isEmpty(map);
    }
    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Map<?, ?> map)
    {
        return isNull(map) || map.isEmpty();
    }
    /**
     * * 判断一个Collection是否非空，包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Collection<?> coll)
    {
        return !isEmpty(coll);
    }

    /**
     * * 判断一个Collection是否为空， 包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll)
    {
        return isNull(coll) || coll.isEmpty();
    }
    /**
     * * 判断一个对象是否为空
     *
     * @param object Object
     * @return true：为空 false：非空
     */
    public static boolean isNull(Object object)
    {
        return object == null;
    }

    /**
     * * 判断一个对象是否非空
     *
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object)
    {
        return !isNull(object);
    }

    /**
     * 去空格
     */
    public static String trim(String str)
    {
        return (str == null ? "" : str.trim());
    }

    /**
     * 截取字符串
     *
     * @param str 字符串
     * @param start 开始
     * @param end 结束
     * @return 结果
     */
    public static String substring(final String str, int start, int end)
    {
        if (str == null)
        {
            return NULLSTR;
        }

        if (end < 0)
        {
            end = str.length() + end;
        }
        if (start < 0)
        {
            start = str.length() + start;
        }

        if (end > str.length())
        {
            end = str.length();
        }

        if (start > end)
        {
            return NULLSTR;
        }

        if (start < 0)
        {
            start = 0;
        }
        if (end < 0)
        {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串同时串忽略大小写
     *
     * @param cs 指定字符串
     * @param searchCharSequences 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchCharSequences)
    {
        if (isEmpty(cs) || isEmpty(searchCharSequences))
        {
            return false;
        }
        for (CharSequence testStr : searchCharSequences)
        {
            if (containsIgnoreCase(cs, testStr))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为空，并且不是空白字符
     *
     * @param str 要判断的value
     * @return 结果
     */
    public static boolean hasText(String str)
    {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str)
    {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++)
        {
            if (!Character.isWhitespace(str.charAt(i)))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * 是否包含字符串
     *
     * @param str 验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs)
    {
        if (str != null && strs != null)
        {
            for (String s : strs)
            {
                if (str.equalsIgnoreCase(trim(s)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 格式化文本, {} 表示占位符<br>
     * 此方法只是简单将占位符 {} 按照顺序替换为参数<br>
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可<br>
     * 例：<br>
     * 通常使用：format("this is {} for {}", "a", "b") -> this is a for b<br>
     * 转义{}： format("this is \\{} for {}", "a", "b") -> this is \{} for a<br>
     * 转义\： format("this is \\\\{} for {}", "a", "b") -> this is \a for b<br>
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params 参数值
     * @return 格式化后的文本
     */
    public static String format(String template, Object... params)
    {
        if (isEmpty(params) || isEmpty(template))
        {
            return template;
        }
        return StrFormatter.format(template, params);
    }

    /**
     * 获取参数不为空值
     *
     * @param value defaultValue 要判断的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue)
    {
        return value != null ? value : defaultValue;
    }

    /** 空值工具 */
    public static String nvl(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /**
     * 是否为http(s)://开头
     *
     * @param link 链接
     * @return 结果
     */
    public static boolean ishttp(String link)
    {
        return StringUtils.startsWithAny(link, Constants.HTTP, Constants.HTTPS);
    }

    /**
     * 驼峰转下划线命名
     */
    public static String toUnderScoreCase(String str)
    {
        if (str == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (i > 0)
            {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            }
            else
            {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1))
            {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase)
            {
                sb.append(Constants.SEPARATOR);
            }
            else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase)
            {
                sb.append(Constants.SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }
    /**
     * 数字左边补齐0，使之达到指定长度。注意，如果数字转换为字符串后，长度大于size，则只保留 最后size个字符。
     *
     * @param num 数字对象
     * @param size 字符串指定长度
     * @return 返回数字的字符串格式，该字符串为指定长度。
     */
    public static final String padl(final Number num, final int size)
    {
        return padl(num.toString(), size, '0');
    }

    /**
     * 字符串左补齐。如果原始字符串s长度大于size，则只保留最后size个字符。
     *
     * @param s 原始字符串
     * @param size 字符串指定长度
     * @param c 用于补齐的字符
     * @return 返回指定长度的字符串，由原字符串左补齐或截取得到。
     */
    public static String padl(final String s, final int size, final char c)
    {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null)
        {
            final int len = s.length();
            if (s.length() <= size)
            {
                for (int i = size - len; i > 0; i--)
                {
                    sb.append(c);
                }
                sb.append(s);
            }
            else
            {
                return s.substring(len - size, len);
            }
        }
        else
        {
            for (int i = size; i > 0; i--)
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // decimal转换为字符串，并删除多余0
    public static String fmtNum(BigDecimal value) {
        if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    public static String fmtNum(String str) {
        try {
            BigDecimal value = parseDecimal(str);
            if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
                return "";
            }
            return value.stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return null;
        }
    }

    // decimal转换为字符串，并删除多余0 拼接%
    public static String fmtPct(BigDecimal value) {
        if (value == null || BigDecimal.ZERO.compareTo(value) == 0) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString() + "%";
    }
    public static String fmtPct(Object value) {
        if (value == null) return "";
        try {
            BigDecimal decimalValue;

            if (value instanceof BigDecimal) {
                decimalValue = (BigDecimal) value;
            } else if (value instanceof Number) {
                decimalValue = new BigDecimal(((Number) value).toString());
            } else if (value instanceof String) {
                String str = ((String) value).trim();
                if (str.isEmpty()) return "";
                decimalValue = new BigDecimal(str);
            } else {
                return "";
            }

            if (BigDecimal.ZERO.compareTo(decimalValue) == 0) {
                return "";
            }

            return decimalValue.stripTrailingZeros().toPlainString() + "%";
        } catch (Exception e) {
            // 如果解析失败，返回空字符串，保证不抛异常
            return "";
        }
    }




    // decimal转换为字符串，并删除多余0
    public static String formatInt(int value) {
        return value == Constants.RET_CODE_0_NUM ? "" : String.valueOf(value);
    }

    /**
     * 计算偏差额：核算 - 预算
     */
    public static BigDecimal calculateDeviationAmt(BigDecimal budget, BigDecimal accounting) {
        if (budget == null || accounting == null || budget.compareTo(BigDecimal.ZERO) == 0 || accounting.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return accounting.subtract(Optional.ofNullable(budget).orElse(BigDecimal.ZERO));
    }


    /**
     * 计算偏差率：(核算 - 预算) / 预算 * 100
     */
    public static BigDecimal calculateDeviationRate(BigDecimal budget, BigDecimal accounting) {
        if (budget == null || accounting == null || budget.compareTo(BigDecimal.ZERO) == 0 || accounting.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal deviationAmt = accounting.subtract(budget);
        return deviationAmt
                .divide(budget, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(3, RoundingMode.HALF_UP);
    }
    // 字符串转int
    public static Integer parseInt(String str) {
        try {
            return StringUtils.isBlank(str) ? null : new BigDecimal(str).intValue();
        } catch (Exception e) {
            return null;
        }
    }
    // 字符串转小数
    public static BigDecimal parseDecimal(String str) {
        try {
            return StringUtils.isBlank(str) ? null : new BigDecimal(str);
        } catch (Exception e) {
            return null;
        }
    }
    // 字符串转日期
    public static Date parseDate(String str) {
        try {
            return StringUtils.isBlank(str) ? null : new SimpleDateFormat("yyyy-MM-dd").parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 工时转人日后累加（a 是人日，b 是小时）
     * @param a 已有人日（可为 null）
     * @param b 小时数（可为 null）
     * @return 相加后的人日，保留 2 位小数
     */
    public static BigDecimal safeAddHoursToDays(BigDecimal a, BigDecimal b) {
        BigDecimal daysA = (a == null ? BigDecimal.ZERO : a);
        BigDecimal hoursB = (b == null ? BigDecimal.ZERO : b);

        // 小时转人日：除以 8，保留两位小数，四舍五入
        BigDecimal daysB = hoursB.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);

        return daysA.add(daysB);
    }

    /**
     * 将工时（小时）转换为人日（以 8 小时为 1 人日）
     * 保留 2 位小数，四舍五入
     *
     * @param hours 工时（小时）
     * @return 人日（BigDecimal），默认为 0
     */
    public static BigDecimal convertHoursToManDays(BigDecimal hours) {
        if (hours == null) {
            return BigDecimal.ZERO;
        }
        return hours.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
    }

    // obj对象转为BigDecimal
    public static BigDecimal nvlBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try {
            if (v instanceof BigDecimal bd) return bd;
            if (v instanceof Number n) return new BigDecimal(n.toString());
            if (v instanceof String s && StringUtils.isNotEmpty(s)) return new BigDecimal(s.trim());
        } catch (Exception ignore) { }
        return BigDecimal.ZERO;
    }
}
