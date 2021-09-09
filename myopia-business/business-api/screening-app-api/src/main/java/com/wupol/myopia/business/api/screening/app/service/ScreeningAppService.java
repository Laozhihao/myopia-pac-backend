package com.wupol.myopia.business.api.screening.app.service;

import cn.hutool.core.util.IdcardUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppStudentDTO;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppUserInfo;
import com.wupol.myopia.business.api.screening.app.domain.dto.SysStudent;
import com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress;
import com.wupol.myopia.business.api.screening.app.domain.vo.RescreeningResultVO;
import com.wupol.myopia.business.api.screening.app.domain.vo.StudentScreeningProgressVO;
import com.wupol.myopia.business.api.screening.app.domain.vo.StudentVO;
import com.wupol.myopia.business.api.screening.app.enums.ErrorEnum;
import com.wupol.myopia.business.api.screening.app.enums.StudentExcelEnum;
import com.wupol.myopia.business.api.screening.app.utils.CommUtil;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.common.utils.util.UploadUtil;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningInfoWithResultDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private UploadConfig uploadConfig;
    @Resource
    private S3Utils s3Utils;
    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 获取学生复测数据
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
    public List<SysStudent> getStudentReview(Integer schoolId, String gradeName, String clazzName, Integer screeningOrgId, String studentName, Integer page, Integer size, boolean isRandom) throws JsonProcessingException {
        Set<Integer> currentPlanIds = screeningPlanService.getCurrentPlanIds(screeningOrgId);
        if (CollectionUtils.isEmpty(currentPlanIds)) {
            return new ArrayList<>();
        }
        // 获取学生数据
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentLambdaQueryWrapper = getScreeningPlanSchoolStudentLambdaQueryWrapper(schoolId, gradeName, clazzName, screeningOrgId, studentName, currentPlanIds);
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents;
        if (isRandom) {
            screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getBaseMapper().selectList(screeningPlanSchoolStudentLambdaQueryWrapper);
            ScreeningPlan currentPlan = screeningPlanService.getCurrentPlan(screeningOrgId, schoolId);
            String cacheKey = "app:" + screeningOrgId + currentPlan.getId() + schoolId +  gradeName + clazzName;
            screeningPlanSchoolStudents = getRandomData(screeningPlanSchoolStudents, cacheKey, currentPlan.getEndTime());
        } else {
            Integer startIntem = (page - 1) * size;
            screeningPlanSchoolStudentLambdaQueryWrapper.last("limit " + startIntem + "," + size);
            screeningPlanSchoolStudents = screeningPlanSchoolStudentService.getBaseMapper().selectList(screeningPlanSchoolStudentLambdaQueryWrapper);
        }
        return getSysStudents(screeningPlanSchoolStudents);
    }

    private LambdaQueryWrapper<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudentLambdaQueryWrapper(Integer schoolId, String gradeName, String clazzName, Integer screeningOrgId, String studentName, Set<Integer> currentPlanIds) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        screeningPlanSchoolStudent.setScreeningOrgId(screeningOrgId).setSchoolId(schoolId).setClassName(clazzName).setGradeName(gradeName);
        LambdaQueryWrapper<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(studentName)) {
            screeningPlanSchoolStudentLambdaQueryWrapper.like(ScreeningPlanSchoolStudent::getStudentName, studentName);
        }
        screeningPlanSchoolStudentLambdaQueryWrapper.setEntity(screeningPlanSchoolStudent).in(ScreeningPlanSchoolStudent::getScreeningPlanId, currentPlanIds);
        return screeningPlanSchoolStudentLambdaQueryWrapper;
    }

    /**
     * 获取sysStudents
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
     * 随机获取数据
     * @param screeningPlanSchoolStudents
     * @param cacheKey
     * @param endTime
     * @return
     * @throws JsonProcessingException
     */
    public List<ScreeningPlanSchoolStudent> getRandomData(List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents, String cacheKey, Date endTime) throws JsonProcessingException {
        //查找上次随机筛选的学生
        List<ScreeningPlanSchoolStudent> cacheList = this.getCacheList(cacheKey);
        // 如果cacheList 是null 说明没有数据,
        if(cacheList == null) {
            cacheList = new ArrayList<>();
        }
        int dataSize = CollectionUtils.size(screeningPlanSchoolStudents);
        int  cacheSize = CollectionUtils.size(cacheList);

        int newResultSize = (int) (dataSize * 0.06 - cacheSize);
        //数据长度没有变化
        if (newResultSize <= 0 && cacheSize != 0) {
            return cacheList;
        }
        //初始化数据
        if (newResultSize <=  0) {
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
     * @param key
     * @return
     * @throws JsonProcessingException
     */
    private List<ScreeningPlanSchoolStudent> getCacheList(String key) {
       return  (List<ScreeningPlanSchoolStudent>) redisUtil.get(key);
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
     * 获取筛查就机构对应的学校
     *
     * @param screeningOrgId 机构id
     * @return
     */
    public List<School> getSchoolByScreeningOrgId(Integer screeningOrgId) {
        List<Integer> schoolIds = screeningPlanService.getScreeningSchoolIdByScreeningOrgId(screeningOrgId);
        return schoolService.getSchoolByIds(schoolIds);
    }

    /**
     * 上传筛查机构用户的签名图片
     * @param currentUser
     * @param file
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
                .setGender(StringUtils.isBlank(appStudentDTO.getGrade()) ? null : GenderEnum.getType(appStudentDTO.getSex()))
                .setBirthday(StringUtils.isBlank(appStudentDTO.getBirthday()) ? null : DateFormatUtil.parseDate(appStudentDTO.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE))
                .setNation(StringUtils.isBlank(appStudentDTO.getClan()) ? null : NationEnum.getCode(appStudentDTO.getClan()))
                .setSchoolNo(school.getSchoolNo())
                .setGradeId(schoolGrade.getId())
                .setClassId(schoolClass.getId())
                .setSno(appStudentDTO.getStudentNo())
                .setIdCard(appStudentDTO.getIdCard())
                .setCreateUserId(currentUser.getId())
                .setParentPhone(appStudentDTO.getStudentPhone())
                .setStatus(0);
        return student;
    }

    /**
     * 获取用户的详细信息
     *
     * @param currentUser
     * @return
     */
    public AppUserInfo getUserInfoByUser(CurrentUser currentUser) {
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
        if (StringUtils.isBlank(appStudentDTO.getBirthday()) && StringUtils.isNotBlank(appStudentDTO.getIdCard()) ) {
            appStudentDTO.setBirthday(CommUtil.getBirthday(appStudentDTO.getIdCard()));
        }
        return null;
    }

    /**
     * 获取班级总的筛查进度：汇总统计+每个学生的进度
     *
     * @param schoolName 学校名称
     * @param gradeName 年级名称
     * @param clazzName 班级名称
     * @return com.wupol.myopia.business.api.screening.app.domain.vo.ClassScreeningProgress
     **/
    public ClassScreeningProgress getClassScreeningProgress(String schoolName, String gradeName, String clazzName, Integer screeningOrgId) {
        // 查询班级所有学生
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.listByEntityDescByCreateTime(new ScreeningPlanSchoolStudent()
                .setScreeningOrgId(screeningOrgId)
                .setSchoolName(schoolName)
                .setClassName(clazzName)
                .setGradeName(gradeName));
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(screeningPlanSchoolStudentList)) {
            // 空数据降级处理。根据目前需求（仅显示有筛查数据的学校 008-1.2021-08-26），实际不会进到这里。
            return new ClassScreeningProgress().setPlanCount(0).setScreeningCount(0).setAbnormalCount(0).setUnfinishedCount(0).setStudentScreeningProgressList(new ArrayList<>()).setSchoolAge(SchoolAge.PRIMARY.code).setArtificial(false);
        }

        // 获取学生对应筛查数据
        Set<Integer> screeningPlanSchoolStudentIds = screeningPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> visionScreeningResults = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(screeningPlanSchoolStudentIds);
        Map<Integer, VisionScreeningResult> planStudentVisionResultMap = visionScreeningResults.stream().collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));

        // 转换为筛查进度
        List<StudentScreeningProgressVO> studentScreeningProgressList = screeningPlanSchoolStudentList.stream().map(planStudent -> {
            VisionScreeningResult screeningResult = planStudentVisionResultMap.get(planStudent.getId());
            StudentVO studentVO = StudentVO.getInstance(planStudent);
            return StudentScreeningProgressVO.getInstanceWithDefault(screeningResult, studentVO);
        }).collect(Collectors.toList());

        // 异常的排前面
        Map<Boolean, List<StudentScreeningProgressVO>> finishMap = studentScreeningProgressList.stream().collect(Collectors.groupingBy(StudentScreeningProgressVO::getResult));
        List<StudentScreeningProgressVO> progressList = new ArrayList<>();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(finishMap.get(false))) {
            progressList.addAll(finishMap.get(false));
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(finishMap.get(true))) {
            progressList.addAll(finishMap.get(true));
        }

        // 统计筛查情况，只要有一条是人造的数据，则整个班级数据标记为人造的
        return new ClassScreeningProgress().setStudentScreeningProgressList(progressList)
                .setPlanCount(org.apache.commons.collections4.CollectionUtils.size(studentScreeningProgressList))
                .setScreeningCount(org.apache.commons.collections4.CollectionUtils.size(visionScreeningResults))
                .setAbnormalCount((int) studentScreeningProgressList.stream().filter(StudentScreeningProgressVO::getHasAbnormal).count())
                .setUnfinishedCount((int) studentScreeningProgressList.stream().filter(x -> !x.getResult()).count())
                .setSchoolAge(studentScreeningProgressList.get(0).getGradeType())
                .setArtificial(screeningPlanSchoolStudentList.stream().anyMatch(x -> Objects.nonNull(x.getArtificial()) && x.getArtificial() == 1));
    }
}