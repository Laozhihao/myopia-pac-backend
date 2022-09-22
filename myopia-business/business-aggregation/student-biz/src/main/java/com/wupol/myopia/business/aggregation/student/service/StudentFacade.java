package com.wupol.myopia.business.aggregation.student.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.service.MedicalReportBizService;
import com.wupol.myopia.business.aggregation.student.constant.VisionScreeningConst;
import com.wupol.myopia.business.aggregation.student.domain.vo.StudentWarningArchiveVO;
import com.wupol.myopia.business.aggregation.student.domain.vo.VisionInfoVO;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.domain.dto.Nation;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.MaskUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.domain.model.ReportConclusion;
import com.wupol.myopia.business.core.hospital.service.HospitalDoctorService;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.domain.vo.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.ReScreenCardUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.service.TemplateDistrictService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/20
 **/
@Service
public class StudentFacade {

    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private ScreeningOrganizationService screeningOrganizationService;
    @Resource
    private TemplateDistrictService templateDistrictService;
    @Resource
    private DistrictService districtService;
    @Resource
    private ScreeningPlanService screeningPlanService;
    @Resource
    private StatConclusionService statConclusionService;
    @Resource
    private StudentService studentService;
    @Resource
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;
    @Resource
    private ResourceFileService resourceFileService;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private SchoolStudentService schoolStudentService;
    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private OauthServiceClient oauthServiceClient;
    @Resource
    private MedicalReportService medicalReportService;

    @Autowired
    private HospitalDoctorService hospitalDoctorService;
    @Autowired
    private MedicalReportBizService medicalReportBizService;


    /**
     * 获取学生复测卡
     * @param planStudentId 计划学生ID
     * @param planId 计划ID
     * @return 获取学生复测卡
     */
    public ReScreeningCardVO getRetestResult(Integer planStudentId, Integer planId){
        VisionScreeningResult screeningResult = visionScreeningResultService.getOneScreeningResult(planId, planStudentId, Boolean.FALSE);
        VisionScreeningResult retestResult = visionScreeningResultService.getOneScreeningResult(planId, planStudentId, Boolean.TRUE);
        ScreeningPlanSchool screeningPlanSchool = screeningPlanSchoolService.findOne(new ScreeningPlanSchool().setScreeningPlanId(planId).setSchoolId(screeningResult.getSchoolId()));
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        return ReScreenCardUtil.reScreenResultCard(screeningResult, retestResult, screeningPlanSchool.getQualityControllerName(), planSchoolStudent.getCommonDiseaseId());
    }

    /**
     * 获取学生筛查档案
     *
     * @param studentId 学生ID
     * @param currentUser 当前登录用户
     * @return 学生档案卡返回体
     */
    public  IPage<StudentScreeningResultItemsDTO> getScreeningList(PageRequest pageRequest, Integer studentId, CurrentUser currentUser) {
        // 通过学生id查询结果
        IPage<VisionScreeningResultDTO> resultIPage = visionScreeningResultService.getByStudentIdWithPage(pageRequest, studentId, !currentUser.isPlatformAdminUser());
        List<VisionScreeningResultDTO> resultList = resultIPage.getRecords();
        if (CollectionUtils.isEmpty(resultList)) {
            return new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        }
        // 获取机构
        Map<Integer, ScreeningOrganization> screeningOrganizationMap = getScreeningOrgMap(resultList);
        // 获取结论
        Map<Integer, StatConclusion> statMap = getStatConclusionMap(resultList);
        // 获取复测
        Map<Integer, VisionScreeningResult> reScreeningResultMap = resultList.stream().filter(VisionScreeningResult::getIsDoubleScreen).collect(Collectors.toMap(VisionScreeningResult::getPlanId, Function.identity()));
        // 获取筛查学生
        Map<Integer, ScreeningPlanSchoolStudent> screeningPlanSchoolStudentMap = getPlanStudentMap(resultList);
        // 获取学生信息
        StudentDTO studentDTO = studentService.getStudentById(studentId);
        List<StudentScreeningResultItemsDTO> records = new ArrayList<>();
        // 转换
        for (VisionScreeningResultDTO result : resultList) {
            StudentScreeningResultItemsDTO item = new StudentScreeningResultItemsDTO();
            ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentMap.getOrDefault(result.getScreeningPlanSchoolStudentId(), new ScreeningPlanSchoolStudent());
            StatConclusion statConclusion = statMap.getOrDefault(result.getId(), new StatConclusion());
            // 设置其他
            item.setDetails(getScreeningDataDetail(result, reScreeningResultMap))
                    .setScreeningTitle(result.getPlanTitle())
                    .setScreeningDate(result.getUpdateTime())
                    // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样的
                    .setGlassesTypeDes(Optional.ofNullable(result.getVisionData()).map(VisionDataDO::getLeftEyeData).map(VisionDataDO.VisionData::getGlassesType).map(WearingGlassesSituation::getType).orElse(null))
                    .setResultId(result.getId())
                    .setIsDoubleScreen(result.getIsDoubleScreen())
                    .setTemplateId(getTemplateId(result.getScreeningOrgId(), result.getScreeningType()))
                    .setOtherEyeDiseases(getOtherEyeDiseasesList(result))
                    .setPlanId(result.getPlanId())
                    .setHasScreening(ObjectUtils.anyNotNull(result.getVisionData(), result.getComputerOptometry(), result.getBiometricData(), result.getOtherEyeDiseases()))
                    .setPlanStudentId(result.getScreeningPlanSchoolStudentId())
                    .setScreeningCode(planSchoolStudent.getScreeningCode())
                    .setClassId(planSchoolStudent.getClassId())
                    // 设置预警、近视、远视、散光等级
                    .setWarningLevel(statConclusion.getWarningLevel())
                    .setMyopiaLevel(statConclusion.getMyopiaLevel())
                    .setHyperopiaLevel(statConclusion.getHyperopiaLevel())
                    .setAstigmatismLevel(statConclusion.getAstigmatismLevel())
                    //筛查类型
                    .setScreeningType(result.getScreeningType())
                    //筛查机构名称
                    .setScreeningOrgName(Optional.ofNullable(screeningOrganizationMap.get(result.getScreeningOrgId())).map(ScreeningOrganization::getName).orElse(null))
                    //设置学生性别
                    .setGender(studentDTO.getGender())
                    //设置常见病ID
                    .setCommonDiseasesCode(screeningPlanSchoolStudentMap.get(result.getScreeningPlanSchoolStudentId()).getCommonDiseaseId())
                    .setReleaseStatus(result.getReleaseStatus());
            records.add(item);
        }
        return new Page<StudentScreeningResultItemsDTO>(resultIPage.getCurrent(), resultIPage.getSize(), resultIPage.getTotal()).setRecords(records);
    }

    /**
     * 获取筛查机构Map
     *
     * @param resultList    筛查结果数据集
     * @return java.util.Map<java.lang.Integer,com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization>
     **/
    private Map<Integer, ScreeningOrganization> getScreeningOrgMap(List<VisionScreeningResultDTO> resultList) {
        List<Integer> screeningOrgIds =  resultList.stream().map(VisionScreeningResult::getScreeningOrgId).distinct().collect(Collectors.toList());
        List<ScreeningOrganization> screeningOrganizations = screeningOrganizationService.getByIds(screeningOrgIds);
        return screeningOrganizations.stream().collect(Collectors.toMap(ScreeningOrganization::getId, Function.identity()));
    }

    /**
     * 获取筛查统计结论Map
     *
     * @param resultList    筛查结果数据集
     * @return java.util.Map<java.lang.Integer,com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion>
     **/
    private Map<Integer, StatConclusion> getStatConclusionMap(List<VisionScreeningResultDTO> resultList) {
        List<Integer> resultIds = resultList.stream().map(VisionScreeningResult::getId).collect(Collectors.toList());
        List<StatConclusion> statConclusionList = statConclusionService.getByResultIds(resultIds);
        return statConclusionList.stream().collect(Collectors.toMap(StatConclusion::getResultId, Function.identity()));
    }

    /**
     * 获取筛查计划学生Map
     *
     * @param resultList    筛查结果数据集
     * @return java.util.Map<java.lang.Integer,com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent>
     **/
    private Map<Integer, ScreeningPlanSchoolStudent> getPlanStudentMap(List<VisionScreeningResultDTO> resultList) {
        List<Integer> planStudentIds = resultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).distinct().collect(Collectors.toList());
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudents = screeningPlanSchoolStudentService.listByIds(planStudentIds);
        return screeningPlanSchoolStudents.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
    }

    /**
     * 获取筛查数据详情
     *
     * @param currentResult    筛查结果数据
     * @param reScreeningResultMap  复筛结果数据Map
     * @return com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningInfoDTO
     **/
    private ScreeningInfoDTO getScreeningDataDetail(VisionScreeningResult currentResult, Map<Integer, VisionScreeningResult> reScreeningResultMap) {
        ScreeningInfoDTO screeningInfoDTO  = new ScreeningInfoDTO();
        List<StudentResultDetailsDTO> resultDetail = packageDTO(currentResult);
        resultDetail.forEach(r -> r.setHeightAndWeightData(currentResult.getHeightAndWeightData()));
        //设置视力信息
        screeningInfoDTO.setVision(resultDetail);
        //设置常见病信息
        screeningInfoDTO.setCommonDiseases(getCommonDiseases(currentResult));
        //设置复测信息(为初筛且复测项目都完成才设置该模块)
        VisionScreeningResult reScreeningResult = reScreeningResultMap.get(currentResult.getPlanId());
        if (Boolean.FALSE.equals(currentResult.getIsDoubleScreen()) && Objects.nonNull(reScreeningResult) && ObjectUtils.allNotNull(reScreeningResult.getVisionData(), reScreeningResult.getComputerOptometry(), reScreeningResult.getHeightAndWeightData())){
            screeningInfoDTO.setReScreening(ReScreenCardUtil.reScreeningResult(currentResult, reScreeningResultMap.get(currentResult.getPlanId())));
        }
        return screeningInfoDTO;
    }

    /**
     * 设置常见病信息
     * @param result
     * @return
     */
    private CommonDiseasesDTO getCommonDiseases(VisionScreeningResult result) {
        CommonDiseasesDTO commonDiseases = new  CommonDiseasesDTO();
        BeanUtils.copyProperties(result, commonDiseases);
        commonDiseases.setSaprodontiaStat(SaprodontiaStat.parseFromSaprodontiaDataDO(result.getSaprodontiaData()));
        return commonDiseases;
    }

    /**
     * 封装结果
     *
     * @param result 结果表
     * @return 详情列表
     */
    private List<StudentResultDetailsDTO> packageDTO(VisionScreeningResult result) {

        // 设置左眼
        StudentResultDetailsDTO leftDetails = new StudentResultDetailsDTO();
        leftDetails.setLateriality(CommonConst.LEFT_EYE);
        //设置右眼
        StudentResultDetailsDTO rightDetails = new StudentResultDetailsDTO();
        rightDetails.setLateriality(CommonConst.RIGHT_EYE);

        if (null != result.getVisionData()) {
            // 视力检查结果
            packageVisionResult(result.getVisionData(), leftDetails, rightDetails);
        }
        if (null != result.getComputerOptometry()) {
            // 电脑验光
            packageComputerOptometryResult(result.getComputerOptometry(), leftDetails, rightDetails);
        }
        if (null != result.getBiometricData()) {
            // 生物测量
            packageBiometricDataResult(result, leftDetails, rightDetails);
        }
        if (null != result.getOtherEyeDiseases()) {
            // 眼部疾病
            packageOtherEyeDiseasesResult(result, leftDetails, rightDetails);
        }

        if (null != result.getOtherEyeDiseases()) {
            // 眼部疾病
            packageOtherEyeDiseasesResult(result, leftDetails, rightDetails);
        }

        return Lists.newArrayList(rightDetails, leftDetails);
    }

    /**
     * 封装视力检查结果
     *
     * @param visionData   原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    private void packageVisionResult(VisionDataDO visionData, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼-视力检查结果
        VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
        leftDetails.setGlassesType(leftEyeData.getGlassesType());
        leftDetails.setGlassesTypeDes(WearingGlassesSituation.getType(leftEyeData.getGlassesType()));
        leftDetails.setCorrectedVision(leftEyeData.getCorrectedVision());
        leftDetails.setNakedVision(leftEyeData.getNakedVision());
        leftDetails.setOkDegree(leftEyeData.getOkDegree());

        // 右眼-视力检查结果
        VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();
        rightDetails.setGlassesType(rightEyeData.getGlassesType());
        rightDetails.setGlassesTypeDes(WearingGlassesSituation.getType(rightEyeData.getGlassesType()));
        rightDetails.setCorrectedVision(rightEyeData.getCorrectedVision());
        rightDetails.setNakedVision(rightEyeData.getNakedVision());
        rightDetails.setOkDegree(rightEyeData.getOkDegree());
    }

    /**
     * 封装电脑验光
     *
     * @param computerOptometry 电脑验光筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    private void packageComputerOptometryResult(ComputerOptometryDO computerOptometry, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼--电脑验光
        ComputerOptometryDO.ComputerOptometry leftEyeData = computerOptometry.getLeftEyeData();
        leftDetails.setAxial(leftEyeData.getAxial());
        leftDetails.setSe(StatUtil.getSphericalEquivalent(leftEyeData.getSph(), leftEyeData.getCyl()));
        leftDetails.setCyl(leftEyeData.getCyl());
        leftDetails.setSph(leftEyeData.getSph());

        // 左眼--电脑验光
        ComputerOptometryDO.ComputerOptometry rightEyeData = computerOptometry.getRightEyeData();
        rightDetails.setAxial(rightEyeData.getAxial());
        rightDetails.setSe(StatUtil.getSphericalEquivalent(rightEyeData.getSph(), rightEyeData.getCyl()));
        rightDetails.setCyl(rightEyeData.getCyl());
        rightDetails.setSph(rightEyeData.getSph());
    }

    /**
     * 封装生物测量结果
     *
     * @param result       原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    private void packageBiometricDataResult(VisionScreeningResult result, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼--生物测量
        leftDetails.setAd(result.getBiometricData().getLeftEyeData().getAd());
        leftDetails.setAl(result.getBiometricData().getLeftEyeData().getAl());
        leftDetails.setCct(result.getBiometricData().getLeftEyeData().getCct());
        leftDetails.setLt(result.getBiometricData().getLeftEyeData().getLt());
        leftDetails.setWtw(result.getBiometricData().getLeftEyeData().getWtw());

        // 右眼--生物测量
        rightDetails.setAd(result.getBiometricData().getRightEyeData().getAd());
        rightDetails.setAl(result.getBiometricData().getRightEyeData().getAl());
        rightDetails.setCct(result.getBiometricData().getRightEyeData().getCct());
        rightDetails.setLt(result.getBiometricData().getRightEyeData().getLt());
        rightDetails.setWtw(result.getBiometricData().getRightEyeData().getWtw());
    }

    /**
     * 封装眼部疾病结果
     *
     * @param result       原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    private void packageOtherEyeDiseasesResult(VisionScreeningResult result, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼--眼部疾病
        leftDetails.setEyeDiseases(getEyeDiseases(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases(), result.getSystemicDiseaseSymptom()));
        // 右眼--眼部疾病
        rightDetails.setEyeDiseases(getEyeDiseases(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases(), result.getSystemicDiseaseSymptom()));
    }

    /**
     * 获取疾病描述
     *
     * @param eyeDiseases            眼部疾病
     * @param systemicDiseaseSymptom 全身病
     * @return 疾病描述
     */
    private String getEyeDiseases(List<String> eyeDiseases, String systemicDiseaseSymptom) {
        if (CollectionUtils.isEmpty(eyeDiseases)) {
            return systemicDiseaseSymptom;
        }
        if (StringUtils.isEmpty(systemicDiseaseSymptom)) {
            return String.join(CommonConst.CH_COMMA, eyeDiseases);
        }
        return String.join(CommonConst.CH_COMMA, eyeDiseases) + CommonConst.CH_COMMA + systemicDiseaseSymptom;
    }

    /**
     * 获取机构使用的模板
     *
     * @param screeningOrgId 筛查机构Id
     * @param screeningType  筛查类型
     * @return 模板Id
     */
    private Integer getTemplateId(Integer screeningOrgId, Integer screeningType) {
        ScreeningOrganization org = screeningOrganizationService.getById(screeningOrgId);
        return templateDistrictService.getArchivesByDistrictId(districtService.getProvinceId(org.getDistrictId()), TemplateConstants.getTemplateBizTypeByScreeningType(screeningType));
    }

    /**
     * 获取两眼别的病变
     *
     * @param visionScreeningResult 视力筛查结果
     * @return List<String>
     */
    private List<String> getOtherEyeDiseasesList(VisionScreeningResult visionScreeningResult) {
        List<String> emptyList = new ArrayList<>();
        OtherEyeDiseasesDO otherEyeDiseases = visionScreeningResult.getOtherEyeDiseases();
        if (Objects.isNull(otherEyeDiseases)) {
            return emptyList;
        }
        List<String> leftEyeDate = Objects.nonNull(otherEyeDiseases.getLeftEyeData()) ? otherEyeDiseases.getLeftEyeData().getEyeDiseases() : emptyList;
        List<String> rightEyeDate = Objects.nonNull(otherEyeDiseases.getRightEyeData()) ? otherEyeDiseases.getRightEyeData().getEyeDiseases() : emptyList;
        return ListUtils.sum(leftEyeDate, rightEyeDate);
    }

    /**
     * 根据筛查接口获取档案卡所需要的数据
     *
     * @param visionScreeningResult 筛查结果
     * @return 学生档案卡实体类
     */
    public StudentCardResponseVO getStudentCardResponseDTO(VisionScreeningResult visionScreeningResult) {
        StudentCardResponseVO responseDTO = new StudentCardResponseVO();
        Integer studentId = visionScreeningResult.getStudentId();
        StudentDTO studentInfo = studentService.getStudentInfo(studentId);

        // 获取学生基本信息
        CardInfoVO cardInfoVO = getCardInfo(studentInfo);
        cardInfoVO.setScreeningDate(visionScreeningResult.getCreateTime());
        cardInfoVO.setCountNotCooperate(getCountNotCooperate(visionScreeningResult));
        responseDTO.setInfo(cardInfoVO);

        Integer templateId = getTemplateId(visionScreeningResult.getScreeningOrgId(), visionScreeningResult.getScreeningType());
        return generateCardDetail(visionScreeningResult, studentInfo, templateId, responseDTO);
    }

    /**
     * 批量生成学生档案卡
     *
     * @param resultList 筛查结果列表
     * @return 学生档案卡实体类list
     */
    public List<StudentCardResponseVO> generateBatchStudentCard(List<VisionScreeningResult> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        // 筛查结构的Id都相同，取第一个就行
        Integer templateId = getTemplateId(resultList.get(0).getScreeningOrgId(), resultList.get(0).getScreeningType());

        // 查询学生信息
        List<Integer> studentIds = resultList.stream().map(VisionScreeningResult::getStudentId).collect(Collectors.toList());
        List<StudentDTO> studentInfoList = studentService.getStudentInfoList(studentIds);
        Map<Integer, StudentDTO> studentMaps = studentInfoList.stream().collect(Collectors.toMap(Student::getId, Function.identity()));

        return resultList.stream().map(r -> generateStudentCard(r, studentMaps.get(r.getStudentId()), templateId)).collect(Collectors.toList());
    }

    /**
     * 生成档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param studentInfo           学生信息
     * @param templateId            模板Id
     * @return 学生档案卡实体类
     */
    public StudentCardResponseVO generateStudentCard(VisionScreeningResult visionScreeningResult, StudentDTO studentInfo, Integer templateId) {
        StudentCardResponseVO responseDTO = new StudentCardResponseVO();

        // 获取学生基本信息
        CardInfoVO cardInfoVO = getCardInfo(studentInfo);
        cardInfoVO.setScreeningDate(visionScreeningResult.getCreateTime());
        cardInfoVO.setCountNotCooperate(getCountNotCooperate(visionScreeningResult));
        responseDTO.setInfo(cardInfoVO);
        return generateCardDetail(visionScreeningResult, studentInfo, templateId, responseDTO);
    }

    /**
     * 通过筛查学生Id获取学生档案卡
     *
     * @param planStudentId 筛查学生
     * @return 学生档案卡实体类
     */
    public AppStudentCardResponseDTO getCardDetailByPlanStudentId(Integer planStudentId) {
        AppStudentCardResponseDTO responseDTO = new AppStudentCardResponseDTO();
        VisionScreeningResult result = visionScreeningResultService.getByPlanStudentId(planStudentId);
        if (Objects.isNull(result)) {
            return responseDTO;
        }
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(result.getId());

        responseDTO.setTemplateId(getTemplateId(visionScreeningResult.getScreeningOrgId(), visionScreeningResult.getScreeningType()));
        responseDTO.setStudentCardResponseVO(Lists.newArrayList(getStudentCardResponseDTO(visionScreeningResult)));
        return responseDTO;
    }

    /**
     * 获取学生档案卡
     *
     * @param resultId 筛查结果
     * @return 学生档案卡实体类
     */
    public StudentCardResponseVO getCardDetail(Integer resultId) {
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(resultId);
        return getStudentCardResponseDTO(visionScreeningResult);
    }

    /**
     * 设置学生基本信息
     *
     * @param studentInfo 学生
     * @return 学生档案卡基本信息
     */
    public CardInfoVO getCardInfo(StudentDTO studentInfo) {
        CardInfoVO cardInfoVO = new CardInfoVO();
        cardInfoVO.setName(studentInfo.getName());
        cardInfoVO.setBirthday(studentInfo.getBirthday());
        cardInfoVO.setIdCard(StringUtils.isNotBlank(studentInfo.getIdCard()) ? MaskUtil.maskIdCard(studentInfo.getIdCard()) : MaskUtil.maskPassport(studentInfo.getPassport()));
        cardInfoVO.setGender(studentInfo.getGender());
        cardInfoVO.setAge(DateUtil.ageOfNow(studentInfo.getBirthday()));
        cardInfoVO.setSno(studentInfo.getSno());
        cardInfoVO.setParentPhone(studentInfo.getParentPhone());
        cardInfoVO.setSchoolName(studentInfo.getSchoolName());
        cardInfoVO.setSchoolId(studentInfo.getSchoolId());
        cardInfoVO.setClassName(studentInfo.getClassName());
        cardInfoVO.setGradeName(studentInfo.getGradeName());
        cardInfoVO.setDistrictName(districtService.getDistrictName(studentInfo.getSchoolDistrictName()));
        cardInfoVO.setNation(studentInfo.getNation());
        cardInfoVO.setNationDesc(NationEnum.getName(studentInfo.getNation()));
        cardInfoVO.setPassport(studentInfo.getPassport());
        cardInfoVO.setSchoolType(SchoolAge.get(studentInfo.getGradeType()).type);
        cardInfoVO.setCityDesc(districtService.getDistrictName(studentInfo.getCityCode()));
        cardInfoVO.setAreaDesc(districtService.getDistrictName(studentInfo.getAreaCode()));
        return cardInfoVO;
    }

    /**
     * 统计学生配合程度
     *
     * @param result 筛查结果
     * @return 统计
     */
    private Integer getCountNotCooperate(VisionScreeningResult result) {
        int total = 0;

        // 02
        VisionDataDO visionData = result.getVisionData();
        if (Objects.nonNull(visionData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(visionData.getIsCooperative())) {
            total++;
        }
        // 03
        ComputerOptometryDO computerOptometry = result.getComputerOptometry();
        if (Objects.nonNull(computerOptometry) && VisionScreeningConst.IS_NOT_COOPERATE.equals(computerOptometry.getIsCooperative())) {
            total++;
        }
        // 05
        PupilOptometryDataDO pupilOptometryData = result.getPupilOptometryData();
        if (Objects.nonNull(pupilOptometryData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(pupilOptometryData.getIsCooperative())) {
            total++;
        }
        // 06
        BiometricDataDO biometricData = result.getBiometricData();
        if (Objects.nonNull(biometricData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(biometricData.getIsCooperative())) {
            total++;
        }
        // 07
        EyePressureDataDO eyePressureData = result.getEyePressureData();
        if (Objects.nonNull(eyePressureData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(eyePressureData.getIsCooperative())) {
            total++;
        }

        // 剩下三个特殊处理，只有一个有，就+1
        boolean spFlag = false;
        // 08
        FundusDataDO fundusData = result.getFundusData();
        if (Objects.nonNull(fundusData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(fundusData.getIsCooperative())) {
            spFlag = true;
        }
        // 04
        SlitLampDataDO slitLampData = result.getSlitLampData();
        if (Objects.nonNull(slitLampData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(slitLampData.getIsCooperative())) {
            spFlag = true;
        }
        // 01
        OcularInspectionDataDO ocularInspectionData = result.getOcularInspectionData();
        if (Objects.nonNull(ocularInspectionData) && VisionScreeningConst.IS_NOT_COOPERATE.equals(ocularInspectionData.getIsCooperative())) {
            spFlag = true;
        }
        if (spFlag) {
            total++;
        }
        // 返回total
        return total;
    }

    /**
     * 生成档案卡详情
     *
     * @param visionScreeningResult 筛查结果
     * @param studentInfo           学生信息
     * @param templateId            模板Id
     * @param responseDTO           档案卡实体类
     * @return 学生档案卡实体类
     */
    private StudentCardResponseVO generateCardDetail(VisionScreeningResult visionScreeningResult, StudentDTO studentInfo, Integer templateId, StudentCardResponseVO responseDTO) {
        int age = DateUtil.ageOfNow(studentInfo.getBirthday());
        // 是否全国模板
        if (templateId.equals(TemplateConstants.GLOBAL_TEMPLATE)) {
            // 获取结果记录
            CardDetailsVO cardDetailsVO = packageCardDetail(visionScreeningResult, age);
            responseDTO.setDetails(cardDetailsVO);
        } else if (templateId.equals(TemplateConstants.HAI_NAN_TEMPLATE)) {
            Integer status = studentInfo.getSchoolAgeStatus();
            responseDTO.setStatus(status);
            responseDTO.setHaiNanCardDetail(packageHaiNanCardDetail(visionScreeningResult, age, status));
        }else if (templateId.equals(TemplateConstants.SCREENING_TEMPLATE)) {
            //儿童青少年近视筛查结果记录表
            Integer status = studentInfo.getSchoolAgeStatus();
            responseDTO.setStatus(status);
            responseDTO.setMyopiaScreeningResultCardDetail(packageMyopiaScreeningResultCardDetail(visionScreeningResult,responseDTO));
        }
        return responseDTO;
    }

    /**
     * 青少年近视筛查结果
     *
     * @param result 筛查结果
     * @return 儿童青少年近视筛查结果记录表
     */
    private MyopiaScreeningResultCardDetail packageMyopiaScreeningResultCardDetail(VisionScreeningResult result,StudentCardResponseVO responseDTO){
        MyopiaScreeningResultCardDetail details = new MyopiaScreeningResultCardDetail();
        if (Objects.isNull(result)) {
            return details;
        }

        BeanUtils.copyProperties(result, details);

        details.setVisionResults(setVisionResult(result.getVisionData()));
        details.setRefractoryResults(setRefractoryResults(result.getComputerOptometry()));
        // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样
        setClassType(result, details);

        details.setVisionSignPicUrl(getCreateUserName(result));
        details.setComputerSignPicUrl(getCreateUserName(result));

        String ageInfo = com.wupol.myopia.base.util.DateUtil.getAgeInfo(responseDTO.getInfo().getBirthday(),new Date());
        details.setAgeInfo(ageInfo);

        return details;
    }

    /**
     * 设置戴镜类型
     * @param result
     * @param details
     */
    private void setClassType(VisionScreeningResult result, MyopiaScreeningResultCardDetail details) {
        CardDetailsVO.GlassesTypeObj glassesTypeObj = new CardDetailsVO.GlassesTypeObj();
        VisionDataDO visionData = result.getVisionData();
        if (Objects.nonNull(visionData)) {
            glassesTypeObj.setType(visionData.getLeftEyeData().getGlassesType());
            details.setGlassesTypeObj(glassesTypeObj);
        }
        details.setGlassesTypeObj(glassesTypeObj);
    }

    /**
     * 设置视力信息
     *
     * @param result 筛查结果
     * @param age    学生年龄
     * @return 档案卡视力详情
     */
    private CardDetailsVO packageCardDetail(VisionScreeningResult result, Integer age) {
        CardDetailsVO details = new CardDetailsVO();


        // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样
        CardDetailsVO.GlassesTypeObj glassesTypeObj = new CardDetailsVO.GlassesTypeObj();
        VisionDataDO visionData = result.getVisionData();
        if (Objects.nonNull(visionData)) {
            glassesTypeObj.setType(visionData.getLeftEyeData().getGlassesType());
            details.setGlassesTypeObj(glassesTypeObj);
        }

        details.setVisionResults(setVisionResult(visionData));
        details.setVisionSign(getCreateUserName(result));
        details.setRefractoryResults(setRefractoryResults(result.getComputerOptometry()));
        details.setRefractorySign(getCreateUserName(result));
        details.setCrossMirrorResults(setCrossMirrorResults(result, age));
        details.setCrossMirrorSign(getCreateUserName(result));
        details.setEyeDiseasesResult(setEyeDiseasesResult(result.getOtherEyeDiseases()));
        return details;
    }

    /**
     * 封装海南档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param age                   年轻
     * @param status                0-幼儿园 1-中小学版本
     * @return HaiNanCardDetail
     */
    private HaiNanCardDetail packageHaiNanCardDetail(VisionScreeningResult visionScreeningResult, Integer age, Integer status) {
        HaiNanCardDetail cardDetail = new HaiNanCardDetail();
        if (Objects.isNull(visionScreeningResult)) {
            return cardDetail;
        }
        BeanUtils.copyProperties(visionScreeningResult, cardDetail);
        cardDetail.setVisionDataDO(visionScreeningResult.getVisionData());
        cardDetail.setRemark(Objects.nonNull(visionScreeningResult.getFundusData()) ? visionScreeningResult.getFundusData().getRemark() : "");
        // 其他眼部疾病
        List<String> otherEyeDiseasesList = getOtherEyeDiseasesList(visionScreeningResult);

        // 其他眼病,过滤掉五种特殊情况
        cardDetail.setOtherEyeDiseases(ListUtils.subtract(otherEyeDiseasesList, eyeDiseases()));
        cardDetail.setResultOtherEyeDiseases(visionScreeningResult.getOtherEyeDiseases());
        cardDetail.setEyeDiseases(ListUtils.retainAll(eyeDiseases(), otherEyeDiseasesList));
        // 眼斜
        cardDetail.setSquint(getSquintList(otherEyeDiseasesList));
        cardDetail.setSignPicUrl(getSignPicUrl(visionScreeningResult));

        // 设置屈光不正信息
        Boolean isRefractiveError = setRefractiveErrorInfo(cardDetail, visionScreeningResult, age, status);
        // 设置是否近视、远视
        setMyopiaAndFarsightedness(visionScreeningResult, age, cardDetail);
        // isRefractiveError为Null不展示
        if (Objects.nonNull(isRefractiveError)) {
            // 是否曲光不正
            cardDetail.setIsRefractiveError(isRefractiveError);
            // 是否正常
            boolean isNormal = CollectionUtils.isEmpty(otherEyeDiseasesList);
            cardDetail.setIsNormal(!isRefractiveError && isNormal);
            if (Objects.equals(cardDetail.getIsNormal(),Boolean.TRUE)) {
                // 正常就不显示近、远视
                cardDetail.setIsMyopia(null);
                cardDetail.setIsHyperopia(null);
            }
        }
        // 如果近视和远视，显示屈光不正
        if ((Objects.nonNull(cardDetail.getIsMyopia()) && cardDetail.getIsMyopia())
                || (Objects.nonNull(cardDetail.getIsHyperopia()) && cardDetail.getIsHyperopia())) {
            cardDetail.setIsRefractiveError(true);
        }
        return cardDetail;
    }

    /**
     * 设置视力检查结果
     *
     * @param result 筛查结果
     * @return 视力检查结果List
     */
    private List<CardDetailsVO.VisionResult> setVisionResult(VisionDataDO result) {
        CardDetailsVO.VisionResult left = new CardDetailsVO.VisionResult();
        CardDetailsVO.VisionResult right = new CardDetailsVO.VisionResult();

        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);
        if (null != result) {
            // 左眼
            left.setCorrectedVision(result.getLeftEyeData().getCorrectedVision());
            left.setNakedVision(result.getLeftEyeData().getNakedVision());

            // 右眼
            right.setCorrectedVision(result.getRightEyeData().getCorrectedVision());
            right.setNakedVision(result.getRightEyeData().getNakedVision());
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 设置验光仪检查结果
     *
     * @param result 筛查结果
     * @return 验光仪检查结果列表
     */
    private List<CardDetailsVO.RefractoryResult> setRefractoryResults(ComputerOptometryDO result) {
        CardDetailsVO.RefractoryResult left = new CardDetailsVO.RefractoryResult();
        CardDetailsVO.RefractoryResult right = new CardDetailsVO.RefractoryResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);

        if (null != result) {
            // 左眼
            left.setAxial(result.getLeftEyeData().getAxial());
            left.setSph(result.getLeftEyeData().getSph());
            left.setCyl(result.getLeftEyeData().getCyl());

            // 右眼
            right.setAxial(result.getRightEyeData().getAxial());
            right.setSph(result.getRightEyeData().getSph());
            right.setCyl(result.getRightEyeData().getCyl());
        }
        return Lists.newArrayList(right, left);
    }


    /**
     * 设置串镜检查结果
     *
     * @param result 数据
     * @param age    年龄
     * @return 串镜检查结果列表
     */
    private List<CardDetailsVO.CrossMirrorResult> setCrossMirrorResults(VisionScreeningResult result, Integer age) {
        CardDetailsVO.CrossMirrorResult left = new CardDetailsVO.CrossMirrorResult();
        CardDetailsVO.CrossMirrorResult right = new CardDetailsVO.CrossMirrorResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);

        if (null == result || null == result.getComputerOptometry()) {
            return Lists.newArrayList(right, left);
        }
        ComputerOptometryDO computerOptometry = result.getComputerOptometry();
        VisionDataDO visionData = result.getVisionData();

        // 左眼
        if (Objects.nonNull(visionData) && Objects.nonNull(visionData.getLeftEyeData()) && Objects.nonNull(computerOptometry) && Objects.nonNull(computerOptometry.getLeftEyeData())
                && ObjectsUtil.allNotNull(computerOptometry.getLeftEyeData().getSph(), computerOptometry.getLeftEyeData().getCyl(), visionData.getLeftEyeData().getNakedVision())) {
            left.setMyopia(StatUtil.isMyopia(computerOptometry.getLeftEyeData().getSph(), computerOptometry.getLeftEyeData().getCyl(), age, visionData.getLeftEyeData().getNakedVision()));
            left.setFarsightedness(StatUtil.isHyperopia(computerOptometry.getLeftEyeData().getSph().floatValue(), computerOptometry.getLeftEyeData().getCyl().floatValue(), age));
        }

        // 右眼
        if (Objects.nonNull(visionData) && Objects.nonNull(visionData.getRightEyeData()) && Objects.nonNull(computerOptometry) && Objects.nonNull(computerOptometry.getRightEyeData())
                && ObjectsUtil.allNotNull(computerOptometry.getRightEyeData().getSph(), computerOptometry.getRightEyeData().getCyl(), visionData.getRightEyeData().getNakedVision())) {
            right.setMyopia(StatUtil.isMyopia(computerOptometry.getRightEyeData().getSph(), computerOptometry.getRightEyeData().getCyl(), age, visionData.getRightEyeData().getNakedVision()));
            right.setFarsightedness(StatUtil.isHyperopia(computerOptometry.getRightEyeData().getSph().floatValue(), computerOptometry.getRightEyeData().getCyl().floatValue(), age));
        }

        if (null != result.getOtherEyeDiseases() && !CollectionUtils.isEmpty(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases())) {
            left.setOther(true);
        }
        if (null != result.getOtherEyeDiseases() && !CollectionUtils.isEmpty(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases())) {
            right.setOther(true);
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 其他眼部疾病
     *
     * @param result 其他眼部疾病
     * @return 其他眼病List
     */
    private List<CardDetailsVO.EyeDiseasesResult> setEyeDiseasesResult(OtherEyeDiseasesDO result) {
        CardDetailsVO.EyeDiseasesResult left = new CardDetailsVO.EyeDiseasesResult();
        CardDetailsVO.EyeDiseasesResult right = new CardDetailsVO.EyeDiseasesResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        right.setLateriality(CommonConst.RIGHT_EYE);
        if (null != result) {
            left.setEyeDiseases(result.getLeftEyeData().getEyeDiseases());
            right.setEyeDiseases(result.getRightEyeData().getEyeDiseases());
        } else {
            left.setEyeDiseases(new ArrayList<>());
            right.setEyeDiseases(new ArrayList<>());
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 五种特殊眼病
     *
     * @return List<String>
     */
    private List<String> eyeDiseases() {
        return Lists.newArrayList("眼球震颤", "弱视待排查", "高眼压", "青光眼待排", "大视杯");
    }

    /**
     * 获取斜视疾病
     *
     * @param otherEyeDiseasesList 其他眼病
     * @return 斜视疾病
     */
    private List<String> getSquintList(List<String> otherEyeDiseasesList) {
        if (CollectionUtils.isEmpty(otherEyeDiseasesList)) {
            return new ArrayList<>();
        }
        return ListUtils.retainAll(Lists.newArrayList("内显斜", "外显斜", "内隐斜", "外隐斜", "垂直斜视"), otherEyeDiseasesList);
    }

    /**
     * 获取筛查用户姓名
     *
     * @param visionScreeningResult 筛查数据
     * @return java.lang.String
     **/
    private String getCreateUserName(VisionScreeningResult visionScreeningResult){
        if (Objects.isNull(visionScreeningResult)) {
            return null;
        }
        VisionDataDO visionData = visionScreeningResult.getVisionData();
        Integer visionDataCreateUserId = Optional.ofNullable(visionData).map(VisionDataDO::getCreateUserId).orElse(null);
        if (Objects.nonNull(visionDataCreateUserId)) {
            return getCreateUserName(visionDataCreateUserId);
        }

        ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
        Integer computerOptometryCreateUserId = Optional.ofNullable(computerOptometry).map(ComputerOptometryDO::getCreateUserId).orElse(null);
        if (Objects.nonNull(computerOptometryCreateUserId)) {
            return getCreateUserName(computerOptometryCreateUserId);
        }

        OtherEyeDiseasesDO otherEyeDiseases = visionScreeningResult.getOtherEyeDiseases();
        Integer otherEyeDiseasesCreateUserId = Optional.ofNullable(otherEyeDiseases).map(OtherEyeDiseasesDO::getCreateUserId).orElse(null);
        if (Objects.nonNull(otherEyeDiseasesCreateUserId)) {
            return getCreateUserName(otherEyeDiseasesCreateUserId);
        }
        return null;
    }

    private String getCreateUserName(Integer createUserId){
        User user = oauthServiceClient.getUserDetailByUserId(createUserId);
        return Optional.ofNullable(user).map(User::getRealName).orElse(null);
    }

    /**
     * 获取筛查用户签名
     *
     * @param visionScreeningResult 筛查数据
     * @return java.lang.String
     **/
    private String getVisionCreateUserSignPicUrl(VisionScreeningResult visionScreeningResult){
        if (Objects.isNull(visionScreeningResult)) {
            return null;
        }
        // 优先取眼位的医生签名
        VisionDataDO visionData = visionScreeningResult.getVisionData();
        if (Objects.nonNull(visionData) && Objects.nonNull(visionData.getCreateUserId())) {
            String signPicUrl = getSignPicUrl(visionData.getCreateUserId());
            if (StringUtils.isNotEmpty(signPicUrl)) {
                return signPicUrl;
            }
        }
        return null;
    }
    /**
     * 获取筛查用户签名
     *
     * @param visionScreeningResult 筛查数据
     * @return java.lang.String
     **/
    private String getComputerCreateUserSignPicUrl(VisionScreeningResult visionScreeningResult){
        if (Objects.isNull(visionScreeningResult)) {
            return null;
        }
        // 优先取眼位的医生签名
        ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
        if (Objects.nonNull(computerOptometry) && Objects.nonNull(computerOptometry.getCreateUserId())) {
            String signPicUrl = getSignPicUrl(computerOptometry.getCreateUserId());
            if (StringUtils.isNotEmpty(signPicUrl)) {
                return signPicUrl;
            }
        }
        return null;
    }
    /**
     * 获取签名访问地址
     *
     * @param visionScreeningResult 筛查数据
     * @return java.lang.String
     **/
    private String getSignPicUrl(VisionScreeningResult visionScreeningResult) {
        if (Objects.isNull(visionScreeningResult)) {
            return null;
        }
        // 优先取眼位的医生签名
        OcularInspectionDataDO ocularInspectionData = visionScreeningResult.getOcularInspectionData();
        if (Objects.nonNull(ocularInspectionData) && Objects.nonNull(ocularInspectionData.getCreateUserId())) {
            String signPicUrl = getSignPicUrl(ocularInspectionData.getCreateUserId());
            if (StringUtils.isNotEmpty(signPicUrl)) {
                return signPicUrl;
            }
        }
        // 取裂隙灯医生的签名
        SlitLampDataDO slitLampDataDO = visionScreeningResult.getSlitLampData();
        if (Objects.nonNull(slitLampDataDO) && Objects.nonNull(slitLampDataDO.getCreateUserId())) {
            String signPicUrl = getSignPicUrl(slitLampDataDO.getCreateUserId());
            if (StringUtils.isNotEmpty(signPicUrl)) {
                return signPicUrl;
            }
        }
        return null;
    }

    /**
     * 获取签名访问地址
     *
     * @param screeningOrgStaffUserId 筛查人员的用户ID
     * @return java.lang.String
     **/
    public String getSignPicUrl(Integer screeningOrgStaffUserId) {
        Assert.notNull(screeningOrgStaffUserId, "筛查人员的用户ID为空");
        ScreeningOrganizationStaff screeningOrganizationStaff = screeningOrganizationStaffService.findOne(new ScreeningOrganizationStaff().setUserId(screeningOrgStaffUserId));
        if (Objects.isNull(screeningOrganizationStaff)) {
            return StringUtils.EMPTY;
        }
        return resourceFileService.getResourcePath(screeningOrganizationStaff.getSignFileId());
    }

    /**
     * 设置屈光不正信息
     *
     * @param cardDetail            档案卡信息详情
     * @param visionScreeningResult 筛查数据
     * @param age                   年龄
     * @param status                0-幼儿园 1-中小学版本
     * @return boolean
     **/
    private Boolean setRefractiveErrorInfo(HaiNanCardDetail cardDetail, VisionScreeningResult visionScreeningResult,
                                           Integer age, Integer status) {

        // 2021-10-14需求 获取学生的筛查进度情况
        StudentScreeningProgressVO studentScreeningProgressVO = screeningPlanSchoolStudentService.getStudentScreeningProgress(visionScreeningResult);
        // 初筛项目都没有问题，则视为屈光正常
        if (Boolean.FALSE.equals(studentScreeningProgressVO.getFirstCheckAbnormal())) {
            return false;
        }

        // 幼儿园判断
        VisionDataDO visionData = visionScreeningResult.getVisionData();
        if (status == 0) {
            OcularInspectionDataDO ocularInspectionData = visionScreeningResult.getOcularInspectionData();
            // 视力检查和33cm眼位都正常，为非屈光不正
            if (ObjectsUtil.allNotNull(visionData, ocularInspectionData)
                    && visionData.getDiagnosis().equals(AbstractDiagnosisResult.NORMAL)
                    && ocularInspectionData.getDiagnosis().equals(AbstractDiagnosisResult.NORMAL)) {
                return false;
            }
        }
        // 如果小瞳验光和屈光度数据都没有，则屈光正常
        PupilOptometryDataDO pupilOptometryData = visionScreeningResult.getPupilOptometryData();
        ComputerOptometryDO computerOptometryDO = visionScreeningResult.getComputerOptometry();
        if (ObjectsUtil.allNull(pupilOptometryData, computerOptometryDO)) {
            return null;
        }
        // 获取视力信息，优先取小瞳验光的数据
        TwoTuple<VisionInfoVO, VisionInfoVO> visionInfo = Objects.nonNull(pupilOptometryData) ?
                getVisionInfoByPupilOptometryData(pupilOptometryData, age, visionData) : getVisionInfoByComputerOptometryData(computerOptometryDO, age, visionData);
        VisionInfoVO leftEye = visionInfo.getFirst();
        VisionInfoVO rightEye = visionInfo.getSecond();
        // 是否屈光不正
        Boolean isRefractiveError = isRefractiveError(leftEye, rightEye);
        if (Objects.isNull(isRefractiveError)) {
            return isRefractiveError;
        }
        // 设置近视、远视、散光
        if (Objects.equals(isRefractiveError,Boolean.TRUE) && Objects.nonNull(leftEye)) {
            cardDetail.setLeftMyopiaInfo(leftEye.getMyopiaLevel());
            cardDetail.setLeftFarsightednessInfo(leftEye.getHyperopiaLevel());
            cardDetail.setLeftAstigmatismInfo(leftEye.getAstigmatism());
        }
        if (Objects.equals(isRefractiveError,Boolean.TRUE) && Objects.nonNull(rightEye)) {
            cardDetail.setRightMyopiaInfo(rightEye.getMyopiaLevel());
            cardDetail.setRightFarsightednessInfo(rightEye.getHyperopiaLevel());
            cardDetail.setRightAstigmatismInfo(rightEye.getAstigmatism());
        }
        return isRefractiveError;
    }

    /**
     * 是否屈光不正
     *
     * @param leftEye  左眼数据
     * @param rightEye 右眼数据
     * @return 是否屈光不正
     */
    private Boolean isRefractiveError(VisionInfoVO leftEye, VisionInfoVO rightEye) {
        if (ObjectsUtil.allNull(leftEye, rightEye)) {
            return null;
        }
        if ((Objects.nonNull(leftEye.getMyopiaLevel()) && leftEye.getMyopiaLevel() >= MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)
                || (Objects.nonNull(rightEye.getMyopiaLevel()) && rightEye.getMyopiaLevel() > MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code)) {
            return true;
        }
        if ((Objects.nonNull(leftEye.getHyperopiaLevel()) && leftEye.getHyperopiaLevel() >= HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT.code)
                || (Objects.nonNull(rightEye.getHyperopiaLevel()) && rightEye.getHyperopiaLevel() > HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT.code)) {
            return true;
        }

        if (ObjectsUtil.allNotNull(leftEye.getAstigmatism(), rightEye.getAstigmatism()) && (leftEye.getAstigmatism() || rightEye.getAstigmatism())) {
            return true;
        }
        return null;
    }

    /**
     * 设置是否近视、远视
     *
     * @param visionScreeningResult 数据
     * @param age                   年龄
     * @param cardDetail            档案卡
     */
    private void setMyopiaAndFarsightedness(VisionScreeningResult visionScreeningResult, Integer age, HaiNanCardDetail cardDetail) {
        ComputerOptometryDO computerOptometry = visionScreeningResult.getComputerOptometry();
        VisionDataDO visionData = visionScreeningResult.getVisionData();

        if (ObjectsUtil.hasNull(computerOptometry, visionData)
                || !computerOptometry.valid()
                || !visionData.validNakedVision()) {
            return;
        }
        BigDecimal leftSph = computerOptometry.getLeftEyeData().getSph();
        BigDecimal rightSph = computerOptometry.getRightEyeData().getSph();
        BigDecimal leftCyl = computerOptometry.getLeftEyeData().getCyl();
        BigDecimal rightCyl = computerOptometry.getRightEyeData().getCyl();

        BigDecimal leftNakedVision = visionData.getLeftEyeData().getNakedVision();
        BigDecimal rightNakedVision = visionData.getRightEyeData().getNakedVision();
        // 是否近视
        Boolean leftMyopia = StatUtil.isMyopia(leftSph, leftCyl, age, leftNakedVision);
        Boolean rightMyopia = StatUtil.isMyopia(rightSph, rightCyl, age, rightNakedVision);
        cardDetail.setIsMyopia(StatUtil.getIsExist(leftMyopia,rightMyopia));

        // 是否远视
        Boolean leftHyperopia = StatUtil.isHyperopia(leftSph, leftCyl, age);
        Boolean rightHyperopia = StatUtil.isHyperopia(rightSph, rightCyl, age);
        cardDetail.setIsHyperopia(StatUtil.getIsExist(leftHyperopia,rightHyperopia));

    }

    /**
     * 获取近视情况
     *
     * @param pupilOptometryData 电脑验光数据
     * @param age                年龄
     * @param visionDataDO       视力检查结果
     * @return TwoTuple<VisionInfoVO, VisionInfoVO> left-左眼 right-右眼
     */
    private TwoTuple<VisionInfoVO, VisionInfoVO> getVisionInfoByPupilOptometryData(PupilOptometryDataDO pupilOptometryData,
                                                                                   Integer age, VisionDataDO visionDataDO) {
        if (ObjectsUtil.hasNull(pupilOptometryData, visionDataDO)) {
            return new TwoTuple<>();
        }

        PupilOptometryDataDO.PupilOptometryData leftEyeData = pupilOptometryData.getLeftEyeData();
        PupilOptometryDataDO.PupilOptometryData rightEyeData = pupilOptometryData.getRightEyeData();
        VisionInfoVO leftVision = Objects.isNull(leftEyeData) ? new VisionInfoVO() : getMyopiaLevel(leftEyeData.getSph(), leftEyeData.getCyl(), age, visionDataDO.getLeftEyeData().getNakedVision());
        VisionInfoVO rightVision = Objects.isNull(rightEyeData) ? new VisionInfoVO() : getMyopiaLevel(rightEyeData.getSph(), rightEyeData.getCyl(), age, visionDataDO.getRightEyeData().getNakedVision());
        return new TwoTuple<>(leftVision, rightVision);

    }

    /**
     * 获取近视情况
     *
     * @param computerOptometry 电脑验光数据
     * @param age               年龄
     * @param visionDataDO      视力检查结果
     * @return TwoTuple<VisionInfoVO, VisionInfoVO> left-左眼 right-右眼
     */
    private TwoTuple<VisionInfoVO, VisionInfoVO> getVisionInfoByComputerOptometryData(ComputerOptometryDO computerOptometry,
                                                                                      Integer age, VisionDataDO visionDataDO) {
        if (Objects.isNull(computerOptometry) || Objects.isNull(visionDataDO)) {
            return new TwoTuple<>();
        }
        ComputerOptometryDO.ComputerOptometry leftEyeData = computerOptometry.getLeftEyeData();
        ComputerOptometryDO.ComputerOptometry rightEyeData = computerOptometry.getRightEyeData();
        VisionInfoVO leftVision = Objects.isNull(leftEyeData) ? new VisionInfoVO() : getMyopiaLevel(leftEyeData.getSph(), leftEyeData.getCyl(), age, visionDataDO.getLeftEyeData().getNakedVision());
        VisionInfoVO rightVision = Objects.isNull(rightEyeData) ? new VisionInfoVO() : getMyopiaLevel(rightEyeData.getSph(), rightEyeData.getCyl(), age, visionDataDO.getRightEyeData().getNakedVision());
        return new TwoTuple<>(leftVision, rightVision);
    }

    /**
     * 获取近视预警级别
     *
     * @param sph         球镜
     * @param cyl         柱镜
     * @param nakedVision 裸眼视力
     * @return VisionInfoVO
     */
    private VisionInfoVO getMyopiaLevel(BigDecimal sph, BigDecimal cyl, Integer age, BigDecimal nakedVision) {
        VisionInfoVO visionInfoVO = new VisionInfoVO();
        if (ObjectsUtil.allNotNull(sph, cyl)) {
            // 近视
            MyopiaLevelEnum myopiaWarningLevel = null;
            if (Objects.nonNull(nakedVision)) {
                if ((age < 6 && nakedVision.compareTo(new BigDecimal("4.9")) < 0) || (age >= 6 && nakedVision.compareTo(new BigDecimal("5.0")) < 0)) {
                    myopiaWarningLevel = StatUtil.getMyopiaLevel(sph, cyl);
                }
            }
            // 远视
            HyperopiaLevelEnum farsightednessWarningLevel = StatUtil.getHyperopiaLevel(sph.floatValue(), cyl.floatValue(), age);
            visionInfoVO.setMyopiaLevel(Objects.nonNull(myopiaWarningLevel) ? myopiaWarningLevel.code : null);
            visionInfoVO.setHyperopiaLevel(Objects.nonNull(farsightednessWarningLevel) ? farsightednessWarningLevel.code : null);
        }
        // 散光
        visionInfoVO.setAstigmatism(Objects.nonNull(cyl) && cyl.abs().compareTo(new BigDecimal("0.5")) >= 0);
        return visionInfoVO;
    }


    /**
     * 更新学生统计就诊信息
     *
     * @param studentId 学生Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatConclusion(Integer studentId) {
        // 先查出最新的一条筛查学生
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getLastByStudentId(studentId);
        if (Objects.isNull(planSchoolStudent)) {
            return;
        }
        Student student = studentService.getById(studentId);
        if (Objects.isNull(student)) {
            return;
        }
        StatConclusion statConclusion = statConclusionService.getByPlanStudentId(planSchoolStudent.getId());
        if (Objects.isNull(statConclusion)) {
            return;
        }
        // 判断是否家长手机号码是否为空
        statConclusion.setIsBindMp(StringUtils.isNotBlank(student.getMpParentPhone()));
        statConclusionService.updateById(statConclusion);
    }

    /**
     * 更新绑定家长手机号码
     *
     * @param studentId   学生ID
     * @param parentPhone 家长手机号码
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMpParentPhone(Integer studentId, String parentPhone) {
        List<SchoolStudent> schoolStudents = schoolStudentService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(schoolStudents)) {
            return;
        }
        schoolStudents.forEach(schoolStudent -> {
            String parentPhoneStr = schoolStudent.getMpParentPhone();
            if (StringUtils.isBlank(parentPhoneStr)) {
                // 为空新增
                schoolStudent.setMpParentPhone(parentPhone);
            } else {
                // 家长手机号码是否已经存在
                if (StringUtils.countMatches(parentPhoneStr, parentPhone) == 0) {
                    // 不存在拼接家长手机号码
                    schoolStudent.setMpParentPhone(parentPhoneStr + "," + parentPhone);
                }
            }
        });
        schoolStudentService.updateBatchById(schoolStudents);
    }

    /**
     * 更新医院学生信息
     *
     * @param studentId     学生Id
     * @param committeeCode 行政编码
     * @param recordNo      用户编号
     */
    public void updateHospitalStudentRecordNo(Integer studentId, Long committeeCode, String recordNo) {
        List<HospitalStudent> hospitalStudentList = hospitalStudentService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(hospitalStudentList)) {
            return;
        }
        hospitalStudentList.forEach(hospitalStudent -> {
            hospitalStudent.setCommitteeCode(committeeCode);
            hospitalStudent.setRecordNo(recordNo);
        });
        hospitalStudentService.updateBatchById(hospitalStudentList);
    }

    /**
     * 保存学生，同步到学校端
     *
     * @param student 学生信息
     * @return 学生Id
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudentAndSchoolStudent(Student student) {
        Integer studentId = saveStudent(student);
        // TODO: JS2.02.01-1学校管理后台-自主筛查 ,管理后台-多端管理-用户管理（学生）不同步到学校管理后台

        if (Objects.isNull(student.getSchoolId()) || StringUtils.isBlank(student.getSno())) {
            return studentId;
        }
        SchoolStudent schoolStudent = new SchoolStudent();
        BeanUtils.copyProperties(student, schoolStudent);
        schoolStudent.setId(null);
        schoolStudent.setStudentId(studentId);
        setSchoolStudentInfo(schoolStudent, student.getSchoolId());
        schoolStudentService.saveOrUpdate(schoolStudent);
        return studentId;
    }

    /**
     * 新增学生
     *
     * @param student 学生实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(Student student) {
        // 检查学生年龄
        com.wupol.myopia.base.util.DateUtil.checkBirthday(student.getBirthday());
        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }
        // 检查学生身份证是否重复
        if (studentService.checkIdCardAndPassport(student.getIdCard(), student.getPassport(), null)) {
            throw new BusinessException("学生身份证、护照重复");
        }
        studentService.save(student);
        return student.getId();
    }

    /**
     * 设置学生信息
     *
     * @param schoolStudent 学生
     * @param schoolId      学校Id
     */
    public void setSchoolStudentInfo(SchoolStudent schoolStudent, Integer schoolId) {
        schoolStudent.checkStudentInfo();
        if (!schoolStudentService.getByIdCardAndSnoAndPassport(schoolStudent.getId(), schoolStudent.getIdCard(), schoolStudent.getSno(), schoolStudent.getPassport(), schoolId)) {
            throw new BusinessException("学号、身份证、护照重复");
        }
        schoolStudent.setSchoolId(schoolId);
        SchoolGrade grade = schoolGradeService.getById(schoolStudent.getGradeId());
        schoolStudent.setGradeName(grade.getName());
        schoolStudent.setClassName(schoolClassService.getById(schoolStudent.getClassId()).getName());
        schoolStudent.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        schoolStudent.setSourceClient(SourceClientEnum.MANAGEMENT.type);

        SchoolStudent havaDeletedStudent = schoolStudentService.getByIdCardAndPassport(schoolStudent.getIdCard(), schoolStudent.getPassport(), schoolId);
        if (Objects.nonNull(havaDeletedStudent)) {
            schoolStudent.setId(havaDeletedStudent.getId());
            schoolStudent.setStatus(CommonConst.STATUS_NOT_DELETED);
        }
    }

    /**
     * 通过学生Id获取学生信息
     *
     * @param id 学生Id
     * @return StudentDTO
     */
    public StudentDTO getStudentById(Integer id) {
        StudentDTO student = studentService.getStudentById(id);
        student.setScreeningCodes(getScreeningCode(id));
        student.setBirthdayInfo(com.wupol.myopia.base.util.DateUtil.getAgeInfo(student.getBirthday(), new Date()));
        if (Objects.nonNull(student.getCommitteeCode())) {
            student.setCommitteeLists(districtService.getDistrictPositionDetail(student.getCommitteeCode()));
        }
        return student;
    }

    /**
     * 通过学生Id获取编码
     *
     * @param studentId 学生Id
     * @return 编号
     */
    public List<Long> getScreeningCode(Integer studentId) {
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.getReleasePlanStudentByStudentId(studentId);
        return getScreeningCodesByPlan(planStudentList);
    }

    /**
     * 获取学生编号
     *
     * @param studentPlans 筛查学生计划
     * @return 编号
     */
    public List<Long> getScreeningCodesByPlan(List<ScreeningPlanSchoolStudent> studentPlans) {
        if (CollectionUtils.isEmpty(studentPlans)) {
            return Collections.emptyList();
        }
        return studentPlans.stream().map(ScreeningPlanSchoolStudent::getScreeningCode)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 获取学生预警跟踪档案
     *
     * @param studentId 学生ID
     * @return java.util.List<com.wupol.myopia.business.api.management.domain.vo.StudentWarningArchiveVO>
     **/
    public IPage<StudentWarningArchiveVO> getStudentWarningArchive(PageRequest pageRequest,Integer studentId) {
        Page page = pageRequest.toPage();
        LambdaQueryWrapper<StatConclusion> queryWrapper = Wrappers.lambdaQuery(StatConclusion.class)
                .eq(StatConclusion::getStudentId, studentId)
                .eq(StatConclusion::getIsRescreen, Boolean.FALSE)
                .orderByDesc(StatConclusion::getUpdateTime);
        IPage<StatConclusion> statConclusionPage = statConclusionService.page(page, queryWrapper);

        IPage<StudentWarningArchiveVO> warningArchiveVoPage = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        List<StatConclusion> statConclusionList = statConclusionPage.getRecords();
        if (CollectionUtils.isEmpty(statConclusionList)) {
            return warningArchiveVoPage;
        }
        List<StudentWarningArchiveVO> studentWarningArchiveVOList = new LinkedList<>();
        for (StatConclusion conclusion : statConclusionList) {
            StudentWarningArchiveVO studentWarningArchiveVO = new StudentWarningArchiveVO();
            BeanUtils.copyProperties(conclusion, studentWarningArchiveVO);
            studentWarningArchiveVO.setVisionLabel(conclusion.getWarningLevel());
            studentWarningArchiveVO.setLowVision(Optional.ofNullable(conclusion.getIsLowVision()).map(low-> low ? 1:null).orElse(null));
            // 筛查信息
            studentWarningArchiveVO.setScreeningDate(conclusion.getUpdateTime());
            ScreeningPlan screeningPlan = screeningPlanService.getById(conclusion.getPlanId());
            if (Objects.nonNull(screeningPlan)) {
                studentWarningArchiveVO.setScreeningTitle(screeningPlan.getTitle());
            }
            // 就诊情况
            setVisitInfo(studentWarningArchiveVO, conclusion);
            // 课桌椅信息
            setDeskAndChairInfo(studentWarningArchiveVO);
            studentWarningArchiveVOList.add(studentWarningArchiveVO);
        }
        studentWarningArchiveVOList.sort(Comparator.comparing(StudentWarningArchiveVO::getScreeningDate).reversed());
        warningArchiveVoPage.setRecords(studentWarningArchiveVOList);
        return warningArchiveVoPage;
    }

    /**
     * 设置就诊信息
     *
     * @param studentWarningArchiveVO 预警跟踪档案
     * @param statConclusion          统计结果
     */
    private void setVisitInfo(StudentWarningArchiveVO studentWarningArchiveVO, StatConclusion statConclusion) {

        Integer reportId = statConclusion.getReportId();
        if (Objects.isNull(reportId)) {
            studentWarningArchiveVO.setIsVisited(false);
            return;
        }
        MedicalReport report = medicalReportService.getById(reportId);
        if (Objects.isNull(report)) {
            studentWarningArchiveVO.setIsVisited(false);
            return;
        }
        studentWarningArchiveVO.setIsVisited(true);
        studentWarningArchiveVO.setVisitResult(report.getMedicalContent());
        studentWarningArchiveVO.setGlassesSuggest(report.getGlassesSituation());
    }

    /**
     * 设置课桌椅信息
     *
     * @param studentWarningArchiveVO 预警跟踪信息
     * @return void
     **/
    private void setDeskAndChairInfo(StudentWarningArchiveVO studentWarningArchiveVO) {
        Float height = studentWarningArchiveVO.getHeight();
        Integer schoolAge = studentWarningArchiveVO.getSchoolAge();
        if (Objects.isNull(height) || Objects.isNull(schoolAge)) {
            return;
        }
        List<Integer> deskAndChairType = SchoolAge.KINDERGARTEN.code.equals(schoolAge) ? DeskChairTypeEnum.getKindergartenTypeByHeight(height) : DeskChairTypeEnum.getPrimarySecondaryTypeByHeight(height);
        studentWarningArchiveVO.setDeskType(deskAndChairType);
        studentWarningArchiveVO.setDeskAdviseHeight((int) (height * 0.43));
        studentWarningArchiveVO.setChairType(deskAndChairType);
        studentWarningArchiveVO.setChairAdviseHeight((int) (height * 0.24));
    }

    /**
     * 获取学生就诊列表
     *
     * @param pageRequest 分页请求
     * @param studentId   学生ID
     * @param currentUser 登录用户
     * @param hospitalId  医院Id
     * @return List<MedicalReportDO>
     */
    public IPage<ReportAndRecordDO> getReportList(PageRequest pageRequest, Integer studentId, CurrentUser currentUser, Integer hospitalId) {
        if (!currentUser.isPlatformAdminUser()) {
            hospitalId = currentUser.getOrgId();
        }
        IPage<ReportAndRecordDO> pageReport = medicalReportService.getByStudentIdWithPage(pageRequest, studentId, hospitalId);
        List<ReportAndRecordDO> records = pageReport.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageReport;
        }
        packageReportInfo(records);
        return pageReport;
    }

    /**
     * 设置报告信息
     *
     * @param records 报告
     */
    public void packageReportInfo(List<ReportAndRecordDO> records) {
        // 收集医生Id
        Set<Integer> doctorIds = records.stream().map(ReportAndRecordDO::getDoctorId).collect(Collectors.toSet());
        Map<Integer, String> doctorMap = hospitalDoctorService.listByIds(doctorIds).stream().collect(Collectors.toMap(Doctor::getId, Doctor::getName));
        records.forEach(report -> {
            report.setDoctorName(doctorMap.getOrDefault(report.getDoctorId(), StringUtils.EMPTY));
            if (Objects.nonNull(report.getBirthday())) {
                report.setCreateTimeAge(com.wupol.myopia.base.util.DateUtil.getAgeInfo(report.getBirthday(), report.getCreateTime()));
            }
            ReportConclusion reportConclusion = medicalReportBizService.getReportConclusion(report.getReportId());
            if (Objects.nonNull(reportConclusion)
                    && Objects.nonNull(reportConclusion.getReport())
                    && !CollectionUtils.isEmpty((reportConclusion.getReport().getImageIdList()))) {
                report.setImageFileUrl(resourceFileService.getBatchResourcePath(reportConclusion.getReport().getImageIdList()));
            }
            report.setCheckStatus(DateUtils.isSameDay(report.getCreateTime(), new Date()));
        });
    }

    /**
     * 获取民族信息
     */
    public List<Nation> getNationLists() {
        return NationEnum.getNationList();
    }
}
