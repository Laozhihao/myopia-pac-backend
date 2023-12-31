package com.wupol.myopia.business.api.screening.app.service;

import cn.hutool.core.util.IdcardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.aggregation.student.constant.VisionScreeningConst;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppStudentDTO;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppUserInfo;
import com.wupol.myopia.business.api.screening.app.domain.dto.SysStudent;
import com.wupol.myopia.business.api.screening.app.domain.vo.*;
import com.wupol.myopia.business.api.screening.app.enums.ErrorEnum;
import com.wupol.myopia.business.api.screening.app.enums.StudentExcelEnum;
import com.wupol.myopia.business.api.screening.app.enums.SysEnum;
import com.wupol.myopia.business.api.screening.app.utils.CommUtil;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningInfoWithResultDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentVO;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
@Log4j2
@Service
public class ScreeningAppService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SchoolStudentService schoolStudentService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private VisionScreeningBizService visionScreeningBizService;

    /**
     * 获取学生复测数据
     *
     * @param schoolId
     * @param gradeName
     * @param clazzName
     * @param screeningOrgId
     * @param studentName
     * @param page
     * @param size
     * @param isRandom
     * @return
     * @throws JsonProcessingException
     */
    public List<SysStudent> getStudentReview(Integer schoolId, String gradeName, String clazzName, Integer screeningOrgId, String studentName, Integer page, Integer size, boolean isRandom, Integer channel) {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentReleasePlanIds(screeningOrgId);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        // 获取学生数据
        SchoolGrade schoolGrade = schoolGradeService.getByGradeNameAndSchoolId(schoolId, gradeName);
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentLambdaQueryWrapper = getScreeningPlanSchoolStudentLambdaQueryWrapper(schoolId, schoolGrade.getId(), schoolClassService.getByClassNameAndSchoolId(schoolId, schoolGrade.getId(), clazzName).getId(), screeningOrgId, studentName, currentPlanIds);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents;
        if (isRandom) {
            screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getBaseMapper().selectList(screeningPlanSchoolStudentLambdaQueryWrapper);
            ScreeningPlan currentPlan = screeningPlanService.getCurrentReleasePlan(screeningOrgId, schoolId, channel);
            String cacheKey = "app:" + screeningOrgId + currentPlan.getId() + schoolId + gradeName + clazzName;
            screeningPlanSchoolStudents = getRandomData(screeningPlanSchoolStudents, cacheKey, currentPlan.getEndTime());
        } else {
            Integer startIntem = (page - 1) * size;
            screeningPlanSchoolStudentLambdaQueryWrapper.last("limit " + startIntem + "," + size);
            screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getBaseMapper().selectList(screeningPlanSchoolStudentLambdaQueryWrapper);
        }
        return getSysStudents(screeningPlanSchoolStudents);
    }

    private LambdaQueryWrapper<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudentLambdaQueryWrapper(Integer schoolId, Integer gradeId, Integer clazzId, Integer screeningOrgId, String studentName, Set<Integer> currentPlanIds) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        screeningPlanSchoolStudent.setScreeningOrgId(screeningOrgId)
                .setSchoolId(schoolId)
                .setClassId(clazzId)
                .setGradeId(gradeId);
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentName)) {
            screeningPlanSchoolStudentLambdaQueryWrapper.like(ScreeningPlanSchoolStudent::getStudentName, studentName);
        }
        screeningPlanSchoolStudentLambdaQueryWrapper.setEntity(screeningPlanSchoolStudent).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds);
        return screeningPlanSchoolStudentLambdaQueryWrapper;
    }

    /**
     * 获取sysStudents
     *
     * @param screeningPlanSchoolStudents
     * @return
     */
    public List<SysStudent> getSysStudents(List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents) {
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
        // 获取年级
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(screeningPlanSchoolStudents, ScreeningPlanSchoolStudent::getGradeId);

        // 获取班级
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(screeningPlanSchoolStudents, ScreeningPlanSchoolStudent::getClassId);
        return this.getSysStudentData(screeningStudentIdStatusMap, screeningPlanSchoolStudents,gradeMap,classMap);
    }

    /**
     * 获取数据
     *
     * @param screeningStudentIdStatusMap
     * @param screeningPlanSchoolStudents
     * @return
     */
    private List<SysStudent> getSysStudentData(Map<Integer, Integer> screeningStudentIdStatusMap, List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents,
                                               Map<Integer, SchoolGrade> gradeMap,Map<Integer, SchoolClass> classMap) {
        return screeningPlanSchoolStudents.stream().map(screeningPlanSchoolStudent ->
                SysStudent.getInstance(screeningPlanSchoolStudent, screeningStudentIdStatusMap.get(screeningPlanSchoolStudent.getId()),gradeMap,classMap)
        ).collect(Collectors.toList());
    }

    /**
     * 随机获取数据
     *
     * @param screeningPlanSchoolStudents
     * @param cacheKey
     * @param endTime
     * @return
     * @throws JsonProcessingException
     */
    public List<ScreeningPlanSchoolStudent> getRandomData(List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents, String cacheKey, Date endTime) {
        //查找上次随机筛选的学生
        List<ScreeningPlanSchoolStudent> cacheList = this.getCacheList(cacheKey);
        // 如果cacheList 是null 说明没有数据,
        if (cacheList == null) {
            cacheList = new ArrayList<>();
        }
        int dataSize = CollectionUtils.size(screeningPlanSchoolStudents);
        int cacheSize = CollectionUtils.size(cacheList);

        int newResultSize = (int) (dataSize * 0.06 - cacheSize);
        //数据长度没有变化
        if (newResultSize <= 0 && cacheSize != 0) {
            return cacheList;
        }
        //初始化数据
        if (newResultSize <= 0) {
            newResultSize = 1;
        }
        Collections.shuffle(screeningPlanSchoolStudents);
        screeningPlanSchoolStudents = screeningPlanSchoolStudents.stream().limit(newResultSize).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(screeningPlanSchoolStudents)) {
            cacheList.addAll(screeningPlanSchoolStudents);
            redisUtil.set(cacheKey, cacheList, endTime.getTime() - System.currentTimeMillis());
        }
        return cacheList;
    }

    /**
     * 获取随机复测数据
     *
     * @param key
     * @return
     * @throws JsonProcessingException
     */
    private List<ScreeningPlanSchoolStudent> getCacheList(String key) {
        return (List<ScreeningPlanSchoolStudent>) redisUtil.get(key);
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
            if (Objects.equals(statConclusion.getIsRescreen(),Boolean.TRUE)) {
                reScreeningStatConclusion = statConclusion;
            } else {
                firstScreeningStatConclusion = statConclusion;
            }
        }
        // 3补充数据 4 复测完成 2 补充数据 0 不可复测  1 可复测
        if (firstScreeningStatConclusion == null || !firstScreeningStatConclusion.getIsValid()) {
            return 0;
        }

        Integer resultId = firstScreeningStatConclusion.getResultId();
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(resultId);
        boolean isWearing = visionScreeningResult.getVisionData().getLeftEyeData().getGlassesType().equals(WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY);
        // 不可复测
        if (isWearing) {
            return 0;
        }
        // 确认复测
        if (reScreeningStatConclusion == null) {
            return 1;
        }

        if (Objects.equals(reScreeningStatConclusion.getIsValid(),Boolean.TRUE)) {
            return 4;// 完成复测
        }
        return 2;

    }

    /**
     * 获取筛查就机构对应的学校
     *
     * @param screeningOrgId 机构id
     * @return
     */
    public List<School> getSchoolByScreeningOrgId(Integer screeningOrgId, Integer channel) {
        List<Integer> schoolIds = screeningPlanService.getReleasePlanSchoolIdByScreeningOrgId(screeningOrgId, channel);
        return schoolService.getSchoolByIds(schoolIds);
    }

    /**
     * 上传筛查机构用户的签名图片
     *
     * @param currentUser
     * @param file
     * @return
     */
    public String uploadSignPic(CurrentUser currentUser, MultipartFile file) {
        try {
            // 上传图片
            ResourceFile resourceFile = resourceFileService.uploadFileAndSave(file);
            // 增加到筛查用户中
            screeningOrganizationStaffService.updateOrganizationStaffSignId(currentUser, resourceFile);
            return resourceFileService.getResourcePath(resourceFile.getId());
        } catch (Exception e) {
            throw new BusinessException(e instanceof BusinessException ? e.getMessage() : "文件上传失败", e);
        }
    }


    /**
     * 获取复测质控结果
     *
     * @return
     */
    public List<RescreeningResultVO> getAllReviewResult(ScreeningResultSearchDTO screeningResultDTO) {
        // 查找班级年级
        SchoolGrade schoolGrade = schoolGradeService.getByGradeNameAndSchoolId(screeningResultDTO.getSchoolId(), screeningResultDTO.getGradeName());
        SchoolClass schoolClass = schoolClassService.getByClassNameAndSchoolId(screeningResultDTO.getSchoolId(), schoolGrade.getId(), screeningResultDTO.getClazzName());
        screeningResultDTO.setGradeId(schoolGrade.getId());
        screeningResultDTO.setClassId(schoolClass.getId());

        //拿到班级信息或者学生信息之后，进行查询数据
        List<StudentScreeningInfoWithResultDTO> studentInfoWithResult = screeningPlanSchoolStudentService.getStudentInfoWithResult(screeningResultDTO);
        //先分组
        Map<String, List<StudentScreeningInfoWithResultDTO>> stringListMap = this.groupByKey(screeningResultDTO.getStatisticType(), studentInfoWithResult);
        //进行统计
        Set<String> schoolIdSet = stringListMap.keySet();
        return schoolIdSet.stream().map(keyId ->
                RescreeningResultVO.getRescreeningResult(stringListMap.get(keyId))
        ).collect(Collectors.toList());
    }

    public Map<String, List<StudentScreeningInfoWithResultDTO>> groupByKey(RescreeningStatisticEnum statisticType, List<StudentScreeningInfoWithResultDTO> studentInfoWithResult) {
        return studentInfoWithResult.stream().collect(Collectors.groupingBy(e -> e.getGroupKey(statisticType)));
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
        SchoolGrade schoolGrade = schoolGradeService.getByGradeNameAndSchoolId(schoolId.intValue(), appStudentDTO.getGrade());
        SchoolClass schoolClass = schoolClassService.getByClassNameAndSchoolId(schoolId.intValue(), schoolGrade.getId(), appStudentDTO.getClazz());
        // excel格式：姓名、性别、出生日期、民族(1：汉族  2：蒙古族  3：藏族  4：壮族  5:回族  6:其他  )、学校编号、年级、班级、学号、身份证号、手机号码、省、市、县区、镇/街道、居住地址
        student.setName(appStudentDTO.getStudentName())
                .setGender(StringUtils.isBlank(appStudentDTO.getGrade()) ? null : GenderEnum.getType(appStudentDTO.getSex()))
                .setBirthday(StringUtils.isBlank(appStudentDTO.getBirthday()) ? null : DateFormatUtil.parseDate(appStudentDTO.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                .setNation(StringUtils.isBlank(appStudentDTO.getClan()) ? null : NationEnum.getCodeByName(appStudentDTO.getClan()))
                .setGradeId(schoolGrade.getId())
                .setClassId(schoolClass.getId())
                .setSno(appStudentDTO.getStudentNo())
                .setIdCard(appStudentDTO.getIdCard())
                .setCreateUserId(currentUser.getId())
                .setParentPhone(appStudentDTO.getStudentPhone())
                .setStatus(0)
                .setPassport(appStudentDTO.getPassport())
                .setSchoolId(schoolId.intValue());
        return student;
    }

    /**
     * 获取用户的详细信息
     *
     * @param currentUser 用户
     *
     * @return AppUserInfo
     */
    public AppUserInfo getUserInfoByUser(CurrentUser currentUser) {
        if (Objects.equals(currentUser.getUserType(), UserType.SCREENING_STAFF_TYPE_ORG.getType())) {
            ScreeningOrganization screeningOrganization = screeningOrganizationService.getById(currentUser.getOrgId());
            AppUserInfo appUserInfo = getAppUserInfo(currentUser.getId(), screeningOrganization.getName(), screeningOrganization.getId());
            ScreeningOrganizationStaff screeningOrganizationStaff = screeningOrganizationStaffService.findOne(new ScreeningOrganizationStaff().setUserId(currentUser.getId()));
            if (screeningOrganizationStaff == null) {
                throw new BusinessException("无法找到该员工");
            }
            String resourcePath = resourceFileService.getResourcePath(screeningOrganizationStaff.getSignFileId());
            appUserInfo.setAutImage(resourcePath);
            return appUserInfo;
        }
        School school = schoolService.getById(currentUser.getOrgId());
        return getAppUserInfo(currentUser.getId(), school.getName(), school.getId());
    }

    /**
     * 获取用户基本信息
     *
     * @param userId   用户Id
     * @param deptName 机构名称
     * @param deptId   机构Id
     *
     * @return AppUserInfo
     */
    private AppUserInfo getAppUserInfo(Integer userId, String deptName, Integer deptId) {
        User user = oauthServiceClient.getUserDetailByUserId(userId);
        AppUserInfo appUserInfo = new AppUserInfo();
        appUserInfo.setUsername(user.getRealName());
        appUserInfo.setUserId(userId);
        appUserInfo.setDeptName(deptName);
        appUserInfo.setDeptId(deptId);
        appUserInfo.setPhone(user.getPhone());
        return appUserInfo;
    }

    /**
     * 校验学生数据的有效性
     *
     * @param appStudentDTO
     * @return
     */
    public ApiResult validStudentParam(AppStudentDTO appStudentDTO) {
        //验证学生生日格式
        if (StringUtils.isNotBlank(appStudentDTO.getBirthday())) {
            String validDate = DateUtil.isValidDate(appStudentDTO.getBirthday());
            if (validDate == null) {
                return ApiResult.failure(ErrorEnum.SYS_STUDENT_BIRTHDAY_FORMAT_ERROR.getCode(), ErrorEnum.SYS_STUDENT_BIRTHDAY_FORMAT_ERROR.getMessage());
            } else {
                appStudentDTO.setBirthday(validDate);
            }
        }
        if (appStudentDTO.getSchoolId() == null || appStudentDTO.getSchoolId() == 0) {
            return ApiResult.failure(ErrorEnum.SYS_STUDENT_SCHOOL_NULL.getCode(), ErrorEnum.SYS_STUDENT_SCHOOL_NULL.getMessage());
        }
        //验证身份号
        if (StringUtils.isNotBlank(appStudentDTO.getIdCard())) {
            boolean flag = IdcardUtil.isValidCard(appStudentDTO.getIdCard());
            if (!flag) {
                return ApiResult.failure(StudentExcelEnum.EXCEL_IDCARD_ERROR.getCode(), StudentExcelEnum.EXCEL_IDCARD_ERROR.getMessage());
            }
        }

        //验证手机号
        if (StringUtils.isNotBlank(appStudentDTO.getStudentPhone())) {
            boolean flag = CommUtil.isMobileNO(appStudentDTO.getStudentPhone());
            if (!flag) {
                //验证是否为电话号
                boolean isPhone = CommUtil.isPhoneNO(appStudentDTO.getStudentPhone());
                if (!isPhone) {
                    return ApiResult.failure(StudentExcelEnum.EXCEL_PHONE_ERROR.getCode(), StudentExcelEnum.EXCEL_PHONE_ERROR.getMessage());
                }
            }
        }
        //设置出生日期
        if (StringUtils.isBlank(appStudentDTO.getBirthday()) && StringUtils.isNotBlank(appStudentDTO.getIdCard())) {
            appStudentDTO.setBirthday(CommUtil.getBirthday(appStudentDTO.getIdCard()));
        }
        return null;
    }

    /**
     * 获取学生对应筛查计划
     *
     * @param schoolId
     * @param gradeId
     * @param classId
     * @param screeningOrgId
     * @param channel
     * @return
     */
    private List<ScreeningPlanSchoolStudent> getReleasePlanStudent(Integer schoolId, Integer gradeId, Integer classId, Integer screeningOrgId, Integer channel) {
        ScreeningPlanSchoolStudent query = new ScreeningPlanSchoolStudent()
                .setScreeningOrgId(screeningOrgId)
                .setSchoolId(schoolId)
                .setClassId(classId)
                .setGradeId(gradeId);
        // 查询班级所有学生
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.listReleasePlanStudentByEntityDescByCreateTime(query, channel);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudentList)) {
            // 空数据降级处理。根据目前需求（仅显示有筛查数据的学校 008-1.2021-08-26），实际不会进到这里。
            return Lists.newArrayList();
        }

        // 获取学生对应筛查数据
        return screeningPlanSchoolStudentList;
    }

    /**
     * 转换为筛查进度
     *
     * @param screeningPlanSchoolStudents screeningPlanSchoolStudents
     * @param haiNanVersion               是否海南版本
     *
     * @return List<StudentScreeningProgressVO>
     */
    private List<StudentScreeningProgressVO> getStudentScreeningProgress(List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents, Map<Integer, VisionScreeningResult> planStudentVisionResultMap, boolean haiNanVersion) {
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toList()));
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(screeningPlanSchoolStudents.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toList()));
        return screeningPlanSchoolStudents.stream().map(planStudent -> {
            VisionScreeningResult screeningResult = planStudentVisionResultMap.get(planStudent.getId());
            StudentVO studentVO = StudentVO.getInstance(planStudent, gradeMap.getOrDefault(planStudent.getGradeId(), new SchoolGrade()), classMap.getOrDefault(planStudent.getClassId(), new SchoolClass()));
            StudentScreeningProgressVO studentProgress = StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO, planStudent);
            studentProgress.setStudentId(planStudent.getId());
            if (Objects.nonNull(screeningResult)) {
                try {
                    if (haiNanVersion) {
                        visionScreeningBizService.verifyHaiNanScreening(screeningResult, SchoolAge.checkKindergarten(planStudent.getGradeType()));
                        studentProgress.setResult(true);
                    } else {
                        visionScreeningBizService.verifyScreening(screeningResult, screeningResult.getScreeningType() == 1);
                        studentProgress.setResult(true);
                    }
                } catch (Exception e) {
                    studentProgress.setResult(false);
                }
                studentProgress.setIsFirst(true);
                return studentProgress;
            }
            studentProgress.setIsFirst(false);
            return studentProgress;
        }).collect(Collectors.toList());
    }

    private ClassScreeningProgress getClassScreeningProgress(List<StudentScreeningProgressVO> studentScreeningProgressList,
                                                             List<VisionScreeningResult> visionScreeningResults,
                                                             List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList) {
        // 异常的排前面
        Map<Boolean, List<StudentScreeningProgressVO>> finishMap = studentScreeningProgressList.stream().collect(Collectors.groupingBy(StudentScreeningProgressVO::getResult));
        List<StudentScreeningProgressVO> progressList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(finishMap.get(false))) {
            progressList.addAll(finishMap.get(false));
        }
        if (CollectionUtils.isNotEmpty(finishMap.get(true))) {
            progressList.addAll(finishMap.get(true));
        }
        // %5计算
        BigDecimal decimal = new BigDecimal(visionScreeningResults.size()).multiply(BigDecimal.valueOf(0.05));
        int needCount;
        if (BigDecimalUtil.isBetweenRight(decimal,"0","0.5")){
            //(0,0.5] 算 1
            needCount = 1;
        }else {
            needCount = decimal.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }

        ClassScreeningProgress classScreeningProgress = new ClassScreeningProgress().setStudentScreeningProgressList(progressList)
                .setPlanCount(CollectionUtils.size(screeningPlanSchoolStudentList))
                .setScreeningCount(CollectionUtils.size(visionScreeningResults))
                .setAbnormalCount((int) studentScreeningProgressList.stream().filter(StudentScreeningProgressVO::getHasAbnormal).count())
                .setUnfinishedCount((int) studentScreeningProgressList.stream().filter(x -> !x.getResult()).count())
                .setSchoolAge(studentScreeningProgressList.isEmpty() ? null : studentScreeningProgressList.get(0).getGradeType())
                .setNeedReScreeningCount(needCount)
                // 统计筛查情况，只要有一条是人造的数据，则整个班级数据标记为人造的
                .setArtificial(screeningPlanSchoolStudentList.stream().anyMatch(x -> Objects.nonNull(x.getArtificial()) && x.getArtificial() == 1))
                .setFinishedCount((int) studentScreeningProgressList.stream().filter(StudentScreeningProgressVO::getResult).count());

        classScreeningProgress.setNormalCount(classScreeningProgress.getScreeningCount() - classScreeningProgress.getAbnormalCount());
        classScreeningProgress.setUnCheckCount(classScreeningProgress.getPlanCount() - classScreeningProgress.getScreeningCount());
        return classScreeningProgress;
    }

    /**
     * 获取班级总的筛查进度：汇总统计+每个学生的进度
     *
     * @param schoolId 学校名称
     * @param gradeId  年级名称
     * @param classId  班级名称
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress
     **/
    public ClassScreeningProgress getClassScreeningProgress(Integer schoolId, Integer gradeId, Integer classId, CurrentUser user, Boolean isFilter, Integer state, Integer channel) {

        Integer orgId = user.getOrgId();
        boolean haiNanVersion;
        if (user.isSchoolScreeningUser()) {
            haiNanVersion = schoolService.isHaiNanVersion(orgId);
        } else {
            haiNanVersion = screeningOrganizationService.isHaiNanVersion(orgId);
        }

        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = getReleasePlanStudent(schoolId, gradeId, classId, orgId, channel);
        if (screeningPlanSchoolStudentList.isEmpty()) {
            return new ClassScreeningProgress().setPlanCount(0).setScreeningCount(0).setAbnormalCount(0).setUnfinishedCount(0).setStudentScreeningProgressList(new ArrayList<>()).setSchoolAge(SchoolAge.PRIMARY.code).setArtificial(false);
        }
        Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentMap.keySet(), state == 1);
        Map<Integer, VisionScreeningResult> planStudentVisionResultMap = visionScreeningResults.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        // 只显示有姓名或有筛查数据的
        if (Boolean.TRUE.equals(isFilter)) {
            Set<Integer> hasNameStudentIds = screeningPlanSchoolStudentList.stream().filter(x -> !x.getStudentName().equals(String.valueOf(x.getScreeningCode()))).map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
            Set<Integer> hasScreeningDataStudentIds = planStudentVisionResultMap.keySet();
            Set<Integer> totalStudentIds = new HashSet<>();
            totalStudentIds.addAll(hasNameStudentIds);
            totalStudentIds.addAll(hasScreeningDataStudentIds);
            screeningPlanSchoolStudentList = totalStudentIds.stream().map(screeningPlanSchoolStudentMap::get).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(screeningPlanSchoolStudentList)) {
                return new ClassScreeningProgress().setPlanCount(0).setScreeningCount(0).setAbnormalCount(0).setUnfinishedCount(0).setStudentScreeningProgressList(new ArrayList<>()).setSchoolAge(SchoolAge.PRIMARY.code).setArtificial(false);
            }
        }
        // 进度转换
        List<StudentScreeningProgressVO> studentScreeningProgressList = getStudentScreeningProgress(screeningPlanSchoolStudentList, planStudentVisionResultMap, haiNanVersion);
        return getClassScreeningProgress(studentScreeningProgressList, visionScreeningResults, screeningPlanSchoolStudentList);
    }

    public ClassScreeningProgress findClassScreeningStudent(Integer schoolId, Integer gradeId, Integer classId, Integer screeningOrgId, Integer channel) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = getReleasePlanStudent(schoolId, gradeId, classId, screeningOrgId, channel);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudentList)) {
            // 空数据降级处理。根据目前需求（仅显示有筛查数据的学校 008-1.2021-08-26），实际不会进到这里。
            return new ClassScreeningProgress();
        }
        // 获取学生对应筛查数据
        Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        List<VisionScreeningResult> first = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentMap.keySet(), false)
                .stream().filter(item -> item.getScreeningType().equals(channel)).collect(Collectors.toList());
        List<VisionScreeningResult> second = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentMap.keySet(), true)
                .stream().filter(item -> item.getScreeningType().equals(channel)).collect(Collectors.toList());
        ClassScreeningProgressState screeningProgressState = new ClassScreeningProgressState();
        screeningProgressState.setPlanCount((CollectionUtils.size(screeningPlanSchoolStudentList)));
        screeningProgressState.setScreeningCount(CollectionUtils.size(first));
        Map<Integer, VisionScreeningResult> firstPlanStudentVisionResultMap = first.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        Map<Integer, VisionScreeningResult> secondPlanStudentVisionResultMap = second.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        // 转换为筛查进度 有数据的学生 和其他条件
        List<StudentScreeningProgressVO> firstProgress = getStudentScreeningProgress(screeningPlanSchoolStudentList, firstPlanStudentVisionResultMap, false)
                .stream().filter(StudentScreeningProgressVO::getIsFirst)
                .filter(item -> {
                    VisionScreeningResult result = firstPlanStudentVisionResultMap.get(item.getStudentId());
                    // 当视力，屈光不配合时不参与复测
                    Integer visionCooperative = Optional.ofNullable(result).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getIsCooperative).orElse(null);
                    Integer optometryCooperative = Optional.ofNullable(result).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getIsCooperative).orElse(null);
                    if (VisionScreeningConst.IS_NOT_COOPERATE.equals(visionCooperative) || VisionScreeningConst.IS_NOT_COOPERATE.equals(optometryCooperative)) {
                        return false;
                    }
                    // 夜戴角膜镜不参与复测
                    Integer leftGlassesType = Optional.ofNullable(result).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
                    Integer rightGlassesType = Optional.ofNullable(result).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
                    if (WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY.equals(leftGlassesType) || WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY.equals(rightGlassesType)) {
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
        // 转换为筛查进度
        List<StudentScreeningProgressVO> secondProgress = getStudentScreeningProgress(screeningPlanSchoolStudentList, secondPlanStudentVisionResultMap, false)
                .stream().filter(item -> secondPlanStudentVisionResultMap.keySet().contains(item.getStudentId())).collect(Collectors.toList());

        firstProgress = numerationStatus(firstProgress, secondProgress);
        firstProgress = firstProgress.stream().sorted(Comparator.comparing(StudentScreeningProgressVO::getScreeningStatus)).collect(Collectors.toList());
        ClassScreeningProgress classScreeningProgress = getClassScreeningProgress(firstProgress, first, screeningPlanSchoolStudentList);
        // finishCount
        long finishCount = firstProgress.stream().filter(item -> item.getScreeningStatus() == 3).count();
        if (finishCount >= classScreeningProgress.getNeedReScreeningCount()) {
            classScreeningProgress.setFinish(true);
        }
        classScreeningProgress.setStudentScreeningProgressList(classScreeningProgress.getStudentScreeningProgressList().stream().sorted(Comparator.comparing(StudentScreeningProgressVO::getScreeningStatus)).collect(Collectors.toList()));
        return classScreeningProgress;
    }

    public ClassScreeningProgressState findClassScreeningStudentState(Integer schoolId, Integer gradeId, Integer classId, Integer screeningOrgId, Integer channel) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = getReleasePlanStudent(schoolId, gradeId, classId, screeningOrgId, channel);
        if (CollectionUtils.isEmpty(screeningPlanSchoolStudentList)) {
            // 空数据降级处理。根据目前需求（仅显示有筛查数据的学校 008-1.2021-08-26），实际不会进到这里。
            return new ClassScreeningProgressState();
        }
        // 获取学生对应筛查数据
        Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = screeningPlanSchoolStudentList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        List<VisionScreeningResult> first = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentMap.keySet(), false)
                .stream().filter(item -> item.getScreeningType().equals(channel)).collect(Collectors.toList());
        List<VisionScreeningResult> second = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentMap.keySet(), true)
                .stream().filter(item -> item.getScreeningType().equals(channel)).collect(Collectors.toList());
        ClassScreeningProgressState screeningProgressState = new ClassScreeningProgressState();
        screeningProgressState.setPlanCount((CollectionUtils.size(screeningPlanSchoolStudentList)));
        screeningProgressState.setScreeningCount(CollectionUtils.size(first));
        Map<Integer, VisionScreeningResult> firstPlanStudentVisionResultMap = first.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        Map<Integer, VisionScreeningResult> secondPlanStudentVisionResultMap = second.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        // 转换为筛查进度
        List<StudentScreeningProgressVO> firstProgress = getStudentScreeningProgress(screeningPlanSchoolStudentList, firstPlanStudentVisionResultMap, false);
        // 转换为筛查进度
        List<StudentScreeningProgressVO> secondProgress = getStudentScreeningProgress(screeningPlanSchoolStudentList, secondPlanStudentVisionResultMap, false)
                .stream().filter(item -> secondPlanStudentVisionResultMap.containsKey(item.getStudentId())).collect(Collectors.toList());

        firstProgress = numerationStatus(firstProgress, secondProgress);

        screeningProgressState.setNeedReScreeningCount((int) firstProgress.stream().filter(item -> item.getScreeningStatus() == 1 || item.getScreeningStatus() == 3).count());
        screeningProgressState.setWearingGlasses((int) firstProgress.stream().filter(item -> item.getScreeningStatus() == 3
                && Objects.nonNull(firstPlanStudentVisionResultMap.get(item.getStudentId())) && firstPlanStudentVisionResultMap.get(item.getStudentId()).getVisionData().getLeftEyeData().getGlassesType() != 0).count());
        screeningProgressState.setNoWearingGlasses((int) firstProgress.stream().filter(item -> item.getScreeningStatus() == 3
                && Objects.nonNull(firstPlanStudentVisionResultMap.get(item.getStudentId())) && firstPlanStudentVisionResultMap.get(item.getStudentId()).getVisionData().getLeftEyeData().getGlassesType() == 0).count());


        screeningProgressState.setRetestStudents(firstProgress.stream().filter(StudentScreeningProgressVO::getIsFirst)
                .filter(item -> {
                    VisionScreeningResult result = firstPlanStudentVisionResultMap.get(item.getStudentId());
                    // 当视力，屈光不配合时不参与复测
                    Integer visionCooperative = Optional.ofNullable(result).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getIsCooperative).orElse(null);
                    Integer optometryCooperative = Optional.ofNullable(result).map(VisionScreeningResult::getComputerOptometry).map(ComputerOptometryDO::getIsCooperative).orElse(null);
                    if (VisionScreeningConst.IS_NOT_COOPERATE.equals(visionCooperative) || VisionScreeningConst.IS_NOT_COOPERATE.equals(optometryCooperative)) {
                        return false;
                    }
                    // 夜戴角膜镜不参与复测
                    Integer leftGlassesType = Optional.ofNullable(result).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
                    Integer rightGlassesType = Optional.ofNullable(result).map(VisionScreeningResult::getVisionData).map(VisionDataDO::getRightEyeData).map(VisionDataDO.VisionData::getGlassesType).orElse(null);
                    if (WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY.equals(leftGlassesType) || WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_KEY.equals(rightGlassesType)) {
                        return false;
                    }
                    // 复测完成的
                    return item.getScreeningStatus().equals(3);
                }).filter(item -> item.getScreeningStatus().equals(1) || item.getScreeningStatus().equals(3)).map(item -> {
                    RetestStudentVO vo = BeanCopyUtil.copyBeanPropertise(item, RetestStudentVO.class);
                    VisionScreeningResult firstPlan = firstPlanStudentVisionResultMap.get(item.getStudentId());
                    vo.setVisionScreeningResult(new TwoTuple<>(firstPlan, secondPlanStudentVisionResultMap.get(item.getStudentId())));
                    vo.setStudentVisionScreeningResult(new TwoTuple<>(ScreeningResultDataVO.getInstance(firstPlan), ScreeningResultDataVO.getInstance(secondPlanStudentVisionResultMap.get(item.getStudentId()))));
                    return numerationSecondCheck(vo);
                }).collect(Collectors.toList()));
        screeningProgressState.setErrorItemCount(screeningProgressState.getRetestStudents().stream().mapToInt(RetestStudentVO::getErrorItemCount).sum());
        screeningProgressState.setRetestItemCount(screeningProgressState.getRetestStudents().stream().mapToInt(RetestStudentVO::getRetestItemCount).sum());
        screeningProgressState.setErrorRatio(screeningProgressState.getRetestItemCount() == 0 ? BigDecimal.ZERO : new BigDecimal(screeningProgressState.getErrorItemCount()).divide(new BigDecimal(screeningProgressState.getRetestItemCount()), 4, BigDecimal.ROUND_HALF_UP));
        screeningProgressState.setRetestRatio(screeningProgressState.getScreeningCount() == 0 ? BigDecimal.ZERO : new BigDecimal(screeningProgressState.getRetestStudents().size()).divide(new BigDecimal(screeningProgressState.getScreeningCount()), 4, BigDecimal.ROUND_HALF_UP));
        screeningProgressState.setReScreeningCount(screeningProgressState.getRetestStudents().size());
        return screeningProgressState;
    }

    /**
     * 检查是否有误差
     *
     * @param screeningResultBasicData screeningResultBasicData
     * @return RetestStudentVO RetestStudentVO
     */
    public RetestStudentVO getNumerationSecondCheck(ScreeningResultBasicData screeningResultBasicData) {
        TwoTuple<VisionScreeningResult, VisionScreeningResult> visDate = visionScreeningBizService.getAllFirstAndSecondResult(screeningResultBasicData);
        RetestStudentVO vo = new RetestStudentVO();
        vo.setVisionScreeningResult(new TwoTuple(visDate.getSecond(), visDate.getFirst()));
        return numerationSecondCheck(vo);
    }

    /**
     * 复测状态计算
     *
     * @param firstStudentScreeningProgressVOS
     * @param secondStudentScreeningProgressVOS
     * @return
     */
    private List<StudentScreeningProgressVO> numerationStatus(List<StudentScreeningProgressVO> firstStudentScreeningProgressVOS, List<StudentScreeningProgressVO> secondStudentScreeningProgressVOS) {
        Map<Integer, StudentScreeningProgressVO> secondStudentIdMap = secondStudentScreeningProgressVOS
                .stream().collect(Collectors.toMap(StudentScreeningProgressVO::getStudentId, Function.identity()));
        return firstStudentScreeningProgressVOS.stream().map(item ->
                numerationStatus(item, secondStudentIdMap.get(item.getStudentId()))
        ).collect(Collectors.toList());
    }

    private StudentScreeningProgressVO numerationStatus(StudentScreeningProgressVO firstStudentScreeningProgressVO, StudentScreeningProgressVO secondStudentScreeningProgressVOS) {
        if (Boolean.FALSE.equals(firstStudentScreeningProgressVO.getIsFirst()) || Boolean.FALSE.equals(firstStudentScreeningProgressVO.getResult())) {
            // 初筛未完成
            firstStudentScreeningProgressVO.setScreeningStatus(4);
        } else if (Objects.isNull(secondStudentScreeningProgressVOS)) {
            // 开始复测
            firstStudentScreeningProgressVO.setScreeningStatus(2);
        } else if (Boolean.FALSE.equals(secondStudentScreeningProgressVOS.getResult())) {
            // 复测中
            firstStudentScreeningProgressVO.setScreeningStatus(1);
        } else {
            // 复测完成
            firstStudentScreeningProgressVO.setScreeningStatus(3);
        }
        return firstStudentScreeningProgressVO;
    }

    /**
     * 学生复测面板数据计算
     *
     * @param retestStudentVO
     * @return
     */
    private RetestStudentVO numerationSecondCheck(RetestStudentVO retestStudentVO) {
        VisionDataDO.VisionData secondLeftVision = null;
        VisionDataDO.VisionData secondRightVision = null;
        VisionDataDO.VisionData firstLeftVision = null;
        VisionDataDO.VisionData firstRightVision = null;
        ComputerOptometryDO.ComputerOptometry secondLeftComputerOptometry = null;
        ComputerOptometryDO.ComputerOptometry secondRightComputerOptometry = null;
        ComputerOptometryDO.ComputerOptometry firstLeftComputerOptometry = null;
        ComputerOptometryDO.ComputerOptometry firstRightComputerOptometry = null;
        HeightAndWeightDataDO secondHeightWeight = null;
        HeightAndWeightDataDO firstHeightWeight = null;
        retestStudentVO.setRetestItemCount(0);
        retestStudentVO.setErrorItemCount(0);
        if (Objects.nonNull(retestStudentVO.getStudentVisionScreeningResult())) {
            if (Objects.nonNull(retestStudentVO.getVisionScreeningResult().getSecond())) {
                secondLeftVision = Objects.isNull(retestStudentVO.getVisionScreeningResult().getSecond().getVisionData())
                        ? null : retestStudentVO.getVisionScreeningResult().getSecond().getVisionData().getLeftEyeData();
                secondRightVision = Objects.isNull(retestStudentVO.getVisionScreeningResult().getSecond().getVisionData())
                        ? null : retestStudentVO.getVisionScreeningResult().getSecond().getVisionData().getRightEyeData();
                secondLeftComputerOptometry = Objects.isNull(retestStudentVO.getVisionScreeningResult().getSecond().getComputerOptometry()) ? null : retestStudentVO.getVisionScreeningResult().getSecond().getComputerOptometry().getLeftEyeData();
                secondRightComputerOptometry = Objects.isNull(retestStudentVO.getVisionScreeningResult().getSecond().getComputerOptometry()) ? null : retestStudentVO.getVisionScreeningResult().getSecond().getComputerOptometry().getRightEyeData();
                secondHeightWeight = retestStudentVO.getVisionScreeningResult().getSecond().getHeightAndWeightData();
            }
            if (Objects.nonNull(retestStudentVO.getVisionScreeningResult().getFirst())) {
                firstLeftVision = Objects.isNull(retestStudentVO.getVisionScreeningResult().getFirst().getVisionData())
                        ? null : retestStudentVO.getVisionScreeningResult().getFirst().getVisionData().getLeftEyeData();
                firstRightVision = Objects.isNull(retestStudentVO.getVisionScreeningResult().getFirst().getVisionData())
                        ? null : retestStudentVO.getVisionScreeningResult().getFirst().getVisionData().getRightEyeData();
                firstLeftComputerOptometry = Objects.isNull(retestStudentVO.getVisionScreeningResult().getFirst().getComputerOptometry()) ? null : retestStudentVO.getVisionScreeningResult().getFirst().getComputerOptometry().getLeftEyeData();
                firstRightComputerOptometry = Objects.isNull(retestStudentVO.getVisionScreeningResult().getFirst().getComputerOptometry()) ? null : retestStudentVO.getVisionScreeningResult().getFirst().getComputerOptometry().getRightEyeData();
                firstHeightWeight = retestStudentVO.getVisionScreeningResult().getFirst().getHeightAndWeightData();
            }
        }

        if (Objects.nonNull(secondLeftVision) || Objects.nonNull(secondRightVision)) {
            retestStudentVO.setRetestItemCount(retestStudentVO.existCheckVision(secondLeftVision) + retestStudentVO.existCheckVision(secondRightVision));
            retestStudentVO.setErrorItemCount(retestStudentVO.checkVision(firstLeftVision, secondLeftVision, retestStudentVO) + retestStudentVO.checkVision(firstRightVision, secondRightVision, retestStudentVO));
        }
        if (Objects.nonNull(secondLeftComputerOptometry) || Objects.nonNull(secondRightComputerOptometry)) {
            retestStudentVO.setRetestItemCount(retestStudentVO.getRetestItemCount() + retestStudentVO.existCheckComputerOptometry(secondLeftComputerOptometry) + retestStudentVO.existCheckComputerOptometry(secondRightComputerOptometry));
            retestStudentVO.setErrorItemCount(retestStudentVO.getErrorItemCount() + retestStudentVO.checkComputerOptometry(firstLeftComputerOptometry, secondLeftComputerOptometry, retestStudentVO) + retestStudentVO.checkComputerOptometry(firstRightComputerOptometry, secondRightComputerOptometry, retestStudentVO));
        }

        retestStudentVO.setRetestItemCount(retestStudentVO.getRetestItemCount() + retestStudentVO.existCheckHeightAndWeight(secondHeightWeight));
        retestStudentVO.setErrorItemCount(retestStudentVO.getErrorItemCount() + retestStudentVO.checkHeightAndWeight(firstHeightWeight, secondHeightWeight, retestStudentVO));
        return retestStudentVO;
    }

    /**
     * 根据筛查学生ID和筛查机构ID获取筛查数据
     *
     * @param planStudentId
     * @param screeningOrgId
     * @return com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult
     **/
    public VisionScreeningResult getVisionScreeningResultByPlanStudentId(Integer planStudentId, Integer screeningOrgId) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(new ScreeningPlanSchoolStudent().setId(planStudentId).setScreeningOrgId(screeningOrgId));
        Assert.notNull(screeningPlanSchoolStudent, SysEnum.SYS_STUDENT_NULL.getMessage());
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanSchoolStudent.getScreeningPlanId());
        Assert.isTrue(CommonConst.STATUS_RELEASE.equals(plan.getReleaseStatus()), "学生所属筛查计划已作废！");
        return visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(false));
    }


    /**
     * 根据筛查学生ID和筛查机构ID，是否復測获取筛查数据
     *
     * @param planStudentId
     * @param screeningOrgId
     * @param isState
     * @return com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult
     **/
    public VisionScreeningResult getVisionScreeningResultByPlanStudentIdAndState(Integer planStudentId, Integer screeningOrgId, Integer isState) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.findOne(new ScreeningPlanSchoolStudent().setId(planStudentId).setScreeningOrgId(screeningOrgId));
        Assert.notNull(screeningPlanSchoolStudent, SysEnum.SYS_STUDENT_NULL.getMessage());
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanSchoolStudent.getScreeningPlanId());
        Assert.isTrue(CommonConst.STATUS_RELEASE.equals(plan.getReleaseStatus()), "学生所属筛查计划已作废！");
        return visionScreeningResultService.findOne(new VisionScreeningResult().setScreeningPlanSchoolStudentId(planStudentId).setIsDoubleScreen(isState == 1));
    }

    /**
     * 新增学校端学生
     *
     * @param student 学生
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertSchoolStudent(Student student) {
        Integer schoolId = student.getSchoolId();
        String idCard = student.getIdCard();
        String sno = student.getSno();
        if (Objects.isNull(schoolId) || StringUtils.isBlank(idCard) || StringUtils.isBlank(sno)) {
            return;
        }
        if (!schoolStudentService.checkIdCardAndSno(null, idCard, sno, schoolId)) {
            return;
        }
        SchoolStudent schoolStudent;

        SchoolStudent deletedByIdCardAndSno = schoolStudentService.getDeletedByIdCardAndSno(idCard, sno, schoolId);
        if (Objects.nonNull(deletedByIdCardAndSno)) {
            schoolStudent = deletedByIdCardAndSno;
        } else {
            schoolStudent = new SchoolStudent();
        }
        SchoolGrade schoolGrade = schoolGradeService.getById(student.getGradeId());
        SchoolClass schoolClass = schoolClassService.getById(student.getClassId());

        schoolStudent.setStudentId(student.getId());
        schoolStudent.setSchoolId(schoolId);
        schoolStudent.setCreateUserId(student.getCreateUserId());
        schoolStudent.setSno(sno);
        schoolStudent.setGradeId(student.getGradeId());
        schoolStudent.setGradeName(schoolGrade.getName());
        schoolStudent.setGradeType(GradeCodeEnum.getByCode(schoolGrade.getGradeCode()).getType());
        schoolStudent.setClassId(student.getClassId());
        schoolStudent.setClassName(schoolClass.getName());
        schoolStudent.setName(student.getName());
        schoolStudent.setGender(student.getGender());
        schoolStudent.setBirthday(student.getBirthday());
        schoolStudent.setNation(student.getNation());
        schoolStudent.setIdCard(idCard);
        schoolStudent.setParentPhone(student.getParentPhone());
        schoolStudent.setMpParentPhone(student.getMpParentPhone());
        schoolStudent.setStatus(CommonConst.STATUS_NOT_DELETED);
        schoolStudent.setAddress(student.getAddress());
        schoolStudent.setProvinceCode(student.getProvinceCode());
        schoolStudent.setCityCode(student.getCityCode());
        schoolStudent.setAreaCode(student.getAreaCode());
        schoolStudent.setTownCode(student.getTownCode());
        schoolStudent.setPassport(student.getPassport());
        schoolStudentService.saveOrUpdate(schoolStudent);
    }

}