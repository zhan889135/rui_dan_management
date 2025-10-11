package com.talent.interview.service;

import com.talent.interview.entity.Location;
import com.talent.common.domain.AjaxResult;

import java.util.List;

/**
 * 面试点位 Service
 */
public interface LocationService {

    /**
     * 查询全部（不做供应商数据权限）
     */
    List<Location> allListNoDept(Location entity);

    /**
     * 查询数据
     */
    List<Location> queryList(Location entity);
    List<Location> queryPage(Location entity);

    /**
     * 根据ID获取详细信息
     */
    AjaxResult selectById(Long id);

    /**
     * 保存或更新
     */
    AjaxResult saveOrUpdate(Location entity);

    /**
     * 删除
     */
    AjaxResult delete(Long[] ids);
}
