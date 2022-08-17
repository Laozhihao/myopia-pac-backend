package com.wupol.myopia.business.api.questionnaire.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.questionnaire.domain.dto.QuestionnaireQesDTO;
import com.wupol.myopia.business.api.questionnaire.domain.vo.QuestionnaireQesVO;
import com.wupol.myopia.business.api.questionnaire.service.QuestionnaireQesFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 问卷QES文件管理
 *
 * @author hang.yuan
 * @date 2022/8/4
 */
@ResponseResultBody
@RestController
@RequestMapping("/questionnaire/qes")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class QuestionnaireQesController {

    private final QuestionnaireQesFacade questionnaireQesFacade;

    /**
     * 创建问卷模板
     *
     * @param questionnaireQesDTO 创建问卷模板入参对象
     */
    @PostMapping("/save")
    public void save(@RequestBody @Valid QuestionnaireQesDTO questionnaireQesDTO){
        questionnaireQesFacade.save(questionnaireQesDTO);
    }

    /**
     * 上传/更新 QES问卷
     * @param file qes文件流
     * @param qesId qes问卷管理ID
     */
    @PostMapping("/upload/{qesId}")
    public void uploadQes(MultipartFile file,@PathVariable Integer qesId){
        questionnaireQesFacade.uploadQes(file,qesId);
    }

    /**
     * 预览qes文件
     * @param qesId qes问卷管理ID
     */
    @GetMapping("/preview/{qesId}")
    public ApiResult<String> preview(@PathVariable Integer qesId){
        return ApiResult.success(questionnaireQesFacade.preview(qesId));
    }

    /**
     * 根据年份获取问卷模板qes列表
     * @param year 年份
     */
    @GetMapping("/list")
    public Map<Integer,List<QuestionnaireQesVO>> list(@RequestParam(required = false) Integer year){
        return questionnaireQesFacade.list(year);
    }
}
