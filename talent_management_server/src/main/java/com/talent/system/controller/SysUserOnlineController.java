package com.talent.system.controller;

import com.github.pagehelper.PageInfo;
import com.talent.common.constant.BusinessType;
import com.talent.common.constant.CacheConstants;
import com.talent.common.constant.HttpStatus;
import com.talent.common.controller.BaseController;
import com.talent.common.domain.AjaxResult;
import com.talent.common.page.TableDataInfo;
import com.talent.system.config.annotation.Log;
import com.talent.system.entity.SysUserOnline;
import com.talent.system.entity.login.LoginUser;
import com.talent.system.service.ISysUserOnlineService;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.redis.RedisCache;
import com.talent.system.utils.SysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线用户监控
 * 
 * @author JamesRay
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController
{
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName)
    {
        List<SysUserOnline> userOnlineList = SysUtil.searchOnlineUser(ipaddr, userName, redisCache, userOnlineService);
        return getDataTable(userOnlineList);
    }

    /**
     * 强退用户
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId)
    {
        redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return success();
    }

    /**
     * 查询全部用户
     */
    @GetMapping("/allOnlineList")
    public AjaxResult AllOnlineList(String ipaddr, String userName)
    {
        List<SysUserOnline> userOnlineList = SysUtil.searchOnlineUser(ipaddr, userName, redisCache, userOnlineService);

        // 过滤去重
        List<SysUserOnline> distinctList = userOnlineList.stream()
                .filter(Objects::nonNull)  // 过滤掉null对象
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() ->
                                new TreeSet<>(Comparator.comparing(SysUserOnline::getUserName))),
                        ArrayList::new
                ));
        return AjaxResult.success(distinctList);
    }
}
