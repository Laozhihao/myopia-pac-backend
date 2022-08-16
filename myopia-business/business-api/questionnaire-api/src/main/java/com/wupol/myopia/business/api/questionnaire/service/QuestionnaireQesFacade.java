package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.domain.dto.QuestionnaireQesDTO;
import com.wupol.myopia.business.api.questionnaire.domain.vo.QuestionnaireQesVO;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQes;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQesService;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 问卷QES文件管理门面
 *
 * @author hang.yuan 2022/8/4 23:54
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class QuestionnaireQesFacade {

    private final QuestionnaireQesService questionnaireQesService;
    private final ResourceFileService resourceFileService;
    private final S3Utils s3Utils;
    private final QesFieldMappingFacade qesFieldMappingFacade;

    /**
     * 创建问卷模板
     *
     * @param questionnaireQesDTO 创建问卷模板入参对象
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(QuestionnaireQesDTO questionnaireQesDTO) {
        if (Objects.isNull(questionnaireQesDTO.getDistrictId())){
            questionnaireQesDTO.setDistrictId(-1);
        }
        if (Objects.nonNull(questionnaireQesDTO.getId())) {
            QuestionnaireQes questionnaireQes = questionnaireQesService.getById(questionnaireQesDTO.getId());
            BeanUtil.copyProperties(questionnaireQesDTO,questionnaireQes);
            questionnaireQesService.updateById(questionnaireQes);
        }else {
            QuestionnaireQes questionnaireQes = new QuestionnaireQes();
            BeanUtil.copyProperties(questionnaireQesDTO,questionnaireQes);
            questionnaireQesService.save(questionnaireQes);
        }
    }

    /**
     * 上传/更新 QES问卷
     * @param file qes文件流
     * @param qesId qes问卷管理ID
     */
    public void uploadQes(MultipartFile file, Integer qesId) {
        String qesPath = resourceFileService.checkFileAndSaveToLocal(file, QuestionnaireConstant.QES);
        QuestionnaireQes questionnaireQes = questionnaireQesService.getById(qesId);
        //解析qes文件
        String parseSavePath = resourceFileService.parseSavePath(qesPath, QuestionnaireConstant.TXT);
        EpiDataUtil.qesToTxt(qesPath,parseSavePath);
        //上传
        uploadQesFile(qesPath, questionnaireQes,Boolean.TRUE);
        uploadQesFile(parseSavePath, questionnaireQes,Boolean.FALSE);
        questionnaireQesService.updateById(questionnaireQes);

        //保存qes字段映射
        qesFieldMappingFacade.saveQesFieldMapping(questionnaireQes);
    }


    /**
     * 上传qes文件
     * @param qesPath qes文件路径
     * @param questionnaireQes QES问卷模板对象
     * @param msg 消息
     */
    private void uploadQesFile(String qesPath, QuestionnaireQes questionnaireQes,Boolean msg) {
        //上传QES文件
        ResourceFile qesResourceFile;
        try {
            qesResourceFile = s3Utils.uploadS3AndGetResourceFile(FileUtil.newFile(qesPath));
        } catch (UtilException e) {
            String errorMsg;
            if (Objects.equals(Boolean.TRUE,msg)){
                errorMsg = String.format("QES文件%s失败", Objects.nonNull(questionnaireQes.getQesFileId()) ? "更新" : "上传");
            }else {
                errorMsg = String.format("QES解析预览文件%s失败", Objects.nonNull(questionnaireQes.getQesFileId()) ? "更新" : "上传");
            }
            throw new BusinessException(errorMsg);
        }
        if(Objects.equals(Boolean.TRUE,msg)){
            questionnaireQes.setQesFileId(qesResourceFile.getId());
        }else {
            questionnaireQes.setPreviewFileId(qesResourceFile.getId());
        }
    }


    /**
     * 预览qes文件
     * @param qesId qes问卷管理ID
     */
    public String preview(Integer qesId) {
        QuestionnaireQes questionnaireQes = questionnaireQesService.getById(qesId);
        if (Objects.nonNull(questionnaireQes.getPreviewFileId())){
            return resourceFileService.getResourcePath(questionnaireQes.getPreviewFileId());
        }
        return StrUtil.EMPTY;
    }

    /**
     * 根据年份查询问卷模板QES集合
     * @param year 年份
     */
    public List<QuestionnaireQesVO> list(Integer year) {
        List<QuestionnaireQes> questionnaireQesList = questionnaireQesService.listByYear(year);
        if (CollectionUtils.isEmpty(questionnaireQesList)){
            return Lists.newArrayList();
        }
        return questionnaireQesList.stream().map(this::buildQuestionnaireQesVO).collect(Collectors.toList());
    }

    /**
     * 构建问卷模板qes响应实体
     * @param questionnaireQes 问卷模板qes对象
     */
    private QuestionnaireQesVO buildQuestionnaireQesVO(QuestionnaireQes questionnaireQes){
        QuestionnaireQesVO questionnaireQesVO = new QuestionnaireQesVO();
        questionnaireQesVO.setId(questionnaireQes.getId());
        questionnaireQesVO.setYear(questionnaireQes.getYear());
        questionnaireQesVO.setName(questionnaireQes.getName());
        questionnaireQesVO.setDescription(questionnaireQes.getDescription());
        questionnaireQesVO.setIsPreview(Objects.nonNull(questionnaireQes.getPreviewFileId()));
        questionnaireQesVO.setIsExistQes(Objects.nonNull(questionnaireQes.getQesFileId()));
        return questionnaireQesVO;
    }
}
