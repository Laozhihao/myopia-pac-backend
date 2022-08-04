package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireExcelFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 导出问卷数据
 *
 * @author hang.yuan 2022/7/20 11:18
 */
@Slf4j
@Service(ExportExcelServiceNameConstant.QUESTIONNAIRE_SERVICE)
public class ExportQuestionnaireService extends BaseExportExcelFileService {

    public static final String ERROR_MSG ="不存在此导出类型:%s";

    private static List<Integer> schoolQuestionnaireType = Lists.newArrayList(QuestionnaireConstant.STUDENT_TYPE, QuestionnaireTypeEnum.VISION_SPINE.getType());

    @Autowired
    private QuestionnaireExcelFactory questionnaireExcelFactory;
    @Autowired
    private UserQuestionRecordService userQuestionRecordService;

    @Override
    public void export(ExportCondition exportCondition) {

        String noticeKeyContent = null;
        String parentPath = null;
        try {
            preProcess(exportCondition);
            // 1.获取文件名(如果导出的是压缩包，这里文件名不带后缀，将作为压缩包的文件名)
            String fileName = getFileName(exportCondition);
            // 2.获取文件保存父目录路径
            parentPath = getFileSaveParentPath();
            // 3.获取文件保存路径
            String fileSavePath = getFileSavePath(parentPath, fileName);
            // 4.生成excel
            generateExcelFile(fileSavePath,null,exportCondition);
            // 5.压缩文件
            File file = compressFile(fileSavePath);
            // 没有文件直接返回
            if (Objects.isNull(file)){return;}
            // 6.上传文件
            Integer fileId = uploadFile(file);
            // 7.获取通知的关键内容
            noticeKeyContent = getNoticeKeyContent(exportCondition);
            // 8.发送成功通知
            sendSuccessNotice(exportCondition.getApplyExportFileUserId(), noticeKeyContent, fileId);
        } catch (Exception e) {
            String requestData = JSON.toJSONString(exportCondition);
            log.error("【导出Excel异常】{}", requestData, e);
            // 发送失败通知
            if (!StringUtils.isEmpty(noticeKeyContent)) {
                sendFailNotice(exportCondition.getApplyExportFileUserId(), noticeKeyContent);
            }
        } finally {
            // 7.删除临时文件
            deleteTempFile(parentPath);
            // 8.释放锁
            unlock(getLockKey(exportCondition));
        }


    }

    @Override
    public File generateExcelFile(String fileName, List data, ExportCondition exportCondition) throws IOException {

        List<Integer> questionnaireTypeList = exportCondition.getQuestionnaireType();
        if (CollectionUtil.isEmpty(questionnaireTypeList)){
            return null;
        }
        for (Integer questionnaireType : questionnaireTypeList) {
            if (Objects.equals(QuestionnaireConstant.STUDENT_TYPE,questionnaireType)){
                String filePath = getFileName(QuestionnaireConstant.STUDENT_TYPE, exportCondition.getExportType(), exportCondition.getDistrictId(), fileName);
                for (Integer type : QuestionnaireConstant.STUDENT_TYPE_LIST) {
                    generateExcelFile(filePath, exportCondition, type);
                }
            }else {
                generateExcelFile(getFileName(questionnaireType, exportCondition.getExportType(), exportCondition.getDistrictId(), fileName), exportCondition, questionnaireType);
            }
        }

        return null;
    }

    /**
     * 根据地区导出数据时，获取地区的各个学校问卷数据文件夹名称
     *
     * @param questionnaireType 问卷类型
     * @param exportType 导出类型
     * @param districtId 地区ID
     * @param fileName 文件路径
     */
    private String getFileName(Integer questionnaireType,Integer exportType,Integer districtId,String fileName){
        if (schoolQuestionnaireType.contains(questionnaireType) && Objects.nonNull(districtId)){
            Optional<ExportType> exportTypeOptional = questionnaireExcelFactory.getExportTypeService(exportType);
            if (exportTypeOptional.isPresent()) {
                ExportType exportTypeService = exportTypeOptional.get();
                String districtKey = exportTypeService.getDistrictKey(districtId);
                return getFileSavePath(fileName,districtKey);
            }
            throw new BusinessException(String.format(ExportQuestionnaireService.ERROR_MSG,exportType));
        }
        return fileName;
    }

    private void generateExcelFile(String fileName, ExportCondition exportCondition, Integer questionnaireType) throws IOException {
        Optional<QuestionnaireExcel> questionnaireExcelService = questionnaireExcelFactory.getQuestionnaireExcelService(questionnaireType);
        if (questionnaireExcelService.isPresent()) {
            QuestionnaireExcel questionnaireExcel = questionnaireExcelService.get();
            questionnaireExcel.generateExcelFile(exportCondition,fileName);
        }
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getNoticeKeyContent(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getFileName(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getLockKey(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
    }

    @Override
    public void preProcess(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            exportType.preProcess(exportCondition);
        }
    }


    @Override
    public List getExcelData(ExportCondition exportCondition) {
        return Lists.newArrayList();
    }

    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        this.preProcess(exportCondition);

        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByNoticeIdOrTaskIdOrPlanId(exportCondition.getNotificationId(),exportCondition.getTaskId(),exportCondition.getPlanId(), QuestionnaireStatusEnum.FINISH.getCode());
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            throw new BusinessException("暂无数据");
        }

        Stream<UserQuestionRecord> userQuestionRecordStream = userQuestionRecordList.stream();

        List<Integer> questionnaireType = exportCondition.getQuestionnaireType();
        if (!CollectionUtils.isEmpty(questionnaireType)){
            userQuestionRecordStream = userQuestionRecordStream.filter(userQuestionRecord -> {
                boolean contains = questionnaireType.contains(userQuestionRecord.getQuestionnaireType());
                boolean studentType = questionnaireType.contains(QuestionnaireConstant.STUDENT_TYPE)
                        && QuestionnaireConstant.STUDENT_TYPE_LIST.contains(userQuestionRecord.getQuestionnaireType());
                return contains || studentType;
            });
        }

        if (Objects.nonNull(exportCondition.getSchoolId())){
            userQuestionRecordStream = userQuestionRecordStream.filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getSchoolId(),exportCondition.getSchoolId()));
        }

        userQuestionRecordList = userQuestionRecordStream.collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            throw new BusinessException("暂无数据");
        }

    }

    @Override
    public String getFileSavePath(String parentPath, String fileName) {
        String fileSavePath = super.getFileSavePath(parentPath, fileName);
        if(!FileUtil.exist(fileSavePath)){
            FileUtil.mkdir(fileSavePath);
        }
        return fileSavePath;
    }

    @Override
    public File compressFile(String fileSavePath) {
        File srcFile = FileUtil.file(fileSavePath);
        //压缩时查看父文件或父文件夹以下是否有文件，没文件时直接返回
        if (Objects.equals(Boolean.FALSE, FileUtils.includeFiles(srcFile))) {
            return null;
        }
        final File zipFile = FileUtil.file(FileUtil.file(srcFile).getParentFile(), FileUtil.mainName(srcFile) + ".zip");
        // 将本目录也压缩
        return ZipUtil.zip(zipFile, CharsetUtil.defaultCharset(), true, srcFile);
    }

}
