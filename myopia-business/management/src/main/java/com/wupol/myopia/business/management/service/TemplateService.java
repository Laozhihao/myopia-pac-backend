package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.dto.TemplateBindItem;
import com.wupol.myopia.business.management.domain.dto.TemplateBindRequest;
import com.wupol.myopia.business.management.domain.dto.TemplateResponse;
import com.wupol.myopia.business.management.domain.mapper.TemplateMapper;
import com.wupol.myopia.business.management.domain.model.Template;
import com.wupol.myopia.business.management.domain.model.TemplateDistrict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public List<TemplateResponse> getTemplateLists(Integer type) {

        List<TemplateResponse> responses = new ArrayList<>();

        // 根据类型查模板
        List<Template> templateList = baseMapper.selectList(new QueryWrapper<Template>().eq("type", type));

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
            TemplateResponse response = new TemplateResponse();
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
     * 绑定区域
     *
     * @param request 入参
     * @return boolean 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean districtBind(TemplateBindRequest request) {

        Integer templateId = request.getTemplateId();
        List<TemplateBindItem> newDistrictLists = request.getDistrictInfo();

        // 先获取原有的
        List<TemplateBindItem> originLists = templateDistrictService.getByTemplateId(templateId);

        // 看看和原来的比，多了什么，就是新增
        List<TemplateBindItem> addLists = newDistrictLists.stream().filter(item -> !originLists.stream().map(TemplateBindItem::getDistrictId).collect(
                Collectors.toList()).contains(item.getDistrictId())).collect(Collectors.toList());

        // 同理，取删除的
        List<TemplateBindItem> deletedLists = originLists.stream().filter(item -> !newDistrictLists.stream().map(TemplateBindItem::getDistrictId).collect(
                Collectors.toList()).contains(item.getDistrictId())).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(addLists)) {
            templateDistrictService.batchInsert(templateId, addLists);
        }

        if (!CollectionUtils.isEmpty(deletedLists)) {
            templateDistrictService
                    .deletedByTemplateIdAndDistrictIds(templateId,
                            deletedLists.stream().map(TemplateBindItem::getDistrictId).collect(Collectors.toList()));
        }
        return Boolean.TRUE;
    }
}