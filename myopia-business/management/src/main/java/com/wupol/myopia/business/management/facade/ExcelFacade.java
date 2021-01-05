package com.wupol.myopia.business.management.facade;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.*;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
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
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
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
    public File generateScreeningOrganization(ScreeningOrganizationQuery query) throws IOException {
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("筛查机构");
        if (StringUtils.isNotBlank(query.getOrgIdLike())) builder.append("-").append(query.getOrgIdLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("-").append(query.getNameLike());
        if (Objects.nonNull(query.getType())) builder.append("-").append(ScreeningOrganizationEnum.getTypeName(query.getType()));
        if (Objects.nonNull(query.getCode())) {
            District district = districtService.findOne(new District().setCode(query.getCode()));
            if (Objects.nonNull(district)) builder.append("-").append(district.getName());
        }
        String fileName = builder.toString();
        // 构建数据
        List<ScreeningOrganization> list = screeningOrganizationService.getExportData(query);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException("未找到对应的数据");
        }
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
        return ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationExportVo.class);
    }

    /**
     * 生成筛查机构人员Excel
     * @param query    查询条件
     **/
    public File generateScreeningOrganizationStaff(ScreeningOrganizationStaffQuery query) throws IOException {
        if (Objects.isNull(query.getScreeningOrgId())) {
            throw new BusinessException("筛查机构id不能为空");
        }
        ApiResult apiResult = oauthServiceClient.getUserListPage(new UserDTO()
                //TODO 待改成批量
                .setSize(11)
                .setCurrent(1)
                .setOrgId(query.getScreeningOrgId())
                .setRealName(query.getNameLike())
                .setIdCard(query.getIdCardLike())
                .setPhone(query.getPhoneLike()));
        if (!apiResult.isSuccess()) {
            throw new BusinessException(apiResult.getMessage());
        }
        Page<UserDTO> userPage = JSONObject.parseObject(JSONObject.toJSONString(apiResult.getData()), Page.class);
        List<UserDTO> userList = JSONObject.parseArray(JSONObject.toJSONString(userPage.getRecords()), UserDTO.class);
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("筛查机构人员");
        builder.append("-").append(screeningOrganizationService.getById(query.getScreeningOrgId()).getName());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("-").append(query.getNameLike());
        if (StringUtils.isNotBlank(query.getIdCardLike())) builder.append("-").append(query.getIdCardLike());
        if (StringUtils.isNotBlank(query.getPhoneLike())) builder.append("-").append(query.getPhoneLike());
        String fileName = builder.toString();

        // 获取完整的用户信息
        List<Integer> ids = userList.stream().map(UserDTO::getId).collect(Collectors.toList());
        Map<Integer, ScreeningOrganizationStaff> staffMap = screeningOrganizationStaffService.getByIds(ids).stream()
                .collect(Collectors.toMap(ScreeningOrganizationStaff::getUserId, Function.identity()));
        String orgName = screeningOrganizationService.getById(query.getScreeningOrgId()).getName();
        // 构建数据
        List<ScreeningOrganizationStaffExportVo> exportList = userList.stream()
                .map(item -> {
                    return new ScreeningOrganizationStaffExportVo()
                            // 如果未找到对应的机构员工,新建默认的
                            .setStaffNo(staffMap.getOrDefault(item.getId(), new ScreeningOrganizationStaff()).getStaffNo())
                            .setName(item.getRealName())
                            .setGender(GenderEnum.getName(item.getGender()))
                            .setPhone(item.getPhone())
                            .setIdCard(item.getIdCard())
                            .setOrganization(orgName);
                }).collect(Collectors.toList());

        log.info("导出文件: {}", fileName);
        return ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationStaffExportVo.class);
    }

    /**
     * 生成医院Excel
     * @param query    查询条件
     **/
    public File generateHospital(HospitalQuery query) throws IOException {
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("医院");
        if (StringUtils.isNotBlank(query.getNoLike())) builder.append("-").append(query.getNoLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("-").append(query.getNameLike());
        if (Objects.nonNull(query.getType())) builder.append("-").append(HospitalEnum.getTypeName(query.getType()));
        if (Objects.nonNull(query.getKind())) builder.append("-").append(HospitalEnum.getKindName(query.getKind()));
        if (Objects.nonNull(query.getLevel())) builder.append("-").append(query.getLevel());
        if (Objects.nonNull(query.getCode())) {
            District district = districtService.findOne(new District().setCode(query.getCode()));
            if (Objects.nonNull(district)) builder.append("-").append(district.getName());
        }
        String fileName = builder.toString();
        // 构建数据
        List<Hospital> list = hospitalService.getExportData(query);
        List<HospitalExportVo> exportList = list.stream()
                .map(item -> {
                    return new HospitalExportVo()
                            .setNo(item.getHospitalNo())
                            .setName(item.getName())
                            .setLevel(item.getLevelDesc())
                            .setType(HospitalEnum.getTypeName(item.getType()))
                            .setKind(HospitalEnum.getKindName(item.getKind()))
                            .setRemark(item.getRemark())
                            .setAddress(getAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()));
                }).collect(Collectors.toList());
        return ExcelUtil.exportListToExcel(fileName, exportList, HospitalExportVo.class);
    }


    /**
     * 生成学校Excel
     * @param query    查询条件
     **/
    public File generateSchool(SchoolQuery query) throws IOException {
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("学校");
        if (StringUtils.isNotBlank(query.getNoLike())) builder.append("-").append(query.getNoLike());
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("-").append(query.getNameLike());
        if (Objects.nonNull(query.getType())) builder.append("-").append(SchoolEnum.getTypeName(query.getType()));
        if (Objects.nonNull(query.getCode())) {
            District district = districtService.findOne(new District().setCode(query.getCode()));
            if (Objects.nonNull(district)) builder.append("-").append(district.getName());
        }
        String fileName = builder.toString();
        // 构建数据
        List<School> list = schoolService.getExportData(query);

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
        return ExcelUtil.exportListToExcel(fileName, exportList, SchoolExportVo.class);
    }
    /**
     * 生成学生Excel
     * @param query    查询条件
     **/
    public File generateStudent(StudentQuery query) throws IOException {
        if (Objects.isNull(query.getSchoolId())) {
            throw new BusinessException("学校id不能为空");
        }
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("学生");
        if (StringUtils.isNotBlank(query.getNameLike())) builder.append("-").append(query.getNameLike());
        if (StringUtils.isNotBlank(query.getIdCardLike())) builder.append("-").append(query.getIdCardLike());
        if (StringUtils.isNotBlank(query.getSnoLike())) builder.append("-").append(query.getSnoLike());
        if (StringUtils.isNotBlank(query.getPhoneLike())) builder.append("-").append(query.getPhoneLike());
        if (Objects.nonNull(query.getGender())) builder.append("-").append(GenderEnum.getName(query.getGender()));
        if (Objects.nonNull(query.getGradeList())) builder.append("-").append(query.getGradeList());
        if (Objects.nonNull(query.getStartDate())) builder.append("-").append(query.getStartDate());
        if (Objects.nonNull(query.getEndDate())) builder.append("-").append(query.getEndDate());
        String fileName = builder.toString();
        // 构建数据
        String schoolName = schoolService.getById(query.getSchoolId()).getName();
        List<Student> list = studentService.getExportData(query);
        // 获取年级班级信息
        List<Integer> gradeIdList = list.stream().map(Student::getGradeId).collect(Collectors.toList());
        List<Integer> classIdList = list.stream().map(Student::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getByIds(gradeIdList).stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
        Map<Integer, SchoolClass> classMap = schoolClassService.getByIds(classIdList).stream().collect(Collectors.toMap(SchoolClass::getId, Function.identity()));

        List<StudentExportVo> exportList = list.stream()
                .map(item -> {
                    return new StudentExportVo()
                            .setNo(item.getSno())
                            .setName(item.getName())
                            .setGender(GenderEnum.getName(query.getGender()))
                            .setBirthday(DateFormatUtil.format(item.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                            .setNation(NationEnum.getName(item.getNation()))
                            .setSchool(schoolName)
                            .setGrade(gradeMap.get(item.getGradeId()).getName())
                            .setClassName(classMap.get(item.getClassId()).getName())
                            .setIdCard(item.getIdCard())
                            .setPhone(item.getParentPhone())
                            .setAddress(getAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()));
                }).collect(Collectors.toList());
        return ExcelUtil.exportListToExcel(fileName, exportList, StudentExportVo.class);
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
            if (listMap.size() != 0) { // 去头部
                listMap.remove(0);
            }
            List<Student> importList = listMap.stream()
                    .map(item -> {
                        Student student = new Student();
                        try {
                            student.setName(item.get(1))
                                    .setGender(GenderEnum.getType(item.get(2)))
                                    .setNation(Integer.valueOf(item.get(4)))
                                    .setStudentNo(item.get(7))
                                    .setIdCard(item.get(8))
                                    .setParentPhone(item.get(9))
                                    //TODO 待分拆地址,转code
                                    .setAddress(item.get(9))
                                    .setProvinceCode(140000000L)
                                    .setCityCode(140100000L)
                                    .setAreaCode(140105000L)
                                    .setTownCode(140105001L)
                                    .setSchoolId(schoolId)
                                    //TODO 年级班级名转id
                                    .setGradeId(12)
                                    .setClassId(324)
                                    .setCreateUserId(createUserId)
                                    .setBirthday(DateFormatUtil.parseDate(item.get(3), DateFormatUtil.FORMAT_ONLY_DATE2))
                            ;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        return student;
                    }).collect(Collectors.toList());
            studentService.saveBatch(importList);
        }catch (Exception e) {
            log.error("解析学生excel数据失败",e);
            throw new IOException("解析学生excel数据失败");
        }
    }

    /**
     * 导入机构人员
     */
    // TODO 管理端做还是筛查端做?
    public void importScreeningOrganizationStaff(Integer orgId, Integer createUserId, MultipartFile multipartFile) throws IOException {
        String fileName = IOUtils.getTempPath() +multipartFile.getName()+"_"+System.currentTimeMillis()+".xlsx";
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        try {
            // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
            List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
            if (listMap.size() != 0) { // 去头部
                listMap.remove(0);
            }
            List<UserDTO> userList = listMap.stream()
                    .map(item -> {
                        UserDTO user = new UserDTO()
                                .setRealName(item.get(1))
                                .setGender(GenderEnum.getType(item.get(2)))
                                .setIdCard(item.get(3))
                                .setPhone(item.get(4))
                                .setRemark(item.get(5))
                                .setCreateUserId(createUserId)
                                .setIsLeader(0)
                                .setOrgId(orgId)
                                .setSystemCode(SystemCode.MANAGEMENT_CLIENT.getCode());
                        return user;
                    }).collect(Collectors.toList());
            // 批量新增, 并设置返回的userId
            ApiResult<List<Integer>> apiResult = oauthServiceClient.addScreeningUserBatch(userList);
            if (!apiResult.isSuccess()) {
                throw new BusinessException(apiResult.getMessage());
            }
            List<ScreeningOrganizationStaffVo> importList = userList.stream().map(item -> {
                ScreeningOrganizationStaffVo staff = new ScreeningOrganizationStaffVo()
                        .setIdCard(item.getIdCard());
                staff.setScreeningOrgId(item.getOrgId())
                        .setCreateUserId(item.getCreateUserId())
                        .setRemark(item.getRemark())
                        //TODO 设置哪个?
                        .setGovDeptId(1);
                return staff;
            }).collect(Collectors.toList());
            // 设置返回的userId
            for (int i = 0; i < importList.size(); i++) {
                importList.get(i).setUserId(apiResult.getData().get(i));
            }
            screeningOrganizationStaffService.saveBatch(importList);
        }catch (Exception e) {
            log.error("解析机构人员excel数据失败",e);
            throw new IOException("解析机构人员excel数据失败");
        }
    }

    /** 获取学生的导入模版 */
    public File getStudentImportDemo() {
        //TODO 待完成文件系统再修改
        return new File("C:\\Users\\Chikong\\AppData\\Local\\Temp\\export\\excel\\demo.xlsx");
    }

    /** 获取筛查机构人员的导入模版 */
    public File getScreeningOrganizationStaffImportDemo() {
        //TODO 待完成文件系统再修改
        return new File("C:\\Users\\Chikong\\AppData\\Local\\Temp\\export\\excel\\demo.xlsx");
    }

    private String getAddress(Long provinceCode, Long cityCode, Long areaCode, Long townCode, String address) {
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
