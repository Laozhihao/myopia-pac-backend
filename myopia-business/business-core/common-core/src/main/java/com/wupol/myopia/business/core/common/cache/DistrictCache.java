package com.wupol.myopia.business.core.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.core.common.constant.DistrictCacheKey;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/1/26
 **/
@Component
@Slf4j
public class DistrictCache implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(DistrictCache.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(String... args) throws Exception {
        logger.debug("开始缓存district数据");
        // 缓存全国行政区域-列表结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_LIST)) {
            logger.debug("...缓存全国行政区域-列表结构");
            List<District> districtList = districtService.findByList(new District());
            Map<String, District> districtMap = districtList.stream().collect(Collectors.toMap(x -> String.valueOf(x.getCode()), Function.identity()));
            redisTemplate.opsForHash().putAll(DistrictCacheKey.DISTRICT_ALL_LIST, districtMap);
            logger.debug("...完成缓存全国行政区域-列表结构");
        }

        // 缓存全国行政区域-树结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_TREE)) {
            logger.debug("...缓存全国行政区域-树结构");
            redisUtil.set(DistrictCacheKey.DISTRICT_ALL_TREE, districtService.getWholeCountryDistrictTree());
            logger.debug("...完成全国行政区域-树结构");
        }

        // 缓存各省行政区域-树结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_PROVINCE_TREE)) {
            logger.debug("...缓存各省行政区域-树结构");
            Object cacheTree = redisUtil.get(DistrictCacheKey.DISTRICT_ALL_TREE);
            List<District> allDistrictTree = JSON.parseObject(JSON.toJSONString(cacheTree), new TypeReference<List<District>>() {});
            Map<String, District> districtMap = allDistrictTree.stream().collect(Collectors.toMap(x -> String.valueOf(x.getCode()).substring(0, 2), Function.identity()));
            redisTemplate.opsForHash().putAll(DistrictCacheKey.DISTRICT_ALL_PROVINCE_TREE, districtMap);
        }
        logger.debug("完成缓存district数据");
    }
}
