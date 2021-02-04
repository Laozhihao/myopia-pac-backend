package com.wupol.myopia.business.management.facade;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ExcelUtil;
import com.wupol.myopia.base.util.IOUtils;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.management.client.OauthService;
import com.wupol.myopia.business.management.constant.*;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import com.wupol.myopia.business.management.domain.query.ScreeningOrganizationQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.*;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
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
     *
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

        // 查询数据
        ScreeningOrganizationQuery query = new ScreeningOrganizationQuery();
        query.setDistrictId(districtId);
        List<ScreeningOrganization> list = screeningOrganizationService.getBy(query);
        if (CollectionUtils.isEmpty(list)) {
            return ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), ScreeningOrganizationExportVo.class);
        }

        // 创建人姓名
        Set<Integer> createUserIds = list.stream()
                .map(ScreeningOrganization::getCreateUserId)
                .collect(Collectors.toSet());
        Map<Integer, UserDTO> userMap = userService.getUserMapByIds(createUserIds);

        List<ScreeningOrganizationExportVo> exportList = new ArrayList<>();
        for (ScreeningOrganization item : list) {
            ScreeningOrganizationExportVo exportVo = new ScreeningOrganizationExportVo();
            exportVo.setId(item.getId())
                    .setName(item.getName())
                    .setType(ScreeningOrganizationEnum.getTypeName(item.getType()))
                    .setConfigType(ScreeningOrgConfigTypeEnum.getTypeName(item.getConfigType()))
                    .setPhone(item.getPhone())
                    .setPersonSituation("886")
                    .setRemark(item.getRemark())
                    .setScreeningCount(886)
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setAddress(item.getAddress())
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME));
            if (null != item.getProvinceCode()) {
                exportVo.setProvince(districtService.getDistrictName(item.getProvinceCode()));
            }
            if (null != item.getCityCode()) {
                exportVo.setCity(districtService.getDistrictName(item.getCityCode()));
            }
            if (null != item.getAreaCode()) {
                exportVo.setArea(districtService.getDistrictName(item.getAreaCode()));
            }
            if (null != item.getTownCode()) {
                exportVo.setTown(districtService.getDistrictName(item.getTownCode()));
            }
            exportList.add(exportVo);
        }
        log.info("导出文件: {}", fileName);
        return ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationExportVo.class);
    }

    /**
     * 生成筛查机构人员Excel
     *
     * @param screeningOrgId 机构id
     **/
    public File generateScreeningOrganizationStaff(Integer screeningOrgId) throws IOException {
        if (Objects.isNull(screeningOrgId)) {
            throw new BusinessException("筛查机构id不能为空");
        }
        UserDTOQuery userQuery = new UserDTOQuery();
        userQuery.setSize(11)
                .setCurrent(1)
                .setOrgId(screeningOrgId)
                .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
        Page<UserDTO> userPage = oauthService.getUserListPage(userQuery);
        List<UserDTO> userList = JSONObject.parseArray(JSONObject.toJSONString(userPage.getRecords()), UserDTO.class);
        // 设置文件名
        StringBuilder builder = new StringBuilder().append("筛查机构人员");
        String orgName = screeningOrganizationService.getById(screeningOrgId).getName();
        builder.append("-").append(orgName);
        String fileName = builder.toString();

        if (CollectionUtils.isEmpty(userList)) {
            return ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), ScreeningOrganizationStaffExportVo.class);
        }

        // 获取完整的用户信息
//        List<Integer> ids = userList.stream().map(UserDTO::getId).collect(Collectors.toList());
//        Map<Integer, ScreeningOrganizationStaff> staffMap = screeningOrganizationStaffService.getByIds(ids).stream()
//                .collect(Collectors.toMap(ScreeningOrganizationStaff::getUserId, Function.identity()));
        // 构建数据
        List<ScreeningOrganizationStaffExportVo> exportList = userList.stream()
                .map(item -> new ScreeningOrganizationStaffExportVo()
                        .setId(item.getId())
                        .setName(item.getRealName())
                        .setGender(GenderEnum.getName(item.getGender()))
                        .setPhone(item.getPhone())
                        .setIdCard(item.getIdCard())
                        .setOrganization(orgName)).collect(Collectors.toList());
        log.info("导出文件: {}", fileName);
        return ExcelUtil.exportListToExcel(fileName, exportList, ScreeningOrganizationStaffExportVo.class);
    }

    /**
     * 生成医院Excel
     *
     * @param districtId 地区id
     **/
    public File generateHospital(Integer districtId) throws IOException {
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

        List<HospitalExportVo> exportList = new ArrayList<>();

        HospitalQuery query = new HospitalQuery();
        query.setDistrictId(districtId);
        List<Hospital> list = hospitalService.getBy(query);

        if (CollectionUtils.isEmpty(list)) {
            return ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), HospitalExportVo.class);
        }

        // 创建人姓名
        Set<Integer> createUserIds = list.stream().map(Hospital::getCreateUserId).collect(Collectors.toSet());
        Map<Integer, UserDTO> userMap = userService.getUserMapByIds(createUserIds);

        for (Hospital item : list) {
            HospitalExportVo exportVo = new HospitalExportVo()
                    .setId(item.getId())
                    .setName(item.getName())
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setLevel(HospitalLevelEnum.getLevel(item.getLevel()))
                    .setType(HospitalEnum.getTypeName(item.getType()))
                    .setKind(HospitalEnum.getKindName(item.getKind()))
                    .setRemark(item.getRemark())
                    .setAccountNo(item.getName())
                    .setAddress(item.getAddress())
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME));
            if (null != item.getProvinceCode()) {
                exportVo.setProvince(districtService.getDistrictName(item.getProvinceCode()));
            }
            if (null != item.getCityCode()) {
                exportVo.setCity(districtService.getDistrictName(item.getCityCode()));
            }
            if (null != item.getAreaCode()) {
                exportVo.setArea(districtService.getDistrictName(item.getAreaCode()));
            }
            if (null != item.getTownCode()) {
                exportVo.setTown(districtService.getDistrictName(item.getTownCode()));
            }
            exportList.add(exportVo);
        }
        return ExcelUtil.exportListToExcel(fileName, exportList, HospitalExportVo.class);
    }

    /**
     * 生成学校Excel
     *
     * @param districtId 地区id
     **/
    public File generateSchool(Integer districtId) throws IOException {
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

        SchoolQuery query = new SchoolQuery();
        query.setDistrictId(districtId);
        List<School> list = schoolService.getBy(query);

        if (CollectionUtils.isEmpty(list)) {
            return ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), SchoolExportVo.class);
        }

        List<Integer> schoolIds = list.stream().map(School::getId).collect(Collectors.toList());
        Set<Integer> createUserIds = list.stream().map(School::getCreateUserId).collect(Collectors.toSet());

        // 创建人姓名
        Map<Integer, UserDTO> userMap = userService.getUserMapByIds(createUserIds);

        // 学生统计
        // TODO: 优化查询，传入学校编号，有空再弄（留给有缘人）
        List<StudentCountVO> studentCountVOS = studentService.countStudentBySchoolNo();
        Map<String, Integer> studentCountMaps = studentCountVOS.stream()
                .collect(Collectors.toMap(StudentCountVO::getSchoolNo, StudentCountVO::getCount));

        // 年级统计
        List<SchoolGradeExportVO> grades = schoolGradeService.getBySchoolIds(schoolIds);
        List<Integer> gradeIds = grades.stream().map(SchoolGradeExportVO::getId).collect(Collectors.toList());

        // 班级统计
        List<SchoolClassExportVO> classes = schoolClassService.getByGradeIds(gradeIds);
        // 通过班级id分组
        Map<Integer, List<SchoolClassExportVO>> classMaps = classes.stream().collect(Collectors.groupingBy(SchoolClassExportVO::getGradeId));
        // 年级设置班级
        grades.forEach(g -> g.setChild(classMaps.get(g.getId())));

        // 年级通过学校ID分组
        Map<Integer, List<SchoolGradeExportVO>> gradeMaps = grades.stream().collect(Collectors.groupingBy(SchoolGradeExportVO::getSchoolId));

        List<SchoolExportVo> exportList = new ArrayList<>();
        for (School item : list) {
            SchoolExportVo exportVo = new SchoolExportVo()
                    .setNo(item.getSchoolNo())
                    .setName(item.getName())
                    .setKind(SchoolEnum.getKindName(item.getKind()))
                    .setType(SchoolEnum.getTypeName(item.getType()))
                    .setStudentCount(studentCountMaps.getOrDefault(item.getSchoolNo(), 0))
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setAddress(item.getAddress())
                    .setRemark(item.getRemark())
                    .setScreeningCount(886)
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME));

            // TODO: 留给有缘人
            StringBuilder result = new StringBuilder();
            List<SchoolGradeExportVO> exportGrade = gradeMaps.get(item.getId());
            if (!CollectionUtils.isEmpty(exportGrade)) {
                for (SchoolGradeExportVO g : exportGrade) {
                    result.append(g.getName()).append(": ");
                    if (!CollectionUtils.isEmpty(g.getChild())) {
                        for (int i = 0; i < g.getChild().size(); i++) {
                            result.append(g.getChild().get(i).getName());
                            if (i < g.getChild().size() - 1) {
                                result.append("、");
                            } else {
                                result.append("。");
                            }
                        }
                    }
                }
                exportVo.setClassName(result.toString());
            }
            if (null != item.getLodgeStatus()) {
                exportVo.setLodgeStatus(SchoolEnum.getLodgeName(item.getLodgeStatus()));
            }
            if (null != item.getProvinceCode()) {
                exportVo.setProvince(districtService.getDistrictName(item.getProvinceCode()));
            }
            if (null != item.getCityCode()) {
                exportVo.setCity(districtService.getDistrictName(item.getCityCode()));
            }
            if (null != item.getAreaCode()) {
                exportVo.setArea(districtService.getDistrictName(item.getAreaCode()));
            }
            if (null != item.getTownCode()) {
                exportVo.setTown(districtService.getDistrictName(item.getTownCode()));
            }
            exportList.add(exportVo);
        }
        log.info("导出文件: {}", fileName);
        return ExcelUtil.exportListToExcel(fileName, exportList, SchoolExportVo.class);
    }

    /**
     * 生成学生Excel
     *
     * @param schoolId 学校id
     * @param gradeId  年级id
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
        School school = schoolService.getBySchoolId(schoolId);
        String schoolName = school.getName();
        String gradeName = schoolGradeService.getById(gradeId).getName();
        builder.append("-").append(schoolName);
        builder.append("-").append(gradeName);
        String fileName = builder.toString();

        // 查询学生
        List<Student> list = studentService.getBySchoolIdAndGradeIdAndClassId(schoolId, null, gradeId);

        // 为空直接导出
        if (CollectionUtils.isEmpty(list)) {
            return ExcelUtil.exportListToExcel(fileName, new ArrayList<>(), StudentExportVo.class);
        }
        // 获取年级班级信息
        List<Integer> classIdList = list.stream().map(Student::getClassId).collect(Collectors.toList());
        Map<Integer, SchoolClass> classMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(classIdList)) {
            classMap = schoolClassService.getClassMapByIds(classIdList);
        }

        List<StudentExportVo> exportList = new ArrayList<>();
        for (Student item : list) {
            StudentExportVo exportVo = new StudentExportVo()
                    .setId(item.getId())
                    .setNo(item.getSno())
                    .setName(item.getName())
                    .setSchoolNo(school.getSchoolNo())
                    .setGender(GenderEnum.getName(item.getGender()))
                    .setBirthday(DateFormatUtil.format(item.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                    .setNation(NationEnum.getName(item.getNation()))
                    .setSchoolName(schoolName)
                    .setGrade(gradeName)
                    .setIdCard(item.getIdCard())
                    .setBindPhone(item.getMpParentPhone())
                    .setPhone(item.getParentPhone())
                    .setAddress(item.getAddress())
                    .setLabel(item.getVisionLabel())
                    .setSituation(item.getCurrentSituation())
                    .setScreeningCount(886)
                    //TODO 就诊次数
                    .setVisitsCount(886)
                    .setQuestionCount(886)
                    .setLastScreeningTime(null);

            if (null != item.getClassId() && null != classMap.get(item.getClassId())) {
                exportVo.setClassName(classMap.get(item.getClassId()).getName());
            }

            if (null != item.getProvinceCode()) {
                exportVo.setProvince(districtService.getDistrictName(item.getProvinceCode()));
            }
            if (null != item.getCityCode()) {
                exportVo.setCity(districtService.getDistrictName(item.getCityCode()));
            }
            if (null != item.getAreaCode()) {
                exportVo.setArea(districtService.getDistrictName(item.getAreaCode()));
            }
            if (null != item.getTownCode()) {
                exportVo.setTown(districtService.getDistrictName(item.getTownCode()));
            }
            exportList.add(exportVo);
        }
        return ExcelUtil.exportListToExcel(fileName, exportList, StudentExportVo.class);
    }


    /**
     * 导入学生
     *
     * @param createUserId  创建人userID
     * @param multipartFile 导入文件
     * @throws BusinessException 异常
     */
    public void importStudent(Integer createUserId, MultipartFile multipartFile) throws IOException, ParseException {
        String fileName = IOUtils.getTempPath() + multipartFile.getName() + "_" + System.currentTimeMillis() + ".xlsx";
        File file = new File(fileName);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        // 这里 也可以不指定class，返回一个list，然后读取第一个sheet 同步读取会自动finish
        List<Map<Integer, String>> listMap = EasyExcel.read(fileName).sheet().doReadSync();
        if (listMap.size() != 0) {
            // 去头部
            listMap.remove(0);
            listMap.remove(1);
        }
        // 收集学校编号
        List<String> schoolNos = listMap.stream().map(s -> s.get(4)).collect(Collectors.toList());
        List<School> schools = schoolService.getBySchoolNos(schoolNos);
        if (CollectionUtils.isEmpty(schools)) {
            throw new BusinessException("学校编号异常");
        }

        List<String> idCards = listMap.stream().map(s -> s.get(8)).collect(Collectors.toList());
        if (studentService.checkIdCards(idCards)) {
            throw new BusinessException("学生身份证号码重复");
        }

        // 收集年级信息
        List<SchoolGradeExportVO> grades = schoolGradeService.getBySchoolIds(schools.stream()
                .map(School::getId).collect(Collectors.toList()));
        List<Integer> gradeIds = grades.stream().map(SchoolGradeExportVO::getId)
                .collect(Collectors.toList());
        // 班级统计
        List<SchoolClassExportVO> classes = schoolClassService.getByGradeIds(gradeIds);
        // 通过班级id分组
        Map<Integer, List<SchoolClassExportVO>> classMaps = classes.stream().collect(Collectors.groupingBy(SchoolClassExportVO::getGradeId));
        // 年级设置班级
        grades.forEach(g -> g.setChild(classMaps.get(g.getId())));

        // 通过学校编号分组
        Map<String, List<SchoolGradeExportVO>> schoolGradeMaps = grades.stream()
                .collect(Collectors.groupingBy(SchoolGradeExportVO::getSchoolNo));

        List<Student> importList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            Student student = new Student();
            if (StringUtils.isBlank(item.get(0))) {
                break;
            }

            if (StringUtils.isBlank(item.get(1)) || GenderEnum.getType(item.get(1)).equals(0)) {
                throw new BusinessException("学生性别异常");
            }

            if (StringUtils.isBlank(item.get(2))) {
                throw new BusinessException("学生出生日期不能为空");
            }

            if (StringUtils.isBlank(item.get(4))) {
                throw new BusinessException("学校编号不能为空");
            }

            if (StringUtils.isBlank(item.get(5))) {
                throw new BusinessException("学生年级不能为空");
            }

            if (StringUtils.isBlank(item.get(6))) {
                throw new BusinessException("学生班级不能为空");
            }

            if (StringUtils.isBlank(item.get(7))) {
                throw new BusinessException("学生学号异常");
            }

            if (StringUtils.isBlank(item.get(8)) || !Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(8))) {
                throw new BusinessException("学生身份证异常");
            }

            if (StringUtils.isBlank(item.get(9))) {
                if (!Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(9))) {
                    throw new BusinessException("学生手机号码异常");
                }
            }

            // excel 格式： 姓名	性别	出生日期	民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  ) 年级	班级	学号	身份证号	手机号码	省	市	县区	镇/街道	详细
            student.setName(item.get(0))
                    .setGender(GenderEnum.getType(item.get(1)))
                    .setBirthday(DateFormatUtil.parseDate(item.get(2), DateFormatUtil.FORMAT_ONLY_DATE2))
                    .setNation(NationEnum.getCode(item.get(3)))
                    .setSchoolNo(item.get(4))
                    .setGradeType(GradeCodeEnum.getByName(item.get(5)).getType())
                    .setSno((item.get(7)))
                    .setIdCard(item.get(8))
                    .setParentPhone(item.get(9))
                    .setProvinceCode(districtService.getCodeByName(item.get(10)))
                    .setCityCode(districtService.getCodeByName(item.get(11)))
                    .setAreaCode(districtService.getCodeByName(item.get(12)))
                    .setTownCode(districtService.getCodeByName(item.get(13)))
                    .setAddress(item.get(14))
                    .setCreateUserId(createUserId);

            // 通过学校编号获取改学校的年级信息
            List<SchoolGradeExportVO> schoolGradeExportVOS = schoolGradeMaps.get(item.get(4));

            // 转换成年级Maps，年级名称作为Key
            Map<String, SchoolGradeExportVO> gradeMaps = schoolGradeExportVOS.stream()
                    .collect(Collectors.toMap(SchoolGradeExportVO::getName, Function.identity()));

            // 年级信息
            SchoolGradeExportVO schoolGradeExportVO = gradeMaps.get(item.get(5));
            if (null == schoolGradeExportVO) {
                throw new BusinessException("年级数据异常");
            } else {
                // 设置年级ID
                student.setGradeId(schoolGradeExportVO.getId());

                // 获取年级内的班级信息
                List<SchoolClassExportVO> classExportVOS = schoolGradeExportVO.getChild();

                // 转换成班级Maps 把班级名称作为key
                Map<String, Integer> classExportMaps = classExportVOS.stream()
                        .collect(Collectors.toMap(SchoolClassExportVO::getName, SchoolClassExportVO::getId));
                Integer classId = classExportMaps.get(item.get(6));
                if (null == classId) {
                    throw new BusinessException("班级数据异常");
                } else {
                    // 设置班级信息
                    student.setClassId(classId);
                }
            }
            importList.add(student);
        }
        studentService.saveBatch(importList);
    }


    /**
     * 导入机构人员
     *
     * @param currentUser    当前登录用户
     * @param multipartFile  导入文件
     * @param screeningOrgId 筛查机构id
     * @throws BusinessException io异常
     */
    public void importScreeningOrganizationStaff(CurrentUser currentUser, MultipartFile multipartFile,
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

        // 收集身份证号码
        List<String> idCards = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());

        // 收集手机号码
        List<String> phones = listMap.stream().map(s -> s.get(3)).collect(Collectors.toList());


        List<UserDTO> checkPhones = oauthService.getUserBatchByPhones(phones, SystemCode.SCREENING_CLIENT.getCode());
        if (!CollectionUtils.isEmpty(checkPhones)) {
            throw new BusinessException("手机号码重复");
        }

        // excel格式：序号	姓名	性别	身份证号	手机号码	说明
        List<UserDTO> userList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            if (StringUtils.isBlank(item.get(0))) {
                break;
            }
            if (StringUtils.isBlank(item.get(1)) || GenderEnum.getType(item.get(1)).equals(0)) {
                throw new BusinessException("性别异常");
            }

            if (StringUtils.isBlank(item.get(2)) || !Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(2))) {
                throw new BusinessException("身份证异常");
            }

            if (StringUtils.isBlank(item.get(3)) || !Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(3))) {
                throw new BusinessException("手机号码异常");
            }
            UserDTO userDTO = new UserDTO()
                    .setRealName(item.get(0))
                    .setGender(GenderEnum.getType(item.get(1)))
                    .setIdCard(item.get(2))
                    .setPhone(item.get(3))
                    .setCreateUserId(currentUser.getId())
                    .setIsLeader(0)
                    .setOrgId(screeningOrgId)
                    .setSystemCode(SystemCode.SCREENING_CLIENT.getCode());
            if (null != item.get(4)) {
                userDTO.setRemark(item.get(4));
            }
            userList.add(userDTO);
        }
        List<ScreeningOrganizationStaffVo> importList = userList.stream().map(item -> {
            ScreeningOrganizationStaffVo staff = new ScreeningOrganizationStaffVo();
            staff.setIdCard(item.getIdCard())
                    .setScreeningOrgId(item.getOrgId())
                    .setCreateUserId(item.getCreateUserId())
                    .setRemark(item.getRemark())
                    .setGovDeptId(currentUser.getOrgId());
            return staff;
        }).collect(Collectors.toList());

        // 批量新增OAuth2
        List<UserDTO> userDTOS = oauthService.addScreeningUserBatch(userList);
        Map<String, Integer> userMaps = userDTOS.stream()
                .collect(Collectors.toMap(UserDTO::getIdCard, UserDTO::getId));
        // 设置userId
        importList.forEach(i -> i.setUserId(userMaps.get(i.getIdCard())));
        screeningOrganizationStaffService.saveBatch(importList);
    }

    /**
     * 获取学生的导入模版
     */
    public File getStudentImportDemo() throws IOException {
        ClassPathResource resource = new ClassPathResource("excel" + File.separator + "导入学生.xlsx");
        // 获取文件
        return resource.getFile();
    }

    /**
     * 获取筛查机构人员的导入模版
     */
    public File getScreeningOrganizationStaffImportDemo() throws IOException {
        ClassPathResource resource = new ClassPathResource("excel" + File.separator + "导入筛查人员.xlsx");
        // 获取文件
        return resource.getFile();
    }
}
