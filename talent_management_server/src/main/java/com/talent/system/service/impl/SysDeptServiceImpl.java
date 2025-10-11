package com.talent.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.talent.common.constant.Constants;
import com.talent.common.constant.UserConstants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Location;
import com.talent.interview.mapper.FeedbackMapper;
import com.talent.interview.mapper.LocationMapper;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.system.config.annotation.DataScope;
import com.talent.system.entity.*;
import com.talent.common.domain.tree.TreeSelect;
import com.talent.system.mapper.*;
import com.talent.system.service.ISysDeptService;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.text.Convert;
import com.talent.common.utils.exception.ServiceException;
import com.talent.system.utils.SysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理 服务实现
 * 
 * @author JamesRay
 */
@Service
public class SysDeptServiceImpl implements ISysDeptService
{
    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

//    @Autowired
//    private SysLocationMapper sysLocationMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private LocationMapper locationMapper;

    /**
     * 查询部门管理数据
     * 
     * @param dept 部门信息
     * @return 部门信息集合
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysDept> selectDeptList(SysDept dept)
    {
        // 1. 查部门
        List<SysDept> deptList = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .like(StringUtils.isNotEmpty(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
                        .eq(StringUtils.isNotEmpty(dept.getStatus()), SysDept::getStatus, dept.getStatus())
                        .orderByAsc(SysDept::getOrderNum)
        );

        if (deptList.isEmpty()) {
            return deptList;
        }

        // 2. 查询面试反馈
        calculateDeptStats(deptList);

        // 3. 查点位名称
        List<Location> locationList = locationMapper.selectList(null);

        // ===== 3. 构建 deptId -> List<locationName> 的映射 =====
        Map<Long, List<String>> deptId2LocationNames = new HashMap<>();
        for (Location loc : locationList) {
            if (StringUtils.isNotEmpty(loc.getDeptId())) {
                for (String id : loc.getDeptId().split(",")) {
                    Long deptId = Long.valueOf(id.trim());
                    deptId2LocationNames
                            .computeIfAbsent(deptId, k -> new ArrayList<>())
                            .add(loc.getName());
                }
            }
        }

        // ===== 4. 只对 deptLevel == 2 的数据赋值 locationName =====
        deptList.forEach(d -> {
            if (d.getDeptLevel() == Constants.RET_CODE_2_NUM) {
                List<String> names = deptId2LocationNames.get(d.getDeptId());
                if (names != null) {
                    // 去重后拼接
                    String joined = names.stream()
                            .distinct()
                            .collect(Collectors.joining(","));
                    d.setLocationName(joined);
                }
            }
        });


        /*// 2. 一次性查所有点位
        List<SysLocation> locationList = sysLocationMapper.selectList(null);

        // 3. 按部门ID分组
        Map<Long, List<SysLocation>> locationMap = locationList.stream()
                .collect(Collectors.groupingBy(SysLocation::getDeptId));

        // 4. 绑定到部门对象
        deptList.forEach(d -> d.setSysLocationList(locationMap.getOrDefault(d.getDeptId(), new ArrayList<>())));*/

        return deptList;
    }

    /**
     * 根据部门列表和面试反馈列表，计算每个部门的经验值（exp）和交付力（deliveryPower），
     * 并按照部门层级从下向上汇总到上级部门。
     *
     * <p>统计规则：
     * <ul>
     *   <li>交付力（deliveryPower）：统计所有硬性条件为 "Y" 的反馈条数，不区分是否计费。</li>
     *   <li>经验值（exp）：统计硬性条件为 "Y" 且是否计费为 "Y" 的反馈条数。</li>
     *   <li>如果只想按计费为 Y 计算经验值，可在方法内调整注释部分。</li>
     * </ul>
     *
     * <p>汇总规则：
     * <ul>
     *   <li>先把 deptLevel=3 的子部门数据汇总到 parentId（通常是二级部门）。</li>
     *   <li>再把 deptLevel=2 的子部门数据汇总到 parentId（通常是一级部门）。</li>
     * </ul>
     *
     * @param deptList 部门列表，包含 deptId、parentId、deptLevel 等信息；方法会在对象内直接填充 exp 和 deliveryPower。
     */
    public void calculateDeptStats(List<SysDept> deptList) {

        if (deptList == null || deptList.isEmpty()) {
            return;
        }

        // 查询面试反馈
        List<Feedback> feedbackList = feedbackMapper.selectList(null);

        // 1. 初始化和建映射
        Map<Long, SysDept> deptMap = deptList.stream()
                .peek(d -> {
                    d.setExp(0);                // 经验值初始值
                    d.setDeliveryPower(0);      //交付力初始值
                })
                .collect(Collectors.toMap(SysDept::getDeptId, d -> d));

        // 2. 按子部门分组反馈 分支id
        Map<Long, List<Feedback>> fbByDeptId = feedbackList.stream()
                .filter(Objects::nonNull)
                .filter(fb -> fb.getSubDeptId() != null)
                .collect(Collectors.groupingBy(fb -> fb.getSubDeptId().longValue()));

        // 3. 计算每个部门的 exp 和 deliveryPower
        for (Map.Entry<Long, List<Feedback>> e : fbByDeptId.entrySet()) {
            Long deptId = e.getKey();
            SysDept dept = deptMap.get(deptId);
            if (dept == null) continue;

            int deliveryPower = 0;
            int exp = 0;

            for (Feedback fb : e.getValue()) {
                // 是否计费Y-是；N-否
                String bill = safeUpper(fb.getIsBilling());

                // 交付力统计(面试反馈中，是否计费为Y||N)
                if ("Y".equals(bill) || "N".equals(bill)) {
                    deliveryPower++;
                }

                // 经验值统计(面试反馈中，是否计费为Y的)
                if ("Y".equals(bill)) {
                    exp++;
                }
            }

            // 交付力统计(面试反馈中，是否计费为Y||N)
            dept.setDeliveryPower(dept.getDeliveryPower() + deliveryPower);
            // 经验值统计(面试反馈中，是否计费为Y的)
            dept.setExp(dept.getExp() + exp);
        }

        // 4. 自下而上汇总（3→2→1）  按照部门等级分组
        Map<Integer, List<SysDept>> byLevel = deptList.stream()
                .collect(Collectors.groupingBy(SysDept::getDeptLevel));

        // 先 3 级加到 2 级
        rollup(byLevel.getOrDefault(3, Collections.emptyList()), deptMap);
        // 再 2 级加到 1 级
        rollup(byLevel.getOrDefault(2, Collections.emptyList()), deptMap);
    }

    /**
     * 处理面试点位名称赋值
     */
    private void processLocationNames(List<SysDept> deptList) {

        // 查询面试点位
        List<Location> locationList = locationMapper.selectList(null);

        // 将locationList转换为按deptId分组的Map，一个deptId可能对应多个location
        Map<String, List<Location>> locationMap = locationList.stream()
                .collect(Collectors.groupingBy(Location::getDeptId));

        // 遍历部门列表，只处理deptLevel等于2的部门
        for (SysDept dept : deptList) {
            if (dept.getDeptLevel() != null && dept.getDeptLevel() == Constants.RET_CODE_2_NUM) {
                String deptIdStr = String.valueOf(dept.getDeptId());

                // 从map中获取该部门对应的所有面试点位
                List<Location> matchedLocations = locationMap.get(deptIdStr);

                if (matchedLocations != null && !matchedLocations.isEmpty()) {
                    // 拼接所有面试点位的name
                    String locationNames = matchedLocations.stream()
                            .map(Location::getName)
                            .filter(StringUtils::isNotEmpty)
                            .collect(Collectors.joining("、"));

                    // 设置到部门的locationName字段
                    dept.setLocationName(locationNames);
                } else {
                    // 如果没有匹配的面试点位，可以设置为空字符串或null
                    dept.setLocationName("");
                }
            }
        }
    }

    // 先把 3 级部门的值加到 2 级部门（第一行），
    // 再把 2 级部门的值加到 1 级部门（第二行）。
    private void rollup(List<SysDept> children, Map<Long, SysDept> allMap) {
        if (children == null) return;
        for (SysDept child : children) {
            Long parentId = child.getParentId();
            if (parentId == null) continue;
            SysDept parent = allMap.get(parentId);
            if (parent == null) continue;
            parent.setExp(safe(parent.getExp()) + safe(child.getExp()));
            parent.setDeliveryPower(safe(parent.getDeliveryPower()) + safe(child.getDeliveryPower()));
        }
    }

    // 工具方法
    private static String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private static int safe(Integer v) {
        return v == null ? 0 : v;
    }

    /**
     * 查询部门树结构信息
     */
    @Override
    public List<TreeSelect> selectDeptTreeList(SysDept dept)
    {
        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getOrderNum));
        return buildDeptTreeSelect(depts);
    }

    /**
     * 获取自己部门，以及当前部门
     */
    @Override
    public List<TreeSelect> selectSubDeptTreeList(SysDept dept) {
        // 获取当前部门及子部门 ID
        List<Long> deptIds = DeptPermissionUtil.getDeptAndChildrenIds(SecurityUtils.getDeptId());

        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>().in(SysDept::getDeptId, deptIds).orderByAsc(SysDept::getOrderNum));
        return buildDeptTreeSelect(depts);
    }

    /**
     * 构建前端所需要树结构
     */
    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts)
    {
        List<SysDept> returnList = new ArrayList<SysDept>();
        List<Long> tempList = depts.stream().map(SysDept::getDeptId).collect(Collectors.toList());
        for (SysDept dept : depts)
        {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(dept.getParentId()))
            {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = depts;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     */
    @Override
    public List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts)
    {
        List<SysDept> deptTrees = buildDeptTree(depts);
        return deptTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 根据角色ID查询部门树信息
     * 
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId)
    {
        SysRole role = roleMapper.selectById(roleId);
        boolean deptCheckStrictly = role != null && role.isDeptCheckStrictly();

        // 查询该角色已分配的部门
        List<SysRoleDept> roleDepts = roleDeptMapper.selectList(
                new LambdaQueryWrapper<SysRoleDept>()
                        .eq(SysRoleDept::getRoleId, roleId)
        );

        if (roleDepts.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> deptIds = new ArrayList<>();
        for (SysRoleDept rd : roleDepts) {
            if (rd.getDeptId() != null) {
                deptIds.add(rd.getDeptId());
            }
        }

        // 如果不启用严格模式，直接查询这些部门
        if (!deptCheckStrictly) {
            return deptIds;
        }

        // 启用严格模式：排除出现在 parent_id 中的 deptId
        List<SysDept> allDepts = deptMapper.selectBatchIds(deptIds);
        Set<Long> parentIds = new HashSet<>();
        for (SysDept dept : allDepts) {
            if (dept.getParentId() != null) {
                parentIds.add(dept.getParentId());
            }
        }

        List<Long> finalDeptIds = new ArrayList<>();
        for (Long id : deptIds) {
            if (!parentIds.contains(id)) {
                finalDeptIds.add(id);
            }
        }

        return finalDeptIds;
    }

    /**
     * 根据部门ID查询信息
     * 
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Override
    public SysDept selectDeptById(Long deptId) {
        // 1. 查部门
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            return null;
        }

        // 2. 查子表 sys_location
//        List<SysLocation> locationList = sysLocationMapper.selectList(
//                new LambdaQueryWrapper<SysLocation>()
//                        .eq(SysLocation::getDeptId, deptId)
//        );
//
//        // 3. 绑定
//        dept.setSysLocationList(locationList);

        return dept;
    }

    /**
     * 根据ID查询所有子部门（正常状态）
     * 
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public int selectNormalChildrenDeptById(Long deptId)
    {
        return deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getStatus, Constants.RET_CODE_0)
                        .apply("FIND_IN_SET({0}, ancestors)", deptId)
        );
    }

    /**
     * 是否存在子节点
     * 
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public boolean hasChildByDeptId(Long deptId)
    {
        int count = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getParentId, deptId)
                        .last("limit 1")
        );
        return count > 0;

    }

    /**
     * 查询部门是否存在用户
     * 
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId)
    {
        int count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeptId, deptId)
        );
        return count > 0;
    }

    /**
     * 校验部门名称是否唯一
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public boolean checkDeptNameUnique(SysDept dept)
    {
        Long deptId = StringUtils.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        SysDept info = deptMapper.selectOne(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getDeptName, dept.getDeptName())
                        .eq(SysDept::getParentId, dept.getParentId())
                        .last("limit 1")
        );

        if (StringUtils.isNotNull(info) && info.getDeptId().longValue() != deptId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验部门是否有数据权限
     * 
     * @param deptId 部门id
     */
//    @Override
//    public void checkDeptDataScope(Long deptId)
//    {
//        if (!(SecurityUtils.getUserId() == Constants.RET_CODE_1_NUM) && StringUtils.isNotNull(deptId))
//        {
//            SysDept dept = new SysDept();
//            dept.setDeptId(deptId);
//            List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
//            if (StringUtils.isEmpty(depts))
//            {
//                throw new ServiceException("没有权限访问部门数据！");
//            }
//        }
//    }

    /**
     * 新增保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDept(SysDept dept) {
        SysDept info = deptMapper.selectById(dept.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!Constants.RET_CODE_0.equals(info.getStatus())) {
            throw new ServiceException("部门停用，不允许新增");
        }
        dept.setAncestors(info.getAncestors() + "," + dept.getParentId());

        // ===== 在这里判断 ancestors，设置 deptLevel =====
        dept.setDeptLevel(calcDeptLevel(dept.getAncestors()));

        // 1. 先插入部门，主键 deptId 会自动回填到对象中（前提是 deptMapper 配置了主键返回）
        int rows = deptMapper.insert(dept);

        // 2. 再插入子表 sys_location
//        if (dept.getSysLocationList() != null && !dept.getSysLocationList().isEmpty()) {
//            for (SysLocation location : dept.getSysLocationList()) {
//                location.setDeptId(dept.getDeptId());   // 现在有值了
//                location.setDeptName(dept.getDeptName());
//                sysLocationMapper.insert(location);
//            }
//        }

        return rows;
    }


    /**
     * 修改保存部门信息
     * 
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public int updateDept(SysDept dept)
    {
        SysDept newParentDept = deptMapper.selectById(dept.getParentId());
        SysDept oldDept = deptMapper.selectById(dept.getDeptId());
        if (StringUtils.isNotNull(newParentDept) && StringUtils.isNotNull(oldDept))
        {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
        }

        // ===== 在这里判断 ancestors，设置 deptLevel =====
        dept.setDeptLevel(calcDeptLevel(dept.getAncestors()));

        // 1. 更新部门
        int result = deptMapper.updateById(dept);

        // 2. 清空旧的 sys_location
//        sysLocationMapper.delete(new LambdaQueryWrapper<SysLocation>()
//                .eq(SysLocation::getDeptId, dept.getDeptId()));
//
//        // 3. 插入新的 sys_location
//        if (dept.getSysLocationList() != null && !dept.getSysLocationList().isEmpty()) {
//            for (SysLocation location : dept.getSysLocationList()) {
//                location.setDeptId(dept.getDeptId());
//                location.setDeptName(dept.getDeptName());
//                sysLocationMapper.insert(location);
//            }
//        }

        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StringUtils.isNotEmpty(dept.getAncestors())
                && !StringUtils.equals("0", dept.getAncestors()))
        {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     * 
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDept dept)
    {
        String ancestors = dept.getAncestors(); // 例如 "1,2,3"
        Long[] deptIds = Convert.toLongArray(ancestors);

        deptMapper.update(
                null,
                new LambdaUpdateWrapper<SysDept>()
                        .in(SysDept::getDeptId, Arrays.asList(deptIds))
                        .set(SysDept::getStatus, Constants.RET_CODE_0)
        );
    }

    /**
     * 修改子元素关系
     * 
     * @param deptId 被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDept> children = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .apply("FIND_IN_SET({0}, ancestors)", deptId)
        );

        for (SysDept child : children) {
            String updatedAncestors = child.getAncestors().replaceFirst(oldAncestors, newAncestors);
            child.setAncestors(updatedAncestors);
            child.setDeptLevel(calcDeptLevel(updatedAncestors)); // ✅ 更新 deptLevel

            // 逐条更新（也可以批量 updateById(child)）
            deptMapper.update(
                    null,
                    new LambdaUpdateWrapper<SysDept>()
                            .eq(SysDept::getDeptId, child.getDeptId())
                            .set(SysDept::getAncestors, updatedAncestors)
                            .set(SysDept::getDeptLevel, child.getDeptLevel()) // ✅ 更新 deptLevel
            );
        }
    }

    /**
     * 根据 ancestors 计算部门等级
     * @param ancestors 祖级列表（例如 "0,100,121"）
     * @return 部门等级（1=总部门；2=供应商；3=员工）
     */
    public static int calcDeptLevel(String ancestors) {
        if (ancestors == null || ancestors.trim().isEmpty()) {
            return 1; // 默认一级
        }
        String[] parts = ancestors.split(",");
        int count = parts.length;
        if (count == 1) {
            return 1;
        } else if (count == 2) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDeptById(Long deptId) {
        // 1. 先删除子表 sys_location
//        sysLocationMapper.delete(
//                new LambdaQueryWrapper<SysLocation>()
//                        .eq(SysLocation::getDeptId, deptId)
//        );

        // 2. 再删除部门
        return deptMapper.deleteById(deptId);
    }


    /**
     * 状态修改
     */
    @Override
    public int updateDeptStatus(SysDept dept) {
        return deptMapper.updateById(dept);
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysDept> list, SysDept t)
    {
        // 得到子节点列表
        List<SysDept> childList = SysUtil.getChildList(list, t);
        t.setChildren(childList);
        for (SysDept tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysDept> list, SysDept t)
    {
        return SysUtil.getChildList(list, t).size() > 0;
    }

    /**
     * 查询全部供应商，多级的按照 / 拼接
     */
    @Override
    public AjaxResult selectLevel2DeptName(Location entity) {
        // 1. 查询全部部门（过滤掉 deptId = 100 的）
        List<SysDept> sysDeptList = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getDeptLevel, Constants.RET_CODE_2_NUM)
        );

        return AjaxResult.success(sysDeptList);
    }

    /**
     * 递归构造全路径部门名
     */
    private void buildDeptFullName(SysDept dept, Map<Long, List<SysDept>> childrenMap,
                                   String path, List<SysDept> result) {
        // 改写 deptName 为全路径
        dept.setDeptName(path);
        result.add(dept);

        // 子部门递归
        List<SysDept> children = childrenMap.get(dept.getDeptId());
        if (children != null) {
            for (SysDept child : children) {
                // 拼接时不会出现 “总部门/...”，因为总部门已在最初过滤掉
                buildDeptFullName(child, childrenMap, path + "/" + child.getDeptName(), result);
            }
        }
    }

    /**
     * 查询当前供应商存在的点位
     */
//    @Override
//    public AjaxResult selectSysLocationByDeptId(SysLocation entity) {
//        Long deptId = entity.getDeptId();
//
//        LambdaQueryWrapper<SysLocation> wrapper = new LambdaQueryWrapper<>();
//        // 只有当 deptId != 100 时才加 eq 条件
//        if (!deptId.equals(100L)) {
//            wrapper.eq(SysLocation::getDeptId, deptId);
//        }
//        // 添加 name 不为 null 且不等于空字符串的条件
//        wrapper.isNotNull(SysLocation::getName)
//                .and(w -> w.ne(SysLocation::getName, ""));
//
//        List<SysLocation> sysLocationList = sysLocationMapper.selectList(wrapper);
//        return AjaxResult.success(sysLocationList);
//    }
}
