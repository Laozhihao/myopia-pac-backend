package com.wupol.myopia.business.core.system.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.domain.dos.TemplateDO;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindItemDTO;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindRequestDTO;
import com.wupol.myopia.business.core.system.domain.mapper.TemplateMapper;
import com.wupol.myopia.business.core.system.domain.model.Template;
import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板Service
 *
 * @author Simple4H
 */
@Service
public class TemplateService extends BaseService<TemplateMapper, Template> {

    @Resource
    private TemplateDistrictService templateDistrictService;

    /**
     * 获取模板列表
     *
     * @param type 类型
     * @return java.util.Map<java.lang.Integer,java.util.List<com.wupol.myopia.business.core.system.domain.dos.TemplateDO>>
     **/
    public Map<Integer, List<TemplateDO>> getTemplateLists(Integer type) {
        // 根据类型查模板
        List<Template> templateList = findByList(new Template().setType(type));
        if (CollectionUtils.isEmpty(templateList)) {
            return Collections.emptyMap();
        }
        // 查询使用该模板的行政区域
        List<TemplateDistrict> templateDistrictList = templateDistrictService.getByTemplateIds(templateList.stream().map(Template::getId).collect(Collectors.toList()));
        Map<Integer, List<TemplateDistrict>> districtMaps = templateDistrictList.stream().collect(Collectors.groupingBy(TemplateDistrict::getTemplateId));

        return templateList.stream()
                .map(t -> TemplateDO.parseFromTemplate(t).setDistrictInfo(districtMaps.getOrDefault(t.getId(), null)))
                .collect(Collectors.groupingBy(TemplateDO::getBiz));
    }

    /**
     * 绑定区域的模板
     *
     * @param request 入参
     * @return void
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindDistrictToTemplate(TemplateBindRequestDTO request) {
        Integer templateId = request.getTemplateId();
        List<TemplateBindItemDTO> bindingDistrictList = request.getDistrictInfo();
        Assert.notNull(templateId, "模板ID不能为空");
        // 清空该模板下所有绑定
        templateDistrictService.remove(new TemplateDistrict().setTemplateId(templateId));
        if (CollectionUtils.isEmpty(bindingDistrictList)) {
            return;
        }
        // 为档案卡模板时，解除与其他模板的绑定关系（2022-04-24 目前同筛查业务类型下，1个行政区域仅允许绑定1个模板）
        Template template = getById(templateId);
        if (TemplateConstants.TYPE_TEMPLATE_STUDENT_ARCHIVES.equals(template.getType())) {
            List<Integer> districtIds = bindingDistrictList.stream().map(TemplateBindItemDTO::getDistrictId).collect(Collectors.toList());
            templateDistrictService.removeArchivesBindingDistrictBatch(districtIds, template.getBiz());
        }
        // 批量绑定到当前模板下
        templateDistrictService.bindDistrictBatch(templateId, bindingDistrictList);
    }
}