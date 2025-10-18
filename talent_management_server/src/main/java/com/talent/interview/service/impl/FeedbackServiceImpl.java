package com.talent.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.talent.common.constant.Constants;
import com.talent.common.domain.AjaxResult;
import com.talent.common.utils.SecurityUtils;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.exception.ServiceException;
import com.talent.common.utils.mybatisPlus.MyBatisBatchInsertHelper;
import com.talent.interview.entity.Feedback;
import com.talent.interview.entity.Location;
import com.talent.interview.entity.Report;
import com.talent.interview.mapper.FeedbackMapper;
import com.talent.interview.mapper.LocationMapper;
import com.talent.interview.service.FeedbackService;
import com.talent.interview.utils.ExcelUtils;
import com.talent.interview.utils.LambdaQueryBuilderUtil;
import com.talent.system.entity.SysDept;
import com.talent.system.entity.SysDictData;
import com.talent.system.mapper.SysDeptMapper;
import com.talent.system.mapper.SysDictDataMapper;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.talent.common.page.PageUtils.startPage;

/**
 * 面试反馈 Service 实现类
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    // 文件路径
    @Value("${logging.home}")
    private String uploadPath;

    /**
     * 条件查询（不分页）
     */
    @Override
    public List<Feedback> queryList(Feedback entity) {
        LambdaQueryWrapper<Feedback> wrapper = LambdaQueryBuilderUtil.buildFeedbackQueryWrapper(entity);
        return feedbackMapper.selectList(wrapper);
    }

    /**
     * 条件查询（分页）
     */
    @Override
    public List<Feedback> queryPage(Feedback entity) {
        LambdaQueryWrapper<Feedback> wrapper = LambdaQueryBuilderUtil.buildFeedbackQueryWrapper(entity);
        startPage();
        return feedbackMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取详细信息
     */
    @Override
    public AjaxResult selectById(Long id) {
        return AjaxResult.success(feedbackMapper.selectById(id));
    }

    /**
     * 保存或更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult saveOrUpdate(Feedback entity) {
        // 先检查名称唯一性
        Integer count = feedbackMapper.selectCount(
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getPhone, entity.getPhone())
                        .ne(entity.getId() != null, Feedback::getId, entity.getId()) // 如果是更新，排除自己
        );

        if (count != null && count > 0) {
            return AjaxResult.error("手机号码已存在，请重新输入");
        }

        if (entity.getId() == null) {
            entity.setCreateName(SecurityUtils.getLoginUser().getUser().getNickName());
            feedbackMapper.insert(entity);
        } else {
            MyBatisBatchInsertHelper.updateAllFieldsById(entity, feedbackMapper);
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

        // 删除数据
        feedbackMapper.deleteBatchIds(idList);

        return AjaxResult.success();
    }

    /**
     * 导入面试反馈（方案A：统一返回JSON + 错误文件下载地址）
     */
    @Override
    public AjaxResult importData(HttpServletResponse response, MultipartFile file) {
        List<Feedback> feedbackList = new ArrayList<>();

        // 查询全部点位
        List<Location> locationList = Optional.ofNullable(locationMapper.selectList(null)).orElse(new ArrayList<>());

        // 查询全部部门
        List<SysDept> sysDeptList = Optional.ofNullable(sysDeptMapper.selectList(null)).orElse(new ArrayList<>());

        // 查询全部字典
        List<SysDictData> sysDictDataList = Optional.ofNullable(dictDataMapper.selectList(null))
                .orElse(new ArrayList<>());

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(Constants.RET_CODE_0_NUM);

            if (sheet == null) {
                throw new ServiceException("Excel中未找到有效sheet页");
            }

            // 第1行为表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ServiceException("未找到标题行");
            }

            // 读取标题列的位置
            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String col = cell.getStringCellValue().trim();
                colMap.put(col, cell.getColumnIndex());
            }

            // 必须字段
            String[] needed = {"ID", "面试点位", "归属供应商", "姓名", "性别", "电话", "年龄", "学历", "面试日期", "反馈原因", "硬性条件", "是否计费"};
            for (String key : needed) {
                if (!colMap.containsKey(key)) {
                    throw new ServiceException("缺少列：" + key);
                }
            }

            // ===== 新增：错误信息列 =====
            int errorColIndex = headerRow.getLastCellNum();
            boolean hasErrorCol = false;
            for (Cell cell : headerRow) {
                if ("错误信息".equals(cell.getStringCellValue().trim())) {
                    errorColIndex = cell.getColumnIndex();
                    hasErrorCol = true;
                    break;
                }
            }
            if (!hasErrorCol) {
                Cell errorHeader = headerRow.createCell(errorColIndex);
                errorHeader.setCellValue("错误信息");
            }

            int errorCount = 0;

            // 从第2行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Feedback info = new Feedback();
                StringBuilder rowError = new StringBuilder();

                // ID
                info.setId(ExcelUtils.getCellInteger(row.getCell(colMap.get("ID"))));

                // 面试点位
                String locationName = ExcelUtils.getCellString(row.getCell(colMap.get("面试点位")));
                info.setLocationName(locationName);
                info.setLocationId(ExcelUtils.getLocationId(locationList, locationName, rowError));

                // 归属供应商
                String deptName = ExcelUtils.getCellString(row.getCell(colMap.get("归属供应商")));
                info.setDeptName(deptName);
                info.setDeptId(ExcelUtils.getDeptId(sysDeptList, deptName, rowError));

                // 姓名
                info.setName(ExcelUtils.getCellString(row.getCell(colMap.get("姓名"))));

                // 性别（sys_user_sex，可为空）
                info.setSex(ExcelUtils.getDictValueAllowNull(sysDictDataList, "sys_user_sex",
                        ExcelUtils.getCellString(row.getCell(colMap.get("性别"))), rowError, "性别"));

                // 电话
                info.setPhone(ExcelUtils.getCellString(row.getCell(colMap.get("电话"))));

                // 年龄
                info.setAge(ExcelUtils.getCellString(row.getCell(colMap.get("年龄"))));

                // 学历（sys_education，可为空）
                info.setEducation(ExcelUtils.getDictValueAllowNull(sysDictDataList, "sys_education",
                        ExcelUtils.getCellString(row.getCell(colMap.get("学历"))), rowError, "学历"));

                // 面试日期
                info.setInterviewDate(ExcelUtils.getCellDate(row.getCell(colMap.get("面试日期"))));

                // 反馈原因
                info.setReason(ExcelUtils.getCellString(row.getCell(colMap.get("反馈原因"))));

                // 硬性条件（sys_judge，可为空）
                info.setHardRequirements(ExcelUtils.getDictValueAllowNull(sysDictDataList, "sys_judge",
                        ExcelUtils.getCellString(row.getCell(colMap.get("硬性条件"))), rowError, "硬性条件"));

                // 是否计费（sys_judge，可为空）
                info.setIsBilling(ExcelUtils.getDictValueAllowNull(sysDictDataList, "sys_judge",
                        ExcelUtils.getCellString(row.getCell(colMap.get("是否计费"))), rowError, "是否计费"));

                // 设置总部门审批信息
                info.setLevel1Person(SecurityUtils.getUsername());
                info.setLevel1Time(new Date());

                // ===== 写入错误信息列 =====
                if (rowError.length() > 0) {
                    errorCount++;
                    Cell errorCell = row.getCell(errorColIndex);
                    if (errorCell == null) {
                        errorCell = row.createCell(errorColIndex);
                    }
                    errorCell.setCellValue(rowError.toString());
                } else {
                    feedbackList.add(info);
                }
            }

            // 入库正确数据
            feedbackList.forEach(info -> {
                if (info.getId() == null) {
                    feedbackMapper.insert(info); // 新增
                } else {
                    LambdaUpdateWrapper<Feedback> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(Feedback::getId, info.getId())
                            .set(StringUtils.isNotEmpty(info.getLocationName()), Feedback::getLocationName, info.getLocationName())
                            .set(null != info.getLocationId(), Feedback::getLocationId, info.getLocationId())
                            .set(StringUtils.isNotEmpty(info.getDeptName()), Feedback::getDeptName, info.getDeptName())
                            .set(null !=info.getDeptId(), Feedback::getDeptId, info.getDeptId())
                            .set(StringUtils.isNotEmpty(info.getName()), Feedback::getName, info.getName())
                            .set(StringUtils.isNotEmpty(info.getSex()), Feedback::getSex, info.getSex())
                            .set(StringUtils.isNotEmpty(info.getPhone()), Feedback::getPhone, info.getPhone())
                            .set(StringUtils.isNotEmpty(info.getAge()), Feedback::getAge, info.getAge())
                            .set(StringUtils.isNotEmpty(info.getEducation()), Feedback::getEducation, info.getEducation())
                            .set(null !=info.getInterviewDate(), Feedback::getInterviewDate, info.getInterviewDate())
                            .set(StringUtils.isNotEmpty(info.getReason()), Feedback::getReason, info.getReason())
                            .set(StringUtils.isNotEmpty(info.getHardRequirements()), Feedback::getHardRequirements, info.getHardRequirements())
                            .set(StringUtils.isNotEmpty(info.getIsBilling()), Feedback::getIsBilling, info.getIsBilling())
                            .set(StringUtils.isNotEmpty(info.getLevel1Person()), Feedback::getLevel1Person, info.getLevel1Person())
                            .set(null !=info.getLevel1Time(), Feedback::getLevel1Time, info.getLevel1Time())
                    ;
                    feedbackMapper.update(null, wrapper);
                }
            });

            int successCount = feedbackList.size();

            if (errorCount > 0) {
                String fileName = "面试反馈导入结果.xlsx";
                String dirPath = uploadPath + File.separator + "importTemp";
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String filePath = dirPath + File.separator + fileName;

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                }

                return AjaxResult.error("导入完成，成功 " + successCount + " 条，失败 " + errorCount + " 条，请下载文件查看")
                        .put("fileUrl", "/profile/importTemp/" + fileName)
                        .put("successCount", successCount)
                        .put("failCount", errorCount);
            } else {
                return AjaxResult.success("导入成功，共导入 " + successCount + " 条数据")
                        .put("successCount", successCount)
                        .put("failCount", 0);
            }

        } catch (Exception e) {
            throw new ServiceException("读取Excel失败：" + e.getMessage());
        }
    }

    /**
     * 二级一键推送数据
     */
    @Override
    public AjaxResult secondPushData(Feedback entity) {
        List<Feedback> feedbackList = feedbackMapper.selectList(
                LambdaQueryBuilderUtil.buildFeedbackQueryWrapper(entity));

        if (CollectionUtils.isEmpty(feedbackList)) {
            return AjaxResult.success("no Data");
        }

        String username = SecurityUtils.getUsername();
        Date now = new Date();

        feedbackList.forEach(f -> {
            // 反馈原因2
            if (StringUtils.isEmpty(f.getReason2())) {
                f.setReason2(f.getReason());
            }
            // 硬性条件2
            if (StringUtils.isEmpty(f.getHardRequirements2())) {
                f.setHardRequirements2(f.getHardRequirements());
            }
            // 是否计费2
            if (StringUtils.isEmpty(f.getIsBilling2())) {
                f.setIsBilling2(f.getIsBilling());
            }
            // 设置二级已审批
            f.setLevel2Person(username);
            f.setLevel2Time(now);

            // 更新固定字段
            LambdaUpdateWrapper<Feedback> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Feedback::getId, f.getId())
                    .set(StringUtils.isNotEmpty(f.getReason2()), Feedback::getReason2, f.getReason2())
                    .set(StringUtils.isNotEmpty(f.getHardRequirements2()), Feedback::getHardRequirements2, f.getHardRequirements2())
                    .set(StringUtils.isNotEmpty(f.getIsBilling2()), Feedback::getIsBilling2, f.getIsBilling2())
                    .set(StringUtils.isNotEmpty(f.getLevel2Person()), Feedback::getLevel2Person, f.getLevel2Person())
                    .set(null != f.getLevel2Time(), Feedback::getLevel2Time, f.getLevel2Time());
            feedbackMapper.update(null, updateWrapper);
        });

        return AjaxResult.success("second Data Push Success!");
    }
}
