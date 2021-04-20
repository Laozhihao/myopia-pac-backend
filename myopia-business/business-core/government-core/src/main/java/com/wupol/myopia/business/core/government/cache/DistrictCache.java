package com.wupol.myopia.business.core.government.cache;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.core.government.constant.DistrictCacheKey;
import com.wupol.myopia.business.core.government.domian.model.District;
import com.wupol.myopia.business.core.government.service.DistrictService;
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
public class DistrictCache implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void run(String... args) throws Exception {
        Long startTime = System.currentTimeMillis();
        // 缓存全国行政区域-列表结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_LIST)) {
            List<District> districtList = districtService.findByList(new District());
            Map<String, District> districtMap = districtList.stream().collect(Collectors.toMap(x -> String.valueOf(x.getCode()), Function.identity()));
            redisTemplate.opsForHash().putAll(DistrictCacheKey.DISTRICT_ALL_LIST, districtMap);
        }

        // 缓存全国行政区域-树结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_TREE)) {
            redisUtil.set(DistrictCacheKey.DISTRICT_ALL_TREE, districtService.getWholeCountryDistrictTree());
        }

        // 缓存各省行政区域-树结构
        if (!redisUtil.hasKey(DistrictCacheKey.DISTRICT_ALL_PROVINCE_TREE)) {
            Object cacheTree = redisUtil.get(DistrictCacheKey.DISTRICT_ALL_TREE);
            List<District> allDistrictTree = JSONObject.parseObject(JSONObject.toJSONString(cacheTree), new TypeReference<List<District>>() {});
            Map<String, District> districtMap = allDistrictTree.stream().collect(Collectors.toMap(x -> String.valueOf(x.getCode()).substring(0, 2), Function.identity()));
            redisTemplate.opsForHash().putAll(DistrictCacheKey.DISTRICT_ALL_PROVINCE_TREE, districtMap);
        }
        Long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime)/1000);
    }
}
