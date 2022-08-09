package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.questionnaire.service.QesFieldMappingFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Simple4H
 */
@ResponseResultBody
@RestController
@RequestMapping("/qesFieldMapping")
public class QesFieldMappingController  {

    @Autowired
    private QesFieldMappingFacade qesFieldMappingFacade;

    /**
     * 保存qes文件字段映射
     *
     * @param qesId qes文件流
     */
    @GetMapping("/save")
    public ApiResult<Object> save(@RequestParam Integer qesId) {
        qesFieldMappingFacade.saveQesFieldMapping(qesId);
        return ApiResult.success();
    }
}
