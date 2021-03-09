package com.wupol.myopia.business.parent.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.vo.ReportAndRecordVo;
import com.wupol.myopia.business.hospital.service.MedicalReportService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.service.VisionScreeningResultService;
import com.wupol.myopia.business.management.util.StatUtil;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.parent.domain.dto.*;
import com.wupol.myopia.business.parent.domain.mapper.ParentStudentMapper;
import com.wupol.myopia.business.parent.domain.model.ParentStudent;
import com.wupol.myopia.business.parent.domain.vo.ParentStudentVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家长端-家长查看学生信息
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class ParentStudentService extends BaseService<ParentStudentMapper, ParentStudent> {

    @Resource
    private StudentService studentService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private MedicalReportService medicalReportService;

    /**
     * 孩子统计、孩子列表
     *
     * @param parentId 家长ID
     * @return CountParentStudentResponseDTO
     */
    public CountParentStudentResponseDTO countParentStudent(Integer parentId) {
        CountParentStudentResponseDTO responseDTO = new CountParentStudentResponseDTO();
        List<ParentStudentVO> parentStudentVOS = baseMapper.countParentStudent(parentId);
        responseDTO.setTotal(parentStudentVOS.size());
        responseDTO.setItem(parentStudentVOS);
        return responseDTO;
    }

    /**
     * 检查身份证
     *
     * @param request 请求入参
     * @return 学生ID
     */
    public ApiResult<Integer> checkIdCard(CheckIdCardRequest request) {
        Student student = studentService.getByIdCard(request.getIdCard());

        if (null == student) {
            // 为空说明是新增
            return ApiResult.success();
        } else {
            // 检查与姓名是否匹配
            if (!StringUtils.equals(request.getName(), student.getName())) {
                throw new BusinessException("身份证数据异常");
            }
            return ApiResult.success(student.getId());
        }
    }

    /**
     * 学生报告统计
     *
     * @param studentId 学生ID
     * @return ReportCountResponseDTO
     */
    public ReportCountResponseDTO studentReportCount(Integer studentId) {
        ReportCountResponseDTO responseDTO = new ReportCountResponseDTO();

        // 学生筛查报告
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByStudentId(studentId);
        List<CountReportItems> screeningLists = screeningResults.stream().map(s -> {
            CountReportItems items = new CountReportItems();
            items.setId(s.getId());
            items.setCreateTime(s.getCreateTime());
            return items;
        }).collect(Collectors.toList());
        ScreeningDetail screeningDetail = new ScreeningDetail();
        screeningDetail.setTotal(screeningResults.size());
        screeningDetail.setItems(screeningLists);
        responseDTO.setScreeningDetail(screeningDetail);

        // 学生就诊档案统计
        VisitsDetail visitsDetail = new VisitsDetail();
        // 获取就诊记录
        List<ReportAndRecordVo> visitLists = medicalReportService.getStudentId(studentId);
        visitsDetail.setTotal(visitLists.size());
        visitsDetail.setItems(visitLists);
        responseDTO.setVisitsDetail(visitsDetail);
        return responseDTO;
    }

    /**
     * 获取学生最新一次筛查结果
     *
     * @param studentId 学生ID
     * @return ScreeningReportResponseDTO
     */
    public ScreeningReportResponseDTO latestScreeningReport(Integer studentId) {
        VisionScreeningResult result = visionScreeningResultService.getLatestResultByStudentId(studentId);
        if (null == result) {
            return new ScreeningReportResponseDTO();
        }
        return packageScreeningReport(visionScreeningResultService.getById(result.getId()));
    }

    /**
     * 获取筛查结果详情
     *
     * @param reportId 报告ID
     * @return ScreeningReportResponseDTO
     */
    public ScreeningReportResponseDTO getScreeningReportDetail(Integer reportId) {
        VisionScreeningResult result = visionScreeningResultService.getById(reportId);
        if (null == result) {
            return new ScreeningReportResponseDTO();
        }
        return packageScreeningReport(result);
    }

    /**
     * 获取最新的就诊报告
     *
     * @param studentId 学生ID
     * @return StudentReportResponseDTO
     */
    public StudentReportResponseDTO latestVisitsReport(Integer studentId) {
        // 查找学生最近的就诊记录
        List<ReportAndRecordVo> visitLists = medicalReportService.getStudentId(studentId);
        if (CollectionUtils.isEmpty(visitLists)) {
            return new StudentReportResponseDTO();
        }
        ReportAndRecordVo reportAndRecordVo = visitLists.get(0);
        return medicalReportService.getStudentReport(reportAndRecordVo.getRecordId(), reportAndRecordVo.getReportId());
    }


    /**
     * 获取就诊报告详情
     *
     * @param request 请求入参
     * @return StudentReportResponseDTO
     */
    public StudentReportResponseDTO getVisitsReportDetails(VisitsReportDetailRequest request) {
        return medicalReportService.getStudentReport(request.getRecordId(), request.getReportId());
    }

    /**
     * 视力趋势
     *
     * @param studentId 学生ID
     * @return ScreeningVisionTrendsResponseDTO
     */
    public ScreeningVisionTrendsResponseDTO screeningVisionTrends(Integer studentId) {
        ScreeningVisionTrendsResponseDTO responseDTO = new ScreeningVisionTrendsResponseDTO();
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByStudentId(studentId);
        responseDTO.setCorrectedVisionDetails(packageVisionTrendsByCorrected(resultList));
        responseDTO.setCylDetails(packageVisionTrendsByCyl(resultList));
        responseDTO.setSphDetails(packageVisionTrendsBySph(resultList));
        responseDTO.setNakedVisionDetails(packageVisionTrendsByNakedVision(resultList));
        return responseDTO;
    }

    /**
     * 家长绑定学生
     *
     * @param request 请求入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void parentBindStudent(ParentBindRequest request) {
        ParentStudent parentStudent = new ParentStudent();

        Integer studentId = request.getStudentId();
        Integer parentId = request.getParentId();
        if (null == parentId || null == studentId) {
            throw new BusinessException("数据异常");
        }
        ParentStudent checkResult = baseMapper.getByParentIdAndStudentId(parentId, studentId);
        if (null != checkResult) {
            throw new BusinessException("已经绑定");
        }
        parentStudent.setParentId(parentId);
        parentStudent.setStudentId(studentId);

        baseMapper.insert(parentStudent);
    }

    /**
     * 封装筛查结果
     *
     * @param result 筛查结果
     * @return ScreeningReportResponseDTO
     */
    private ScreeningReportResponseDTO packageScreeningReport(VisionScreeningResult result) {
        // 没有佩戴眼镜
        Integer glassesType = 2;

        // 查询学生
        Student student = studentService.getById(result.getStudentId());
        int age = getAgeByBirthday(student.getBirthday());

        ScreeningReportDetail responseDTO = new ScreeningReportDetail();
        responseDTO.setScreeningDate(result.getCreateTime());
        responseDTO.setGlassesType(glassesType);
        // 视力检查结果
        responseDTO.setVisionResultItems(packageVisionResult(result.getVisionData(), age));
        // 验光仪检查结果
        TwoTuple<List<RefractoryResultItems>, Integer> refractoryResult = packageRefractoryResult(result.getComputerOptometry(), age);
        responseDTO.setRefractoryResultItems(refractoryResult.getFirst());
        // 生物测量
        responseDTO.setBiometricItems(packageBiometricResult(result.getBiometricData(), result.getOtherEyeDiseases()));
        // 医生建议一
        responseDTO.setDoctorAdvice1(refractoryResult.getSecond() - 5);

        // 获取左右眼的裸眼视力
        BigDecimal leftNakedVision = result.getVisionData().getLeftEyeData().getNakedVision();
        BigDecimal rightNakedVision = result.getVisionData().getRightEyeData().getNakedVision();

        // 取裸眼视力的结果
        TwoTuple<BigDecimal, Integer> resultVision = getResultVision(leftNakedVision, rightNakedVision);

        // 获取矫正视力
        BigDecimal correctedVision;
        // 球镜
        BigDecimal sph;
        // 柱镜
        BigDecimal cyl;
        if (resultVision.getSecond().equals(CommonConst.LEFT_EYE)) {
            // 左眼
            correctedVision = result.getVisionData().getLeftEyeData().getCorrectedVision();
            sph = result.getComputerOptometry().getLeftEyeData().getSph();
            cyl = result.getComputerOptometry().getLeftEyeData().getCyl();
        } else {
            // 右眼
            correctedVision = result.getVisionData().getRightEyeData().getCorrectedVision();
            sph = result.getComputerOptometry().getRightEyeData().getSph();
            cyl = result.getComputerOptometry().getRightEyeData().getCyl();
        }
        // 医生建议二
        responseDTO.setDoctorAdvice2(packageDoctorAdvice(
                resultVision.getFirst(), correctedVision, sph, cyl, glassesType, student.getGradeType()));
        ScreeningReportResponseDTO response = new ScreeningReportResponseDTO();
        response.setDetail(responseDTO);
        return response;
    }

    /**
     * 视力检查结果
     * <p>预警级别 {@link WarningLevel}</p>
     *
     * @param date 数据
     * @param age  年龄
     * @return List<NakedVisionItems>
     */
    private List<VisionItems> packageVisionResult(VisionDataDO date, Integer age) {
        List<VisionItems> itemsList = new ArrayList<>();

        // 裸眼视力
        VisionItems nakedVision = new VisionItems();
        nakedVision.setTitle("裸眼视力");

        VisionItems.Item leftNakedVision = new VisionItems.Item();
        leftNakedVision.setVision(date.getLeftEyeData().getNakedVision());
        leftNakedVision.setType(lowVisionType(date.getLeftEyeData().getNakedVision(), age));
        nakedVision.setOs(leftNakedVision);

        VisionItems.Item rightNakedVision = new VisionItems.Item();
        rightNakedVision.setVision(date.getRightEyeData().getNakedVision());
        rightNakedVision.setType(lowVisionType(date.getRightEyeData().getNakedVision(), age));
        nakedVision.setOd(rightNakedVision);

        itemsList.add(nakedVision);

        // 矫正视力
        VisionItems correctedVision = new VisionItems();
        correctedVision.setTitle("矫正视力");

        VisionItems.Item leftCorrectedVision = new VisionItems.Item();
        leftCorrectedVision.setVision(date.getLeftEyeData().getCorrectedVision());
        leftCorrectedVision.setType(2);
        correctedVision.setOs(leftCorrectedVision);

        VisionItems.Item rightCorrectedVision = new VisionItems.Item();
        rightCorrectedVision.setVision(date.getRightEyeData().getCorrectedVision());
        rightCorrectedVision.setType(3);
        correctedVision.setOd(rightCorrectedVision);

        itemsList.add(correctedVision);

        return itemsList;
    }

    /**
     * 验光仪检查结果
     *
     * @param date 数据
     * @param age  年龄
     * @return TwoTuple<List < RefractoryResultItems>, Integer>
     */
    private TwoTuple<List<RefractoryResultItems>, Integer> packageRefractoryResult(ComputerOptometryDO date, Integer age) {

        Integer dockerAdvice = 0;
        List<RefractoryResultItems> items = new ArrayList<>();

        // 轴位
        RefractoryResultItems axialItems = new RefractoryResultItems();
        axialItems.setTitle("轴位");

        RefractoryResultItems.Item leftAxialItems = new RefractoryResultItems.Item();
        leftAxialItems.setVision(date.getLeftEyeData().getAxial().toString());
        leftAxialItems.setTypeName(getAxialTypeName(date.getLeftEyeData().getAxial()));
        axialItems.setOs(leftAxialItems);

        RefractoryResultItems.Item rightAxialItems = new RefractoryResultItems.Item();
        rightAxialItems.setVision(date.getRightEyeData().getAxial().toString());
        rightAxialItems.setTypeName(getAxialTypeName(date.getLeftEyeData().getAxial()));
        axialItems.setOd(rightAxialItems);

        items.add(axialItems);

        // 球镜
        RefractoryResultItems sphItems = new RefractoryResultItems();
        sphItems.setTitle("球镜");

        RefractoryResultItems.Item leftSphItems = new RefractoryResultItems.Item();
        leftSphItems.setVision(date.getLeftEyeData().getSph().toString());
        // 获取球镜TypeName
        TwoTuple<String, Integer> leftSphType = getSphTypeName(date.getLeftEyeData().getSph(), date.getLeftEyeData().getCyl(), age);
        leftSphItems.setTypeName(leftSphType.getFirst());
        Integer result1 = leftSphType.getSecond();
        dockerAdvice = dockerAdvice > result1 ? dockerAdvice : result1;
        leftSphItems.setType(result1);
        sphItems.setOs(leftSphItems);

        RefractoryResultItems.Item rightSphItems = new RefractoryResultItems.Item();
        rightSphItems.setVision(date.getRightEyeData().getSph().toString());
        // 获取球镜TypeName
        TwoTuple<String, Integer> rightSphType = getSphTypeName(date.getRightEyeData().getSph(), date.getRightEyeData().getCyl(), age);
        rightSphItems.setTypeName(rightSphType.getFirst());
        Integer result2 = rightSphType.getSecond();
        dockerAdvice = dockerAdvice > result2 ? dockerAdvice : result2;
        rightSphItems.setType(result2);
        sphItems.setOd(rightSphItems);

        items.add(sphItems);

        // 柱镜
        RefractoryResultItems cylItems = new RefractoryResultItems();
        cylItems.setTitle("柱镜");

        RefractoryResultItems.Item leftCylItems = new RefractoryResultItems.Item();
        leftCylItems.setVision(date.getLeftEyeData().getCyl().toString());
        // 获取散光TypeName
        TwoTuple<String, Integer> leftCylType = getCylTypeName(date.getLeftEyeData().getCyl());
        Integer result3 = leftCylType.getSecond();
        dockerAdvice = dockerAdvice > result3 ? dockerAdvice : result3;
        leftCylItems.setType(result3);
        leftCylItems.setTypeName(leftCylType.getFirst());
        cylItems.setOs(leftCylItems);

        RefractoryResultItems.Item rightCylItems = new RefractoryResultItems.Item();
        rightCylItems.setVision(date.getRightEyeData().getCyl().toString());
        // 获取散光TypeName
        TwoTuple<String, Integer> rightCylType = getCylTypeName(date.getRightEyeData().getCyl());
        Integer result4 = rightCylType.getSecond();
        rightCylItems.setType(result4);
        dockerAdvice = dockerAdvice > result4 ? dockerAdvice : result4;
        rightCylItems.setTypeName(rightCylType.getFirst());
        cylItems.setOd(rightCylItems);

        items.add(cylItems);

        return new TwoTuple<>(items, dockerAdvice);
    }

    /**
     * 生物测量
     *
     * @param date       数据
     * @param diseasesDO 其他眼病
     * @return List<BiometricItems>
     */
    private List<BiometricItems> packageBiometricResult(BiometricDataDO date, OtherEyeDiseasesDO diseasesDO) {
        List<BiometricItems> items = new ArrayList<>();

        // 房水深度AD
        BiometricItems ADItems = getBiometricItems("房水深度AD", date.getLeftEyeData().getAd(), date.getRightEyeData().getAd());
        items.add(ADItems);

        // 眼轴AL
        BiometricItems ALItems = getBiometricItems("眼轴AL", date.getLeftEyeData().getAl(), date.getRightEyeData().getAl());
        items.add(ALItems);

        // 角膜中央厚度CCT
        BiometricItems CCTItems = getBiometricItems("角膜中央厚度CCT", date.getLeftEyeData().getCct(), date.getRightEyeData().getCct());
        items.add(CCTItems);

        // 状体厚度LT
        BiometricItems LTItems = getBiometricItems("状体厚度LT", date.getLeftEyeData().getLt(), date.getRightEyeData().getLt());
        items.add(LTItems);

        // 角膜白到白距离WTW
        BiometricItems WTWItems = getBiometricItems("角膜白到白距离WTW", date.getLeftEyeData().getWtw(), date.getRightEyeData().getWtw());
        items.add(WTWItems);

        // 其他眼病
        BiometricItems otherItems = getBiometricItems("其他眼病", diseasesDO.getLeftEyeData().getEyeDiseases().toString(), diseasesDO.getRightEyeData().getEyeDiseases().toString());
        items.add(otherItems);

        return items;
    }

    /**
     * 生物测量数据
     *
     * @param title     标题
     * @param leftDate  左眼数据
     * @param rightDate 右眼数据
     * @return BiometricItems
     */
    private BiometricItems getBiometricItems(String title, String leftDate, String rightDate) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle(title);

        BiometricItems.Item leftItem = new BiometricItems.Item();
        leftItem.setDate(leftDate);
        biometricItems.setOs(leftItem);

        BiometricItems.Item rightItem = new BiometricItems.Item();
        rightItem.setDate(rightDate);
        biometricItems.setOd(rightItem);
        return biometricItems;
    }

    /**
     * 矫正视力详情
     *
     * @param results 筛查结果
     * @return List<CorrectedVisionDetails>
     */
    private List<CorrectedVisionDetails> packageVisionTrendsByCorrected(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            CorrectedVisionDetails details = new CorrectedVisionDetails();

            // 左眼
            CorrectedVisionDetails.Item left = new CorrectedVisionDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCorrectedVision(result.getVisionData().getLeftEyeData().getCorrectedVision());
            left.setCreateTime(result.getCreateTime());

            // 右眼
            CorrectedVisionDetails.Item right = new CorrectedVisionDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCorrectedVision(result.getVisionData().getRightEyeData().getCorrectedVision());
            right.setCreateTime(result.getCreateTime());

            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 柱镜详情
     *
     * @param results 筛查结果
     * @return List<CylDetails>
     */
    private List<CylDetails> packageVisionTrendsByCyl(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            CylDetails details = new CylDetails();

            // 左眼
            CylDetails.Item left = new CylDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(result.getCreateTime());
            left.setCyl(result.getComputerOptometry().getLeftEyeData().getCyl());

            // 右眼
            CylDetails.Item right = new CylDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(result.getCreateTime());
            right.setCyl(result.getComputerOptometry().getRightEyeData().getCyl());

            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 柱镜详情
     *
     * @param results 筛查结果
     * @return List<CylDetails>
     */
    private List<SphDetails> packageVisionTrendsBySph(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            SphDetails details = new SphDetails();

            // 左眼
            SphDetails.Item left = new SphDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(result.getCreateTime());
            left.setSph(result.getComputerOptometry().getLeftEyeData().getSph());

            // 右眼
            SphDetails.Item right = new SphDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(result.getCreateTime());
            right.setSph(result.getComputerOptometry().getRightEyeData().getSph());

            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 裸眼视力详情
     *
     * @param results 筛查结果
     * @return List<CylDetails>
     */
    private List<NakedVisionDetails> packageVisionTrendsByNakedVision(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            NakedVisionDetails details = new NakedVisionDetails();

            // 左眼
            NakedVisionDetails.Item left = new NakedVisionDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(result.getCreateTime());
            left.setNakedVision(result.getVisionData().getLeftEyeData().getNakedVision());

            // 右眼
            NakedVisionDetails.Item right = new NakedVisionDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(result.getCreateTime());
            right.setNakedVision(result.getVisionData().getRightEyeData().getNakedVision());

            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }


    /**
     * 医生建议
     *
     * @param nakedVision     裸眼视力
     * @param correctedVision 矫正视力
     * @param sph             球镜
     * @param cyl             柱镜
     * @param glassesType     戴镜类型
     * @param schoolAge       学龄段
     * @return 医生建议
     */
    private String packageDoctorAdvice(BigDecimal nakedVision, BigDecimal correctedVision,
                                       BigDecimal sph, BigDecimal cyl,
                                       Integer glassesType, Integer schoolAge) {

        // 等效球镜
        BigDecimal se = calculationSE(sph, cyl);

        if (nakedVision.compareTo(new BigDecimal("4.9")) < 0) {
            // 裸眼视力小于4.9
            if (glassesType > 2) {
                // 佩戴眼镜
                if (correctedVision.compareTo(new BigDecimal("4.9")) < 0) {
                    // 矫正视力小于4.9
                    return "裸眼远视力下降，戴镜远视力下降。建议：请及时到医疗机构复查。";
                } else {
                    // 矫正视力大于4.9
                    return "裸眼远视力下降，戴镜远视力≥4.9。建议：请3个月或半年1次检查裸眼视力和戴镜视力。";
                }
            } else {
                // 没有佩戴眼镜
                boolean checkCyl = cyl.abs().compareTo(new BigDecimal("1.5")) < 0;
                // (小学生 && 0<=SE<2 && Cyl <1.5) || (初中生 && -0.5<=SE<3 && Cyl <1.5)
                if ((SchoolAge.PRIMARY.code.equals(schoolAge) && isBetweenLeft(se, "0.00", "2.00") && checkCyl)
                        ||
                        (SchoolAge.JUNIOR.code.equals(schoolAge) && isBetweenLeft(se, "-0.50", "3.00") && checkCyl)
                ) {
                    return "裸眼远视力下降，视功能可能异常。建议：请到医疗机构接受检查，明确诊断并及时采取措施。";
                    // (小学生 && !(0 <= SE < 2)) || (初中生 && (Cyl >= 1.5 || !(-0.5 <= SE < 3)))
                } else if ((SchoolAge.PRIMARY.code.equals(schoolAge) && !isBetweenLeft(se, "0.00", "2.00"))
                        ||
                        (SchoolAge.JUNIOR.code.equals(schoolAge) && (!isBetweenLeft(se, "-0.50", "3.00") || !checkCyl))) {
                    return "裸眼远视力下降，屈光不正筛查阳性。建议：请到医疗机构接受检查，明确诊断并及时采取措施。";
                }
            }
        }
        // SE >= 0
        if (se.compareTo(new BigDecimal("0.00")) >= 0) {
            return "裸眼远视力≥4.9，目前尚无近视高危因素。建议：1、6-12个月复查。2、6岁儿童SE≥+2.00D，请到医疗机构接受检查。";
        } else {
            // SE < 0
            return "裸眼远视力≥4.9，可能存在近视高危因素。建议：1、严格注意用眼卫生。2、到医疗机构检查了解是否可能发展未近视。";
        }
    }

    /**
     * 取视力值高的眼球
     *
     * @param left  左眼
     * @param right 右眼
     * @return BigDecimal
     */
    private TwoTuple<BigDecimal, Integer> getResultVision(BigDecimal left, BigDecimal right) {
        if (left.compareTo(right) <= 0) {
            return new TwoTuple<>(left, CommonConst.LEFT_EYE);
        }
        return new TwoTuple<>(right, CommonConst.RIGHT_EYE);
    }

    /**
     * 计算 等效球镜
     *
     * @param sph 球镜
     * @param cyl 柱镜
     * @return 等效球镜
     */
    private BigDecimal calculationSE(BigDecimal sph, BigDecimal cyl) {
        return sph.add(cyl.multiply(new BigDecimal("0.5")))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 判断是否在某个区间，左闭右开区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    private Boolean isBetweenLeft(BigDecimal val, String start, String end) {
        return val.compareTo(new BigDecimal(start)) >= 0 && val.compareTo(new BigDecimal(end)) < 0;
    }

    /**
     * 计算年龄
     *
     * @param birthday 生日
     * @return 年龄
     */
    private static int getAgeByBirthday(Date birthday) {
        int age;
        try {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());// 当前时间

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthday);

            if (birth.after(now)) {//如果传入的时间，在当前时间的后面，返回0岁
                age = 0;
            } else {
                age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (now.get(Calendar.DAY_OF_YEAR) > birth.get(Calendar.DAY_OF_YEAR)) {
                    age += 1;
                }
            }
            return age;
        } catch (Exception e) {//兼容性更强,异常后返回数据
            return 0;
        }
    }

    /**
     * 获取散光轴位
     *
     * @param axial 轴位
     * @return String
     */
    private String getAxialTypeName(BigDecimal axial) {
        return "散光轴位" + axial.abs() + "°";
    }

    /**
     * 获取球镜typeName
     *
     * @param sph 球镜
     * @param cyl 柱镜
     * @param age 年龄
     * @return TwoTuple
     */
    private TwoTuple<String, Integer> getSphTypeName(BigDecimal sph, BigDecimal cyl, Integer age) {
        if (sph.compareTo(new BigDecimal("0.00")) <= 0) {
            // 近视
            WarningLevel myopiaWarningLevel = StatUtil.getMyopiaWarningLevel(sph.floatValue(), cyl.floatValue());
            if (null != myopiaWarningLevel) {
                if (myopiaWarningLevel.code == WarningLevel.NORMAL.code || myopiaWarningLevel.code == WarningLevel.ONE.code) {
                    return new TwoTuple<>("近视" + sph.abs() + "度", 5);
                }
                return new TwoTuple<>("近视" + sph.abs() + "度", myopiaWarningLevel.code + 5);
            } else {
                return new TwoTuple<>("近视" + sph.abs() + "度", 5);
            }
        } else {
            // 远视
            WarningLevel hyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(sph.floatValue(), cyl.floatValue(), age);
            if (null != hyperopiaWarningLevel) {
                if (hyperopiaWarningLevel.code == WarningLevel.NORMAL.code || hyperopiaWarningLevel.code == WarningLevel.ZERO.code) {
                    return new TwoTuple<>("远视" + sph.abs() + "度", 5);
                } else {
                    return new TwoTuple<>("远视" + sph.abs() + "度", hyperopiaWarningLevel.code + 5);
                }
            } else {
                return new TwoTuple<>("远视" + sph.abs() + "度", 5);
            }

        }
    }

    /**
     * 获取散光TypeName
     *
     * @param cyl 柱镜
     * @return String
     */
    private TwoTuple<String, Integer> getCylTypeName(BigDecimal cyl) {
        WarningLevel astigmatismWarningLevel = StatUtil.getAstigmatismWarningLevel(cyl.floatValue());
        return new TwoTuple<>("散光" + cyl.abs() + "度", astigmatismWarningLevel.code + 5);
    }

    /**
     * 获取裸眼视力类型
     *
     * @param nakedVision 裸眼视力
     * @param age         年龄
     * @return Integer
     */
    private Integer lowVisionType(BigDecimal nakedVision, Integer age) {
        boolean lowVision = StatUtil.isLowVision(nakedVision.floatValue(), age);
        if (lowVision) {
            return 1;
        }
        return 0;
    }
}
