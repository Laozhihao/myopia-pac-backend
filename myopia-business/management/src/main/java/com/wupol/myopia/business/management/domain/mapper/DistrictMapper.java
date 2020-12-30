package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.District;

import java.util.List;

/**
 * 行政区域表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-23
 */
public interface DistrictMapper extends BaseMapper<District> {
    /**
     * 获取行政区树
     *
     * @param code 根节点code
     * @return java.util.List<com.wupol.myopia.business.management.domain.model.GovDept>
     **/
    List<District> selectDistrictTree(Integer code);
    List<District> findByCodeList(Integer provinceCode, Integer cityCode, Integer areaCode, Integer townCode);
}
