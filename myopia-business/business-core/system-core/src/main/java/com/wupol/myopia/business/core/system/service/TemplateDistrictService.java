package com.wupol.myopia.business.core.system.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindItemDTO;
import com.wupol.myopia.business.core.system.domain.mapper.TemplateDistrictMapper;
import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * 批量绑定行政区域
     *
     * @param templateId    模板ID
     * @param districtList  行政区域集
     */
    public void bindDistrictBatch(Integer templateId, List<TemplateBindItemDTO> districtList) {
        if (CollectionUtils.isEmpty(districtList)) {
            return;
        }
        List<TemplateDistrict> templateDistrictList = districtList.stream().map(x -> new TemplateDistrict().setTemplateId(templateId).setDistrictId(x.getDistrictId()).setDistrictName(x.getDistrictName())).collect(Collectors.toList());
        saveBatch(templateDistrictList);
    }

    /**
     * 解除指定的档案卡所绑定的区域
     *
     * @param districtIds   行政区域集
     * @param bizType       业务类型
     * @return void
     */
    public void removeArchivesBindingDistrictBatch(List<Integer> districtIds, Integer bizType) {
        if (CollectionUtils.isEmpty(districtIds)) {
            return;
        }
        baseMapper.deleteByTemplateTypeAndBizType(TemplateConstants.TYPE_TEMPLATE_STUDENT_ARCHIVES, bizType, districtIds);
    }

    /**
     * 根据行政区域ID获取档案卡模板ID（同一业务类型下，1个行政区域只有1个模板）
     *
     * @param districtId 行政区域
     * @param bizType    业务类型
     * @return 模版Id
     */
    public Integer getArchivesByDistrictId(Integer districtId, Integer bizType) {
        Integer templateId = baseMapper.getArchivesIdByDistrictId(districtId, bizType);
        return Objects.isNull(templateId) ? TemplateConstants.GLOBAL_TEMPLATE : templateId;
    }

}
