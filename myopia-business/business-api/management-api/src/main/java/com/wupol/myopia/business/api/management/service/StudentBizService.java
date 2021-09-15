package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.constant.VisionScreeningConst;
import com.wupol.myopia.business.api.management.domain.vo.VisionInfoVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.constant.GlassesType;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentResultDetailsDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningResultItemsDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningResultResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.*;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningResultUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganizationStaff;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationStaffService;
import com.wupol.myopia.business.core.system.constants.TemplateConstants;
import com.wupol.myopia.business.core.system.service.TemplateDistrictService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学生
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class StudentBizService {

    @Resource
    private StudentService studentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Resource
    private DistrictService districtService;

    @Resource
    private MedicalReportService medicalReportService;

    @Resource
    private VistelToolsService vistelToolsService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private TemplateDistrictService templateDistrictService;

    @Autowired
    private ScreeningOrganizationStaffService screeningOrganizationStaffService;

    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 获取学生列表
     *
     * @param pageRequest     分页
     * @param studentQueryDTO 请求体
     * @return IPage<Student> {@link IPage}
     */
    public IPage<StudentDTO> getStudentLists(PageRequest pageRequest, StudentQueryDTO studentQueryDTO) {

        TwoTuple<List<Integer>, List<Integer>> conditionalFilter = studentService.conditionalFilter(
                studentQueryDTO.getGradeIds(), studentQueryDTO.getVisionLabels());

        IPage<StudentDTO> pageStudents = studentService.getStudentListByCondition(pageRequest,
                studentQueryDTO, conditionalFilter);
        List<StudentDTO> students = pageStudents.getRecords();

        // 为空直接放回
        if (CollectionUtils.isEmpty(students)) {
            return pageStudents;
        }
        // 获取学生ID
        List<Integer> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());

        // 筛查次数
        List<StudentScreeningCountDTO> studentScreeningCountVOS = visionScreeningResultService.countScreeningTime();
        Map<Integer, Integer> countMaps = studentScreeningCountVOS.stream().collect(Collectors
                .toMap(StudentScreeningCountDTO::getStudentId,
                        StudentScreeningCountDTO::getCount));

        // 获取就诊记录
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentIds(studentIds);
        Map<Integer, List<ReportAndRecordDO>> visitMap = visitLists.stream()
                .collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId));

        // 获取筛查记录
        List<ScreeningPlanSchoolStudent> plans = screeningPlanSchoolStudentService.getByStudentIds(studentIds);
        Map<Integer, List<ScreeningPlanSchoolStudent>> studentPlans = plans.stream()
                .collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getStudentId));

        // 封装DTO
        for (StudentDTO student : students) {
            // 筛查次数
            student.setScreeningCount(countMaps.getOrDefault(student.getId(), 0));
            // 筛查码
            student.setScreeningCodes(getScreeningCodesByPlan(studentPlans.get(student.getId())));
            if (Objects.nonNull(visitMap.get(student.getId()))) {
                // 就诊次数
                student.setNumOfVisits(visitMap.get(student.getId()).size());
            } else {
                student.setNumOfVisits(0);
            }
            // 问卷次数
            student.setQuestionnaireCount(0);
        }
        return pageStudents;
    }

    public StudentDTO getStudentById(Integer id) {
        StudentDTO student = studentService.getStudentById(id);
        student.setScreeningCodes(getScreeningCodesByPlan(screeningPlanSchoolStudentService.getByStudentId(id)));
        return student;
    }

    /**
     * 获取学生筛查档案
     *
     * @param studentId 学生ID
     * @return 学生档案卡返回体
     */
    public StudentScreeningResultResponseDTO getScreeningList(Integer studentId) {
        StudentScreeningResultResponseDTO responseDTO = new StudentScreeningResultResponseDTO();
        List<StudentScreeningResultItemsDTO> items = new ArrayList<>();

        // 通过学生id查询结果
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByStudentId(studentId);

        for (VisionScreeningResult result : resultList) {
            StudentScreeningResultItemsDTO item = new StudentScreeningResultItemsDTO();
            List<StudentResultDetailsDTO> resultDetail = packageDTO(result);
            item.setDetails(resultDetail);
            item.setScreeningDate(result.getUpdateTime());
            // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样的
            if (null != result.getVisionData() && null != result.getVisionData().getLeftEyeData() && null != result.getVisionData().getLeftEyeData().getGlassesType()) {
                item.setGlassesType(WearingGlassesSituation.getType(result.getVisionData().getLeftEyeData().getGlassesType()));
            }
            item.setResultId(result.getId());
            item.setIsDoubleScreen(result.getIsDoubleScreen());
            item.setTemplateId(getTemplateId(result.getScreeningOrgId()));
            items.add(item);
        }
        responseDTO.setTotal(resultList.size());
        responseDTO.setItems(items);
        return responseDTO;
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
            packageVisionResult(result, leftDetails, rightDetails);
        }
        if (null != result.getComputerOptometry()) {
            // 电脑验光
            packageComputerOptometryResult(result, leftDetails, rightDetails);
        }
        if (null != result.getBiometricData()) {
            // 生物测量
            packageBiometricDataResult(result, leftDetails, rightDetails);
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
     * @param result       原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    private void packageVisionResult(VisionScreeningResult result, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼-视力检查结果
        leftDetails.setGlassesType(WearingGlassesSituation.getType(result.getVisionData().getLeftEyeData().getGlassesType()));
        leftDetails.setCorrectedVision(result.getVisionData().getLeftEyeData().getCorrectedVision());
        leftDetails.setNakedVision(result.getVisionData().getLeftEyeData().getNakedVision());

        // 右眼-视力检查结果
        rightDetails.setGlassesType(WearingGlassesSituation.getType(result.getVisionData().getRightEyeData().getGlassesType()));
        rightDetails.setCorrectedVision(result.getVisionData().getRightEyeData().getCorrectedVision());
        rightDetails.setNakedVision(result.getVisionData().getRightEyeData().getNakedVision());
    }

    /**
     * 封装电脑验光
     *
     * @param result       原始视力筛查结果
     * @param leftDetails  左眼数据
     * @param rightDetails 右眼数据
     */
    private void packageComputerOptometryResult(VisionScreeningResult result, StudentResultDetailsDTO leftDetails, StudentResultDetailsDTO rightDetails) {
        // 左眼--电脑验光
        leftDetails.setAxial(result.getComputerOptometry().getLeftEyeData().getAxial());
        leftDetails.setSe(calculationSE(result.getComputerOptometry().getLeftEyeData().getSph(),
                result.getComputerOptometry().getLeftEyeData().getCyl()));
        leftDetails.setCyl(result.getComputerOptometry().getLeftEyeData().getCyl());
        leftDetails.setSph(result.getComputerOptometry().getLeftEyeData().getSph());

        // 左眼--电脑验光
        rightDetails.setAxial(result.getComputerOptometry().getRightEyeData().getAxial());
        rightDetails.setSe(calculationSE(result.getComputerOptometry().getRightEyeData().getSph(),
                result.getComputerOptometry().getRightEyeData().getCyl()));
        rightDetails.setCyl(result.getComputerOptometry().getRightEyeData().getCyl());
        rightDetails.setSph(result.getComputerOptometry().getRightEyeData().getSph());
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
        leftDetails.setAD(result.getBiometricData().getLeftEyeData().getAd());
        leftDetails.setAL(result.getBiometricData().getLeftEyeData().getAl());
        leftDetails.setCCT(result.getBiometricData().getLeftEyeData().getCct());
        leftDetails.setLT(result.getBiometricData().getLeftEyeData().getLt());
        leftDetails.setWTW(result.getBiometricData().getLeftEyeData().getWtw());

        // 右眼--生物测量
        rightDetails.setAD(result.getBiometricData().getRightEyeData().getAd());
        rightDetails.setAL(result.getBiometricData().getRightEyeData().getAl());
        rightDetails.setCCT(result.getBiometricData().getRightEyeData().getCct());
        rightDetails.setLT(result.getBiometricData().getRightEyeData().getLt());
        rightDetails.setWTW(result.getBiometricData().getRightEyeData().getWtw());
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
        leftDetails.setEyeDiseases(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases());
        // 右眼--眼部疾病
        rightDetails.setEyeDiseases(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases());
    }

    /**
     * 计算 等效球镜
     *
     * @param sph 球镜
     * @param cyl 柱镜
     * @return 等效球镜
     */
    private BigDecimal calculationSE(BigDecimal sph, BigDecimal cyl) {
        if (Objects.isNull(sph) || Objects.isNull(cyl)) {
            return null;
        }
        return sph.add(cyl.multiply(new BigDecimal("0.5")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 通过筛查学生Id获取学生档案卡
     *
     * @param planStudentId 筛查学生
     * @return 学生档案卡实体类
     */
    public List<StudentCardResponseVO> getCardDetailByPlanStudentId(Integer planStudentId) {
        VisionScreeningResult result = visionScreeningResultService.getByPlanStudentId(planStudentId);
        if (Objects.isNull(result)) {
            return new ArrayList<>();
        }
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(result.getId());
        return Lists.newArrayList(getStudentCardResponseDTO(visionScreeningResult));
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

        Integer templateId = getTemplateId(visionScreeningResult.getScreeningOrgId());
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
        Integer templateId = getTemplateId(resultList.get(0).getScreeningOrgId());

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
            responseDTO.setStatus(studentInfo.getSchoolAgeStatus());
            responseDTO.setHaiNanCardDetail(packageHaiNanCardDetail(visionScreeningResult, age));
        }
        return responseDTO;
    }

    /**
     * 获取机构使用的模板
     *
     * @param screeningOrgId 筛查机构Id
     * @return 模板Id
     */
    private Integer getTemplateId(Integer screeningOrgId) {
        ScreeningOrganization org = screeningOrganizationService.getById(screeningOrgId);
        return templateDistrictService.getByDistrictId(districtService.getProvinceId(org.getDistrictId()));
    }

    /**
     * 设置学生基本信息
     *
     * @param studentInfo 学生
     * @return 学生档案卡基本信息
     */
    private CardInfoVO getCardInfo(StudentDTO studentInfo) {
        CardInfoVO cardInfoVO = new CardInfoVO();

        cardInfoVO.setName(studentInfo.getName());
        cardInfoVO.setBirthday(studentInfo.getBirthday());
        cardInfoVO.setIdCard(studentInfo.getIdCard());
        cardInfoVO.setGender(studentInfo.getGender());
        cardInfoVO.setAge(DateUtil.ageOfNow(studentInfo.getBirthday()));
        cardInfoVO.setSno(studentInfo.getSno());
        cardInfoVO.setParentPhone(studentInfo.getParentPhone());

        cardInfoVO.setSchoolName(studentInfo.getSchoolName());
        cardInfoVO.setClassName(studentInfo.getClassName());
        cardInfoVO.setGradeName(studentInfo.getGradeName());
        cardInfoVO.setDistrictName(districtService.getDistrictName(studentInfo.getSchoolDistrictName()));
        return cardInfoVO;
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
        VisionDataDO visionData = result.getVisionData();

        // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样
        CardDetailsVO.GlassesTypeObj glassesTypeObj = new CardDetailsVO.GlassesTypeObj();
        if (Objects.nonNull(visionData)) {
            glassesTypeObj.setType(visionData.getLeftEyeData().getGlassesType());
            details.setGlassesTypeObj(glassesTypeObj);
        }

        details.setVisionResults(setVisionResult(visionData));
        details.setRefractoryResults(setRefractoryResults(result.getComputerOptometry()));
        details.setCrossMirrorResults(setCrossMirrorResults(result, age));
        details.setEyeDiseasesResult(setEyeDiseasesResult(result.getOtherEyeDiseases()));
        return details;
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

        // 左眼
        if (Objects.nonNull(computerOptometry) && Objects.nonNull(computerOptometry.getLeftEyeData())
                && ObjectsUtil.allNotNull(computerOptometry.getLeftEyeData().getSph(), computerOptometry.getLeftEyeData().getCyl())) {
            left.setMyopia(StatUtil.isMyopia(computerOptometry.getLeftEyeData().getSph().floatValue(), computerOptometry.getLeftEyeData().getCyl().floatValue()));
            left.setFarsightedness(StatUtil.isHyperopia(computerOptometry.getLeftEyeData().getSph().floatValue(), computerOptometry.getLeftEyeData().getCyl().floatValue(), age));
        }

        // 右眼
        if (Objects.nonNull(computerOptometry) && Objects.nonNull(computerOptometry.getRightEyeData())
                && ObjectsUtil.allNotNull(computerOptometry.getRightEyeData().getSph(), computerOptometry.getRightEyeData().getCyl())) {
            right.setMyopia(StatUtil.isMyopia(computerOptometry.getRightEyeData().getSph().floatValue(), computerOptometry.getRightEyeData().getCyl().floatValue()));
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
     * 删除学生
     *
     * @param id 学生id
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletedStudent(Integer id) {
        if (checkStudentHavePlan(id)) {
            throw new BusinessException("该学生有对应的筛查计划，无法进行删除");
        }
        Student student = new Student();
        student.setId(id);
        student.setStatus(CommonConst.STATUS_IS_DELETED);
        return studentService.updateById(student);
    }

    /**
     * 检查学生是否有筛查计划
     *
     * @param studentId 学生ID
     * @return true-存在筛查计划 false-不存在
     */
    private boolean checkStudentHavePlan(Integer studentId) {
        return !CollectionUtils.isEmpty(screeningPlanSchoolStudentService.getByStudentId(studentId));
    }

    /**
     * 更新学生实体并返回统计信息
     *
     * @param student 学生实体
     * @return 学生
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudentReturnCountInfo(Student student) {
        haveIdCardOrCode(student);
        StudentDTO studentDTO = studentService.updateStudent(student);
        studentDTO.setScreeningCount(student.getScreeningCount())
                .setQuestionnaireCount(student.getQuestionnaireCount());
        // 就诊次数
        List<ReportAndRecordDO> reportList = medicalReportService.getByStudentId(student.getId());
        if (CollectionUtils.isEmpty(reportList)) {
            studentDTO.setNumOfVisits(reportList.size());
        } else {
            studentDTO.setNumOfVisits(0);
        }
        return studentDTO;
    }

    /**
     * 获取学生就诊列表
     *
     * @param pageRequest 分页请求
     * @param studentId   学生ID
     * @return List<MedicalReportDO>
     */
    public IPage<ReportAndRecordDO> getReportList(PageRequest pageRequest, Integer studentId) {
        return medicalReportService.getByStudentIdWithPage(pageRequest, studentId);
    }

    /**
     * 发送短信
     *
     * @param studentMaps 学生Maps
     * @return Consumer<VisionScreeningResult>
     */
    public Consumer<VisionScreeningResult> getVisionScreeningResultConsumer(Map<Integer, Student> studentMaps) {
        return result -> {
            Student student = studentMaps.get(result.getStudentId());
            VisionDataDO visionData = result.getVisionData();
            ComputerOptometryDO computerOptometry = result.getComputerOptometry();
            if (Objects.isNull(visionData)) {
                return;
            }
            VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
            VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();

            BigDecimal leftNakedVision = leftEyeData.getNakedVision();
            BigDecimal leftCorrectedVision = leftEyeData.getCorrectedVision();
            BigDecimal rightNakedVision = rightEyeData.getNakedVision();
            BigDecimal rightCorrectedVision = rightEyeData.getCorrectedVision();

            // 左右眼的裸眼视力都是为空直接返回
            if (Objects.isNull(leftNakedVision) && Objects.isNull(rightNakedVision)) {
                return;
            }

            TwoTuple<BigDecimal, Integer> nakedVisionResult = ScreeningResultUtil.getResultVision(leftNakedVision, rightNakedVision);
            Integer glassesType = leftEyeData.getGlassesType();

            // 裸眼视力是否小于4.9
            if (nakedVisionResult.getFirst().compareTo(new BigDecimal("4.9")) < 0) {
                // 是否佩戴眼镜
                String noticeInfo;
                if (glassesType >= GlassesType.FRAME_GLASSES.code) {
                    noticeInfo = getSMSNoticeInfo(student.getName(), leftNakedVision, rightNakedVision,
                            getWearingGlassesConclusion(leftCorrectedVision, rightCorrectedVision,
                                    leftNakedVision, rightNakedVision, nakedVisionResult));
                } else {
                    // 没有佩戴眼镜
                    noticeInfo = getSMSNoticeInfo(student.getName(),
                            leftNakedVision, rightNakedVision,
                            "裸眼视力下降，建议：请到医疗机构接受检查，明确诊断并及时采取措施。");
                }
                // 发送短信
                sendSMS(str2List(student.getMpParentPhone()), student.getParentPhone(), noticeInfo, result);
            } else {
                if (Objects.isNull(computerOptometry)) {
                    return;
                }
                BigDecimal leftSph = computerOptometry.getLeftEyeData().getSph();
                BigDecimal leftCyl = computerOptometry.getLeftEyeData().getCyl();
                BigDecimal rightSph = computerOptometry.getRightEyeData().getSph();
                BigDecimal rightCyl = computerOptometry.getRightEyeData().getCyl();
                BigDecimal leftSe = ScreeningResultUtil.calculationSE(leftSph, leftCyl);
                BigDecimal rightSe = ScreeningResultUtil.calculationSE(rightSph, rightCyl);
                // 裸眼视力大于4.9
                String noticeInfo = getSMSNoticeInfo(student.getName(),
                        leftNakedVision, rightNakedVision,
                        nakedVisionNormal(leftNakedVision, rightNakedVision,
                                leftSe, rightSe, nakedVisionResult));
                // 发送短信
                sendSMS(str2List(student.getMpParentPhone()), student.getParentPhone(), noticeInfo, result);
            }
        };
    }

    /**
     * 戴镜获取结论
     *
     * @param leftCorrectedVision  左眼矫正视力
     * @param rightCorrectedVision 右眼矫正视力
     * @param leftNakedVision      左眼裸眼视力
     * @param rightNakedVision     右眼裸眼视力
     * @param nakedVisionResult    取视力值低的眼球
     * @return 结论
     */
    private String getWearingGlassesConclusion(BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                               BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                               TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        if (Objects.isNull(leftCorrectedVision) && Objects.isNull(rightCorrectedVision)) {
            return "";
        }
        BigDecimal visionVal = ScreeningResultUtil.getResultVision(leftCorrectedVision, rightCorrectedVision,
                leftNakedVision, rightNakedVision, nakedVisionResult);
        if (visionVal.compareTo(new BigDecimal("4.9")) < 0) {
            // 矫正视力小于4.9
            return "裸眼视力下降，建议：请及时到医疗机构复查。";
        } else {
            // 矫正视力大于4.9
            return "裸眼视力下降，建议：3个月或半年复查视力。";
        }
    }

    /**
     * 正常裸眼视力获取结论
     *
     * @param leftNakedVision   左眼裸眼视力
     * @param rightNakedVision  右眼裸眼视力
     * @param leftSe            左眼等效球镜
     * @param rightSe           右眼等效球镜
     * @param nakedVisionResult 取视力值低的眼球
     * @return 结论
     */
    private String nakedVisionNormal(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                     BigDecimal leftSe, BigDecimal rightSe,
                                     TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        BigDecimal se = ScreeningResultUtil.getSE(leftNakedVision, rightNakedVision,
                leftSe, rightSe, nakedVisionResult);
        // SE >= 0
        if (se.compareTo(new BigDecimal("0.00")) >= 0) {
            return "建议：目前尚无近视高危风险。";
        } else {
            // SE < 0
            return "建议：可能存在近视高危因素，建议严格注意用眼卫生，到医疗机构检查了解是否可能发展为近视。";
        }
    }

    /**
     * 获取短信通知详情
     *
     * @param studentName      学校名称
     * @param leftNakedVision  左眼裸眼视力
     * @param rightNakedVision 右眼裸眼视力
     * @param advice           建议
     * @return 短信通知详情
     */
    private String getSMSNoticeInfo(String studentName, BigDecimal leftNakedVision, BigDecimal rightNakedVision, String advice) {
        if (Objects.isNull(leftNakedVision)) {
            return String.format(CommonConst.SEND_SMS_TO_PARENT_MESSAGE, packageStudentName(studentName),
                    "--", rightNakedVision.toString(), advice);
        }
        if (Objects.isNull(rightNakedVision)) {
            return String.format(CommonConst.SEND_SMS_TO_PARENT_MESSAGE, packageStudentName(studentName),
                    leftNakedVision, "--", advice);
        }
        return String.format(CommonConst.SEND_SMS_TO_PARENT_MESSAGE, packageStudentName(studentName),
                leftNakedVision, rightNakedVision, advice);
    }

    /**
     * 封装短信内容需要的学生姓名
     * <p>超过4个字符以上：显示前5个字符，其中前3个字符正常回显，后2个字符用*代替。
     * 如陈旭格->陈旭格、陈旭格力->陈旭格力、陈旭格力哈->陈旭格**、陈旭格力哈特->陈旭格**
     * </p>
     *
     * @param studentName 学生姓名
     * @return 学生姓名
     */
    private String packageStudentName(String studentName) {
        if (studentName.length() < 5) {
            return studentName;
        }
        return StringUtils.overlay(studentName, "**", 3, studentName.length());
    }

    /**
     * String 转换成List
     *
     * @param string 字符串
     * @return 字符串
     */
    public static List<String> str2List(String string) {
        if (StringUtils.isNotBlank(string)) {
            return Arrays.stream(string.split(",")).map(String::valueOf)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 发送短信
     *
     * @param mpParentPhone 家长端绑定的手机号码
     * @param parentPhone   多端绑定的手机号码
     * @param noticeInfo    短信内容
     * @param result        筛查数据
     */
    private void sendSMS(List<String> mpParentPhone, String parentPhone, String noticeInfo, VisionScreeningResult result) {
        // 优先家长端绑定的手机号码
        if (com.wupol.framework.core.util.CollectionUtils.isNotEmpty(mpParentPhone)) {
            mpParentPhone.forEach(phone -> {
                MsgData msgData = new MsgData(phone, "+86", noticeInfo);
                SmsResult smsResult = vistelToolsService.sendMsg(msgData);
                checkSendMsgStatus(smsResult, msgData, result);
            });
            return;
        }
        if (StringUtils.isNotBlank(parentPhone)) {
            MsgData msgData = new MsgData(parentPhone, "+86", noticeInfo);
            SmsResult smsResult = vistelToolsService.sendMsg(msgData);
            checkSendMsgStatus(smsResult, msgData, result);
        }
    }

    /**
     * 检查发送短信是否成功
     *
     * @param smsResult 发送结果
     * @param msgData   请求参数
     * @param result    筛查结果
     */
    private void checkSendMsgStatus(SmsResult smsResult, MsgData msgData, VisionScreeningResult result) {
        if (smsResult.isSuccessful()) {
            result.setIsNotice(true);
        } else {
            log.error("发送通知到手机号码错误，提交信息:{}, 异常信息:{}", JSONObject.toJSONString(msgData), smsResult);
        }
    }

    /**
     * 封装海南档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param age                   年轻
     * @return HaiNanCardDetail
     */
    private HaiNanCardDetail packageHaiNanCardDetail(VisionScreeningResult visionScreeningResult, Integer age) {
        HaiNanCardDetail cardDetail = new HaiNanCardDetail();
        if (Objects.isNull(visionScreeningResult)) {
            return cardDetail;
        }
        BeanUtils.copyProperties(visionScreeningResult, cardDetail);
        cardDetail.setVisionDataDO(visionScreeningResult.getVisionData());
        cardDetail.setRemark(Objects.nonNull(visionScreeningResult.getFundusData()) ? visionScreeningResult.getFundusData().getRemark() : "");
        // 其他眼部疾病
        List<String> otherEyeDiseasesList = getOtherEyeDiseasesList(visionScreeningResult);
        // 设置屈光不正信息
        boolean isRefractiveError = setRefractiveErrorInfo(cardDetail, visionScreeningResult, age);
        // 其他眼病
        cardDetail.setOtherEyeDiseases(otherEyeDiseasesList);
        cardDetail.setIsRefractiveError(isRefractiveError);
        // 眼斜
        cardDetail.setSquint(getSquintList(otherEyeDiseasesList));
        cardDetail.setIsNormal(Objects.nonNull(isRefractiveError) && !isRefractiveError && CollectionUtils.isEmpty(otherEyeDiseasesList));
        cardDetail.setSignPicUrl(getSignPicUrl(visionScreeningResult));
        return cardDetail;
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
            return getSignPicUrl(ocularInspectionData.getCreateUserId());
        }
        // 取裂隙灯医生的签名
        SlitLampDataDO slitLampDataDO = visionScreeningResult.getSlitLampData();
        if (Objects.nonNull(slitLampDataDO) && Objects.nonNull(slitLampDataDO.getCreateUserId())) {
            return getSignPicUrl(slitLampDataDO.getCreateUserId());
        }
        return null;
    }

    /**
     * 获取签名访问地址
     *
     * @param screeningOrgStaffUserId 筛查人员的用户ID
     * @return java.lang.String
     **/
    private String getSignPicUrl(Integer screeningOrgStaffUserId){
        Assert.notNull(screeningOrgStaffUserId, "筛查人员的用户ID为空");
        ScreeningOrganizationStaff screeningOrganizationStaff = screeningOrganizationStaffService.findOne(new ScreeningOrganizationStaff().setUserId(screeningOrgStaffUserId));
        return resourceFileService.getResourcePath(screeningOrganizationStaff.getSignFileId());
    }

    /**
     *  设置屈光不正信息
     *
     * @param cardDetail 档案卡信息详情
     * @param visionScreeningResult 筛查数据
     * @param age 年龄
     * @return boolean
     **/
    private boolean setRefractiveErrorInfo(HaiNanCardDetail cardDetail, VisionScreeningResult visionScreeningResult, Integer age) {
        // 获取学生的筛查进度情况
        StudentScreeningProgressVO studentScreeningProgressVO = screeningPlanSchoolStudentService.getStudentScreeningProgress(visionScreeningResult);
        // 初筛项目都没有问题，则视为屈光正常
        if (Boolean.FALSE.equals(studentScreeningProgressVO.getHasAbnormal())) {
            return false;
        }
        // 如果小瞳验光和屈光度数据都没有，则屈光正常
        PupilOptometryDataDO pupilOptometryData = visionScreeningResult.getPupilOptometryData();
        ComputerOptometryDO computerOptometryDO = visionScreeningResult.getComputerOptometry();
        if (ObjectsUtil.allNull(pupilOptometryData, computerOptometryDO)) {
            return false;
        }
        // 获取视力信息，优先取小瞳验光的数据
        TwoTuple<VisionInfoVO, VisionInfoVO> visionInfo = Objects.nonNull(pupilOptometryData) ? getVisionInfoByPupilOptometryData(pupilOptometryData, age) : getVisionInfoByComputerOptometryData(computerOptometryDO, age);
        VisionInfoVO leftEye = visionInfo.getFirst();
        VisionInfoVO rightEye = visionInfo.getSecond();
        // 是否屈光不正
        boolean isRefractiveError = isRefractiveError(leftEye, rightEye);
        // 设置近视、远视、散光
        if (isRefractiveError && Objects.nonNull(leftEye)) {
            cardDetail.setLeftMyopiaInfo(leftEye.getMyopiaLevel());
            cardDetail.setLeftFarsightednessInfo(leftEye.getFarsightednessLevel());
            cardDetail.setLeftAstigmatismInfo(leftEye.getAstigmatism());
        }
        if (isRefractiveError && Objects.nonNull(rightEye)) {
            cardDetail.setRightMyopiaInfo(rightEye.getMyopiaLevel());
            cardDetail.setRightFarsightednessInfo(rightEye.getFarsightednessLevel());
            cardDetail.setRightAstigmatismInfo(rightEye.getAstigmatism());
        }
        return isRefractiveError;
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
     * 获取近视情况
     *
     * @param pupilOptometryData 电脑验光数据
     * @param age               年龄
     * @return TwoTuple<VisionInfoVO, VisionInfoVO> left-左眼 right-右眼
     */
    private TwoTuple<VisionInfoVO, VisionInfoVO> getVisionInfoByPupilOptometryData(PupilOptometryDataDO pupilOptometryData, Integer age) {
        if (Objects.isNull(pupilOptometryData)) {
            return new TwoTuple<>();
        }
        PupilOptometryDataDO.PupilOptometryData leftEyeData = pupilOptometryData.getLeftEyeData();
        PupilOptometryDataDO.PupilOptometryData rightEyeData = pupilOptometryData.getRightEyeData();
        VisionInfoVO leftVision = Objects.isNull(leftEyeData) ?  new VisionInfoVO() : getMyopiaLevel(leftEyeData.getSph(), leftEyeData.getCyl(), age);
        VisionInfoVO rightVision = Objects.isNull(rightEyeData) ?  new VisionInfoVO() : getMyopiaLevel(rightEyeData.getSph(), rightEyeData.getCyl(), age);
        return new TwoTuple<>(leftVision, rightVision);

    }

    /**
     * 获取近视情况
     *
     * @param computerOptometry 电脑验光数据
     * @param age               年龄
     * @return TwoTuple<VisionInfoVO, VisionInfoVO> left-左眼 right-右眼
     */
    private TwoTuple<VisionInfoVO, VisionInfoVO> getVisionInfoByComputerOptometryData(ComputerOptometryDO computerOptometry, Integer age) {
        if (Objects.isNull(computerOptometry)) {
            return new TwoTuple<>();
        }
        ComputerOptometryDO.ComputerOptometry leftEyeData = computerOptometry.getLeftEyeData();
        ComputerOptometryDO.ComputerOptometry rightEyeData = computerOptometry.getRightEyeData();
        VisionInfoVO leftVision = Objects.isNull(leftEyeData) ?  new VisionInfoVO() : getMyopiaLevel(leftEyeData.getSph(), leftEyeData.getCyl(), age);
        VisionInfoVO rightVision = Objects.isNull(rightEyeData) ?  new VisionInfoVO() : getMyopiaLevel(rightEyeData.getSph(), rightEyeData.getCyl(), age);
        return new TwoTuple<>(leftVision, rightVision);

    }

    /**
     * 获取近视预警级别
     *
     * @param sph 球镜
     * @param cyl 柱镜
     * @return VisionInfoVO
     */
    private VisionInfoVO getMyopiaLevel(BigDecimal sph, BigDecimal cyl, Integer age) {
        VisionInfoVO visionInfoVO = new VisionInfoVO();
        if (ObjectsUtil.allNotNull(sph, cyl)) {
            WarningLevel myopiaWarningLevel = StatUtil.getMyopiaWarningLevel(sph.floatValue(), cyl.floatValue());
            WarningLevel farsightednessWarningLevel = StatUtil.getHyperopiaWarningLevel(sph.floatValue(), cyl.floatValue(), age);
            visionInfoVO.setMyopiaLevel(Objects.nonNull(myopiaWarningLevel) ? myopiaWarningLevel.code : null);
            visionInfoVO.setFarsightednessLevel(Objects.nonNull(farsightednessWarningLevel) ? farsightednessWarningLevel.code : null);
        }
        visionInfoVO.setAstigmatism(Objects.nonNull(cyl) && cyl.abs().compareTo(new BigDecimal("0.5")) > 0);
        return visionInfoVO;
    }

    /**
     * 是否屈光不正
     *
     * @param leftEye  左眼数据
     * @param rightEye 右眼数据
     * @return 是否屈光不正
     */
    private boolean isRefractiveError(VisionInfoVO leftEye, VisionInfoVO rightEye) {
        if (ObjectsUtil.allNull(leftEye, rightEye)) {
            return false;
        }

        Integer myopiaLevel = ScreeningResultUtil.getSeriousLevel(leftEye.getMyopiaLevel(), rightEye.getMyopiaLevel());
        if (Objects.nonNull(myopiaLevel) && myopiaLevel.compareTo(WarningLevel.ZERO.code) > 0) {
            return true;
        }

        Integer farsightednessLevel = ScreeningResultUtil.getSeriousLevel(leftEye.getFarsightednessLevel(), rightEye.getFarsightednessLevel());
        if (Objects.nonNull(farsightednessLevel) && farsightednessLevel.compareTo(WarningLevel.ZERO.code) > 0) {
            return true;
        }

        return (Objects.nonNull(leftEye.getAstigmatism()) && leftEye.getAstigmatism()) || (Objects.nonNull(rightEye.getAstigmatism()) && rightEye.getAstigmatism());
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
     * 获取斜视疾病
     *
     * @param otherEyeDiseasesList 其他眼病
     * @return 斜视疾病
     */
    private List<String> getSquintList(List<String> otherEyeDiseasesList) {
        if (CollectionUtils.isEmpty(otherEyeDiseasesList)) {
            return new ArrayList<>();
        }
        return ListUtils.retainAll(Lists.newArrayList("内显斜", "外显斜", "内隐斜", "外隐斜", "交替性斜视"), otherEyeDiseasesList);
    }

    /**
     * 获取学生编号
     *
     * @param studentPlans 筛查学生计划
     * @return 编号
     */
    private List<Long> getScreeningCodesByPlan(List<ScreeningPlanSchoolStudent> studentPlans) {
        if (CollectionUtils.isEmpty(studentPlans)) {
            return Collections.emptyList();
        }
        return studentPlans.stream().filter(plan -> Objects.nonNull(plan.getScreeningCode()))
                .map(ScreeningPlanSchoolStudent::getScreeningCode).collect(Collectors.toList());
    }

    /**
     * 编码身份证二选一
     *
     * @param student 学生
     */
    private void haveIdCardOrCode(Student student) {
        if (StringUtils.isBlank(student.getIdCard()) && CollectionUtils.isEmpty(getScreeningCode(student.getId()))) {
            throw new BusinessException("身份证和编码不能都为空");
        }
    }

    /**
     * 通过学生Id获取编码
     *
     * @param studentId 学生Id
     * @return 编号
     */
    private List<Long> getScreeningCode(Integer studentId) {
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(planStudentList)) {
            return Collections.emptyList();
        }
        return planStudentList.stream().map(ScreeningPlanSchoolStudent::getScreeningCode).collect(Collectors.toList());
    }

}