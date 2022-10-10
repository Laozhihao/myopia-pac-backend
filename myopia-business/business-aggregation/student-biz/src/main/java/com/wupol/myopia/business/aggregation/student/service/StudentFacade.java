package com.wupol.myopia.business.aggregation.student.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.student.domain.builder.StudentBizBuilder;
import com.wupol.myopia.business.aggregation.student.domain.vo.StudentWarningArchiveVO;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.domain.dto.Nation;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.*;
import com.wupol.myopia.business.core.screening.flow.domain.vo.*;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.ReScreenCardUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.service.TemplateDistrictService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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
    private SchoolService schoolService;


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
            ScreeningInfoDTO screeningInfoDTO = getScreeningDataDetail(result, reScreeningResultMap);
            Integer templateId = getTemplateId(result.getScreeningOrgId(), result.getScreeningType(), Objects.equals(result.getSchoolId(), result.getScreeningOrgId()));

            StudentScreeningResultItemsDTO item = StudentBizBuilder.builderStudentScreeningResultItemsDTO(screeningOrganizationMap, statMap, screeningPlanSchoolStudentMap, studentDTO, result);
            StudentBizBuilder.setStudentScreeningResultItemInfo(item,screeningInfoDTO,templateId);
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
        List<StudentResultDetailsDTO> resultDetail = StudentBizBuilder.packageDTO(currentResult);
        resultDetail.forEach(r -> r.setHeightAndWeightData(currentResult.getHeightAndWeightData()));
        //设置视力信息
        screeningInfoDTO.setVision(resultDetail);
        //设置常见病信息
        screeningInfoDTO.setCommonDiseases(StudentBizBuilder.getCommonDiseases(currentResult));
        //设置复测信息(为初筛且复测项目都完成才设置该模块)
        VisionScreeningResult reScreeningResult = reScreeningResultMap.get(currentResult.getPlanId());
        if (Boolean.FALSE.equals(currentResult.getIsDoubleScreen()) && Objects.nonNull(reScreeningResult) && ObjectUtils.allNotNull(reScreeningResult.getVisionData(), reScreeningResult.getComputerOptometry(), reScreeningResult.getHeightAndWeightData())){
            screeningInfoDTO.setReScreening(ReScreenCardUtil.reScreeningResult(currentResult, reScreeningResultMap.get(currentResult.getPlanId())));
        }
        return screeningInfoDTO;
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
     * 获取机构使用的模板
     *
     * @param screeningOrgId 筛查机构Id
     * @param screeningType  筛查类型
     * @param isSchool  筛查机构是否是学校
     * @return 模板Id
     */
    private Integer getTemplateId(Integer screeningOrgId, Integer screeningType,Boolean isSchool) {
        if (Objects.equals(isSchool,Boolean.FALSE)){
            return getTemplateId(screeningOrgId,screeningType);
        }
        School school = schoolService.getById(screeningOrgId);
        return templateDistrictService.getArchivesByDistrictId(districtService.getProvinceId(school.getDistrictId()), TemplateConstants.getTemplateBizTypeByScreeningType(screeningType));
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
        ThreeTuple<String, String, String> districtInfo = getDistrictInfo(studentInfo.getSchoolDistrictName(), studentInfo.getCityCode(), studentInfo.getAreaCode());
        CardInfoVO cardInfoVO = StudentBizBuilder.getCardInfo(studentInfo,districtInfo);
        cardInfoVO.setScreeningDate(visionScreeningResult.getCreateTime());
        cardInfoVO.setCountNotCooperate(StudentBizBuilder.getCountNotCooperate(visionScreeningResult));
        responseDTO.setInfo(cardInfoVO);

        Integer templateId = getTemplateId(visionScreeningResult.getScreeningOrgId(), visionScreeningResult.getScreeningType(),Objects.equals(visionScreeningResult.getSchoolId(),visionScreeningResult.getScreeningOrgId()));
        return generateCardDetail(visionScreeningResult, studentInfo, templateId, responseDTO, screeningPlanSchoolStudentService.getById(visionScreeningResult.getScreeningPlanSchoolStudentId()));
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
        Integer templateId = getTemplateId(resultList.get(0).getScreeningOrgId(), resultList.get(0).getScreeningType(),Objects.equals(resultList.get(0).getSchoolId(),resultList.get(0).getScreeningOrgId()));

        // 查询学生信息
        List<Integer> studentIds = resultList.stream().map(VisionScreeningResult::getStudentId).collect(Collectors.toList());
        List<StudentDTO> studentInfoList = studentService.getStudentInfoList(studentIds);
        Map<Integer, StudentDTO> studentMaps = studentInfoList.stream().collect(Collectors.toMap(Student::getId, Function.identity()));

        // 计划学生
        List<Integer> planStudentIds = resultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByIds(planStudentIds);
        Map<Integer, ScreeningPlanSchoolStudent> planStudentMaps = planStudents.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));

        return resultList.stream().map(r -> generateStudentCard(r, studentMaps.get(r.getStudentId()), templateId, planStudentMaps.get(r.getScreeningPlanSchoolStudentId()))).collect(Collectors.toList());
    }

    /**
     * 生成档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param studentInfo           学生信息
     * @param templateId            模板Id
     * @param planSchoolStudent     计划学生
     *
     * @return 学生档案卡实体类
     */
    public StudentCardResponseVO generateStudentCard(VisionScreeningResult visionScreeningResult,
                                                     StudentDTO studentInfo, Integer templateId,
                                                     ScreeningPlanSchoolStudent planSchoolStudent) {
        StudentCardResponseVO responseDTO = new StudentCardResponseVO();

        // 获取学生基本信息
        ThreeTuple<String, String, String> districtInfo = getDistrictInfo(studentInfo.getSchoolDistrictName(), studentInfo.getCityCode(), studentInfo.getAreaCode());
        CardInfoVO cardInfoVO = StudentBizBuilder.getCardInfo(studentInfo,districtInfo);
        cardInfoVO.setScreeningDate(visionScreeningResult.getCreateTime());
        cardInfoVO.setCountNotCooperate(StudentBizBuilder.getCountNotCooperate(visionScreeningResult));
        responseDTO.setInfo(cardInfoVO);

        return generateCardDetail(visionScreeningResult, studentInfo, templateId, responseDTO, planSchoolStudent);
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

        responseDTO.setTemplateId(getTemplateId(visionScreeningResult.getScreeningOrgId(), visionScreeningResult.getScreeningType(),Objects.equals(visionScreeningResult.getSchoolId(),visionScreeningResult.getScreeningOrgId())));
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
     * 获取区域信息
     * @param schoolDistrictName
     * @param cityCode
     * @param areaCode
     */
    private ThreeTuple<String,String,String> getDistrictInfo(String schoolDistrictName,Long cityCode,Long areaCode){
        String districtName = districtService.getDistrictName(schoolDistrictName);
        String cityDesc = districtService.getDistrictName(cityCode);
        String areaDesc = districtService.getDistrictName(areaCode);
        return new ThreeTuple<>(districtName,cityDesc,areaDesc);
    }

    /**
     * 获取区域信息
     * @param schoolDistrictId
     * @param cityCode
     * @param areaCode
     */
    private ThreeTuple<String,String,String> getDistrictInfo(Integer schoolDistrictId,Long cityCode,Long areaCode){
        String districtName = districtService.getDistrictNameByDistrictId(schoolDistrictId);
        String cityDesc = districtService.getDistrictName(cityCode);
        String areaDesc = districtService.getDistrictName(areaCode);
        return new ThreeTuple<>(districtName,cityDesc,areaDesc);
    }



    /**
     * 生成档案卡详情
     *
     * @param visionScreeningResult 筛查结果
     * @param studentInfo           学生信息
     * @param templateId            模板Id
     * @param responseDTO           档案卡实体类
     * @param planSchoolStudent     计划学生
     *
     * @return 学生档案卡实体类
     */
    private StudentCardResponseVO generateCardDetail(VisionScreeningResult visionScreeningResult, StudentDTO studentInfo,
                                                     Integer templateId, StudentCardResponseVO responseDTO,
                                                     ScreeningPlanSchoolStudent planSchoolStudent) {
        int age = DateUtil.ageOfNow(studentInfo.getBirthday());
        // 是否全国模板
        if (templateId.equals(TemplateConstants.GLOBAL_TEMPLATE)) {
            // 获取结果记录
            CardDetailsVO cardDetailsVO = StudentBizBuilder.packageCardDetail(visionScreeningResult, age);
            setCardDetailsVOInfo(cardDetailsVO,visionScreeningResult);
            responseDTO.setDetails(cardDetailsVO);
        } else if (templateId.equals(TemplateConstants.HAI_NAN_TEMPLATE)) {
            Integer status = studentInfo.getSchoolAgeStatus();

            HaiNanCardDetail haiNanCardDetail = getHaiNanCardDetail(visionScreeningResult, age, status);
            // 特殊处理海南学生的信息获取源头
            ThreeTuple<String, String, String> districtInfo = getDistrictInfo(planSchoolStudent.getSchoolDistrictId(), planSchoolStudent.getCityCode(), planSchoolStudent.getAreaCode());
            responseDTO.setStatus(status);
            responseDTO.setHaiNanCardDetail(haiNanCardDetail);
            responseDTO.setInfo(StudentBizBuilder.getHeiNanCardInfo(planSchoolStudent, responseDTO.getInfo(),districtInfo));
        } else if (templateId.equals(TemplateConstants.SCREENING_TEMPLATE)) {
            //儿童青少年近视筛查结果记录表
            Integer status = studentInfo.getSchoolAgeStatus();
            responseDTO.setStatus(status);
            MyopiaScreeningResultCardDetail myopiaScreeningResultCardDetail = StudentBizBuilder.packageMyopiaScreeningResultCardDetail(visionScreeningResult, responseDTO);
            setMyopiaScreeningResultCardDetailInfo(myopiaScreeningResultCardDetail,visionScreeningResult);
            responseDTO.setMyopiaScreeningResultCardDetail(myopiaScreeningResultCardDetail);
        }
        return responseDTO;
    }

    /**
     * 获取海南省学生眼疾病筛查单
     * @param visionScreeningResult
     * @param age
     * @param status
     */
    private HaiNanCardDetail getHaiNanCardDetail(VisionScreeningResult visionScreeningResult, int age, Integer status) {
        HaiNanCardDetail haiNanCardDetail = StudentBizBuilder.packageHaiNanCardDetail(visionScreeningResult, age);
        Boolean isRefractiveError = setRefractiveErrorInfo(haiNanCardDetail, visionScreeningResult, age, status);
        StudentBizBuilder.setRefractiveErrorInfo(isRefractiveError,haiNanCardDetail,visionScreeningResult);
        haiNanCardDetail.setSignPicUrl(getSignPicUrl(visionScreeningResult));
        return haiNanCardDetail;
    }


    /**
     * 设置学生档案卡-近视筛查结果记录信息
     *
     * @param myopiaScreeningResultCardDetail
     * @param result
     */
    private void setMyopiaScreeningResultCardDetailInfo(MyopiaScreeningResultCardDetail myopiaScreeningResultCardDetail,VisionScreeningResult result){
        myopiaScreeningResultCardDetail.setVisionSignPicUrl(getCreateUserName(result));
        myopiaScreeningResultCardDetail.setComputerSignPicUrl(getCreateUserName(result));
    }


    /**
     * 设置学生档案卡视力详情
     * @param details
     * @param result
     */
    private void setCardDetailsVOInfo(CardDetailsVO details,VisionScreeningResult result){
        details.setVisionSign(getCreateUserName(result));
        details.setRefractorySign(getCreateUserName(result));
        details.setCrossMirrorSign(getCreateUserName(result));
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
        return StudentBizBuilder.getIsRefractiveError(cardDetail, visionScreeningResult, age, status);

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
        // TODO: JS2.02.01-1学校管理后台-自主筛查 ,管理后台-多端管理-用户管理（学生）不同步到学校管理后台(代码移除，要看代码查看历史记录)
        return saveStudent(student);
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
        checkSnoAndIdCardAndPassport(schoolStudent, schoolId);
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
     * 检查学号、身份证、护照是否重复
     * @param schoolStudent
     * @param schoolId
     */
    private void checkSnoAndIdCardAndPassport(SchoolStudent schoolStudent, Integer schoolId) {
        List<SchoolStudent> schoolStudentList = schoolStudentService.listByIdCardAndSnoAndPassport(schoolStudent.getId(), schoolStudent.getIdCard(), schoolStudent.getSno(), schoolStudent.getPassport(), schoolId);
        if (CollUtil.isNotEmpty(schoolStudentList)){
            checkParam(schoolStudent, schoolStudentList,SchoolStudent::getSno,"学号重复");
            checkParam(schoolStudent, schoolStudentList,SchoolStudent::getIdCard,"身份证重复");
            checkParam(schoolStudent, schoolStudentList,SchoolStudent::getPassport,"护照重复");
        }
    }

    /**
     * 检查参数
     * @param schoolStudent
     * @param schoolStudentList
     * @param function
     * @param errorMsg
     */
    private void checkParam(SchoolStudent schoolStudent, List<SchoolStudent> schoolStudentList,Function<SchoolStudent,String> function,String errorMsg) {
        if (StrUtil.isNotBlank(getValue(schoolStudent,function))){
            List<SchoolStudent> schoolStudents = schoolStudentList.stream().filter(student -> Objects.equals(getValue(student,function),getValue(schoolStudent,function))).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(schoolStudents)){
                throw new BusinessException(errorMsg);
            }
        }
    }

    /**
     * 获取学校学生的参数值
     * @param schoolStudent
     * @param function
     */
    private String getValue(SchoolStudent schoolStudent,Function<SchoolStudent,String> function){
        return Optional.ofNullable(schoolStudent).map(function).orElse(null);
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
        return StudentBizBuilder.getScreeningCodesByPlan(planStudentList);
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
            studentWarningArchiveVO.setLowVision(StudentBizBuilder.getLowVision(conclusion.getIsLowVision()));
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
     * 获取民族信息
     */
    public List<Nation> getNationLists() {
        return NationEnum.getNationList();
    }
}
