package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.TemplateBindRequest;
import com.wupol.myopia.business.management.domain.dto.TemplateResponse;
import com.wupol.myopia.business.management.service.TemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 获取模板列表
     *
     * @return 模板列表
     */
    @GetMapping("lists")
    public List<TemplateResponse> getLists() {
        return templateService.getTemplateLists(CommonConst.TYPE_TEMPLATE_STUDENT_ARCHIVES);
    }

    /**
     * 保存模板
     *
     * @param request 请求入参
     * @return 是否成功
     */
    @PutMapping("save")
    public Boolean save(@RequestBody TemplateBindRequest request) {
        return templateService.districtBind(request, CommonConst.TYPE_TEMPLATE_STUDENT_ARCHIVES);
    }
}