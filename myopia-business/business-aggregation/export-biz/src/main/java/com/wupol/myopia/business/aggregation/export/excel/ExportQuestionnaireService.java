package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireExcelFacade;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 导出问卷数据
 *
 * @author hang.yuan 2022/7/20 11:18
 */
@Slf4j
@Service(ExportExcelServiceNameConstant.QUESTIONNAIRE_SERVICE)
public class ExportQuestionnaireService extends BaseExportExcelFileService {

    private static final String ERROR_MSG ="不存在此导出类型:%s";

    @Autowired
    private QuestionnaireExcelFacade questionnaireExcelFacade;

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
//            generateExcelFile(fileSavePath,null,exportCondition);
            // 5.压缩文件
            File file = compressFile(fileSavePath);
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
            deleteTempFile(excelSavePath+parentPath);
            // 8.释放锁
            unlock(getLockKey(exportCondition));
        }


    }

    @Override
    public File generateExcelFile(String fileName, List data, ExportCondition exportCondition) throws IOException {

        List<Integer> questionnaireTypeList = exportCondition.getQuestionnaireType();
        if (CollectionUtil.isNotEmpty(questionnaireTypeList)){
            for (Integer questionnaireType : questionnaireTypeList) {
                if (Objects.equals(QuestionnaireConstant.STUDENT_TYPE,questionnaireType)){
                    for (Integer type : QuestionnaireConstant.STUDENT_TYPE_LIST) {
                        generateExcelFile(fileName, exportCondition, type);
                    }
                }else {
                    generateExcelFile(fileName, exportCondition, questionnaireType);
                }
            }
        }
        return null;
    }

    private void generateExcelFile(String fileName, ExportCondition exportCondition, Integer questionnaireType) throws IOException {
        Optional<QuestionnaireExcel> questionnaireExcelService = questionnaireExcelFacade.getQuestionnaireExcelService(questionnaireType);
        if (questionnaireExcelService.isPresent()) {
            QuestionnaireExcel questionnaireExcel = questionnaireExcelService.get();
            questionnaireExcel.generateExcelFile(exportCondition,fileName);
        }
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFacade.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getNoticeKeyContent(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFacade.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getFileName(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFacade.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getLockKey(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
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
        // do something validate parameter
    }

    @Override
    public String getFileSavePath(String parentPath, String fileName) {
        String fileSavePath = super.getFileSavePath(parentPath, fileName);
        if(!FileUtil.exist(fileSavePath)){
            FileUtil.mkdir(fileSavePath);
        }
        return fileSavePath;
    }
}
