package com.wupol.myopia.business.api.screening.app.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppStudentDTO;
import com.wupol.myopia.business.api.screening.app.domain.dto.AppUserInfo;
import com.wupol.myopia.business.api.screening.app.domain.dto.DeviceScreenDataDTO;
import com.wupol.myopia.business.api.screening.app.domain.dto.SysStudent;
import com.wupol.myopia.business.api.screening.app.domain.vo.RescreeningResultVO;
import com.wupol.myopia.business.common.utils.config.UploadConfig;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.RescreeningStatisticEnum;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.common.utils.util.UploadUtil;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.common.util.S3Utils;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;
import com.wupol.myopia.business.core.device.service.DeviceSourceDataService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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

    private static final String DEVICE_PATIENTID_PREFIX = "VS@";

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private StatConclusionBizService statConclusionBizService;
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
    @Autowired
    private DeviceSourceDataService deviceSourceDataService;

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
     * 保存学生眼镜信息
     *
     * @param screeningResultBasicData
     * @return 返回statconclusion
     */
    @Transactional(rollbackFor = Exception.class)
    public TwoTuple<VisionScreeningResult, StatConclusion> saveOrUpdateStudentScreenData(ScreeningResultBasicData screeningResultBasicData) throws IOException {
        TwoTuple<VisionScreeningResult, VisionScreeningResult> allFirstAndSecondResult = visionScreeningResultService.getAllFirstAndSecondResult(screeningResultBasicData);
        VisionScreeningResult currentVisionScreeningResult = allFirstAndSecondResult.getFirst();
        currentVisionScreeningResult = visionScreeningResultService.getScreeningResult(screeningResultBasicData, currentVisionScreeningResult);
        allFirstAndSecondResult.setFirst(currentVisionScreeningResult);
        //更新vision_result表
        visionScreeningResultService.saveOrUpdateStudentScreenData(allFirstAndSecondResult.getFirst());
        //更新statConclusion表
        StatConclusion statConclusion = statConclusionBizService.saveOrUpdateStudentScreenData(allFirstAndSecondResult);
        //更新学生表的数据
        this.updateStudentVisionData(allFirstAndSecondResult.getFirst(),statConclusion);
        //返回最近一次的statConclusion
        TwoTuple<VisionScreeningResult, StatConclusion> visionScreeningResultStatConclusionTwoTuple = new TwoTuple<>();
        visionScreeningResultStatConclusionTwoTuple.setFirst(allFirstAndSecondResult.getFirst());
        visionScreeningResultStatConclusionTwoTuple.setSecond(statConclusion);
        return visionScreeningResultStatConclusionTwoTuple;
    }

    /**
     * 更新学生数据
     * @param visionScreeningResult
     * @param statConclusion
     */
    private void updateStudentVisionData(VisionScreeningResult visionScreeningResult,StatConclusion statConclusion) {
        //获取学生数据
        Integer studentId = visionScreeningResult.getStudentId();
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new ManagementUncheckedException("无法通过id找到student，id = " + studentId);
        }
        //填充数据
        student.setIsAstigmatism(statConclusion.getIsAstigmatism());
        student.setIsHyperopia(statConclusion.getIsHyperopia());
        student.setIsMyopia(statConclusion.getIsMyopia());
        student.setGlassesType(statConclusion.getGlassesType());
        student.setVisionLabel(statConclusion.getWarningLevel());
        student.setLastScreeningTime(visionScreeningResult.getUpdateTime());
        student.setUpdateTime(new Date());
        studentService.updateStudent(student);
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
     * 检查patientId是否来自设备近视防控系统
     * @param patientId
     */
    public boolean checkIsDevicePatientId(String patientId) {
        if (StringUtils.isBlank(patientId)) {
            return false;
        }
       return patientId.startsWith(DEVICE_PATIENTID_PREFIX);
    }

    /**
     * 保存设备上传的原始数据(目前只有vs666)
     * @param device
     * @param deviceScreenDataDTOList
     */
    public void saveDeviceSourceDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
        // screeningAppService.checkIsDevicePatientId(deviceScreenDataDTO.getPatientId());
        List<DeviceSourceData> deviceSourceDataList = this.getDeviceSourceDataList(device, deviceScreenDataDTOList);
        //保存到src表中
        deviceSourceDataService.saveBatch(deviceSourceDataList);
    }

    /**
     * 获取DeviceSourceData 的 List
     * @param device
     * @param deviceScreenDataDTOList
     * @return
     */
    private List<DeviceSourceData> getDeviceSourceDataList(Device device, List<DeviceScreenDataDTO> deviceScreenDataDTOList) {
      return deviceScreenDataDTOList.stream().map(deviceScreenDataDTO -> DeviceSourceData.getNewInstance(device, JSON.toJSONString(deviceScreenDataDTO), deviceScreenDataDTO.getPatientId())).collect(Collectors.toList());
    }
}