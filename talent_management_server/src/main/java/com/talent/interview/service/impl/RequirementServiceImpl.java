package com.talent.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.exception.ServiceException;
import com.talent.interview.entity.Requirement;
import com.talent.interview.mapper.RequirementMapper;
import com.talent.interview.service.RequirementService;
import com.talent.interview.utils.FileOperateUtil;
import com.talent.interview.utils.LambdaQueryBuilderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 招聘需求 Service 实现类
 */
@Service
public class RequirementServiceImpl implements RequirementService {

    // 文件路径
    @Value("${logging.home}")
    private String uploadPath;

    @Autowired
    private RequirementMapper requirementMapper;

    /**
     * 条件查询
     */
    @Override
    public List<Requirement> query(Requirement entity) {
        LambdaQueryWrapper<Requirement> wrapper = LambdaQueryBuilderUtil.buildRequirementQueryWrapper(entity);
        return requirementMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取详细信息
     */
    @Override
    public AjaxResult selectById(Long id) {
        return AjaxResult.success(requirementMapper.selectById(id));
    }

    /**
     * 保存或更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult saveOrUpdate(Requirement entity, MultipartFile picture) {
        if (entity.getId() == null) {
            // 新增
            requirementMapper.insert(entity);
        } else {
            // 构建更新 wrapper
            LambdaUpdateWrapper<Requirement> updateWrapper = new LambdaUpdateWrapper<Requirement>()
                    .eq(Requirement::getId, entity.getId())
                    .set(Requirement::getOrderNum, entity.getOrderNum())
                    .set(Requirement::getTitle, entity.getTitle())
                    .set(Requirement::getContent, entity.getContent());

            requirementMapper.update(null, updateWrapper);
        }

        // 先尝试保存图片
        String picPath = null;
        if (picture != null && !picture.isEmpty()) {
            picPath = FileOperateUtil.saveFile(
                    uploadPath,
                    "upload" + File.separator + "logoPictureFolder" + File.separator + entity.getId(),
                    picture
            );
            // 构建更新 wrapper
            LambdaUpdateWrapper<Requirement> updateWrapper = new LambdaUpdateWrapper<Requirement>()
                    .eq(Requirement::getId, entity.getId())
                    // 只有 picPath 非空时才更新 logoPath
                    .set(StringUtils.isNotEmpty(picPath), Requirement::getLogoPath, picPath);

            requirementMapper.update(null, updateWrapper);
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
        requirementMapper.deleteBatchIds(idList);
        return AjaxResult.success();
    }

    /**
     * 获取文件流
     */
    @Override
    public void getItemPic(Integer id, HttpServletResponse response) {
        Requirement info = requirementMapper.selectById(id);

        if (info == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            throw new ServiceException("文件未找到");
        }

        String fullPath = uploadPath + File.separator + info.getLogoPath();

        FileOperateUtil.getFile(fullPath, response);
    }
}
