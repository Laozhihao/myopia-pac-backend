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

    void batchDelete(@Param("templateId") Integer templateIds, @Param("items") List<Integer> items);

    void batchDeleteTemplateIdsAndDistrictIds(@Param("templateIds") List<Integer> templateIds, @Param("districtIds") List<Integer> districtIds);

    List<TemplateDistrict> getByTemplateIds(@Param("templateIds") List<Integer> templateIds);

    Integer getArchivesByDistrictId(@Param("districtId") Integer districtId);

    Integer getByDistrictIdAndTemplateIds(@Param("districtId") Integer districtId,@Param("templateIds") List<Integer> templateIds);
}