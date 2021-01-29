package com.wupol.myopia.business.management.facade;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.*;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.*;
import com.wupol.myopia.business.management.domain.vo.*;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
@Log4j2
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
    private OauthService oauthService;
    @Autowired
    private UserService userService;

    /**
     * 生成筛查机构Excel
     * @param districtId 地区id
     **/
    public File generateScreeningOrganization(Integer districtId) throws IOException {
        if (Objects.isNull(districtId)) {
            throw new BusinessException("行政区域id不能为空");
        }
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("筛查机构");
        District district = districtService.findOne(new District().setId(districtId));
        if (Objects.isNull(district)) {
            throw new BusinessException("未找到该行政区域");
        }
        builder.append("-").append(district.getName());
        String fileName = builder.toString();
        // 构建数据
        ScreeningOrganizationQuery query = new ScreeningOrganizationQuery();
        query.setDistrictId(districtId);
        List<ScreeningOrganization> list = screeningOrganizationService.getBy(query);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException("未找到对应的数据");
        }
        List<ScreeningOrganizationExportVo> exportList = new ArrayList<>();
        for (ScreeningOrganization item : list) {
            List<Integer> staffIdList = screeningOrganizationStaffService.findByList(new ScreeningOrganizationStaff().setScreeningOrgId(item.getId()))
                    .stream().map(ScreeningOrganizationStaff::getId).collect(Collectors.toList());
            // 获取筛查人员信息
            List<UserDTO> UserDTOList = oauthService.getUserBatchByIds(staffIdList);
            List<String> staffNameList = UserDTOList.stream().map(UserDTO::getRealName).collect(Collectors.toList());

            ScreeningOrganizationExportVo exportVo = new ScreeningOrganizationExportVo();
            exportVo.setId(item.getId())
                    .setName(item.getName())
                    .setAddress(getAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()))
                    .setType(ScreeningOrganizationEnum.getTypeName(item.getType()))
                    .setRemark(item.getRemark())
                    .setPersonSituation("共" + staffNameList.size() + "名。" + toUserNameString(staffNameList))
                    .setScreeningCount(1)
                    //TODO 待改成最新的筛查任务
                    .setScreeningTitle("筛查标题")
                    .setScreeningTime("筛查时间段")
                    .setScreeningProgress("筛查进度")
                    .setScreeningSchool("负责筛查学校")
                    .setScreeningPersonSituation("共" + staffNameList.size() + "名。" + toUserNameString(staffNameList));
            exportList.add(exportVo);
        }

        log.info("导出文件: {}", fileName);
        return ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationExportVo.class);
    }

    /**
     * 生成筛查机构人员Excel
     * @param screeningOrgId    机构id
     **/
    public File generateScreeningOrganizationStaff(Integer screeningOrgId) throws IOException {
        if (Objects.isNull(screeningOrgId)) {
            throw new BusinessException("筛查机构id不能为空");
        }
        UserDTOQuery userQuery = new UserDTOQuery();
        //TODO 待改成批量模糊查询
        userQuery.setSize(11)
                .setCurrent(1)
                .setOrgId(screeningOrgId);
        Page<UserDTO> userPage = oauthService.getUserListPage(userQuery);
        List<UserDTO> userList = JSONObject.parseArray(JSONObject.toJSONString(userPage.getRecords()), UserDTO.class);
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("筛查机构人员");
        String orgName = screeningOrganizationService.getById(screeningOrgId).getName();
        builder.append("-").append(orgName);
        String fileName = builder.toString();

        // 获取完整的用户信息
//        List<Integer> ids = userList.stream().map(UserDTO::getId).collect(Collectors.toList());
//        Map<Integer, ScreeningOrganizationStaff> staffMap = screeningOrganizationStaffService.getByIds(ids).stream()
//                .collect(Collectors.toMap(ScreeningOrganizationStaff::getUserId, Function.identity()));
        // 构建数据
        List<ScreeningOrganizationStaffExportVo> exportList = userList.stream()
                .map(item -> {
                    return new ScreeningOrganizationStaffExportVo()
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
     * @param districtId 地区id
     **/
    public File generateHospital(Integer districtId) throws IOException, ValidationException {
        if (Objects.isNull(districtId)) {
            throw new BusinessException("行政区域id不能为空");
        }
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("医院");
        District district = districtService.findOne(new District().setId(districtId));
        if (Objects.isNull(district)) {
            throw new BusinessException("未找到该行政区域");
        }
        builder.append("-").append(district.getName());
        String fileName = builder.toString();
        // 构建数据
        Set<Integer> createUserIds = new HashSet<>();
        HospitalQuery query = new HospitalQuery();
        query.setDistrictId(districtId);
        List<Hospital> list = hospitalService.getBy(query);
        List<HospitalExportVo> exportList = new ArrayList<>();
        for (Hospital item : list) {
            HospitalExportVo exportVo = new HospitalExportVo()
                    .setId(item.getId())
                    .setName(item.getName())
                    .setDistrictName("层级")
                    .setLevel(item.getLevelDesc())
                    .setType(HospitalEnum.getTypeName(item.getType()))
                    .setKind(HospitalEnum.getKindName(item.getKind()))
                    .setRemark(item.getRemark())
                    .setAddress(item.getAddress())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME));
            List<String> districtList = districtService.getSplitAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode());
            exportVo.setProvince(districtList.get(0))
                    .setCity(districtList.get(1))
                    .setArea(districtList.get(2))
                    .setTown(districtList.get(3));
            createUserIds.add(item.getCreateUserId());
            exportList.add(exportVo);
        }

        // 批量设置创建人姓名
        Map<Integer, UserDTO> userMap = userService.getUserMapByIds(createUserIds);
        exportList.forEach(item -> {
            if (null != userMap.get(item.getCreateUserId())) {
                item.setCreateUser(userMap.get(item.getCreateUserId()).getRealName());
            }
        });
        return ExcelUtil.exportListToExcel(fileName, exportList, HospitalExportVo.class);
    }


    /**
     * 生成学校Excel
     * @param districtId 地区id
     **/
    public File generateSchool(Integer districtId) throws IOException, ValidationException {
        if (Objects.isNull(districtId)) {
            throw new BusinessException("行政区域id不能为空");
        }
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("学校");
        District district = districtService.findOne(new District().setId(districtId));
        if (Objects.isNull(district)) {
            throw new BusinessException("未找到该行政区域");
        }
        builder.append("-").append(district.getName());
        String fileName = builder.toString();
        // 构建数据
        Set<Integer> createUserIds = new HashSet<>();
        SchoolQuery query = new SchoolQuery();
        query.setDistrictId(districtId);
        List<School> list = schoolService.getBy(query);
        List<SchoolExportVo> exportList = new ArrayList<>();
        for (School item : list) {
            SchoolExportVo exportVo = new SchoolExportVo()
                    .setNo(item.getSchoolNo())
                    .setName(item.getName())
                    .setKind(item.getKindDesc())
                    .setLodgeStatus(SchoolEnum.getLodgeName(item.getLodgeStatus()))
                    .setType(SchoolEnum.getTypeName(item.getType()))
                    .setStudentCount(123)
                    .setDistrictName("层级")
                    .setAddress(item.getAddress())
                    // TODO 待组装数据. X年级：X班、Y班 ；Q年级：X班、Y班
                    .setClassName("年级")
                    .setRemark(item.getRemark())
                    .setScreeningCount(2121)
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME));
            List<String> districtList = districtService.getSplitAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode());
            exportVo.setProvince(districtList.get(0))
                    .setCity(districtList.get(1))
                    .setArea(districtList.get(2))
                    .setTown(districtList.get(3));
            createUserIds.add(item.getCreateUserId());
            exportList.add(exportVo);
        }

        // 批量设置创建人姓名
        Map<Integer, UserDTO> userMap = userService.getUserMapByIds(createUserIds);
        exportList.forEach(item -> {
            if (null != userMap.get(item.getCreateUserId())) {
                item.setCreateUser(userMap.get(item.getCreateUserId()).getRealName());
            }
        });
        log.info("导出文件: {}", fileName);
        return ExcelUtil.exportListToExcel(fileName, exportList, SchoolExportVo.class);
    }
    /**
     * 生成学生Excel
     * @param schoolId    学校id
     * @param gradeId    年级id
     **/
    public File generateStudent(Integer schoolId, Integer gradeId) throws IOException, ValidationException {
        if (Objects.isNull(schoolId)) {
            throw new BusinessException("学校id不能为空");
        }
        if (Objects.isNull(gradeId)) {
            throw new BusinessException("年级id不能为空");
        }
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("学生");
        String schoolName = schoolService.getById(schoolId).getName();
        String gradeName = schoolGradeService.getById(gradeId).getName();
        builder.append("-").append(schoolName);
        builder.append("-").append(gradeName);
        String fileName = builder.toString();
        List<Student> list = studentService.getBySchoolIdAndGradeIdAndClassId(schoolId, gradeId, null);
        // 获取年级班级信息
        List<Integer> gradeIdList = list.stream().map(Student::getGradeId).collect(Collectors.toList());
        List<Integer> classIdList = list.stream().map(Student::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getByIds(gradeIdList).stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
        Map<Integer, SchoolClass> classMap = schoolClassService.getByIds(classIdList).stream().collect(Collectors.toMap(SchoolClass::getId, Function.identity()));

        List<StudentExportVo> exportList = new ArrayList<>();
        for (Student item : list) {
            StudentExportVo exportVo = new StudentExportVo()
                    .setNo(item.getSno())
                    .setName(item.getName())
                    .setGender(GenderEnum.getName(item.getGender()))
                    .setBirthday(DateFormatUtil.format(item.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                    .setNation(NationEnum.getName(item.getNation()))
                    .setSchoolName(schoolName)
                    .setGrade(gradeMap.get(item.getGradeId()).getName())
                    .setClassName(classMap.get(item.getClassId()).getName())
                    .setIdCard(item.getIdCard())
                    .setBindPhone(item.getMpParentPhone())
                    .setPhone(item.getParentPhone())
                    .setAddress(item.getAddress())
                    .setLabel(item.getVisionLabel())
                    .setSituation(item.getCurrentSituation())
//                    .setScreeningCount(item.getScreeningCount())
                    //TODO 就诊次数
                    .setVisitsCount(6666)
//                    .setQuestionCount(item.getQuestionnaireCount())
                    .setLastScreeningTime(DateFormatUtil.format(item.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE));
            List<String> districtList = districtService.getSplitAddress(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode());
            exportVo.setProvince(districtList.get(0))
                    .setCity(districtList.get(1))
                    .setArea(districtList.get(2))
                    .setTown(districtList.get(3));
            exportList.add(exportVo);
        }
        return ExcelUtil.exportListToExcel(fileName, exportList, StudentExportVo.class);
    }


    /**
     * 导入学生
     *
     * @param schoolId      学校id
     * @param createUserId  创建人userID
     * @param multipartFile 导入文件
     * @throws BusinessException 异常
     */
    public void importStudent(Integer schoolId, Integer createUserId, MultipartFile multipartFile) throws IOException, ParseException {
        if (null == schoolId) {
            throw new BusinessException("学校ID不能为空");
        }
        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + ".xlsx";
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
        if (listMap.size() != 0) { // 去头部
            listMap.remove(0);
        }
        List<Student> importList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            Student student = new Student();
            // excel 格式： 序号	姓名	性别	出生日期	民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  ) 年级	班级	学号	身份证号	手机号码	省	市	县区	镇/街道	详细
            List<Long> addressCodeList = districtService.getCodeByName(item.get(10), item.get(11), item.get(12), item.get(13));
            student.setName(item.get(1))
                    .setGender(GenderEnum.getType(item.get(2)))
                    .setBirthday(DateFormatUtil.parseDate(item.get(3), DateFormatUtil.FORMAT_ONLY_DATE2))
                    .setNation(Integer.valueOf(item.get(4)))
                    //TODO 年级班级名转id
                    .setGradeId(23)
                    .setClassId(18)
                    .setSno(Integer.valueOf(item.get(7)))
                    .setIdCard(item.get(8))
                    .setParentPhone(item.get(9))
                    .setProvinceCode(addressCodeList.get(0))
                    .setCityCode(addressCodeList.get(1))
                    .setAreaCode(addressCodeList.get(2))
                    .setTownCode(addressCodeList.get(3))
                    .setAddress(item.get(14))
                    .setCreateUserId(createUserId);
        }
        studentService.saveBatch(importList);
    }


    /**
     * 导入机构人员
     *
     * @param createUserId   创建人id
     * @param multipartFile  导入文件
     * @param screeningOrgId 筛查机构id
     * @throws BusinessException io异常
     */
    public void importScreeningOrganizationStaff(Integer createUserId, MultipartFile multipartFile,
                                                 Integer screeningOrgId) throws IOException {
        if (null == screeningOrgId) {
            throw new BusinessException("机构ID不能为空");
        }

        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + ".xlsx";
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap;
        try {
            listMap = EasyExcel.read(fileName).sheet().doReadSync();
        } catch (ExcelAnalysisException excelAnalysisException) {
            log.error("导入机构人员异常", excelAnalysisException);
            throw new BusinessException("解析文件格式异常");
        } catch (Exception e) {
            log.error("导入机构人员异常", e);
            throw new BusinessException("解析Excel文件异常");
        }
        if (listMap.size() != 0) { // 去头部
            listMap.remove(0);
        }
        // excel格式：序号	姓名	性别	身份证号	手机号码	说明
        List<UserDTO> userList = listMap.stream()
                .map(item -> new UserDTO()
                        .setRealName(item.get(1))
                        .setGender(GenderEnum.getType(item.get(2)))
                        .setIdCard(item.get(3))
                        .setPhone(item.get(4))
                        .setRemark(item.get(5))
                        .setCreateUserId(createUserId)
                        .setIsLeader(0)
                        .setOrgId(screeningOrgId)
                        .setSystemCode(SystemCode.SCREENING_CLIENT.getCode())).collect(Collectors.toList());
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
        // 批量新增, 并设置返回的userId
        for (int i = 0; i < importList.size(); i++) {
            importList.get(i).setUserId(oauthService.addScreeningUserBatch(userList).get(i));
        }
        screeningOrganizationStaffService.saveBatch(importList);
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

    /** 用户名列表转成输出的字符串 */
    private String toUserNameString(List<String> userNameList) {
        return userNameList.toString().replaceFirst("\\[", "").replaceFirst("]", "");
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
