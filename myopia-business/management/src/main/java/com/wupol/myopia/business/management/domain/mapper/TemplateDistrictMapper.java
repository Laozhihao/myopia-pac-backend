package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.dto.TemplateBindItem;
import com.wupol.myopia.business.management.domain.model.TemplateDistrict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板区域Mapper
 *
 * @author Simple4H
 */
public interface TemplateDistrictMapper extends BaseMapper<TemplateDistrict> {

    List<TemplateBindItem> getByTemplateId(@Param("templateId") Integer templateId);

    void deletedByTemplateIdAndDistrictIds(@Param("templateId") Integer templateId, @Param("districtIds") List<Integer> districtIds);

    void batchInsert(@Param("templateId") Integer templateId, @Param("items") List<TemplateBindItem> items);
}