package com.talent.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.system.entity.SysDictData;
import com.talent.system.mapper.SysDictDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典工具类
 *
 * @author JamesRay
 */
@Component
public class DictUtils
{

    @Autowired
    private SysDictDataMapper dictDataMapper;

    /**
     * 分隔符
     */
    public static final String SEPARATOR = ",";

    /**
     * 根据字典获取数据
     */
    public static String getDictLabel(String dictType, String dictValue, List<SysDictData> dictList) {
        if (StringUtils.isEmpty(dictType) || StringUtils.isEmpty(dictValue) || dictList == null || dictList.isEmpty()) {
            return "";
        }
        return dictList.stream()
                .filter(d -> Objects.equals(dictType, d.getDictType()))
                .filter(d -> Objects.equals(dictValue, d.getDictValue()))
                .map(SysDictData::getDictLabel)
                .findFirst()
                .orElse("");
    }

    /**
     * 根据字典获取值（已知 Label -> 反查 Value）
     */
    public static String getDictValue(String dictType, String dictLabel, List<SysDictData> dictList) {
        if (StringUtils.isEmpty(dictType) || StringUtils.isEmpty(dictLabel) || dictList == null || dictList.isEmpty()) {
            return "";
        }
        return dictList.stream()
                .filter(d -> Objects.equals(dictType, d.getDictType()))
                .filter(d -> Objects.equals(dictLabel, d.getDictLabel()))
                .map(SysDictData::getDictValue)
                .findFirst()
                .orElse("");
    }

    /**
     * 根据字典类型和多个字典值（用逗号分隔）获取对应的标签串
     *
     * @param dictType 字典类型
     * @param dictValues 多个值（如 "1,2,3"）
     * @param dictList 预加载的字典数据列表
     * @return 标签（如 "自动化集成,技术服务,流水"）
     */
    public static String getDictLabels(String dictType, String dictValues, List<SysDictData> dictList) {
        if (StringUtils.isEmpty(dictType) || StringUtils.isEmpty(dictValues) || dictList == null || dictList.isEmpty()) {
            return "";
        }

        String[] valueArray = dictValues.split(",");
        return Arrays.stream(valueArray)
                .map(value -> getDictLabel(dictType, value, dictList))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(","));
    }

    /**
     * 获取字典缓存
     */
    public List<SysDictData> getDictData(String key)
    {
        return Optional.ofNullable(dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                        .eq(StringUtils.isNotEmpty(key), SysDictData::getDictType, key)))
                .orElse(new ArrayList<>());
    }

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @return 字典标签
     */
    public String getDictLabel(String dictType, String dictValue)
    {
        if (StringUtils.isEmpty(dictValue))
        {
            return StringUtils.EMPTY;
        }
        return getDictLabel(dictType, dictValue, SEPARATOR);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType 字典类型
     * @param dictLabel 字典标签
     * @return 字典值
     */
    public String getDictValue(String dictType, String dictLabel)
    {
        if (StringUtils.isEmpty(dictLabel))
        {
            return StringUtils.EMPTY;
        }
        return getDictValue(dictType, dictLabel, SEPARATOR);
    }

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType 字典类型
     * @param dictValue 字典值
     * @param separator 分隔符
     * @return 字典标签
     */
    public String getDictLabel(String dictType, String dictValue, String separator)
    {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictData> datas = getDictData(dictType);
        if (StringUtils.isNull(datas))
        {
            return StringUtils.EMPTY;
        }
        if (StringUtils.containsAny(separator, dictValue))
        {
            for (SysDictData dict : datas)
            {
                for (String value : dictValue.split(separator))
                {
                    if (value.equals(dict.getDictValue()))
                    {
                        propertyString.append(dict.getDictLabel()).append(separator);
                        break;
                    }
                }
            }
        }
        else
        {
            for (SysDictData dict : datas)
            {
                if (dictValue.equals(dict.getDictValue()))
                {
                    return dict.getDictLabel();
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType 字典类型
     * @param dictLabel 字典标签
     * @param separator 分隔符
     * @return 字典值
     */
    public String getDictValue(String dictType, String dictLabel, String separator)
    {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictData> datas = getDictData(dictType);
        if (StringUtils.isNull(datas))
        {
            return StringUtils.EMPTY;
        }
        if (StringUtils.containsAny(separator, dictLabel))
        {
            for (SysDictData dict : datas)
            {
                for (String label : dictLabel.split(separator))
                {
                    if (label.equals(dict.getDictLabel()))
                    {
                        propertyString.append(dict.getDictValue()).append(separator);
                        break;
                    }
                }
            }
        }
        else
        {
            for (SysDictData dict : datas)
            {
                if (dictLabel.equals(dict.getDictLabel()))
                {
                    return dict.getDictValue();
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 根据字典类型获取字典所有值
     *
     * @param dictType 字典类型
     * @return 字典值
     */
    public String getDictValues(String dictType)
    {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictData> datas = getDictData(dictType);
        if (StringUtils.isNull(datas))
        {
            return StringUtils.EMPTY;
        }
        for (SysDictData dict : datas)
        {
            propertyString.append(dict.getDictValue()).append(SEPARATOR);
        }
        return StringUtils.stripEnd(propertyString.toString(), SEPARATOR);
    }

    /**
     * 根据字典类型获取字典所有标签
     *
     * @param dictType 字典类型
     * @return 字典值
     */
    public String getDictLabels(String dictType)
    {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictData> datas = getDictData(dictType);
        if (StringUtils.isNull(datas))
        {
            return StringUtils.EMPTY;
        }
        for (SysDictData dict : datas)
        {
            propertyString.append(dict.getDictLabel()).append(SEPARATOR);
        }
        return StringUtils.stripEnd(propertyString.toString(), SEPARATOR);
    }
}
