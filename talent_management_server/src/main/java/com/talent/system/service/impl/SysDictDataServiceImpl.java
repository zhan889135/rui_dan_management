package com.talent.system.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.utils.StringUtils;
import com.talent.system.entity.SysDictData;
import com.talent.system.mapper.SysDictDataMapper;
import com.talent.system.service.ISysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 字典 业务层处理
 * 
 * @author JamesRay
 */
@Service
public class SysDictDataServiceImpl implements ISysDictDataService
{
    @Autowired
    private SysDictDataMapper dictDataMapper;

    /**
     * 根据条件分页查询字典数据
     * 
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataList(SysDictData dictData) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();

        if (dictData.getDictType() != null && !dictData.getDictType().isEmpty()) {
            wrapper.eq(SysDictData::getDictType, dictData.getDictType());
        }

        if (dictData.getDictLabel() != null && !dictData.getDictLabel().isEmpty()) {
            wrapper.like(SysDictData::getDictLabel, dictData.getDictLabel());
        }

        if (dictData.getStatus() != null && !dictData.getStatus().isEmpty()) {
            wrapper.eq(SysDictData::getStatus, dictData.getStatus());
        }

        wrapper.orderByAsc(SysDictData::getDictSort);

        return dictDataMapper.selectList(wrapper);
    }


    /**
     * 根据字典类型和字典键值查询字典数据信息
     * 
     * @param dictType 字典类型
     * @param dictValue 字典键值
     * @return 字典标签
     */
    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        if(StringUtils.isEmpty(dictType) || StringUtils.isEmpty(dictValue)){
            return "";
        }
        SysDictData data = dictDataMapper.selectOne(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType)
                        .eq(SysDictData::getDictValue, dictValue)
                        .select(SysDictData::getDictLabel)
                        .last("limit 1") // 保险起见
        );
        return data != null ? data.getDictLabel() : null;
    }


    /**
     * 根据字典数据ID查询信息
     * 
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    @Override
    public SysDictData selectDictDataById(Long dictCode) {
        return dictDataMapper.selectById(dictCode);
    }


    /**
     * 批量删除字典数据信息
     * 
     * @param dictCodes 需要删除的字典数据ID
     */
    @Override
    public void deleteDictDataByIds(Long[] dictCodes) {
        for (Long dictCode : dictCodes) {
            // 1. 查询当前字典数据
            SysDictData data = dictDataMapper.selectById(dictCode);
            if (data == null) continue;

            // 2. 删除
            dictDataMapper.deleteById(dictCode);

            // 3. 重新加载该类型字典项，并写入缓存
            List<SysDictData> dictDatas = dictDataMapper.selectList(
                    new LambdaQueryWrapper<SysDictData>()
                            .eq(SysDictData::getDictType, data.getDictType())
                            .eq(SysDictData::getStatus, "0")
                            .orderByAsc(SysDictData::getDictSort)
            );
//            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }
    }


    /**
     * 新增保存字典数据信息
     * 
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public int insertDictData(SysDictData data) {
        // 插入数据，MyBatis-Plus insert 会忽略 null 字段
        int row = dictDataMapper.insert(data);

        // 插入成功后刷新缓存
        if (row > 0) {
            List<SysDictData> dictDatas = dictDataMapper.selectList(
                    new LambdaQueryWrapper<SysDictData>()
                            .eq(SysDictData::getDictType, data.getDictType())
                            .eq(SysDictData::getStatus, "0")
                            .orderByAsc(SysDictData::getDictSort)
            );
//            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }

        return row;
    }


    /**
     * 修改保存字典数据信息
     * 
     * @param data 字典数据信息
     * @return 结果
     */
    @Override
    public int updateDictData(SysDictData data) {
        int row = dictDataMapper.updateById(data);

        if (row > 0) {
            List<SysDictData> dictDatas = dictDataMapper.selectList(
                    new LambdaQueryWrapper<SysDictData>()
                            .eq(SysDictData::getDictType, data.getDictType())
                            .eq(SysDictData::getStatus, "0")
                            .orderByAsc(SysDictData::getDictSort)
            );
//            DictUtils.setDictCache(data.getDictType(), dictDatas);
        }

        return row;
    }

}
