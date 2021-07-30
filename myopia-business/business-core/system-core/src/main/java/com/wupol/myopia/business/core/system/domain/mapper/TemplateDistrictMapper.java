package com.wupol.myopia.business.core.system.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindItemDTO;
import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板区域Mapper
 *
 * @author Simple4H
 */
public interface TemplateDistrictMapper extends BaseMapper<TemplateDistrict> {

    List<TemplateBindItemDTO> getByTemplateId(@Param("templateId") Integer templateId);

    void deletedByDistrictIds(@Param("districtIds") List<Integer> districtIds);

    void batchInsert(@Param("templateId") Integer templateId, @Param("items") List<TemplateBindItemDTO> items);

    List<TemplateDistrict> getByTemplateIds(@Param("templateIds") List<Integer> templateIds);

    Integer getByDistrictId(@Param("districtId") Integer districtId);
}