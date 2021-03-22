package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.BigScreenMapMapper;
import com.wupol.myopia.business.management.domain.model.BigScreenMap;
import org.springframework.stereotype.Service;

/**
 * @Author jacob
 * @Date 2021-03-20
 */
@Service
public class BigScreenMapService extends BaseService<BigScreenMapMapper, BigScreenMap> {

    /**
     * 通过地区id获取jsonObject 地图数据
     * @param provinceDistrictId
     * @return
     */
    public BigScreenMap getByDistrictId(Integer provinceDistrictId) {
        LambdaQueryWrapper<BigScreenMap> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BigScreenMap::getDistrictId,provinceDistrictId);
        BigScreenMap bigScreenMap = baseMapper.selectOne(queryWrapper);
        return  bigScreenMap;
    }
}
