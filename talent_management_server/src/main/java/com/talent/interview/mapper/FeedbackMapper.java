package com.talent.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.talent.interview.entity.Feedback;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 面试反馈 Mapper
 */
public interface FeedbackMapper extends BaseMapper<Feedback> {

}
