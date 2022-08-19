package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.questionnaire.domain.dto.QuestionnaireQesDTO;
import com.wupol.myopia.business.api.questionnaire.domain.vo.QuestionnaireQesListVO;
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

import java.time.LocalDate;
import java.util.*;
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
     * 获取预览url
     * @param questionnaireQesList 问卷qes文件集合
     */
    private Map<Integer,String> getPreviewUrl(List<QuestionnaireQes> questionnaireQesList){
        if (CollUtil.isEmpty(questionnaireQesList)){
            return Maps.newHashMap();
        }
        Set<Integer> previewFileIds = questionnaireQesList.stream().map(QuestionnaireQes::getPreviewFileId).collect(Collectors.toSet());

        Map<Integer,String> previewFileMap = Maps.newHashMap();
        previewFileIds.forEach(previewFileId-> previewFileMap.put(previewFileId,resourceFileService.getResourcePath(previewFileId)));
        return previewFileMap;
    }

    /**
     * 根据年份查询问卷模板QES集合
     * @param year 年份
     */
    public QuestionnaireQesListVO list(Integer year) {
        QuestionnaireQesListVO questionnaireQesListVO = new QuestionnaireQesListVO();
        questionnaireQesListVO.setDataMap(Maps.newHashMap());

        List<QuestionnaireQes> questionnaireQesList = questionnaireQesService.list();
        if (CollectionUtils.isEmpty(questionnaireQesList)){
            return questionnaireQesListVO;
        }

        List<Integer> yearList = questionnaireQesList.stream().sorted(Comparator.comparing(QuestionnaireQes::getYear)).map(QuestionnaireQes::getYear).collect(Collectors.toList());
        questionnaireQesListVO.setYearList(yearList);

        //根据年份获取
        if (Objects.nonNull(year)){
            questionnaireQesList = questionnaireQesList.stream().filter(questionnaireQes -> Objects.equals(questionnaireQes.getYear(),year)).collect(Collectors.toList());
            questionnaireQesListVO.setDataMap(getDataMap(year,questionnaireQesList));
            return questionnaireQesListVO;
        }


        Map<Integer, List<QuestionnaireQes>> questionnaireQesMap = questionnaireQesList.stream().collect(Collectors.groupingBy(QuestionnaireQes::getYear));
        //没有年获取今年的
        List<QuestionnaireQes> todayYearList = questionnaireQesMap.get(getTodayYear());
        if (CollUtil.isNotEmpty(todayYearList)){
            questionnaireQesListVO.setDataMap(getDataMap(getTodayYear(),todayYearList));
            return questionnaireQesListVO;
        }

        //没有今年的，获取第一个
        questionnaireQesMap = CollUtil.sort(questionnaireQesMap, Comparator.comparing(Integer::intValue));
        Map<Integer, List<QuestionnaireQesVO>> dataMap = Maps.newHashMap();
        boolean flag = true;
        for (Map.Entry<Integer, List<QuestionnaireQes>> entry : questionnaireQesMap.entrySet()) {
            if (flag){
                dataMap = getDataMap(entry.getKey(),entry.getValue());
                flag=false;
            }
        }
        questionnaireQesListVO.setDataMap(dataMap);
        return questionnaireQesListVO;
    }

    private Map<Integer, List<QuestionnaireQesVO>> getDataMap(Integer year,List<QuestionnaireQes> questionnaireQesList) {
        Map<Integer, List<QuestionnaireQesVO>> dataMap = Maps.newHashMap();
        Map<Integer, String> previewUrlMap = getPreviewUrl(questionnaireQesList);
        if(CollUtil.isNotEmpty(questionnaireQesList)){
            dataMap.put(year,questionnaireQesList.stream().map(questionnaireQes -> buildQuestionnaireQesVO(questionnaireQes,previewUrlMap)).collect(Collectors.toList()));
        }
        return dataMap;
    }

    /**
     * 获取今年年份
     */
    private Integer getTodayYear(){
        return LocalDate.now().getYear();
    }


    /**
     * 构建问卷模板qes响应实体
     * @param questionnaireQes 问卷模板qes对象
     */
    private QuestionnaireQesVO buildQuestionnaireQesVO(QuestionnaireQes questionnaireQes,Map<Integer, String> previewUrlMap){
        QuestionnaireQesVO questionnaireQesVO = new QuestionnaireQesVO();
        questionnaireQesVO.setId(questionnaireQes.getId());
        questionnaireQesVO.setYear(questionnaireQes.getYear());
        questionnaireQesVO.setName(questionnaireQes.getName());
        questionnaireQesVO.setDescription(questionnaireQes.getDescription());
        questionnaireQesVO.setPreviewUrl(previewUrlMap.get(questionnaireQes.getPreviewFileId()));
        questionnaireQesVO.setIsExistQes(Objects.nonNull(questionnaireQes.getQesFileId()));
        return questionnaireQesVO;
    }
}
