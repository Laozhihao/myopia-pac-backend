package com.wupol.myopia.business.core.system.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.dto.TemplateBindItem;
import com.wupol.myopia.business.management.domain.mapper.TemplateDistrictMapper;
import com.wupol.myopia.business.management.domain.model.TemplateDistrict;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<TemplateBindItem> getByTemplateId(Integer templateId) {
        return baseMapper.getByTemplateId(templateId);
    }

    /**
     * 通过templateId和districtIds删除
     *
     * @param templateId  模版ID
     * @param districtIds 区域ID List
     */
    public void deletedByTemplateIdAndDistrictIds(Integer templateId, List<Integer> districtIds) {
        baseMapper.deletedByTemplateIdAndDistrictIds(templateId, districtIds);
    }

    /**
     * 批量插入
     *
     * @param templateId 模板ID
     * @param items      详情
     */
    public void batchInsert(Integer templateId, List<TemplateBindItem> items) {
        baseMapper.batchInsert(templateId, items);
    }
}
