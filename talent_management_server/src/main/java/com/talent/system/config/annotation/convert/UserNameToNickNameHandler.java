package com.talent.system.config.annotation.convert;

import com.talent.common.utils.poi.ExcelHandlerAdapter;
import com.talent.common.utils.spring.SpringUtils;
import com.talent.system.entity.SysUser;
import com.talent.system.mapper.SysUserMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 将 userName 转为 nickName 的 Excel 导出处理器
 */
public class UserNameToNickNameHandler implements ExcelHandlerAdapter {

    @Override
    public Object format(Object value, String[] args, Cell cell, Workbook wb) {
        if (value == null) return "";

        // 原始用户名字符串
        String userNames = String.valueOf(value);

        // 获取用户列表（建议缓存）
        SysUserMapper sysUserMapper = SpringUtils.getBean(SysUserMapper.class);
        List<SysUser> userList = sysUserMapper.selectList(null);

        // 创建用户名 -> 昵称的映射
        Map<String, String> userMap = userList.stream()
                .collect(Collectors.toMap(SysUser::getUserName, SysUser::getNickName));

        // 处理多个用户名（如 "zr,siqiang"）
        return Arrays.stream(userNames.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(name -> userMap.getOrDefault(name, name)) // 没找到就原样返回
                .collect(Collectors.joining(","));
    }
}
