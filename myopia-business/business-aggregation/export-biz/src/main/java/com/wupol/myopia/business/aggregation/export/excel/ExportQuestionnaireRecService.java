package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ZipUtil;
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
@Service(ExportExcelServiceNameConstant.QUESTIONNAIRE_REC_SERVICE)
public class ExportQuestionnaireRecService extends BaseExportExcelFileService {

    public static final String ERROR_MSG ="不存在此导出类型:%s";

    private static List<Integer> schoolQuestionnaireType = Lists.newArrayList(QuestionnaireConstant.STUDENT_TYPE, QuestionnaireTypeEnum.VISION_SPINE.getType());

    @Autowired
    private QuestionnaireExcelFactory questionnaireExcelFactory;
    @Autowired
    private UserQuestionRecordService userQuestionRecordService;


    /**
     * 预处理
     * @param exportCondition 导出条件
     */
    @Override
    public void preProcess(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            exportType.preProcess(exportCondition);
        }
    }

    /**
     * 1、获取文件名
     * @param exportCondition 导出条件
     */
    @Override
    public String getFileName(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getFileName(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
    }

    /**
     * 4、获取数据，生成List（暂时空实现）
     * @param exportCondition 导出条件
     */
    @Override
    public List getExcelData(ExportCondition exportCondition) {
        return Lists.newArrayList();
    }

    /**
     * 5.数据处理
     * @param isPackage 是否打包
     * @param exportCondition 导出条件
     * @param fileSavePath 文件保存路径（含基础路径）
     * @param fileName 文件名
     * @param data 数据集合
     */
    @Override
    public File fileDispose(Boolean isPackage, ExportCondition exportCondition, String fileSavePath, String fileName, List data) throws IOException {
        if (Objects.equals(Boolean.TRUE,isPackage)){
            // 生成excel
            generateExcelFile(fileSavePath,data,exportCondition);
            // 压缩文件
            return compressFile(fileSavePath);
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
            throw new BusinessException(String.format(ExportQuestionnaireRecService.ERROR_MSG,exportType));
        }
        return fileName;
    }

    /**
     * 生成Excel
     * @param fileName 文件保存路径（含基础路径）
     * @param data 导出数据集合（暂时没用）
     * @param exportCondition 导出条件
     */
    @Override
    public File generateExcelFile(String fileName, List data, ExportCondition exportCondition) throws IOException {

        List<Integer> questionnaireTypeList = exportCondition.getQuestionnaireType();
        if (CollectionUtils.isEmpty(questionnaireTypeList)){
            return null;
        }
        for (Integer questionnaireType : questionnaireTypeList) {
            if (Objects.equals(QuestionnaireConstant.STUDENT_TYPE,questionnaireType)){
                String filePath = getFileName(QuestionnaireConstant.STUDENT_TYPE, exportCondition.getExportType(), exportCondition.getDistrictId(), fileName);
                for (Integer type : QuestionnaireConstant.getStudentTypeList()) {
                    generateFile(filePath, exportCondition, type);
                }
            }else {
                generateFile(getFileName(questionnaireType, exportCondition.getExportType(), exportCondition.getDistrictId(), fileName), exportCondition, questionnaireType);
            }
        }
        return null;
    }

    /**
     * 生成Excel
     * @param fileName 文件保存路径（含基础路径）
     * @param exportCondition 导出条件
     * @param questionnaireType 问卷类型
     */
    private void generateFile(String fileName, ExportCondition exportCondition, Integer questionnaireType) throws IOException {
        Optional<QuestionnaireExcel> questionnaireExcelService = questionnaireExcelFactory.getQuestionnaireExcelService(questionnaireType);
        if (questionnaireExcelService.isPresent()) {
            QuestionnaireExcel questionnaireExcel = questionnaireExcelService.get();
            questionnaireExcel.generateFile(exportCondition,fileName,QuestionnaireConstant.REC_FILE);
        }
    }


    /**
     * 7.获取通知的关键内容
     * @param exportCondition 导出条件
     */
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
    public String getLockKey(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getLockKey(exportCondition);
        }
        throw new BusinessException(String.format(ERROR_MSG,exportCondition.getExportType()));
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
                        && QuestionnaireConstant.getStudentTypeList().contains(userQuestionRecord.getQuestionnaireType());
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

    /**
     * 3、获取文件保存路径
     * @param parentPath 文件保存父目录路径
     * @param fileName 文件名
     */
    @Override
    public String getFileSavePath(String parentPath, String fileName) {
        String fileSavePath = super.getFileSavePath(parentPath, fileName);
        if(!FileUtil.exist(fileSavePath)){
            FileUtil.mkdir(fileSavePath);
        }
        return fileSavePath;
    }

    /**
     * 压缩文件
     * @param fileSavePath 文件保存路径（含基础路径）
     */
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


    @Override
    public Boolean isPackage() {
        return Boolean.TRUE;
    }
}
