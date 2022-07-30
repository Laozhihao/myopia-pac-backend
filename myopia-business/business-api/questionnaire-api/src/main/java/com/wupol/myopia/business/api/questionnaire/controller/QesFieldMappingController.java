package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.questionnaire.service.QesFieldMappingFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     * 保存qes文件
     *
     * @param file qes文件流
     */
    @PostMapping("/saveQes")
    public ApiResult<Object> saveQes(MultipartFile file) {
        qesFieldMappingFacade.saveQes(file);
        return ApiResult.success();
    }
}
