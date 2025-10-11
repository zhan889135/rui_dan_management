package com.talent.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.mybatisPlus.MyBatisBatchInsertHelper;
import com.talent.interview.entity.Location;
import com.talent.interview.mapper.LocationMapper;
import com.talent.interview.service.LocationService;
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
        wrapper.like(StringUtils.isNotEmpty(entity.getDeptId()), Location::getDeptId, entity.getDeptId());
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

        // 获取选择的供应商id
        String deptId = entity.getDeptId(); // 121,123,122
        // 查询全部部门
        List<SysDept> sysDeptList = Optional.ofNullable(sysDeptMapper.selectList(null)).orElse(new ArrayList<>());

        // deptId不为空
        if (StringUtils.isNotEmpty(deptId)) {
            // 按照逗号拆分deptId
            String[] deptIdArray = deptId.split(",");
            Set<Long> resultDeptIds = new HashSet<>();

            for (String idStr : deptIdArray) {
                try {
                    Long id = Long.parseLong(idStr.trim());

                    // 在sysDeptList中查找对应的部门
                    SysDept dept = sysDeptList.stream()
                            .filter(d -> d.getDeptId().equals(id))
                            .findFirst()
                            .orElse(null);

                    if (dept != null) {
                        // 判断部门等级是否为2（供应商）
                        if (dept.getDeptLevel() != null && dept.getDeptLevel() == Constants.RET_CODE_2_NUM) {
                            // 递归获取所有子部门的deptId
                            List<Long> childDeptIds = SysUtil.getAllChildDeptIds(sysDeptList, dept);
                            resultDeptIds.addAll(childDeptIds);
                            resultDeptIds.add(id); // 包含自身
                        } else {
                            // 如果不是供应商等级，直接添加自身
                            resultDeptIds.add(id);
                        }
                    } else {
                        // 如果没有找到对应的部门，直接添加原始ID
                        resultDeptIds.add(id);
                    }
                } catch (NumberFormatException e) {
                    // 如果ID格式不正确，跳过
                    continue;
                }
            }
            // 将结果拼接回字符串
            String finalDeptIds = resultDeptIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // 设置回entity
            entity.setDeptId(finalDeptIds);
        }

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
