package com.talent.system.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.interview.entity.Report;
import com.talent.interview.utils.DeptPermissionUtil;
import com.talent.system.entity.SysNotice;
import com.talent.common.utils.StringUtils;
import com.talent.system.mapper.SysNoticeMapper;
import com.talent.system.service.ISysNoticeService;
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
        return noticeMapper.insert(notice);
    }

    @Override
    public int updateNotice(SysNotice notice) {
        notice.setUpdateTime(new Date());
        return noticeMapper.updateById(notice);
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
