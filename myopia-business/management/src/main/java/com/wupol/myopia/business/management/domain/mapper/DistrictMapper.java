package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.District;
import org.apache.ibatis.annotations.Param;

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
    List<District> selectDistrictTree(Long code);

    List<District> selectChildNodeByParentCode(Long code);

    List<District> findByCodeList(Long provinceCode, Long cityCode, Long areaCode, Long townCode);

    List<District> getByCodes(@Param("codes") List<Long> codes);
}
