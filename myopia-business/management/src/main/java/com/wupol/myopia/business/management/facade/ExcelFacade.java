package com.wupol.myopia.business.management.facade;

import com.alibaba.excel.EasyExcel;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.HospitalEnum;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.constant.SchoolEnum;
import com.wupol.myopia.business.management.constant.ScreeningOrganizationEnum;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.*;
import com.wupol.myopia.business.management.domain.vo.*;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.ValidationException;
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
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 生成筛查机构Excel
     * @param query    查询条件
     **/
    public String generateScreeningOrganization(ScreeningOrganizationQuery query) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(query.getIdLike())) builder.append("_").append(query.getIdLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("_").append(query.getNameLike());
        if (Objects.nonNull(query.getType())) builder.append("_").append(ScreeningOrganizationEnum.getTypeName(query.getType()));
        if (StringUtils.isNotBlank(query.getCode())) builder.append("_").append(query.getCode());
        builder.append("_").append(DateFormatUtil.formatNow(DateFormatUtil.FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR));
        String fileName = String.format(SCREENING_ORGANIZATION, builder.toString());

        //TODO 待写模糊搜索
        List<ScreeningOrganization> list = screeningOrganizationService.findByList(query);
        List<ScreeningOrganizationExportVo> exportList = list.stream()
                .map(item -> {
                    List<String> staffNameList = new ArrayList<>();
                    try {
                        List<Integer> staffIdList = screeningOrganizationStaffService.findByList(new ScreeningOrganizationStaff().setScreeningOrgId(item.getId()))
                                .stream().map(ScreeningOrganizationStaff::getId).collect(Collectors.toList());
                        ApiResult<List<UserDTO>> result = oauthServiceClient.getUserBatchByIds(staffIdList);
                        if (result.isSuccess()) {
                            staffNameList = result.getData().stream().map(UserDTO::getRealName).collect(Collectors.toList());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new ScreeningOrganizationExportVo()
                            .setId(item.getOrgNo())
                            .setName(item.getName())
                            .setType(ScreeningOrganizationEnum.getTypeName(item.getType()))
                            .setRemark(item.getRemark())
                            .setAddress(getAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()))
                            .setPersonNames(staffNameList.toString().replaceFirst("\\[","").replaceFirst("]",""))
                            .setScreeningCount(1)
                            .setPersonCount(staffNameList.size());
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
        List<Hospital> list = hospitalService.findByList(query);
        List<HospitalExportVo> exportList = list.stream()
                .map(item -> {
                    return new HospitalExportVo()
                            .setNo(item.getHospitalNo())
                            .setName(item.getName())
                            .setLevel(item.getLevelDesc())
                            .setType(HospitalEnum.getTypeName(item.getType()))
                            .setKind(HospitalEnum.getKineName(item.getKind()))
                            .setRemark(item.getRemark())
                            .setAddress(getAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()));
                }).collect(Collectors.toList());
        File file = ExcelUtil.exportListToExcel(fileName, exportList, HospitalExportVo.class);

        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
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
        List<School> list = schoolService.findByList(query);

        List<SchoolExportVo> exportList = list.stream()
                .map(item -> {
                    return new SchoolExportVo()
                            .setNo(item.getSchoolNo())
                            .setName(item.getName())
                            .setKind(item.getKindDesc())
                            .setLodgeStatus(SchoolEnum.getLodgeName(item.getLodgeStatus()))
                            .setType(SchoolEnum.getTypeName(item.getType()))
                            .setOnlineCount(item.getTotalOnline())
                            .setOnlineMaleCount(item.getTotalOnlineMale())
                            .setOnlineFemaleCount(item.getTotalOnlineFemale())
                            .setLodgeCount(item.getTotalLodge())
                            .setLodgeMaleCount(item.getTotalLodgeMale())
                            .setLodgeFemaleCount(item.getTotalLodgeFemale())
                            .setAddress(getAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()))
                            .setClassName("年级")
                            .setRemark(item.getRemark())
                            .setScreeningCount(2121);
                }).collect(Collectors.toList());


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
        List<Student> list = studentService.getStudentsBySchoolId(query.getSchoolId());
        String schoolName = schoolService.getById(query.getSchoolId()).getName();
        List<StudentExportVo> exportVoList = list.stream()
                .map(item -> {
                    return new StudentExportVo()
                            .setNo(item.getSno())
                            .setName(item.getName())
                            //TODO 待转中文
                            .setGender(item.getGender()+"")
                            .setBirthday(DateFormatUtil.format(item.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                            .setNation(NationEnum.getName(item.getNation()))
                            .setSchool(schoolName)
                            .setGrade("年级名")
                            .setClassName("班级名")
                            .setIdCard(item.getIdCard())
                            .setPhone(item.getParentPhone())
                            .setAddress(getAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()));
                }).collect(Collectors.toList());
        File file = ExcelUtil.exportListToExcel(fileName, exportVoList, StudentExportVo.class);
        //TODO 待上传文件
        return "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";
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

    private String getAddress(Integer provinceCode, Integer cityCode, Integer areaCode, Integer townCode, String address) {
        try {
            return districtService.getAddressPrefix(provinceCode, cityCode, areaCode, townCode) + address;
        } catch (ValidationException e) {
            log.error("获取地址失败", e);
        }
        return "";
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
