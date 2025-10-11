package com.talent.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.talent.common.constant.UserConstants;
import com.talent.system.entity.SysConfig;
import com.talent.system.mapper.SysConfigMapper;
import com.talent.system.service.ISysConfigService;
import com.talent.common.utils.StringUtils;
import com.talent.common.utils.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数配置 服务层实现
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    @Autowired
    private SysConfigMapper configMapper;

//    @Autowired
//    private RedisCache redisCache;

    /**
     * 项目启动时，初始化参数到缓存
     */
//    @PostConstruct
//    public void init()
//    {
//        loadingConfigCache();
//    }

    /**
     * 查询参数配置信息
     * 
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        return configMapper.selectById(configId);
    }

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey)
    {
//        String configValue = Convert.toStr(redisCache.getCacheObject(getCacheKey(configKey)));
//        if (StringUtils.isNotEmpty(configValue))
//        {
//            return configValue;
//        }
//        SysConfig config = new SysConfig();
//        config.setConfigKey(configKey);
//        SysConfig retConfig = configMapper.selectConfig(config);
//        if (StringUtils.isNotNull(retConfig))
//        {
//            redisCache.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue());
//            return retConfig.getConfigValue();
//        }
        SysConfig sysConfig = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey)
                        .last("LIMIT 1")
        );
        if(StringUtils.isNotNull(sysConfig)){
            return sysConfig.getConfigValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取验证码开关
     * 
     * @return true开启，false关闭
     */
//    @Override
//    public boolean selectCaptchaEnabled()
//    {
//        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
//        if (StringUtils.isEmpty(captchaEnabled))
//        {
//            return true;
//        }
//        return Convert.toBool(captchaEnabled);
//    }

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotEmpty(config.getConfigName())) {
            wrapper.like(SysConfig::getConfigName, config.getConfigName());
        }

        if (StringUtils.isNotEmpty(config.getConfigType())) {
            wrapper.eq(SysConfig::getConfigType, config.getConfigType());
        }

        if (StringUtils.isNotEmpty(config.getConfigKey())) {
            wrapper.like(SysConfig::getConfigKey, config.getConfigKey());
        }

        // 时间范围
        if (config.getParams() != null) {
            Object beginTime = config.getParams().get("beginTime");
            Object endTime = config.getParams().get("endTime");

            if (beginTime != null && StringUtils.isNotEmpty(beginTime.toString())) {
                wrapper.ge(SysConfig::getCreateTime, beginTime.toString());
            }

            if (endTime != null && StringUtils.isNotEmpty(endTime.toString())) {
                wrapper.le(SysConfig::getCreateTime, endTime.toString());
            }
        }

        return configMapper.selectList(wrapper);
    }



    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        return configMapper.insert(config);
    }

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        return configMapper.updateById(config);
    }

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            configMapper.deleteById(configId);
        }
    }

    /**
     * 加载参数缓存数据
     */
//    @Override
//    public void loadingConfigCache()
//    {
//        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
//        for (SysConfig config : configsList)
//        {
//            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
//        }
//    }

    /**
     * 清空参数缓存数据
     */
//    @Override
//    public void clearConfigCache()
//    {
//        Collection<String> keys = redisCache.keys(CacheConstants.SYS_CONFIG_KEY + "*");
//        redisCache.deleteObject(keys);
//    }

    /**
     * 重置参数缓存数据
     */
//    @Override
//    public void resetConfigCache()
//    {
//        clearConfigCache();
//        loadingConfigCache();
//    }

    /**
     * 校验参数键名是否唯一
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig config) {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();

        SysConfig info = configMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>()
                        .eq(SysConfig::getConfigKey, config.getConfigKey())
                        .last("limit 1")
        );

        if (StringUtils.isNotNull(info) && !info.getConfigId().equals(configId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }


    /**
     * 设置cache key
     * 
     * @param configKey 参数键
     * @return 缓存键key
     */
//    private String getCacheKey(String configKey)
//    {
//        return CacheConstants.SYS_CONFIG_KEY + configKey;
//    }
}
