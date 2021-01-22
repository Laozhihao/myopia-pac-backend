package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.Const;
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
        return templateService.getTemplateLists(Const.STUDENT_ARCHIVES);
    }

    @PutMapping("save")
    public Object save(@RequestBody TemplateBindRequest request) {
        return templateService.districtBind(request);
    }
}