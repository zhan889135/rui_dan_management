package com.talent.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.mybatisPlus.MyBatisBatchInsertHelper;
import com.talent.interview.entity.Location;
import com.talent.interview.mapper.LocationMapper;
import com.talent.interview.service.LocationService;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.interview.utils.LambdaQueryBuilderUtil;
import com.talent.system.entity.SysDept;
import com.talent.system.mapper.SysDeptMapper;
import com.talent.system.utils.SysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.talent.common.page.PageUtils.startPage;

/**
 * 面试点位 Service 实现类
 */
@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    /**
     * 查询全部（不做供应商数据权限）
     */
    @Override
    public List<Location> allListNoDept(Location entity) {
        LambdaQueryWrapper<Location> wrapper = new LambdaQueryWrapper<>();

        // 部门等于1,是查看全部点位,不等于1,才加入条件
        Integer deptLevel = SecurityUtils.getLoginUser().getDeptLevel();
        if(null != deptLevel && Constants.RET_CODE_1_NUM != deptLevel){
            wrapper.like(StringUtils.isNotEmpty(entity.getDeptId()), Location::getDeptId, entity.getDeptId());
        }

        // 只查公司名称 + 只查面试点位
        wrapper.select(Location::getId, Location::getCompanyName, Location::getName);
        return locationMapper.selectList(wrapper);
    }

    /**
     * 条件查询（不分页）
     */
    @Override
    public List<Location> queryList(Location entity) {
        LambdaQueryWrapper<Location> wrapper = LambdaQueryBuilderUtil.buildLocationQueryWrapper(entity);
        return locationMapper.selectList(wrapper);
    }

    /**
     * 条件查询（分页）
     */
    @Override
    public List<Location> queryPage(Location entity) {
        LambdaQueryWrapper<Location> wrapper = LambdaQueryBuilderUtil.buildLocationQueryWrapper(entity);
        startPage();
        return locationMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取详细信息
     */
    @Override
    public AjaxResult selectById(Long id) {
        return AjaxResult.success(locationMapper.selectById(id));
    }

    /**
     * 保存或更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult saveOrUpdate(Location entity) {
        // 先检查名称唯一性
        Integer count = locationMapper.selectCount(
                new LambdaQueryWrapper<Location>()
                        .eq(Location::getName, entity.getName())
                        .ne(entity.getId() != null, Location::getId, entity.getId()) // 如果是更新，排除自己
        );

        if (count != null && count > 0) {
            return AjaxResult.error("面试点位名称已存在，请重新输入");
        }

        // 查询全部部门
        List<SysDept> sysDeptList = Optional.ofNullable(sysDeptMapper.selectList(null)).orElse(new ArrayList<>());

        // 面试点位，选择二级供应商后，要递归回去二级的所有子部门
        String finalDeptIds = SysUtil.searchSubDeptAndJoinId(entity.getDeptId(), sysDeptList);

        // 设置回entity
        entity.setDeptId(finalDeptIds);

        if (entity.getId() == null) {
            // 新增
            locationMapper.insert(entity);
        } else {
            // 修改
            MyBatisBatchInsertHelper.updateAllFieldsById(entity, locationMapper);
        }
        return AjaxResult.success(entity);
    }

    /**
     * 删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult delete(Long[] ids) {
        List<Long> idList = Arrays.stream(ids).collect(Collectors.toList());
        locationMapper.deleteBatchIds(idList);
        return AjaxResult.success();
    }

}
