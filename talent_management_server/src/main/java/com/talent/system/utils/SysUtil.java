package com.talent.system.utils;

import com.talent.common.constant.CacheConstants;
import com.talent.common.constant.Constants;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.redis.RedisCache;
import com.talent.system.entity.SysDept;
import com.talent.system.entity.SysUserOnline;
import com.talent.system.entity.login.LoginUser;
import com.talent.system.service.ISysUserOnlineService;

import java.util.*;
import java.util.stream.Collectors;

// 系统管理通用工具类
public class SysUtil {

    /**
     * 查询登录用户
     */
    public static List<SysUserOnline> searchOnlineUser(String ipaddr, String userName,
                                                      RedisCache redisCache, ISysUserOnlineService userOnlineService){
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys)
        {
            LoginUser user = redisCache.getCacheObject(key);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
            {
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
            }
            else if (StringUtils.isNotEmpty(ipaddr))
            {
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
            }
            else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser()))
            {
                userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
            }
            else
            {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return userOnlineList;
    }

    /**
     * 部门获取得到子节点列表
     */
    public static List<SysDept> getChildList(List<SysDept> list, SysDept t)
    {
        List<SysDept> tlist = new ArrayList<SysDept>();
        Iterator<SysDept> it = list.iterator();
        while (it.hasNext())
        {
            SysDept n = (SysDept) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 递归获取部门的所有子部门ID
     */
    public static List<Long> getAllChildDeptIds(List<SysDept> allDepts, SysDept parentDept) {
        List<Long> childIds = new ArrayList<>();

        // 获取直接子节点
        List<SysDept> childList = getChildList(allDepts, parentDept);

        for (SysDept child : childList) {
            childIds.add(child.getDeptId());
            // 递归获取子节点的子节点
            childIds.addAll(getAllChildDeptIds(allDepts, child));
        }

        return childIds;
    }

    /**
     * 发公告，面试点位，选择二级供应商后，要递归回去二级的所有子部门
     */
    public static String searchSubDeptAndJoinId(String deptId, List<SysDept> sysDeptList){
        String joinDeptId = deptId;
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
            joinDeptId = resultDeptIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        return joinDeptId;
    }
}
