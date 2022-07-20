package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireExcelFacade;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 导出问卷数据
 *
 * @author hang.yuan 2022/7/20 11:18
 */
@Slf4j
@Service(ExportExcelServiceNameConstant.QUESTIONNAIRE_SERVICE)
public class ExportQuestionnaireService extends BaseExportExcelFileService {

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
            generateExcelFile(null,null,exportCondition);
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
                Optional<QuestionnaireExcel> questionnaireExcelService = questionnaireExcelFacade.getQuestionnaireExcelService(questionnaireType);
                if (questionnaireExcelService.isPresent()) {
                    QuestionnaireExcel questionnaireExcel = questionnaireExcelService.get();
                    questionnaireExcel.generateExcelFile(exportCondition);
                }
            }
        }
        return null;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFacade.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getNoticeKeyContent(exportCondition);
        }
        throw new BusinessException(String.format("不存在此导出类型:%s",exportCondition.getExportType()));
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFacade.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getFileName(exportCondition);
        }
        throw new BusinessException(String.format("不存在此导出类型:%s",exportCondition.getExportType()));
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFacade.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getLockKey(exportCondition);
        }
        throw new BusinessException(String.format("不存在此导出类型:%s",exportCondition.getExportType()));
    }


    @Override
    public List getExcelData(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        return null;
    }

}
