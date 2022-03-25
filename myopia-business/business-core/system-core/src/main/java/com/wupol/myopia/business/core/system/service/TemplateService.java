package com.wupol.myopia.business.core.system.service;

import com.google.common.collect.Maps;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindItemDTO;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindRequestDTO;
import com.wupol.myopia.business.core.system.domain.dto.TemplateResponseDTO;
import com.wupol.myopia.business.core.system.domain.mapper.TemplateMapper;
import com.wupol.myopia.business.core.system.domain.model.Template;
import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
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
     * @return List<TemplateResponse>
     */
    public List<TemplateResponseDTO> getTemplateLists(Integer type) {

        List<TemplateResponseDTO> responses = new ArrayList<>();

        // 根据类型查模板
        List<Template> templateList = baseMapper.getByType(type);

        if (CollectionUtils.isEmpty(templateList)) {
            return responses;
        }

        // 查询使用的省
        Map<Integer, List<TemplateDistrict>> districtMaps = Maps.newHashMap();

        List<TemplateDistrict> templateDistricts = templateDistrictService.getByTemplateIds(templateList.stream().map(Template::getId).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(templateDistricts)) {
            districtMaps = templateDistricts.stream().collect(Collectors.groupingBy(TemplateDistrict::getTemplateId));
        }

        // 封装DTO
        for (Template t : templateList) {
            TemplateResponseDTO response = new TemplateResponseDTO();
            response.setId(t.getId());
            response.setName(t.getName());
            if (null != districtMaps.get(t.getId())) {
                response.setDistrictInfo(districtMaps.get(t.getId()));
            }
            responses.add(response);
        }
        return responses;
    }

    /**
     * 绑定区域（档案卡绑定区域）
     *
     * @param request 入参
     * @return boolean 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean districtBind(TemplateBindRequestDTO request) {

        Integer templateId = request.getTemplateId();
        List<TemplateBindItemDTO> bindItemDTOS = request.getDistrictInfo();

        if (bindItemDTOS==null){
            templateDistrictService.remove(new TemplateDistrict().setTemplateId(templateId));
        }

        List<Integer> districtIds = bindItemDTOS.stream().map(TemplateBindItemDTO::getDistrictId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(districtIds)) {
            // 批量删除
            templateDistrictService.batchDelete(templateId,districtIds);
            // 批量插入
            templateDistrictService.batchInsert(templateId, bindItemDTOS);
        }
        return true;
    }


    /**
     * 检查是否一个省是否绑定一个模板
     * <p>Collections.disjoint() 如果有相同元素则返回false</p>
     *
     * @param type 类型
     * @param list 新增列表
     * @return 是否重复绑定
     */
    public boolean check(Integer type, List<TemplateBindItemDTO> list) {
        // 根据类型查模板
        List<Template> templateList = baseMapper.getByType(type);

        // 根据模板ID获取所有的行政ID
        List<TemplateDistrict> allDistrict = templateDistrictService.getByTemplateIds(
                templateList.stream().map(Template::getId).collect(Collectors.toList()));

        // 判断两个list是否有相同元素
        return !Collections.disjoint(list.stream().map(TemplateBindItemDTO::getDistrictId).collect(Collectors.toList()),
                allDistrict.stream().map(TemplateDistrict::getDistrictId).collect(Collectors.toList()));
    }
}