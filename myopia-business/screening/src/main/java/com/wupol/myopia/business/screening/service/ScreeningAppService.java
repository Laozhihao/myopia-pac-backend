package com.wupol.myopia.business.screening.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.myopia.common.exceptions.ManagementUncheckedException;
import com.myopia.common.utils.JsonUtil;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.management.config.UploadConfig;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.ImportExcelEnum;
import com.wupol.myopia.business.management.constant.NationEnum;
import com.wupol.myopia.business.management.constant.RescreeningStatisticEnum;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.service.SchoolClassService;
import com.wupol.myopia.business.management.service.SchoolGradeService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.domain.vo.StudentInfoVO;
import com.wupol.myopia.business.management.service.*;
import com.wupol.myopia.business.management.util.S3Utils;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.management.util.UploadUtil;
import com.wupol.myopia.business.screening.domain.dto.AppStudentDTO;
import com.wupol.myopia.business.screening.domain.dto.AppUserInfo;
import com.wupol.myopia.business.screening.domain.vo.RescreeningResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查端App接口
 *
 * @Author Chikong
 * @Date 2021-01-21
 */
@Service
public class ScreeningAppService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentScreeningRawDataService studentScreeningRawDataService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private UploadConfig uploadConfig;
    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

    /**
     * 查询学校的年级名称
     *
     * @param schoolName     学校名
     * @param screeningOrgId 机构id
     * @return
     */
    public List<String> getGradeNameBySchoolName(String schoolName, Integer screeningOrgId) {
        return schoolGradeService.getBySchoolName(schoolName, screeningOrgId).stream().map(SchoolGrade::getName).collect(Collectors.toList());
    }


    /**
     * 获取学校年级的班级名称
     *
     * @param schoolName     学校名称
     * @param gradeName      年级名称
     * @param screeningOrgId 机构id
     * @return
     */
    public List<String> getClassNameBySchoolNameAndGradeName(String schoolName, String gradeName, Integer screeningOrgId) {
        return schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName, screeningOrgId).stream()
                .map(SchoolClass::getName).collect(Collectors.toList());
    }

    /**
     * 获取学校年级班级对应的学生名称
     *
     * @param schoolId       学校id, 仅复测时有
     * @param schoolName     学校名称
     * @param gradeName      年级名称
     * @param clazzName      班级名称
     * @param studentName    学生名称
     * @param screeningOrgId 机构id
     * @param isReview       是否复测
     * @return
     */
    public IPage<Student> getStudentBySchoolNameAndGradeNameAndClassName(PageRequest pageRequest, Integer schoolId, String schoolName, String gradeName, String clazzName, String studentName, Integer screeningOrgId, Boolean isReview) {
        //TODO 管理端，待修改
        //TODO 待增加复测的逻辑
        List<SchoolClass> classList = schoolClassService.getBySchoolNameAndGradeName(schoolName, gradeName, screeningOrgId);
        for (SchoolClass item : classList) {
            if (item.getName().equals(clazzName)) { // 匹配对应的班级
                //TODO 增加学生名过滤
                StudentQuery query = new StudentQuery();
                query.setClassId(item.getId()).setGradeId(item.getGradeId());
                return studentService.getByPage(pageRequest.toPage(), query);
            }
        }
        return (IPage<Student>) Collections.emptyList();

    }


    /**
     * 随机获取学生复测信息
     *
     * @param pageRequest    分页
     * @param screeningOrgId 机构id
     * @param schoolId       学校id
     * @param schoolName     学校名称
     * @param gradeName      年级名称
     * @param clazzName      班级名称
     * @return
     */
    public IPage<Student> getStudentReviewWithRandom(PageRequest pageRequest, Integer schoolId, String schoolName, String gradeName, String clazzName, Integer screeningOrgId) {
        //TODO 管理端，待修改
        //TODO 待做随机
        return getStudentBySchoolNameAndGradeNameAndClassName(pageRequest, schoolId, schoolName, gradeName, clazzName, "", screeningOrgId, true);
    }

    /**
     * 保存学生眼镜信息
     *
     * @param screeningResultBasicData
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateStudentScreenData(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.saveOrUpdateStudentScreenData(screeningResultBasicData);
        statConclusionService.saveOrUpdateStudentScreenData(visionScreeningResult);
    }

    /*
     *//**
     * 创建记录
     *
     * @param visionDataDTO
     * @param screeningPlan
     *//*
    private void createScreeningResultAndSave(VisionDataDTO visionDataDTO, ScreeningPlan screeningPlan) {
        VisionScreeningResult visionScreeningResult = new VisionScreeningResult()
                .setCreateTime(new Date())
                .setPlanId(screeningPlan.getId())
                .setTaskId(screeningPlan.getScreeningTaskId())
                .setDistrictId(screeningPlan.getDistrictId())
                .setIsDoubleScreen(false)
                .setStudentId(visionDataDTO.getStudentId())
                .setSchoolId(visionDataDTO.getSchoolId())
                .setGlassesType(WearingGlassesSituation.getKey(visionDataDTO.getGlassesType()).get())
                .setRightNakedVision(visionDataDTO.getRightNakedVision())
                .setLeftNakedVision(visionDataDTO.getLeftNakedVision())
                .setRightCorrectedVision(visionDataDTO.getRightCorrectedVision())
                .setLeftCorrectedVision(visionDataDTO.getLeftCorrectedVision());
        boolean isSaveSuccess = screeningResultService.save(visionScreeningResult);
        if (!isSaveSuccess) {
            throw new ManagementUncheckedException("screeningResultService.save失败，screeningResult =  " + JsonUtil.objectToJsonString(visionScreeningResult));
        }
    }*/

    /**
     * 保存原始数据
     *
     * @param visionDataDTO
     */
    private void saveRawScreeningData(VisionDataDTO visionDataDTO) {
        StudentScreeningRawData studentScreeningRawData = new StudentScreeningRawData();
        studentScreeningRawData.setScreeningRawData(JsonUtil.objectToJsonString(visionDataDTO));
        //studentScreeningRawData.setScreeningPlanSchoolStudentId(visionDataDTO);
        studentScreeningRawData.setCreateTime(new Date());
        studentScreeningRawDataService.save(studentScreeningRawData);
    }


    /**
     * 获取筛查就机构对应的学校
     *
     * @param screeningOrgId 机构id
     * @return
     */
    public List<School> getSchoolByScreeningOrgId(Integer screeningOrgId) {
        List<Long> schoolIds = screeningPlanService.getScreeningSchoolIdByScreeningOrgId(screeningOrgId);
        return schoolService.getSchoolByIds(schoolIds);
    }

    /**
     * 获取学生
     *
     * @param id 学生id
     * @return
     */
    public Student getStudentById(Integer id) {
        return studentService.getById(id);
    }


    /**
     * 查询学生录入的最新一条数据(慢性病)
     *
     * @param studentId      学生id
     * @param screeningOrgId 机构id
     * @return
     */
    public List<Object> getStudentChronicNewByStudentId(Integer studentId, Integer screeningOrgId) {
        //TODO 筛查端，待修改
        return Collections.emptyList();
    }


    /**
     * 更新复测质控结果
     *
     * @return
     */
    public Boolean updateReviewResult(Integer eyeId) {
        //TODO 筛查端，待修改
        return true;
    }

    /**
     * 上传筛查机构用户的签名图片
     *
     * @param deptId      筛查机构id
     * @param currentUser 用户id
     * @param file        签名
     * @return
     */
    public String uploadSignPic(CurrentUser currentUser, MultipartFile file) {
        ResourceFile resourceFile;
        try {
            String savePath = uploadConfig.getSavePath();
            TwoTuple<String, String> uploadToServerResults = UploadUtil.upload(file, savePath);
            String tempPath = uploadToServerResults.getSecond();
            // 判断上传的文件是否图片或者PDF
            String allowExtension = uploadConfig.getSuffixs();
            UploadUtil.validateFileIsAllowed(file, allowExtension.split(","));
            // 上传
            resourceFile = s3Utils.uploadS3AndGetResourceFile(tempPath, UploadUtil.genNewFileName(file));
            // 增加到筛查用户中
            screeningOrganizationStaffService.updateOrganizationStaffSignId(currentUser, resourceFile);
            return   resourceFileService.getResourcePath(resourceFile.getId());
        } catch (Exception e) {
            throw new BusinessException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }


    /**
     * 人脸识别
     *
     * @return
     */
    public Object recognitionFace(Integer deptId, MultipartFile file) {
        //TODO
        return new Object();
    }

    /**
     * 获取复测质控结果
     *
     * @return
     */
    public List<RescreeningResultVO> getAllReviewResult(ScreeningResultSearchDTO screeningResultDTO) {
        //拿到班级信息或者学生信息之后，进行查询数据
        List<StudentScreeningInfoWithResultDTO> studentInfoWithResult = screeningPlanSchoolStudentService.getStudentInfoWithResult(screeningResultDTO);
        //先分组
        Map<String, List<StudentScreeningInfoWithResultDTO>> stringListMap = this.groupByKey(screeningResultDTO.getStatisticType(), studentInfoWithResult);
        //进行统计
        Set<String> schoolIdSet = stringListMap.keySet();
        List<RescreeningResultVO> rescreeningResultVOS = schoolIdSet.stream().map(keyId ->
                RescreeningResultVO.getRescreeningResult(stringListMap.get(keyId))
        ).collect(Collectors.toList());
        return rescreeningResultVOS;
    }

    public Map<String, List<StudentScreeningInfoWithResultDTO>> groupByKey(RescreeningStatisticEnum statisticType, List<StudentScreeningInfoWithResultDTO> studentInfoWithResult) {
        return studentInfoWithResult.stream().collect(Collectors.groupingBy(e -> e.getGroupKey(statisticType)));
    }

    /**
     * 设置其他数据
     *
     * @param rescreeningResult
     * @param studentClazzDTO
     */
    private void setOtherInfo(List<StudentInfoVO> rescreeningResult, StudentClazzDTO studentClazzDTO) {
        rescreeningResult.stream().forEach(studentInfoVO -> {
            studentInfoVO.addOtherInfo(studentClazzDTO);
        });
    }

    /**
     * 获取学生
     *
     * @param currentUser
     * @param appStudentDTO
     * @return
     */
    public Student getStudent(CurrentUser currentUser, AppStudentDTO appStudentDTO) throws ParseException {
        Student student = new Student();
        Long schoolId = appStudentDTO.getSchoolId();
        School school = schoolService.getById(schoolId);
        if (school == null) {
            throw new ManagementUncheckedException("无法找到该schoolId = " + schoolId);
        }
        SchoolGrade schoolGrade = schoolGradeService.getByGradeNameAndSchoolId(schoolId.intValue(), appStudentDTO.getGrade());
        if (schoolGrade == null) {
            throw new ManagementUncheckedException("无法找到该grade = " + appStudentDTO.getGrade());
        }
        SchoolClass schoolClass = schoolClassService.getByClassNameAndSchoolId(schoolId.intValue(), appStudentDTO.getClazz());
        if (schoolClass == null) {
            throw new ManagementUncheckedException("无法找到该class = " + appStudentDTO.getClazz());
        }
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        student.setName(appStudentDTO.getStudentName())
                .setGender(StringUtils.isBlank(appStudentDTO.getGrade()) ? null : GenderEnum.getType(appStudentDTO.getGrade()))
                .setBirthday(StringUtils.isBlank(appStudentDTO.getBirthday()) ? null : DateFormatUtil.parseDate(appStudentDTO.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE2))
                .setNation(StringUtils.isBlank(appStudentDTO.getClan()) ? null : NationEnum.getCode(appStudentDTO.getClan()))
                .setSchoolNo(school.getSchoolNo())
                .setGradeId(schoolGrade.getId())
                .setClassId(schoolClass.getId())
                .setSno(appStudentDTO.getStudentNo())
                .setIdCard(appStudentDTO.getIdCard())
                .setCreateUserId(currentUser.getId())
                .setAddress(appStudentDTO.getAddress());

        String provinceName = appStudentDTO.getProvince();
        String cityName = appStudentDTO.getCity();
        String areaName = appStudentDTO.getRegion();
        String townName = appStudentDTO.getStreet();//地区
        appStudentDTO.getAddress();//籍贯
        districtService.getCodeByName(provinceName, cityName, areaName, townName);
        //todo 地区和其他先不保存 student.setProvinceCode().setCityCode().setTownCode()
        return student;

    }

    /**
     * 获取用户的详细信息
     * @param currentUser
     * @return
     */
    public AppUserInfo getUserInfoByUser(CurrentUser currentUser) throws IOException {
        ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(currentUser.getOrgId());
        AppUserInfo appUserInfo = new AppUserInfo();
        appUserInfo.setUsername(currentUser.getUsername());
        appUserInfo.setUserId(currentUser.getId());
        appUserInfo.setDeptName(screeningOrganization.getName());
        appUserInfo.setDeptId(screeningOrganization.getId());
        ScreeningOrganizationStaff screeningOrganizationStaff = screeningOrganizationStaffService.findOne(new ScreeningOrganizationStaff().setUserId(currentUser.getId()));
        if (screeningOrganizationStaff == null) {
            throw new BusinessException("无法找到该员工");
        }
        String resourcePath = resourceFileService.getResourcePath(screeningOrganizationStaff.getSignFileId());
        appUserInfo.setAutImage(resourcePath);
        return appUserInfo;
    }
}