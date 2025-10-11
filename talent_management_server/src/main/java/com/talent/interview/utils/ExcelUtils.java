package com.talent.interview.utils;

import com.talent.common.utils.StringUtils;
import com.talent.interview.entity.Location;
import com.talent.system.entity.SysDept;
import com.talent.system.entity.SysDictData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelUtils {


    /**
     * 日期字段一定要用 getCellDate，用 DataFormatter 或 DateUtil 转换防止格式错
     * @param cell
     * @return
     */
    public static BigDecimal getCellBigDecimal(Cell cell) {
        if (cell == null) return null;
        try {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } catch (Exception e) {
            try {
                return new BigDecimal(cell.getStringCellValue().trim());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * 从 Cell 读取日期，支持 Excel 内部日期格式和多种文本格式
     */
    public static Date getCellDate(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    long timestamp = (long) numericValue;
                    return new Date(timestamp);
                }
            } else if (cell.getCellType() == CellType.STRING) {
                String str = cell.getStringCellValue().trim();
                if (str.isEmpty()) return null;

                // 替换所有可能的分隔符为统一的 -
                str = str.replaceAll("[./]", "-");

                // 支持 yyyy-M-d、yyyy-MM-dd
                String[] formats = {"yyyy-M-d", "yyyy-MM-dd"};

                for (String format : formats) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(format);
                        sdf.setLenient(false); // 严格模式
                        return sdf.parse(str);
                    } catch (ParseException ignored) {
                    }
                }

                return null; // 都解析不了就返回 null
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Cell 读取日期，支持 Excel 内部日期格式和文本
     */
    public static Date formatDate(Date date) {
        if (date == null) return null;
        try {
            // 格式化成 yyyy-MM-dd 再解析成 Date，这样可把多余的时分秒去掉
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String str = sdf.format(date);
            return sdf.parse(str);
        } catch (Exception e) {
            return date;
        }
    }

    /**
     * 获取单元格中的数字
     */
    public static String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 这里强转为 long，再转 String，可防止小数点和科学计数法
                return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString().replace(".0", "");
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 获取单元格中的整数
     */
    public static Integer getCellInteger(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                String str = cell.getStringCellValue().trim();
                return str.isEmpty() ? null : Integer.valueOf(str);
            case NUMERIC:
                // 直接取数值转 int，避免科学计数法
                return (int) cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue() ? 1 : 0;
            default:
                return null;
        }
    }

    /**
     * 校验面试点位是否存在，并返回ID
     *
     * @param locationList 已有点位集合
     * @param locationName Excel中读取的点位名称
     * @param rowError     错误信息收集器
     * @return 点位ID，如果不存在则返回 null
     */
    public static Integer getLocationId(List<Location> locationList,
                                        String locationName,
                                        StringBuilder rowError) {
        if (StringUtils.isBlank(locationName)) {
            rowError.append("面试点位不能为空; ");
            return null;
        }

        Location match = locationList.stream()
                .filter(loc -> locationName.equals(loc.getName()))
                .findFirst()
                .orElse(null);

        if (match != null) {
            return match.getId();
        } else {
            rowError.append("面试点位[").append(locationName).append("]不存在; ");
            return null;
        }
    }

    /**
     * 校验部门是否存在，并返回ID（Integer）
     *
     * @param deptList   已有部门集合
     * @param deptName   Excel中读取的部门名称
     * @param rowError   错误信息收集器
     * @return 部门ID，如果不存在则返回 null
     */
    public static Integer getDeptId(List<SysDept> deptList,
                                    String deptName,
                                    StringBuilder rowError) {
        if (StringUtils.isBlank(deptName)) {
            rowError.append("归属供应商不能为空; ");
            return null;
        }

        SysDept match = deptList.stream()
                .filter(dept -> deptName.equals(dept.getDeptName()))
                .findFirst()
                .orElse(null);

        if (match != null && match.getDeptId() != null) {
            // Long 转 Integer（带范围保护）
            Long deptId = match.getDeptId();
            if (deptId > Integer.MAX_VALUE) {
                rowError.append("部门ID超出范围; ");
                return null;
            }
            return deptId.intValue();
        } else {
            rowError.append("归属供应商[").append(deptName).append("]不存在; ");
            return null;
        }
    }



    /**
     * Excel字典值校验与转换（可为空）
     *
     * @param sysDictDataList 所有字典集合
     * @param dictType        字典类型（如 sys_user_sex / sys_education / sys_judge）
     * @param label           Excel中的值（label）
     * @param rowError        当前行的错误信息收集器
     * @param fieldName       中文字段名（用于拼接错误提示）
     * @return 字典值（dictValue），如果为空或无效则返回null
     */
    public static String getDictValueAllowNull(List<SysDictData> sysDictDataList,
                                               String dictType,
                                               String label,
                                               StringBuilder rowError,
                                               String fieldName) {
        if (StringUtils.isBlank(label)) {
            // 可以为空 → 返回 null
            return null;
        }

        SysDictData dict = sysDictDataList.stream()
                .filter(d -> dictType.equals(d.getDictType()) && label.equals(d.getDictLabel()))
                .findFirst()
                .orElse(null);

        if (dict != null) {
            return dict.getDictValue();
        } else {
            rowError.append(fieldName)
                    .append("[").append(label).append("]无效; ");
            return null;
        }
    }
}
