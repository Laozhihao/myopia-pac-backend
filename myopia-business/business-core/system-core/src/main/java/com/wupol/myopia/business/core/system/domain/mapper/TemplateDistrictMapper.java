package com.wupol.myopia.business.core.system.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板区域Mapper
 *
 * @author Simple4H
 */
public interface TemplateDistrictMapper extends BaseMapper<TemplateDistrict> {

    /**
     * 根据模板类型和业务类型删除指定的行政区域
     *
     * @param templateType  模板类型
     * @param bizType       业务类型
     * @param districtIds   行政区域集合
     * @return void
     **/
    void deleteByTemplateTypeAndBizType(@Param("templateType") Integer templateType, @Param("bizType") Integer bizType, @Param("districtIds") List<Integer> districtIds);

    List<TemplateDistrict> getByTemplateIds(@Param("templateIds") List<Integer> templateIds);

    /**
     * 根据行政区域ID获取档案卡模板ID（同一业务类型下，1个行政区域只有1个模板）
     *
     * @param districtId    行政区域
     * @param bizType       业务类型
     * @return java.lang.Integer
     **/
    Integer getArchivesIdByDistrictId(@Param("districtId") Integer districtId, @Param("bizType") Integer bizType);

}