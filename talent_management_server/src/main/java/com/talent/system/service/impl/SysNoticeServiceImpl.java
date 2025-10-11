package com.talent.system.service.impl;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.interview.entity.Report;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.system.entity.SysDept;
import com.talent.system.entity.SysNotice;
import com.talent.common.utils.StringUtils;
import com.talent.system.mapper.SysDeptMapper;
import com.talent.system.mapper.SysNoticeMapper;
import com.talent.system.service.ISysNoticeService;
import com.talent.system.utils.SysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.talent.common.page.PageUtils.startPage;

/**
 * 公告 服务层实现
 */
@Service
public class SysNoticeServiceImpl implements ISysNoticeService {

    @Autowired
    private SysNoticeMapper noticeMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public SysNotice selectNoticeById(Long noticeId) {
        return noticeMapper.selectById(noticeId);
    }

    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice) {
        LambdaQueryWrapper<SysNotice> wrapper = DeptPermissionUtil.buildDeptScopeWrapper(SysNotice::getDeptId);

        if (StringUtils.isNotEmpty(notice.getNoticeTitle())) {
            wrapper.like(SysNotice::getNoticeTitle, notice.getNoticeTitle());
        }
        if (StringUtils.isNotEmpty(notice.getNoticeType())) {
            wrapper.eq(SysNotice::getNoticeType, notice.getNoticeType());
        }
        if (StringUtils.isNotEmpty(notice.getCreateBy())) {
            wrapper.like(SysNotice::getCreateBy, notice.getCreateBy());
        }
        startPage();
        return noticeMapper.selectList(wrapper);
    }

    @Override
    public int insertNotice(SysNotice notice) {
        notice.setCreateTime(new Date()); // 如果用自动填充也可以去掉
        notice.setDeptId(joinDeptId(notice.getDeptId()));
        return noticeMapper.insert(notice);
    }

    @Override
    public int updateNotice(SysNotice notice) {
        notice.setUpdateTime(new Date());
        notice.setDeptId(joinDeptId(notice.getDeptId()));
        return noticeMapper.updateById(notice);
    }

    // 发公告，选择二级供应商后，要递归回去二级的所有子部门
    public String joinDeptId(String deptId){
        // 查询全部部门
        List<SysDept> sysDeptList = Optional.ofNullable(sysDeptMapper.selectList(null)).orElse(new ArrayList<>());

        // 发公告，选择二级供应商后，要递归回去二级的所有子部门
        return SysUtil.searchSubDeptAndJoinId(deptId, sysDeptList);
    }

    @Override
    public int deleteNoticeById(Long noticeId) {
        return noticeMapper.deleteById(noticeId);
    }

    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        return noticeMapper.deleteBatchIds(Arrays.asList(noticeIds));
    }
}
