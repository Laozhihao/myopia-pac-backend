package com.wupol.myopia.business.aggregation.export.excel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ZipUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExportExcelServiceNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.RecExportDataTypeEnum;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.file.QuestionnaireExcel;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.service.ArchiveService;
import com.wupol.myopia.business.aggregation.export.service.ScreeningFacade;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
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



    @Autowired
    private QuestionnaireFactory questionnaireFactory;
    @Autowired
    private UserQuestionRecordService userQuestionRecordService;
    @Autowired
    private ArchiveService archiveService;
    @Autowired
    private ScreeningFacade screeningFacade;

    private List<Integer> recFileList = Lists.newArrayList(ExportTypeConst.DISTRICT_STATISTICS_REC,ExportTypeConst.SCHOOL_STATISTICS_REC,ExportTypeConst.SCREENING_RECORD_REC);
    private static List<Integer> schoolQuestionnaireType = Lists.newArrayList(QuestionnaireConstant.STUDENT_TYPE, QuestionnaireTypeEnum.VISION_SPINE.getType());

    private volatile String fileType;

    private static String errorRecMsg = "【导出REC异常】{}";

    /**
     * 预处理
     * @param exportCondition 导出条件
     */
    @Override
    public void preProcess(ExportCondition exportCondition) {
        fileType = recFileList.contains(exportCondition.getExportType())?QuestionnaireConstant.REC_FILE:QuestionnaireConstant.EXCEL_FILE;
        ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
        exportTypeService.preProcess(exportCondition);
    }

    /**
     * 1、获取文件名
     * @param exportCondition 导出条件
     */
    @Override
    public String getFileName(ExportCondition exportCondition) {
        ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
        return exportTypeService.getFileName(exportCondition);
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
            // 生成文件
            generateFile(fileSavePath,exportCondition,fileType);
            // 压缩文件
            return compressFile(fileSavePath);
        }
        return null;
    }

    @Override
    public String getErrorMsg() {
        if (Objects.equals(fileType,QuestionnaireConstant.REC_FILE)){
            return errorRecMsg;
        }
        return super.getErrorMsg();
    }

    /**
     * 根据地区导出数据时，获取地区的各个学校问卷数据文件夹名称
     *
     * @param questionnaireType 问卷类型
     * @param exportCondition 导出条件
     * @param fileName 文件路径
     */
    private String getFileName(Integer questionnaireType,ExportCondition exportCondition,String fileName,String fileType){
        if (schoolQuestionnaireType.contains(questionnaireType) && Objects.nonNull(exportCondition.getDistrictId()) && Objects.equals(fileType,QuestionnaireConstant.EXCEL_FILE)){
            ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
            String districtKey = exportTypeService.getFolder(exportCondition.getDistrictId());
            return getFileSavePath(fileName,districtKey);
        }

        if (Objects.equals(ExportTypeConst.SCREENING_RECORD_REC,exportCondition.getExportType())
                && Objects.isNull(exportCondition.getSchoolId())
                && Objects.nonNull(exportCondition.getScreeningOrgId())){
            ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
            String districtKey = exportTypeService.getFolder(exportCondition.getScreeningOrgId());
            return getFileSavePath(fileName,districtKey);
        }
        return fileName;
    }

    /**
     * 生成Excel
     * @param fileName 文件保存路径（含基础路径）
     * @param exportCondition 导出条件
     */
    public File generateFile(String fileName, ExportCondition exportCondition,String fileType) throws IOException {

        if (Objects.equals(1,exportCondition.getDataType())) {
            generateArchiveRec(fileName,exportCondition);
        }

        if (Objects.equals(2,exportCondition.getDataType())) {
            generateQuestionnaireRec(fileName,exportCondition,fileType);
        }

        return null;
    }

    /**
     * 生成问卷rec
     * @param fileName 文件保存路径（含基础路径）
     * @param exportCondition 导出条件
     * @param fileType 文件类型
     */
    private void generateQuestionnaireRec(String fileName, ExportCondition exportCondition,String fileType) throws IOException {
        List<Integer> questionnaireTypeList = exportCondition.getQuestionnaireType();
        if (CollectionUtils.isEmpty(questionnaireTypeList)){
            return ;
        }
        for (Integer questionnaireType : questionnaireTypeList) {
            if (Objects.equals(QuestionnaireConstant.STUDENT_TYPE,questionnaireType)){
                String filePath = getFileName(QuestionnaireConstant.STUDENT_TYPE, exportCondition, fileName,fileType);
                for (Integer type : QuestionnaireConstant.getStudentTypeList()) {
                    generateFile(filePath, exportCondition, type,fileType);
                }
            }else {
                generateFile(getFileName(questionnaireType, exportCondition, fileName,fileType), exportCondition, questionnaireType,fileType);
            }
        }
    }

    /**
     * 生成监测表rec
     * @param fileName 文件保存路径（含基础路径）
     * @param exportCondition 导出条件
     */
    private void generateArchiveRec(String fileName, ExportCondition exportCondition) throws IOException {
        List<Integer> questionnaireTypeList = exportCondition.getQuestionnaireType();
        if (CollectionUtils.isEmpty(questionnaireTypeList)){
            return ;
        }
        generateFile(fileName, exportCondition, QuestionnaireTypeEnum.ARCHIVE_REC.getType(),QuestionnaireConstant.REC_FILE);
    }

    /**
     * 生成文件
     * @param fileName 文件保存路径（含基础路径）
     * @param exportCondition 导出条件
     * @param questionnaireType 问卷类型
     */
    private void generateFile(String fileName, ExportCondition exportCondition, Integer questionnaireType,String fileType) throws IOException {
        QuestionnaireExcel questionnaireExcelService = questionnaireFactory.getQuestionnaireExcelService(questionnaireType);
        questionnaireExcelService.generateFile(exportCondition,fileName,fileType);
    }


    /**
     * 7.获取通知的关键内容
     * @param exportCondition 导出条件
     */
    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
        return exportTypeService.getNoticeKeyContent(exportCondition);
    }


    @Override
    public String getLockKey(ExportCondition exportCondition) {
        ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
        return exportTypeService.getLockKey(exportCondition);
    }


    @Override
    public Class getHeadClass(ExportCondition exportCondition) {
        return null;
    }

    @Override
    public void validateBeforeExport(ExportCondition exportCondition) {
        this.preProcess(exportCondition);

        if (Objects.equals(exportCondition.getDataType(), RecExportDataTypeEnum.ARCHIVE_REC.getCode())){
            archiveService.archiveDataValidate(exportCondition);
        }

        if (Objects.equals(exportCondition.getDataType(),RecExportDataTypeEnum.QUESTIONNAIRE.getCode())){
            questionnaireDataValidate(exportCondition);
        }

    }



    /**
     * 问卷数据校验
     * @param exportCondition 导出条件
     */
    private void questionnaireDataValidate(ExportCondition exportCondition) {
        if (ExportTypeConst.getRecExportTypeList().contains(exportCondition.getExportType()) && Objects.isNull(exportCondition.getDataType())) {
            throw new IllegalArgumentException("导出rec数据类型不能为空");
        }

        List<UserQuestionRecord> userQuestionRecordList = getUserQuestionRecordList(exportCondition);
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
     * 获取不作废的用户问卷记录
     * @param exportCondition 导出条件
     */
    private List<UserQuestionRecord> getUserQuestionRecordList(ExportCondition exportCondition) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByNoticeIdOrTaskIdOrPlanId(exportCondition.getNotificationId(),exportCondition.getTaskId(),exportCondition.getPlanId(), QuestionnaireStatusEnum.FINISH.getCode());
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            throw new BusinessException("暂无数据");
        }

        userQuestionRecordList =  screeningFacade.filterByPlanId(userQuestionRecordList);

        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            throw new BusinessException("暂无数据");
        }
        return userQuestionRecordList;
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
