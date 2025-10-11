//package com.talent.system.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.talent.common.domain.AjaxResult;
//import com.talent.common.utils.mybatisPlus.MyBatisBatchInsertHelper;
//import com.talent.system.entity.SysLocation;
//import com.talent.system.mapper.SysLocationMapper;
//import com.talent.system.service.SysLocationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * 供应商面试点位 Service 实现类
// */
//@Service
//public class SysLocationServiceImpl implements SysLocationService {
//
//    @Autowired
//    private SysLocationMapper sysLocationMapper;
//
//    /**
//     * 条件查询（不分页）
//     */
//    @Override
//    public List<SysLocation> queryList(SysLocation entity) {
//        LambdaQueryWrapper<SysLocation> wrapper = new LambdaQueryWrapper<>();
//        // 所属供应商
//        wrapper.eq(null != entity.getDeptId(), SysLocation::getDeptId, entity.getDeptId());
//        return sysLocationMapper.selectList(wrapper);
//    }
//
//    /**
//     * 根据ID获取详细信息
//     */
//    @Override
//    public AjaxResult selectById(Long id) {
//        return AjaxResult.success(sysLocationMapper.selectById(id));
//    }
//
//    /**
//     * 保存或更新
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public AjaxResult saveOrUpdate(SysLocation entity) {
//
//        if (entity.getId() == null) {
//            // 新增
//            sysLocationMapper.insert(entity);
//        } else {
//            // 修改
//            MyBatisBatchInsertHelper.updateAllFieldsById(entity, sysLocationMapper);
//        }
//        return AjaxResult.success(entity);
//    }
//
//
//    /**
//     * 删除
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public AjaxResult delete(Long[] ids) {
//        List<Long> idList = Arrays.stream(ids).collect(Collectors.toList());
//        sysLocationMapper.deleteBatchIds(idList);
//        return AjaxResult.success();
//    }
//}
