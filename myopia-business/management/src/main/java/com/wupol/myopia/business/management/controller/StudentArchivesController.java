package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.TemplateBindRequest;
import com.wupol.myopia.business.management.service.TemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 档案卡模版设置
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/studentArchives")
public class StudentArchivesController {

    @Resource
    private TemplateService templateService;

    @GetMapping("lists")
    public Object getLists() {
        return templateService.getTemplateLists(CommonConst.TYPE_TEMPLATE_STUDENT_ARCHIVES);
    }

    @PutMapping("save")
    public Object save(@RequestBody TemplateBindRequest request) {
        return templateService.districtBind(request, CommonConst.TYPE_TEMPLATE_STUDENT_ARCHIVES);
    }
}