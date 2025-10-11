package com.talent.common.page;

import com.talent.common.page.sql.SqlUtil;
import com.github.pagehelper.PageHelper;

import java.util.Collections;
import java.util.List;

/**
 * 分页工具类
 * 
 * @author JamesRay
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }

    /**
     * 手动分页工具
     */
    public static <T> List<T> manualSubList(List<T> list) {

        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        if (list == null || list.isEmpty()) return Collections.emptyList();

        int fromIndex = Math.max((pageNum - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, list.size());

        return fromIndex >= toIndex ? Collections.emptyList() : list.subList(fromIndex, toIndex);
    }

}
