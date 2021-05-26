package com.wupol.myopia.business.core.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.system.domain.mapper.BigScreenMapMapper;
import com.wupol.myopia.business.core.system.domain.model.BigScreenMap;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * @Author jacob
 * @Date 2021-03-20
 */
@Service
@EnableCaching
public class BigScreenMapService extends BaseService<BigScreenMapMapper, BigScreenMap> {

    /**
     * 通过地区id获取城市位置
     * @param provinceDistrictId
     * @return
     */
    public  Map<Integer, List<Double>>  getCityCenterLocationByDistrictId(Integer provinceDistrictId) {
        LambdaQueryWrapper<BigScreenMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BigScreenMap::getDistrictId,provinceDistrictId).select(BigScreenMap::getCityCenterLocation);
        BigScreenMap bigScreenMap = baseMapper.selectOne(queryWrapper);
        return bigScreenMap.getCityCenterLocation();
    }

    /**
     * 通过地区id获取MapData 的json数据
     * @param provinceDistrictId
     * @return
     */
    @Cacheable(cacheNames = "myopia:big_screening_map",key = "#provinceDistrictId",condition = "#result == null")
    public Object getMapDataByDistrictId(Integer provinceDistrictId) {
        LambdaQueryWrapper<BigScreenMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BigScreenMap::getDistrictId,provinceDistrictId).select(BigScreenMap::getJson);
        BigScreenMap bigScreenMap = baseMapper.selectOne(queryWrapper);
        return bigScreenMap.getJson();
    }

}
