package com.wupol.myopia.business.core.system.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindItemDTO;
import com.wupol.myopia.business.core.system.domain.mapper.TemplateDistrictMapper;
import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 模板区域Service
 *
 * @author Simple4H
 */
@Service
public class TemplateDistrictService extends BaseService<TemplateDistrictMapper, TemplateDistrict> {

    /**
     * 批量通过templateId查询区域
     *
     * @param templateIds 模板ID
     * @return List<TemplateDistrict>
     */
    public List<TemplateDistrict> getByTemplateIds(List<Integer> templateIds) {
        return baseMapper.getByTemplateIds(templateIds);
    }

    /**
     * 通过templateID获取区域列表
     *
     * @param templateId 模板ID
     * @return List<TemplateDistrict>
     */
    public List<TemplateBindItemDTO> getByTemplateId(Integer templateId) {
        return baseMapper.getByTemplateId(templateId);
    }

    /**
     * 通过districtIds删除
     *
     * @param districtIds 区域ID List
     */
    public void deletedByDistrictIds(List<Integer> districtIds) {
        baseMapper.deletedByDistrictIds(districtIds);
    }

    /**
     * 批量插入
     *
     * @param templateId 模板ID
     * @param items      详情
     */
    public void batchInsert(Integer templateId, List<TemplateBindItemDTO> items) {
        baseMapper.batchInsert(templateId, items);
    }

    /**
     * 批量删除
     * @param templateId 模板ID集合
     * @param districtIds 区域ID集合
     */
    public void batchDelete(Integer templateId, List<Integer> districtIds) {
        baseMapper.batchDelete(templateId,districtIds);
    }


    /**
     * 批量删除
     * @param templateIds 模板ID集合
     * @param districtIds 区域ID集合
     */
    public void batchDeleteTemplateIdsAndDistrictIds(List<Integer> templateIds, List<Integer> districtIds) {
        baseMapper.batchDeleteTemplateIdsAndDistrictIds(templateIds,districtIds);
    }


    /**
     * 通过行政区域获取模版Id
     *
     * @param districtId 行政区域
     * @return 模版Id
     */
    public Integer getArchivesByDistrictId(Integer districtId) {
        Integer templateId = baseMapper.getArchivesByDistrictId(districtId);
        if (Objects.isNull(templateId)) {
            return TemplateConstants.GLOBAL_TEMPLATE;
        }
        return templateId;
    }
}
