package com.talent.system.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.talent.common.constant.UserConstants;
import com.talent.system.entity.SysDictData;
import com.talent.system.entity.SysDictType;
import com.talent.system.mapper.SysDictDataMapper;
import com.talent.common.utils.exception.ServiceException;
import com.talent.system.mapper.SysDictTypeMapper;
import com.talent.system.service.ISysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 字典 业务层处理
 * 
 * @author JamesRay
 */
@Service
public class SysDictTypeServiceImpl implements ISysDictTypeService
{
    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Autowired
    private SysDictDataMapper dictDataMapper;

    /**
     * 项目启动时，初始化字典到缓存
     */
//    @PostConstruct
//    public void init()
//    {
//        loadingDictCache();
//    }

    /**
     * 根据条件分页查询字典类型
     * 
     * @param dictType 字典类型信息
     * @return 字典类型集合信息
     */
    @Override
    public List<SysDictType> selectDictTypeList(SysDictType dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();

        if (dictType.getDictName() != null && !dictType.getDictName().isEmpty()) {
            wrapper.like(SysDictType::getDictName, dictType.getDictName());
        }

        if (dictType.getStatus() != null && !dictType.getStatus().isEmpty()) {
            wrapper.eq(SysDictType::getStatus, dictType.getStatus());
        }

        if (dictType.getDictType() != null && !dictType.getDictType().isEmpty()) {
            wrapper.like(SysDictType::getDictType, dictType.getDictType());
        }

        if (dictType.getParams() != null) {
            Object beginTime = dictType.getParams().get("beginTime");
            Object endTime = dictType.getParams().get("endTime");

            if (beginTime != null && !beginTime.toString().isEmpty()) {
                wrapper.ge(SysDictType::getCreateTime, beginTime.toString());
            }

            if (endTime != null && !endTime.toString().isEmpty()) {
                wrapper.le(SysDictType::getCreateTime, endTime.toString());
            }
        }
        wrapper.orderByDesc(SysDictType::getDictName);

        return dictTypeMapper.selectList(wrapper);
    }


    /**
     * 根据所有字典类型
     * 
     * @return 字典类型集合信息
     */
    @Override
    public List<SysDictType> selectDictTypeAll() {
        return dictTypeMapper.selectList(null); // 查询所有记录
    }


    /**
     * 根据字典类型查询字典数据
     * 
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {

        return dictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType)
                        .eq(SysDictData::getStatus, "0")
                        .orderByAsc(SysDictData::getDictSort)
        );
    }


    /**
     * 根据字典类型ID查询信息
     * 
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    @Override
    public SysDictType selectDictTypeById(Long dictId)
    {
        return dictTypeMapper.selectById(dictId);
    }

    /**
     * 根据字典类型查询信息
     * 
     * @param dictType 字典类型
     * @return 字典类型
     */
    @Override
    public SysDictType selectDictTypeByType(String dictType) {
        return dictTypeMapper.selectOne(
                new LambdaQueryWrapper<SysDictType>()
                        .eq(SysDictType::getDictType, dictType)
                        .last("LIMIT 1") // 保守加一个 LIMIT 1
        );
    }


    /**
     * 批量删除字典类型信息
     * 
     * @param dictIds 需要删除的字典ID
     */
    @Override
    public void deleteDictTypeByIds(Long[] dictIds) {
        for (Long dictId : dictIds) {
            // 查询字典类型
            SysDictType dictType = dictTypeMapper.selectById(dictId);
            if (dictType == null) {
                continue;
            }

            // 判断是否存在字典项
            Integer count = dictDataMapper.selectCount(
                    new LambdaQueryWrapper<SysDictData>()
                            .eq(SysDictData::getDictType, dictType.getDictType())
            );

            if (count != null && count > 0) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", dictType.getDictName()));
            }

            // 删除字典类型
            dictTypeMapper.deleteById(dictId);
        }
    }


//    /**
//     * 加载字典缓存数据
//     */
//    @Override
//    public void loadingDictCache() {
//        // 1. 构造查询条件：只查 status = '0'
//        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(SysDictData::getStatus, "0");
//
//        // 2. 查询所有启用的字典项
//        List<SysDictData> allDictData = dictDataMapper.selectList(wrapper);
//
//        // 3. 按 dictType 分组并排序后存入缓存
//        Map<String, List<SysDictData>> dictDataMap = allDictData.stream()
//                .collect(Collectors.groupingBy(SysDictData::getDictType));
//
//        for (Map.Entry<String, List<SysDictData>> entry : dictDataMap.entrySet()) {
//            List<SysDictData> sortedList = entry.getValue().stream()
//                    .sorted(Comparator.comparing(SysDictData::getDictSort))
//                    .collect(Collectors.toList());
//
//            DictUtils.setDictCache(entry.getKey(), sortedList);
//        }
//    }


    /**
     * 清空字典缓存数据
     */
//    @Override
//    public void clearDictCache()
//    {
//        DictUtils.clearDictCache();
//    }

    /**
     * 重置字典缓存数据
     */
//    @Override
//    public void resetDictCache()
//    {
//        clearDictCache();
//    }

    /**
     * 新增保存字典类型信息
     * 
     * @param dict 字典类型信息
     * @return 结果
     */
    @Override
    public int insertDictType(SysDictType dict)
    {
        return dictTypeMapper.insert(dict);
    }

    /**
     * 修改保存字典类型信息
     * 
     * @param dict 字典类型信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateDictType(SysDictType dict) {
        // 1. 查询旧类型
        SysDictType oldDict = dictTypeMapper.selectById(dict.getDictId());
        if (oldDict == null) {
            throw new ServiceException("字典类型不存在！");
        }

        // 2. 批量更新 sys_dict_data 中的 dict_type
        dictDataMapper.update(
                null,
                new LambdaUpdateWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, oldDict.getDictType())
                        .set(SysDictData::getDictType, dict.getDictType())
        );

        // 3. 更新字典类型表
        int row = dictTypeMapper.updateById(dict);

        // 4. 成功后刷新缓存
        if (row > 0) {
            List<SysDictData> dictDatas = dictDataMapper.selectList(
                    new LambdaQueryWrapper<SysDictData>()
                            .eq(SysDictData::getDictType, dict.getDictType())
                            .eq(SysDictData::getStatus, "0")
                            .orderByAsc(SysDictData::getDictSort)
            );
//            DictUtils.setDictCache(dict.getDictType(), dictDatas);
        }

        return row;
    }


    /**
     * 校验字典类型称是否唯一
     * 
     * @param dict 字典类型
     * @return 结果
     */
    @Override
    public boolean checkDictTypeUnique(SysDictType dict) {
        Long dictId = (dict.getDictId() == null) ? -1L : dict.getDictId();

        SysDictType existing = dictTypeMapper.selectOne(
                new LambdaQueryWrapper<SysDictType>()
                        .eq(SysDictType::getDictType, dict.getDictType())
                        .last("limit 1")
        );

        if (existing != null && !existing.getDictId().equals(dictId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

}
