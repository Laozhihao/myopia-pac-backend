package com.wupol.myopia.business.parent.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import com.wupol.myopia.business.hospital.service.MedicalReportService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.service.VisionScreeningResultService;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.parent.domain.dto.*;
import com.wupol.myopia.business.parent.domain.mapper.ParentStudentMapper;
import com.wupol.myopia.business.parent.domain.model.ParentStudent;
import com.wupol.myopia.business.parent.domain.vo.ParentStudentVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-02-26
 */
@Service
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
        List<MedicalReportVo> visitsResults = medicalReportService.getReportListByStudentId(studentId);
        visitsDetail.setTotal(visitsResults.size());
        List<CountReportItems> visitsLists = visitsResults.stream().map(v -> {
            CountReportItems items = new CountReportItems();
            items.setId(v.getId());
            items.setCreateTime(v.getCreateTime());
            return items;
        }).collect(Collectors.toList());
        visitsDetail.setItems(visitsLists);
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
        // 查找学生最近的就诊报告
        MedicalReport latestVisitsReport = medicalReportService.getLatestVisitsReport(studentId);
        if (null == latestVisitsReport) {
            return new StudentReportResponseDTO();
        }
        return medicalReportService.getStudentReport(latestVisitsReport.getId());
    }


    /**
     * 获取就诊报告详情
     *
     * @param reportId 就诊报告ID
     * @return StudentReportResponseDTO
     */
    public StudentReportResponseDTO getVisitsReportDetails(Integer reportId) {
        // 查找就诊报告
        MedicalReport report = medicalReportService.getById(reportId);
        if (null == report) {
            return new StudentReportResponseDTO();
        }
        return medicalReportService.getStudentReport(report.getId());
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

        ScreeningReportResponseDTO responseDTO = new ScreeningReportResponseDTO();
        responseDTO.setScreeningDate(result.getCreateTime());
        responseDTO.setGlassesType(glassesType);
        responseDTO.setVisionList(setNakedVision(result.getVisionData()));
        responseDTO.setRefractoryResultItems(setRefractoryResult(result.getComputerOptometry()));
        responseDTO.setBiometricItems(packageBiometricResult(result.getBiometricData(), result.getOtherEyeDiseases()));
        responseDTO.setDoctorAdvice1(1);

        // 获取左右眼的裸眼视力
        BigDecimal leftNakedVision = result.getVisionData().getLeftEyeData().getNakedVision();
        BigDecimal rightNakedVision = result.getVisionData().getRightEyeData().getNakedVision();

        // 取裸眼视力的结果
        TwoTuple<BigDecimal, Integer> resultVision = getResultVision(leftNakedVision, rightNakedVision);

        // 获取矫正视力
        BigDecimal correctedVision = resultVision.getSecond().equals(CommonConst.LEFT_EYE) ?
                result.getVisionData().getLeftEyeData().getCorrectedVision() : result.getVisionData().getRightEyeData().getCorrectedVision();

        responseDTO.setDoctorAdvice2(packageDoctorAdvice(resultVision.getFirst(), , glassesType));
        return responseDTO;
    }

    /**
     * 视力检查结果
     *
     * @param date 数据
     * @return List<NakedVisionItems>
     */
    private List<VisionItems> setNakedVision(VisionDataDO date) {
        List<VisionItems> itemsList = new ArrayList<>();

        // 裸眼视力
        VisionItems nakedVision = new VisionItems();
        nakedVision.setTitle("裸眼视力");

        VisionItems.Item leftNakedVision = new VisionItems.Item();
        leftNakedVision.setVision(date.getLeftEyeData().getNakedVision());
        leftNakedVision.setType("TODO");
        nakedVision.setOs(leftNakedVision);

        VisionItems.Item rightNakedVision = new VisionItems.Item();
        rightNakedVision.setVision(date.getRightEyeData().getNakedVision());
        rightNakedVision.setType("TODO");
        nakedVision.setOd(rightNakedVision);

        itemsList.add(nakedVision);

        // 矫正视力
        VisionItems correctedVision = new VisionItems();
        correctedVision.setTitle("矫正视力");

        VisionItems.Item leftCorrectedVision = new VisionItems.Item();
        leftCorrectedVision.setVision(date.getLeftEyeData().getCorrectedVision());
        leftCorrectedVision.setType("TODO");
        correctedVision.setOs(leftCorrectedVision);

        VisionItems.Item rightCorrectedVision = new VisionItems.Item();
        rightCorrectedVision.setVision(date.getRightEyeData().getCorrectedVision());
        rightCorrectedVision.setType("TODO");
        correctedVision.setOd(rightCorrectedVision);

        itemsList.add(correctedVision);

        return itemsList;
    }

    /**
     * 验光仪检查结果
     *
     * @param date 数据
     * @return List<RefractoryResultItems>
     */
    private List<RefractoryResultItems> setRefractoryResult(ComputerOptometryDO date) {
        List<RefractoryResultItems> items = new ArrayList<>();

        // 轴位
        RefractoryResultItems axialItems = new RefractoryResultItems();
        axialItems.setTitle("轴位");

        RefractoryResultItems.Item leftAxialItems = new RefractoryResultItems.Item();
        leftAxialItems.setVision(date.getLeftEyeData().getAxial());
        leftAxialItems.setType("TODO");
        leftAxialItems.setTypeName("TODO-NAME");
        axialItems.setOs(leftAxialItems);

        RefractoryResultItems.Item rightAxialItems = new RefractoryResultItems.Item();
        rightAxialItems.setVision(date.getRightEyeData().getAxial());
        rightAxialItems.setType("TODO");
        rightAxialItems.setTypeName("TODO-NAME");
        axialItems.setOd(rightAxialItems);

        items.add(axialItems);

        // 球镜
        RefractoryResultItems sphItems = new RefractoryResultItems();
        sphItems.setTitle("球镜");

        RefractoryResultItems.Item leftSphItems = new RefractoryResultItems.Item();
        leftSphItems.setVision(date.getLeftEyeData().getSph().toString());
        leftSphItems.setType("TODO");
        leftSphItems.setTypeName("TODO-NAME");
        sphItems.setOs(leftSphItems);

        RefractoryResultItems.Item rightSphItems = new RefractoryResultItems.Item();
        rightSphItems.setVision(date.getRightEyeData().getSph().toString());
        rightSphItems.setType("TODO");
        rightSphItems.setTypeName("TODO-NAME");
        sphItems.setOd(rightSphItems);

        items.add(sphItems);

        // 柱镜
        RefractoryResultItems cylItems = new RefractoryResultItems();
        cylItems.setTitle("轴位");

        RefractoryResultItems.Item leftCylItems = new RefractoryResultItems.Item();
        leftCylItems.setVision(date.getLeftEyeData().getCyl().toString());
        leftCylItems.setType("TODO");
        leftCylItems.setTypeName("TODO-NAME");
        cylItems.setOs(leftCylItems);

        RefractoryResultItems.Item rightCylItems = new RefractoryResultItems.Item();
        rightCylItems.setVision(date.getRightEyeData().getCyl().toString());
        rightCylItems.setType("TODO");
        rightCylItems.setTypeName("TODO-NAME");
        cylItems.setOd(rightCylItems);

        items.add(cylItems);

        return items;
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
        BiometricItems ADItems = new BiometricItems();
        ADItems.setTitle("房水深度AD");

        BiometricItems.Item leftADItem = new BiometricItems.Item();
        leftADItem.setDate(date.getLeftEyeData().getAd());
        ADItems.setOs(leftADItem);

        BiometricItems.Item rightADItem = new BiometricItems.Item();
        rightADItem.setDate(date.getRightEyeData().getAd());
        ADItems.setOd(rightADItem);

        items.add(ADItems);

        // 眼轴AL
        BiometricItems ALItems = new BiometricItems();
        ALItems.setTitle("眼轴AL");

        BiometricItems.Item leftALItem = new BiometricItems.Item();
        leftALItem.setDate(date.getLeftEyeData().getAl());
        ALItems.setOs(leftALItem);

        BiometricItems.Item rightALItem = new BiometricItems.Item();
        rightALItem.setDate(date.getRightEyeData().getAl());
        ALItems.setOd(rightALItem);

        items.add(ALItems);

        // 角膜中央厚度CCT
        BiometricItems CCTItems = new BiometricItems();
        CCTItems.setTitle("角膜中央厚度CCT");

        BiometricItems.Item leftCCTItem = new BiometricItems.Item();
        leftCCTItem.setDate(date.getLeftEyeData().getCct());
        CCTItems.setOs(leftCCTItem);

        BiometricItems.Item rightCCTItem = new BiometricItems.Item();
        rightCCTItem.setDate(date.getRightEyeData().getCct());
        CCTItems.setOd(rightCCTItem);

        items.add(CCTItems);

        // 状体厚度LT
        BiometricItems LTItems = new BiometricItems();
        LTItems.setTitle("状体厚度LT");

        BiometricItems.Item leftLTItem = new BiometricItems.Item();
        leftLTItem.setDate(date.getLeftEyeData().getLt());
        LTItems.setOs(leftLTItem);

        BiometricItems.Item rightLTItem = new BiometricItems.Item();
        rightLTItem.setDate(date.getRightEyeData().getLt());
        LTItems.setOd(rightLTItem);

        items.add(LTItems);

        // 角膜白到白距离WTW
        BiometricItems WTWItems = new BiometricItems();
        WTWItems.setTitle("角膜白到白距离WTW");

        BiometricItems.Item leftWTWItem = new BiometricItems.Item();
        leftWTWItem.setDate(date.getLeftEyeData().getWtw());
        WTWItems.setOs(leftWTWItem);

        BiometricItems.Item rightWTWItem = new BiometricItems.Item();
        rightWTWItem.setDate(date.getRightEyeData().getWtw());
        WTWItems.setOd(rightWTWItem);

        items.add(WTWItems);

        // 其他眼病
        BiometricItems otherItems = new BiometricItems();
        otherItems.setTitle("其他眼病");

        BiometricItems.Item leftOtherItem = new BiometricItems.Item();
        leftOtherItem.setDate(diseasesDO.getLeftEyeData().getEyeDiseases().toString());
        otherItems.setOs(leftOtherItem);

        BiometricItems.Item rightOtherItem = new BiometricItems.Item();
        rightOtherItem.setDate(diseasesDO.getRightEyeData().getEyeDiseases().toString());
        otherItems.setOd(rightOtherItem);

        items.add(otherItems);

        return items;
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
     * @return 医生建议
     */
    private String packageDoctorAdvice(BigDecimal nakedVision, BigDecimal correctedVision, Integer glassesType) {
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

            }

        }
        return "abc";
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

//    private BigDecimal calculationSE(BigDecimal sph,) {
//
//    }
}
