package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.domain.dos.TemplateDO;
import com.wupol.myopia.business.core.system.domain.dto.TemplateBindRequestDTO;
import com.wupol.myopia.business.core.system.service.TemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 筛查报告模板设置
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningReport")
public class ScreeningReportController {

    @Resource
    private TemplateService templateService;

    /**
     * 获取模板列表
     *
     * @return 模板列表
     */
    @GetMapping("lists")
    public Map<Integer, List<TemplateDO>> getLists() {
        return templateService.getTemplateLists(TemplateConstants.TYPE_TEMPLATE_SCREENING_REPORT);
    }

    /**
     * 绑定区域模板
     *
     * @param request 请求入参
     * @return 是否成功
     */
    @PutMapping("save")
    public void save(@RequestBody TemplateBindRequestDTO request) {
        templateService.bindDistrictToTemplate(request);
    }
}
