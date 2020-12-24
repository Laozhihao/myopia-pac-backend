package com.wupol.myopia.business.management.facade;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.management.constant.ScreeningOrganizationEnum;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.*;
import com.wupol.myopia.business.management.domain.vo.*;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String SCHOOL = "学校%s.xlsx";
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
        builder.append("_").append(DateFormatUtil.formatNow(DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR));
        String fileName = String.format(SCREENING_ORGANIZATION, builder.toString());
        //TODO 待写模糊搜索
//        List<ScreeningOrganization> list = screeningOrganizationService.findByList(query);
        List<ScreeningOrganization> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new ScreeningOrganization()
                    .setOrgNo(String.valueOf((long) i))
                    .setName("机构"+i)
                    .setType( i % 6)
                    .setRemark("说明"+i)
            );
        }
        List<ScreeningOrganizationExportVo> exportList = list.stream()
                .map(item -> {
                    return new ScreeningOrganizationExportVo()
                            .setId(Math.toIntExact(Long.parseLong(item.getOrgNo())))
                            .setName(item.getName())
                            .setType(ScreeningOrganizationEnum.getNameByType(item.getType()))
                            .setRemark(item.getRemark())
                            .setAddress("地址")
                            .setPersonNames(Arrays.asList("刘一","刘二").toString().replaceFirst("\\[","").replaceFirst("]",""))
                            .setScreeningCount(Math.toIntExact(Long.parseLong(item.getOrgNo())))
                            .setPersonCount(Math.toIntExact(Long.parseLong(item.getOrgNo())));
                }).collect(Collectors.toList());

        log.info("导出文件: {}", fileName);
        File file = ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationExportVo.class);

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
        builder.append("_").append(DateFormatUtil.formatNow(DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR));
        String fileName = String.format(SCREENING_ORGANIZATION_STAFF, builder.toString());
        //TODO 待写模糊搜索
        List<ScreeningOrganizationStaff> staffList = screeningOrganizationStaffService.findByList(new ScreeningOrganizationStaff());
        //TODO 批量查用户信息, 并转成导出类
        List<ScreeningOrganizationStaffExportVo> exportList = staffList.stream()
                .map(item -> {
                    return new ScreeningOrganizationStaffExportVo()
                            .setStaffNo("41239")
                            .setName("name")
                            .setGender("男")
                            .setPhone("112312311312")
                            .setIdCard("123424125431534534")
                            .setOrganization("438875432804-32");
                }).collect(Collectors.toList());

        log.info("导出文件: {}", fileName);
        File file = ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationStaffExportVo.class);


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
        builder.append("_").append(DateFormatUtil.formatNow(DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR));
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
     * 生成学校Excel
     * @param query    查询条件
     **/
    public String generateSchool(SchoolQuery query) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(query.getIdLike())) builder.append("_").append(query.getIdLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("_").append(query.getNameLike());
        //TODO 待转中文
        if (Objects.nonNull(query.getType())) builder.append("_").append(query.getType());
        if (Objects.nonNull(query.getCode())) builder.append("_").append(query.getCode());
        builder.append("_").append(DateFormatUtil.formatNow(DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR));
        String fileName = String.format(SCHOOL, builder.toString());
        //TODO 待写模糊搜索
//        List<School> list = hospitalService.findByList(query);
        List<SchoolExportVo> exportList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            exportList.add(new SchoolExportVo()
                    .setNo(1)
                    .setName("32423r")
                    .setKind(21)
                    .setLodgeStatus("324323")
                    .setType("123")
                    .setOnlineCount(213)
                    .setOnlineMaleCount(213)
                    .setOnlineFemaleCount(213)
                    .setLodgeCount(213)
                    .setLodgeMaleCount(213)
                    .setLodgeFemaleCount(213)
                    .setAddress("1324321432")
                    .setClassName("42342")
                    .setRemark("41234")
                    .setScreeningCount(2121)
            );
        }


        log.info("导出文件: {}", fileName);
        File file = ExcelUtil.exportListToExcel(fileName, exportList, SchoolExportVo.class);

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
        builder.append("_").append(DateFormatUtil.formatNow(DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR));
        String fileName = String.format(STUDENT, builder.toString());
        //TODO 待写模糊搜索
        List<Student> studentList = studentService.findByList(new Student());
        //TODO 批量查用户信息, 并转成导出类
        log.info("导出文件: {}", fileName);
//        File file = ExcelUtil.exportListToExcel(fileName, list, Hospital.class);

        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
//        return ExcelUtil.exportListToExcel(getFilePathName(fileName), billStatList, BillStat.class);
    }

    /**
     * 导入学生
     */
    public void importStudent(Integer schoolId, Integer createUserId, MultipartFile multipartFile) throws IOException {
        String fileName = IOUtils.getTempPath() +multipartFile.getName()+"_"+System.currentTimeMillis()+".xlsx";
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        try {
            // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
            List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
            if (listMap.size() != 0) {
                listMap.remove(0);
            }
            List<StudentVo> importList = EasyExcel.read(fileName).sheet().doReadSync()
                    .stream()
                    .map(item -> (Map<Integer, String>) item)
                    .map(item -> {
                        StudentVo student = new StudentVo()
                                .setBirthdayString(item.get("3"))
                                .setGradeName(item.get("5"))
                                .setClassName(item.get("6"))
                                ;

                        student.setName("2932kj")
//                        student.setName(item.get("1"))
//                                .setGender(item.get("2"))
//                                .setParentPhone(item.get("3"))
//                                .setNation(item.get("4"))
 //                                .setStudentNo(Long.valueOf(item.get("7")))
//                                .setIdCard(item.get("8"))
//                                .setAddress(item.get("9"))
                                .setGender(1)
                                .setParentPhone("24398454")
                                .setStudentNo(String.valueOf(24123L))
                                .setSno(3324)
                                .setNation(2)
                                .setIdCard("4098245")
                                //TODO 地址待转为code
                                .setAddress("432o5u73weoijf")
                                .setProvinceCode(21312)
                                .setCityCode(21312)
                                .setAreaCode(21312)
                                .setTownCode(21312)
                                .setSchoolId(schoolId)
                                .setGradeId(12)
                                .setClassId(324)
                                .setCreateUserId(createUserId)
                                .setBirthday(new Date())
                        ;

                        return student;
                    }).collect(Collectors.toList());
            //TODO 批量新增,设置回来的数据

            List<Student> list = importList.stream().map(item -> (StudentVo)item).collect(Collectors.toList());
            studentService.saveBatch(list);
        }catch (Exception e) {
            log.error("解析学生excel数据失败",e);
            throw new IOException("解析学生excel数据失败");
        }
    }

    /**
     * 导入机构人员
     */
    public void importScreeningOrganizationStaff(Integer orgId, Integer createUserId, MultipartFile multipartFile) throws IOException {
        String fileName = IOUtils.getTempPath() +multipartFile.getName()+"_"+System.currentTimeMillis()+".xlsx";
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        try {
            // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
            List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
            if (listMap.size() != 0) {
                listMap.remove(0);
            }
//            List<ScreeningOrganizationStaffImportVo> importList = EasyExcel.read(fileName).head(ScreeningOrganizationStaffImportVo.class).sheet().doReadSync();
            //TODO 待批量到Oauth注册
//            {0:"4",1:"陈肖4",2:"女",3:"441434333434342345",4:"12321233452",5:"4"}
            List<ScreeningOrganizationStaffVo> importList = EasyExcel.read(fileName).sheet().doReadSync()
                    .stream()
                    .map(item -> (Map<Integer, String>) item)
                    .map(item -> {
                        ScreeningOrganizationStaffVo staff = new ScreeningOrganizationStaffVo()
                                .setName("name")
                                .setGender(1)
                                .setPhone("112312311312")
                                .setIdCard("123424125431534534");
                        staff.setUserId(2)
                                .setStaffNo(String.valueOf(1L))
                                .setGovDeptId(12)
                                .setRemark(item.get(5))
                                .setScreeningOrgId(orgId)
                                .setCreateUserId(createUserId);
                        return staff;
                    }).collect(Collectors.toList());
            //TODO 批量新增,设置回来的数据

            List<ScreeningOrganizationStaff> list = importList.stream()
                    .map(item -> (ScreeningOrganizationStaff)item)
                    .collect(Collectors.toList());
            screeningOrganizationStaffService.saveBatch(list);
        }catch (Exception e) {
            log.error("解析机构人员excel数据失败",e);
            throw new IOException("解析机构人员excel数据失败");
        }
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
