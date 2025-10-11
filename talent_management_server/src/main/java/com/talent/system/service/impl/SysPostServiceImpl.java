package com.talent.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.UserConstants;
import com.talent.system.entity.SysPost;
import com.talent.system.entity.SysUserPost;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.exception.ServiceException;
import com.talent.system.mapper.SysPostMapper;
import com.talent.system.mapper.SysUserPostMapper;
import com.talent.system.service.ISysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 岗位信息 服务层处理
 * 
 * @author JamesRay
 */
@Service
public class SysPostServiceImpl implements ISysPostService
{
    @Autowired
    private SysPostMapper postMapper;

    @Autowired
    private SysUserPostMapper userPostMapper;

    /**
     * 查询岗位信息集合
     * 
     * @param post 岗位信息
     * @return 岗位信息集合
     */
    @Override
    public List<SysPost> selectPostList(SysPost post) {
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(post.getPostCode())) {
            wrapper.like(SysPost::getPostCode, post.getPostCode());
        }
        if (StringUtils.isNotEmpty(post.getPostName())) {
            wrapper.like(SysPost::getPostName, post.getPostName());
        }
        if (StringUtils.isNotEmpty(post.getStatus())) {
            wrapper.eq(SysPost::getStatus, post.getStatus());
        }
        return postMapper.selectList(wrapper);
    }

    /**
     * 查询所有岗位
     * 
     * @return 岗位列表
     */
    @Override
    public List<SysPost> selectPostAll() {
        return postMapper.selectList(null);
    }

    /**
     * 通过岗位ID查询岗位信息
     * 
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    @Override
    public SysPost selectPostById(Long postId) {
        return postMapper.selectById(postId);
    }

    /**
     * 根据用户ID获取岗位选择框列表
     * 
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    @Override
    public List<Long> selectPostListByUserId(Long userId) {
        // 第一步：通过 user_post 表查出该用户绑定的 postId 列表
        List<SysUserPost> userPosts = userPostMapper.selectList(
                new LambdaQueryWrapper<SysUserPost>()
                        .eq(SysUserPost::getUserId, userId)
        );

        // 第二步：提取 postId 并返回（或可用于查 SysPost 详情）
        List<Long> postIds = new ArrayList<>();
        for (SysUserPost up : userPosts) {
            if (up.getPostId() != null) {
                postIds.add(up.getPostId());
            }
        }

        return postIds;
    }


    /**
     * 校验岗位名称是否唯一
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPost post) {
        Long postId = (post.getPostId() == null) ? -1L : post.getPostId();
        SysPost dbPost = postMapper.selectOne(
                new LambdaQueryWrapper<SysPost>()
                        .eq(SysPost::getPostName, post.getPostName())
                        .last("limit 1")
        );
        return dbPost != null && !dbPost.getPostId().equals(postId)
                ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 校验岗位编码是否唯一
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPost post) {
        Long postId = (post.getPostId() == null) ? -1L : post.getPostId();
        SysPost dbPost = postMapper.selectOne(
                new LambdaQueryWrapper<SysPost>()
                        .eq(SysPost::getPostCode, post.getPostCode())
                        .last("limit 1")
        );
        return dbPost != null && !dbPost.getPostId().equals(postId)
                ? UserConstants.NOT_UNIQUE : UserConstants.UNIQUE;
    }

    /**
     * 通过岗位ID查询岗位使用数量
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public int countUserPostById(Long postId) {
        return userPostMapper.selectCount(
                new LambdaQueryWrapper<SysUserPost>()
                        .eq(SysUserPost::getPostId, postId)
        );
    }

    /**
     * 删除岗位信息
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public int deletePostById(Long postId) {
        return postMapper.deleteById(postId);
    }

    /**
     * 批量删除岗位信息
     * 
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    @Override
    public int deletePostByIds(Long[] postIds) {
        for (Long postId : postIds) {
            SysPost post = selectPostById(postId);
            if (countUserPostById(postId) > 0) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", post.getPostName()));
            }
        }
        return postMapper.deleteBatchIds(Arrays.asList(postIds));
    }


    /**
     * 新增保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public int insertPost(SysPost post) {
        post.setCreateTime(new Date());
        return postMapper.insert(post);
    }

    /**
     * 修改保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public int updatePost(SysPost post) {
        post.setUpdateTime(new Date());
        return postMapper.updateById(post);
    }
}
