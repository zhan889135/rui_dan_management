package com.talent.system.utils;

import com.talent.common.utils.StringUtils;
import com.talent.system.entity.SysDept;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// 系统管理通用工具类
public class SysUtil {

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
}
