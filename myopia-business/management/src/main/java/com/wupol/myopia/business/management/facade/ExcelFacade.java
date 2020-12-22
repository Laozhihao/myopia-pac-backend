package com.wupol.myopia.business.management.facade;

import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.management.constant.ScreeningOrganizationEnum;
import com.wupol.myopia.business.management.domain.model.BillRecord;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationStaffQuery;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 统一处理 Excel 上传/下载
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@Slf4j
@Service
public class ExcelFacade {
    /** 日账单Excel文件名，如 Logs_2020-10-16_(GMT+2).xlsx */
    private static final String BILL_EXCEL_FILE_NAME_OF_DAY = "Logs_%s_(%s).xlsx";
    private static final String SCREENING_ORGANIZATION = "筛查机构%s.xlsx";
    private static final String SCREENING_ORGANIZATION_STAFF = "筛查机构人员%s.xlsx";
    private static final String HOSPITAL = "医院%s.xlsx";
    private static final String STUDENT = "学生%s.xlsx";

    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private StudentService studentService;

    /**
     * 生成筛查机构Excel
     * @param query    查询条件
     **/
    public String generateScreeningOrganization(ScreeningOrganizationQuery query) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(query.getIdLike())) builder.append("_").append(query.getIdLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("_").append(query.getNameLike());
        if (Objects.nonNull(query.getType())) builder.append("_").append(ScreeningOrganizationEnum.getNameByType(query.getType()));
        if (StringUtils.isNotBlank(query.getCode())) builder.append("_").append(query.getCode());
        String fileName = String.format(SCREENING_ORGANIZATION, builder.toString());
        //TODO 待写模糊搜索
//        List<ScreeningOrganization> list = screeningOrganizationService.findByList(query);
        log.info("导出文件: {}", fileName);
//        File file = ExcelUtil.exportListToExcel(fileName, list, ScreeningOrganizationExportVo.class);


        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
//        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billStatList, BillStat.class);
    }

    /**
     * 生成筛查机构人员Excel
     * @param query    查询条件
     **/
    public String generateScreeningOrganizationStaff(ScreeningOrganizationStaffQuery query) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(query.getIdCardLike())) builder.append("_").append(query.getIdCardLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("_").append(query.getNameLike());
        if (StringUtils.isNotBlank(query.getPhoneLike())) builder.append("_").append(query.getPhoneLike());
        if (StringUtils.isNotBlank(query.getOrgNameLike())) builder.append("_").append(query.getOrgNameLike());
        String fileName = String.format(SCREENING_ORGANIZATION_STAFF, builder.toString());
        //TODO 待写模糊搜索
//        List<ScreeningOrganizationStaff> list = screeningOrganizationStaffService.findByList(query);
        log.info("导出文件: {}", fileName);
//        File file = ExcelUtil.exportListToExcel(fileName, list, ScreeningOrganizationExportVo.class);


        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
//        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billStatList, BillStat.class);
    }

    /**
     * 生成医院Excel
     * @param query    查询条件
     **/
    public String generateHospital(HospitalQuery query) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("_").append(query.getNameLike());
        if (StringUtils.isNotBlank(query.getCode())) builder.append("_").append(query.getCode());
        String fileName = String.format(HOSPITAL, builder.toString());
        //TODO 待写模糊搜索
//        List<Hospital> list = hospitalService.findByList(query);
        log.info("导出文件: {}", fileName);
//        File file = ExcelUtil.exportListToExcel(fileName, list, Hospital.class);

        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
//        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billStatList, BillStat.class);
    }


    /**
     * 生成学生Excel
     * @param query    查询条件
     **/
    public String generateStudent(StudentQuery query) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("_").append(query.getNameLike());
        if (StringUtils.isNotBlank(query.getIdCardLike())) builder.append("_").append(query.getIdCardLike());
        if (StringUtils.isNotBlank(query.getSnoLike())) builder.append("_").append(query.getSnoLike());
        if (StringUtils.isNotBlank(query.getPhoneLike())) builder.append("_").append(query.getPhoneLike());
        //TODO 待转中文
        if (Objects.nonNull(query.getGender())) builder.append("_").append(query.getGender());
        if (Objects.nonNull(query.getGradeId())) builder.append("_").append(query.getGradeId());
        if (Objects.nonNull(query.getStartDate())) builder.append("_").append(query.getStartDate());
        if (Objects.nonNull(query.getEndDate())) builder.append("_").append(query.getEndDate());
        String fileName = String.format(STUDENT, builder.toString());
        //TODO 待写模糊搜索
//        List<Student> list = hospitalService.findByList(query);
        log.info("导出文件: {}", fileName);
//        File file = ExcelUtil.exportListToExcel(fileName, list, Hospital.class);

        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
//        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billStatList, BillStat.class);
    }




    /**
     * 生成日账单明细Excel
     *
     * @param billRecordList    账单数据
     * @param timezone          时区
     * @param date              日期
     * @return java.io.File
     **/
    public File generateBillExcelOfDay(List<BillRecord> billRecordList, String timezone, String date) throws IOException {
        String fileName = String.format(BILL_EXCEL_FILE_NAME_OF_DAY, date, timezone);
        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billRecordList, BillRecord.class);
    }


    /**
     * 获取文件路径
     *
     * @param fileName  文件名
     * @return java.lang.String
     **/
    private String getFilePathName(String fileName) {
        return FilenameUtils.concat(IOUtils.getTempSubPath("excel"), fileName);
    }
}
