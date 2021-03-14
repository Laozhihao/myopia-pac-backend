package com.wupol.myopia.business.screening.service;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.common.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.JsonUtil;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.management.config.UploadConfig;
import com.wupol.myopia.business.management.constant.GenderEnum;
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
import com.wupol.myopia.business.screening.others.SysStudent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
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
     * @param schoolId       学校id
     * @param gradeName      年级名称
     * @param clazzName      班级名称
     * @param screeningOrgId 机构id
     * @return
     */
    public List<SysStudent> getStudentReview(Integer schoolId, String gradeName, String clazzName, Integer screeningOrgId, String studentName, Integer page, Integer size) {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(screeningOrgId);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        // 获取学生数据
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        screeningPlanSchoolStudent.setScreeningOrgId(screeningOrgId).setSchoolId(schoolId).setClassName(clazzName).setGradeName(gradeName);
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentName)) {
            screeningPlanSchoolStudentLambdaQueryWrapper.like(ScreeningPlanSchoolStudent::getStudentName, studentName);
        }
        screeningPlanSchoolStudentLambdaQueryWrapper.setEntity(screeningPlanSchoolStudent).in(ScreeningPlanSchoolStudent::getScreeningPlanId,currentPlanIds);
        Integer startIntem = (page - 1) * size;
        screeningPlanSchoolStudentLambdaQueryWrapper.last("limit " + startIntem + "," + size);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getBaseMapper().selectList(screeningPlanSchoolStudentLambdaQueryWrapper);
        Set<Integer> screeningPlanStudentIds = screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudents)) {
            return new ArrayList<>();
        }
        //查找统计数据
        LambdaQueryWrapper<StatConclusion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(StatConclusion::getScreeningPlanSchoolStudentId, screeningPlanStudentIds);
        List<StatConclusion> allStatConclusions = statConclusionService.getBaseMapper().selectList(queryWrapper);
        // 对数据进行分类
        Map<Integer, List<StatConclusion>> idStatConclusionListMap = allStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getScreeningPlanSchoolStudentId));
        Set<Integer> ids = idStatConclusionListMap.keySet();
        // 学生数据对应的状态
        Map<Integer, Integer> screeningStudentIdStatusMap = ids.stream().collect(Collectors.toMap(Function.identity(), id -> {
            List<StatConclusion> statConclusionList = idStatConclusionListMap.get(id);
            return this.getRescreeningStatus(statConclusionList);
        }));
        return this.getSysStudentData(screeningStudentIdStatusMap, screeningPlanSchoolStudents);
    }

    /**
     * 获取数据
     *
     * @param screeningStudentIdStatusMap
     * @param screeningPlanSchoolStudents
     * @return
     */
    private List<SysStudent> getSysStudentData(Map<Integer, Integer> screeningStudentIdStatusMap, List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents) {
        return screeningPlanSchoolStudents.stream().map(screeningPlanSchoolStudent ->
                SysStudent.getInstance(screeningPlanSchoolStudent, screeningStudentIdStatusMap.get(screeningPlanSchoolStudent.getId()))
        ).collect(Collectors.toList());
    }

    /**
     * todo 每次随机6%
     * 缺少： 1.同步功能 2.累计同步的功能
     *
     * @param sysStudents
     * @return
     */
    public List<SysStudent> getRandomData(List<SysStudent> sysStudents) {
        int result = (int) (sysStudents.size() * 0.06) + 1;
        if (result > sysStudents.size()) {
            return new ArrayList<>();
        }
        // 随机打乱顺序
        Collections.shuffle(sysStudents);
        sysStudents = sysStudents.subList(0, result);
        return sysStudents;
    }

    /**
     * 获取复筛质控
     *
     * @param statConclusionList
     * @return
     */
    private Integer getRescreeningStatus(List<StatConclusion> statConclusionList) {
        StatConclusion reScreeningStatConclusion = null;
        StatConclusion firstScreeningStatConclusion = null;

        for (StatConclusion statConclusion : statConclusionList) {
            if (statConclusion.getIsRescreen()) {
                reScreeningStatConclusion = statConclusion;
            } else {
                firstScreeningStatConclusion = statConclusion;
            }
        }
        // 3补充数据 4 复测完成 2 补充数据 0 不可复测  1 可复测
        if (firstScreeningStatConclusion == null || !firstScreeningStatConclusion.getIsValid()) {
            return 0;// 不可复测
        }

        Integer resultId = firstScreeningStatConclusion.getResultId();
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(resultId);
        boolean isWearing = visionScreeningResult.getVisionData().getLeftEyeData().getGlassesType().equals(WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY);

        if (isWearing) {
            return 0;
        }
        if (reScreeningStatConclusion == null) {
            return 1;// 确认复测
        }

        if (reScreeningStatConclusion.getIsValid()) {
            return 4;// 完成复测
        }
        return 2;

    }

    /**
     * 保存学生眼镜信息
     *
     * @param screeningResultBasicData
     * @return
     */

    public void saveOrUpdateStudentScreenData(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult = visionScreeningResultService.getAllFirstAndSecondResult(screeningResultBasicData);
        VisionScreeningResult currentVisionScreeningResult = allFirstAndSecondResult.getFirst();
        currentVisionScreeningResult = visionScreeningResultService.getScreeningResult(screeningResultBasicData, currentVisionScreeningResult);
        allFirstAndSecondResult.setFirst(currentVisionScreeningResult);
        this.saveAll(allFirstAndSecondResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAll(TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult) {
        visionScreeningResultService.saveOrUpdateStudentScreenData(allFirstAndSecondResult.getFirst());
        statConclusionService.saveOrUpdateStudentScreenData(allFirstAndSecondResult);
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
            return resourceFileService.getResourcePath(resourceFile.getId());
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
     * @param school
     * @return
     */
    public Student getStudent(CurrentUser currentUser, AppStudentDTO appStudentDTO, School school) throws ParseException {
        Student student = new Student();
        Long schoolId = appStudentDTO.getSchoolId();
        SchoolGrade schoolGrade = schoolGradeService.getByGradeNameAndSchoolId(schoolId.intValue(), appStudentDTO.getGrade());
        SchoolClass schoolClass = schoolClassService.getByClassNameAndSchoolId(schoolId.intValue(), schoolGrade.getId(),appStudentDTO.getClazz());
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        student.setName(appStudentDTO.getStudentName())
                .setGender(StringUtils.isBlank(appStudentDTO.getGrade()) ? null : GenderEnum.getType(appStudentDTO.getGrade()))
                .setBirthday(StringUtils.isBlank(appStudentDTO.getBirthday()) ? null : DateFormatUtil.parseDate(appStudentDTO.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                .setNation(StringUtils.isBlank(appStudentDTO.getClan()) ? null : NationEnum.getCode(appStudentDTO.getClan()))
                .setSchoolNo(school.getSchoolNo())
                .setGradeId(schoolGrade.getId())
                .setClassId(schoolClass.getId())
                .setSno(appStudentDTO.getStudentNo())
                .setIdCard(appStudentDTO.getIdCard())
                .setCreateUserId(currentUser.getId())
                .setAddress(appStudentDTO.getAddress())
                .setProvinceCode(school.getProvinceCode())
                .setCityCode(school.getCityCode())
                .setTownCode(school.getTownCode())
                .setStatus(0);
        return student;

    }

    /**
     * 获取用户的详细信息
     *
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