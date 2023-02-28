package com.wupol.myopia.business.core.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.core.common.constant.DistrictCacheKey;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/1/26
 **/
@Slf4j
@Component
@ConditionalOnProperty(name = "myopia.district-cache.enabled", havingValue = "true", matchIfMissing = true)
public class DistrictCache implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(DistrictCache.class);

    @Autowired
    private DistrictService districtService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(String... args) {
        // 缓存全国行政区域-列表结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_LIST)) {
            logger.info("...缓存全国行政区域-列表结构");
            List<District> districtList = districtService.findByList(new District());
            Map<String, Object> districtMap = districtList.stream().collect(Collectors.toMap(x -> String.valueOf(x.getCode()), Function.identity()));
            List<Map<String, Object>> mapList = splitMap(districtMap, 0);
            for (int i = 0; i < mapList.size(); i++) {
                redisUtil.hmset(DistrictCacheKey.DISTRICT_ALL_LIST, mapList.get(i));
                logger.info("...第{}批完成！",i+1);
            }
            logger.info("...完成缓存全国行政区域-列表结构");
        }

        // 缓存全国行政区域-树结构
        Object cacheList = redisUtil.get(DistrictCacheKey.DISTRICT_ALL_TREE);
        List<District> districtList = JSON.parseObject(JSON.toJSONString(cacheList), new TypeReference<List<District>>() {});
        if (CollectionUtils.isEmpty(districtList)) {
            logger.info("...缓存全国行政区域-树结构");
            redisUtil.set(DistrictCacheKey.DISTRICT_ALL_TREE, districtService.getWholeCountryDistrictTree());
            logger.info("...完成全国行政区域-树结构");
        }

        // 缓存各省行政区域-树结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_PROVINCE_TREE)) {
            logger.info("...缓存各省行政区域-树结构");
            Object cacheTree = redisUtil.get(DistrictCacheKey.DISTRICT_ALL_TREE);
            List<District> allDistrictTree = JSON.parseObject(JSON.toJSONString(cacheTree), new TypeReference<List<District>>() {});
            Map<String, Object> districtMap = allDistrictTree.stream().collect(Collectors.toMap(x -> String.valueOf(x.getCode()).substring(0, 2), Function.identity()));
            redisUtil.hmset(DistrictCacheKey.DISTRICT_ALL_PROVINCE_TREE, districtMap);
            logger.info("...完成缓存各省行政区域-树结构");
        }
    }

    /**
     *  分割Map大小
     * @author hang.yuan
     * @date 2022/4/2
     */
    private static <K, V> List<Map<K, V>> splitMap(Map<K, V> map, int pageSize) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        pageSize = pageSize == 0 ? 50000 : pageSize;
        List<Map<K, V>> newList = new ArrayList<>();
        int j = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (j % pageSize == 0) {
                newList.add(Maps.newHashMap());
            }
            newList.get(newList.size() - 1).put(entry.getKey(), entry.getValue());
            j++;
        }
        return newList;
    }
}
