package com.wupol.myopia.business.parent.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.constant.GlassesType;
import com.wupol.myopia.business.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.vo.ReportAndRecordVo;
import com.wupol.myopia.business.hospital.service.MedicalReportService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.constant.ParentReportConst;
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
import java.util.*;
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
     * @return CountParentStudentResponseDTO 家长端-统计家长绑定学生
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
     * @return ReportCountResponseDTO 家长端-孩子报告统计
     */
    public ReportCountResponseDTO studentReportCount(Integer studentId) {
        ReportCountResponseDTO responseDTO = new ReportCountResponseDTO();

        Student student = studentService.getById(studentId);
        if (null == student) {
            throw new BusinessException("学生数据异常！");
        }
        responseDTO.setName(student.getName());

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
        List<ReportAndRecordVo> visitLists = medicalReportService.getByStudentId(studentId);
        visitsDetail.setTotal(visitLists.size());
        visitsDetail.setItems(visitLists);
        responseDTO.setVisitsDetail(visitsDetail);
        return responseDTO;
    }

    /**
     * 获取学生最新一次筛查结果
     *
     * @param studentId 学生ID
     * @return ScreeningReportResponseDTO 筛查报告返回体
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
     * @return ScreeningReportResponseDTO 学生就诊记录档案卡
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
        List<ReportAndRecordVo> visitLists = medicalReportService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(visitLists)) {
            return new StudentReportResponseDTO();
        }
        ReportAndRecordVo reportAndRecordVo = visitLists.get(0);
        return medicalReportService.getStudentReport(reportAndRecordVo.getHospitalId(), reportAndRecordVo.getReportId());
    }


    /**
     * 获取就诊报告详情
     *
     * @param request 请求入参
     * @return StudentReportResponseDTO 学生就诊记录档案卡
     */
    public StudentReportResponseDTO getVisitsReportDetails(VisitsReportDetailRequest request) {
        return medicalReportService.getStudentReport(request.getHospitalId(), request.getReportId());
    }

    /**
     * 视力趋势
     *
     * @param studentId 学生ID
     * @return ScreeningVisionTrendsResponseDTO 视力趋势
     */
    public ScreeningVisionTrendsResponseDTO screeningVisionTrends(Integer studentId) {
        ScreeningVisionTrendsResponseDTO responseDTO = new ScreeningVisionTrendsResponseDTO();
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByStudentId(studentId);
        // 矫正视力详情
        responseDTO.setCorrectedVisionDetails(packageVisionTrendsByCorrected(resultList));
        // 柱镜详情
        responseDTO.setCylDetails(packageVisionTrendsByCyl(resultList));
        // 球镜详情
        responseDTO.setSphDetails(packageVisionTrendsBySph(resultList));
        // 裸眼视力详情
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
     * @return ScreeningReportResponseDTO 筛查报告返回体
     */
    private ScreeningReportResponseDTO packageScreeningReport(VisionScreeningResult result) {
        ScreeningReportResponseDTO response = new ScreeningReportResponseDTO();

        // 查询学生
        Student student = studentService.getById(result.getStudentId());
        int age = getAgeByBirthday(student.getBirthday());

        ScreeningReportDetail responseDTO = new ScreeningReportDetail();
        responseDTO.setScreeningDate(result.getCreateTime());
        VisionDataDO visionData = result.getVisionData();
        // 视力检查结果
        responseDTO.setVisionResultItems(packageVisionResult(visionData, age));
        // 验光仪检查结果
        TwoTuple<List<RefractoryResultItems>, Integer> refractoryResult = packageRefractoryResult(result.getComputerOptometry(), age);
        responseDTO.setRefractoryResultItems(refractoryResult.getFirst());
        // 生物测量
        responseDTO.setBiometricItems(packageBiometricResult(result.getBiometricData(), result.getOtherEyeDiseases()));
        // 医生建议一（这里-5是为了type的偏移量）
        responseDTO.setDoctorAdvice1(refractoryResult.getSecond() - 5);
        // 医生建议二
        responseDTO.setDoctorAdvice2(getDoctorAdviceDetail(result, student.getGradeType()));
        if (null != visionData) {
            // 戴镜类型
            responseDTO.setGlassesType(visionData.getLeftEyeData().getGlassesType());
        }
        response.setDetail(responseDTO);
        return response;
    }

    /**
     * 获取医生建议二
     *
     * @param result    筛查结果
     * @param gradeType 学龄段
     * @return 医生建议
     */
    private String getDoctorAdviceDetail(VisionScreeningResult result, Integer gradeType) {

        VisionDataDO visionData = result.getVisionData();
        ComputerOptometryDO computerOptometry = result.getComputerOptometry();
        if (null == visionData || null == computerOptometry) {
            return null;
        }
        // 戴镜类型，取一只眼就行
        Integer glassesType = result.getVisionData().getLeftEyeData().getGlassesType();

        // 获取左右眼的裸眼视力
        BigDecimal leftNakedVision = visionData.getLeftEyeData().getNakedVision();
        BigDecimal rightNakedVision = visionData.getRightEyeData().getNakedVision();
        // 取裸眼视力的结果
        TwoTuple<BigDecimal, Integer> resultVision = getResultVision(leftNakedVision, rightNakedVision);
        // 获取矫正视力
        BigDecimal correctedVision;
        // 球镜
        BigDecimal sph;
        // 柱镜
        BigDecimal cyl;
        // 根据严重的眼镜提供医生建议
        if (resultVision.getSecond().equals(CommonConst.LEFT_EYE)) {
            // 左眼
            correctedVision = visionData.getLeftEyeData().getCorrectedVision();
            sph = computerOptometry.getLeftEyeData().getSph();
            cyl = computerOptometry.getLeftEyeData().getCyl();
        } else {
            // 右眼
            correctedVision = visionData.getRightEyeData().getCorrectedVision();
            sph = computerOptometry.getRightEyeData().getSph();
            cyl = computerOptometry.getRightEyeData().getCyl();
        }
        return packageDoctorAdvice(resultVision.getFirst(), correctedVision, sph, cyl, glassesType, gradeType);
    }

    /**
     * 视力检查结果
     *
     * @param date 数据
     * @param age  年龄
     * @return List<VisionItems> 视力检查结果
     */
    private List<VisionItems> packageVisionResult(VisionDataDO date, Integer age) {
        List<VisionItems> itemsList = new ArrayList<>();

        // 裸眼视力
        VisionItems nakedVision = new VisionItems();
        nakedVision.setTitle("裸眼视力");

        // 矫正视力
        VisionItems correctedVision = new VisionItems();
        correctedVision.setTitle("矫正视力");

        if (null != date) {
            // 戴镜类型，取一只眼就行
            Integer glassesType = date.getLeftEyeData().getGlassesType();

            // 左裸眼视力
            VisionItems.Item leftNakedVision = new VisionItems.Item();
            BigDecimal leftNakedVisionValue = date.getLeftEyeData().getNakedVision();
            leftNakedVision.setVision(leftNakedVisionValue);
            leftNakedVision.setType(lowVisionType(leftNakedVisionValue, age));
            nakedVision.setOs(leftNakedVision);

            // 右裸眼视力
            VisionItems.Item rightNakedVision = new VisionItems.Item();
            BigDecimal rightNakedVisionValue = date.getRightEyeData().getNakedVision();
            rightNakedVision.setVision(rightNakedVisionValue);
            rightNakedVision.setType(lowVisionType(rightNakedVisionValue, age));
            nakedVision.setOd(rightNakedVision);

            // 左矫正视力
            VisionItems.Item leftCorrectedVision = new VisionItems.Item();
            BigDecimal leftCorrectedVisionValue = date.getLeftEyeData().getCorrectedVision();
            leftCorrectedVision.setVision(leftCorrectedVisionValue);
            leftCorrectedVision.setType(getCorrected2Type(leftNakedVisionValue, leftCorrectedVisionValue, glassesType));
            correctedVision.setOs(leftCorrectedVision);

            // 右矫正视力
            VisionItems.Item rightCorrectedVision = new VisionItems.Item();
            BigDecimal rightCorrectedVisionValue = date.getRightEyeData().getCorrectedVision();
            rightCorrectedVision.setVision(rightCorrectedVisionValue);
            rightCorrectedVision.setType(getCorrected2Type(rightNakedVisionValue, rightCorrectedVisionValue, glassesType));
            correctedVision.setOd(rightCorrectedVision);
        }
        itemsList.add(correctedVision);
        itemsList.add(nakedVision);
        return itemsList;
    }

    /**
     * 验光仪检查结果
     *
     * @param date 数据
     * @param age  年龄
     * @return TwoTuple<List < RefractoryResultItems>, Integer> left-验光仪检查数据 right-预警级别
     */
    private TwoTuple<List<RefractoryResultItems>, Integer> packageRefractoryResult(ComputerOptometryDO date, Integer age) {

        List<RefractoryResultItems> items = new ArrayList<>();
        RefractoryResultItems axialItems = new RefractoryResultItems();
        axialItems.setTitle("轴位A");

        RefractoryResultItems sphItems = new RefractoryResultItems();
        sphItems.setTitle("球镜SE");

        // 柱镜
        RefractoryResultItems cylItems = new RefractoryResultItems();
        cylItems.setTitle("柱镜DC");

        if (null != date) {
            // 轴位
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
            RefractoryResultItems.Item leftSphItems = new RefractoryResultItems.Item();
            leftSphItems.setVision(date.getLeftEyeData().getSph().toString());
            TwoTuple<String, Integer> leftSphType = getSphTypeName(date.getLeftEyeData().getSph(), date.getLeftEyeData().getCyl(), age);
            leftSphItems.setTypeName(leftSphType.getFirst());
            leftSphItems.setType(leftSphType.getSecond());
            sphItems.setOs(leftSphItems);

            RefractoryResultItems.Item rightSphItems = new RefractoryResultItems.Item();
            rightSphItems.setVision(date.getRightEyeData().getSph().toString());
            TwoTuple<String, Integer> rightSphType = getSphTypeName(date.getRightEyeData().getSph(), date.getRightEyeData().getCyl(), age);
            rightSphItems.setTypeName(rightSphType.getFirst());
            rightSphItems.setType(rightSphType.getSecond());
            sphItems.setOd(rightSphItems);
            items.add(sphItems);

            RefractoryResultItems.Item leftCylItems = new RefractoryResultItems.Item();
            leftCylItems.setVision(date.getLeftEyeData().getCyl().toString());
            TwoTuple<String, Integer> leftCylType = getCylTypeName(date.getLeftEyeData().getCyl());
            leftCylItems.setType(leftCylType.getSecond());
            leftCylItems.setTypeName(leftCylType.getFirst());
            cylItems.setOs(leftCylItems);

            RefractoryResultItems.Item rightCylItems = new RefractoryResultItems.Item();
            rightCylItems.setVision(date.getRightEyeData().getCyl().toString());
            TwoTuple<String, Integer> rightCylType = getCylTypeName(date.getRightEyeData().getCyl());
            rightCylItems.setType(rightCylType.getSecond());
            rightCylItems.setTypeName(rightCylType.getFirst());
            cylItems.setOd(rightCylItems);
            items.add(cylItems);

            return new TwoTuple<>(items, getIntegerMax(
                    leftSphType.getSecond(), rightSphType.getSecond(),
                    leftCylType.getSecond(), rightCylType.getSecond()));
        }
        items.add(axialItems);
        items.add(sphItems);
        items.add(cylItems);
        return new TwoTuple<>(items, 0);
    }

    /**
     * 生物测量
     *
     * @param date       数据
     * @param diseasesDO 其他眼病
     * @return List<BiometricItems> 生物测量
     */
    private List<BiometricItems> packageBiometricResult(BiometricDataDO date, OtherEyeDiseasesDO diseasesDO) {
        List<BiometricItems> items = new ArrayList<>();
        // 房水深度AD
        BiometricItems ADItems = packageADItem(date);
        items.add(ADItems);

        // 眼轴AL
        BiometricItems ALItems = packageALItem(date);
        items.add(ALItems);

        // 角膜中央厚度CCT
        BiometricItems CCTItems = packageCCTItem(date);
        items.add(CCTItems);

        // 状体厚度LT
        BiometricItems LTItems = packageLTItem(date);
        items.add(LTItems);

        // 角膜白到白距离WTW
        BiometricItems WTWItems = packageWTWItem(date);
        items.add(WTWItems);

        items.add(packageEyeDiseases(diseasesDO));
        return items;
    }

    /**
     * 房水深度AD
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    private BiometricItems packageADItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("房水深度AD");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getAd());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getAd());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 眼轴AL
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    private BiometricItems packageALItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("眼轴AL");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getAl());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getAl());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 角膜中央厚度CCT
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    private BiometricItems packageCCTItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("角膜中央厚度CCT");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getCct());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getCct());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 状体厚度LT
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    private BiometricItems packageLTItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("状体厚度LT");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getLt());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getLt());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 角膜白到白距离WTW
     *
     * @param date 生物测量数据
     * @return BiometricItems 生物测量
     */
    private BiometricItems packageWTWItem(BiometricDataDO date) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("角膜白到白距离WTW");
        if (null != date) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setData(date.getLeftEyeData().getWtw());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setData(date.getRightEyeData().getWtw());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 其他眼疾
     *
     * @param diseasesDO 眼疾
     * @return BiometricItems
     */
    private BiometricItems packageEyeDiseases(OtherEyeDiseasesDO diseasesDO) {
        BiometricItems biometricItems = new BiometricItems();
        biometricItems.setTitle("其他眼病");
        if (null != diseasesDO) {
            BiometricItems.Item leftItem = new BiometricItems.Item();
            leftItem.setEyeDiseases(diseasesDO.getLeftEyeData().getEyeDiseases());
            biometricItems.setOs(leftItem);

            BiometricItems.Item rightItem = new BiometricItems.Item();
            rightItem.setEyeDiseases(diseasesDO.getRightEyeData().getEyeDiseases());
            biometricItems.setOd(rightItem);
        }
        return biometricItems;
    }

    /**
     * 矫正视力详情
     *
     * @param results 筛查结果
     * @return List<CorrectedVisionDetails> 矫正视力详情
     */
    private List<CorrectedVisionDetails> packageVisionTrendsByCorrected(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            CorrectedVisionDetails details = new CorrectedVisionDetails();

            // 左眼
            CorrectedVisionDetails.Item left = new CorrectedVisionDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setVision(result.getVisionData().getLeftEyeData().getCorrectedVision());
            left.setCreateTime(result.getCreateTime());

            // 右眼
            CorrectedVisionDetails.Item right = new CorrectedVisionDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setVision(result.getVisionData().getRightEyeData().getCorrectedVision());
            right.setCreateTime(result.getCreateTime());

            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 柱镜详情
     *
     * @param results 筛查结果
     * @return List<CylDetails> 柱镜详情
     */
    private List<CylDetails> packageVisionTrendsByCyl(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            CylDetails details = new CylDetails();

            // 左眼
            CylDetails.Item left = new CylDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(result.getCreateTime());
            left.setVision(result.getComputerOptometry().getLeftEyeData().getCyl());

            // 右眼
            CylDetails.Item right = new CylDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(result.getCreateTime());
            right.setVision(result.getComputerOptometry().getRightEyeData().getCyl());

            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 球镜详情
     *
     * @param results 筛查结果
     * @return List<SphDetails> 球镜详情
     */
    private List<SphDetails> packageVisionTrendsBySph(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            SphDetails details = new SphDetails();

            // 左眼
            SphDetails.Item left = new SphDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(result.getCreateTime());
            left.setVision(result.getComputerOptometry().getLeftEyeData().getSph());

            // 右眼
            SphDetails.Item right = new SphDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(result.getCreateTime());
            right.setVision(result.getComputerOptometry().getRightEyeData().getSph());

            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 裸眼视力详情
     *
     * @param results 筛查结果
     * @return List<NakedVisionDetails> 裸眼视力详情
     */
    private List<NakedVisionDetails> packageVisionTrendsByNakedVision(List<VisionScreeningResult> results) {
        return results.stream().map(result -> {
            NakedVisionDetails details = new NakedVisionDetails();

            // 左眼
            NakedVisionDetails.Item left = new NakedVisionDetails.Item();
            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(result.getCreateTime());
            left.setVision(result.getVisionData().getLeftEyeData().getNakedVision());

            // 右眼
            NakedVisionDetails.Item right = new NakedVisionDetails.Item();
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(result.getCreateTime());
            right.setVision(result.getVisionData().getRightEyeData().getNakedVision());

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
     * @return TwoTuple<BigDecimal, Integer> left-视力 right-左右眼
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
            // 当前时间
            now.setTime(new Date());

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthday);

            // 如果传入的时间，在当前时间的后面，返回0岁
            if (birth.after(now)) {
                age = 0;
            } else {
                age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (now.get(Calendar.DAY_OF_YEAR) > birth.get(Calendar.DAY_OF_YEAR)) {
                    age += 1;
                }
            }
            return age;
        } catch (Exception e) {
            // 兼容性更强,异常后返回数据
            return 0;
        }
    }

    /**
     * 获取散光轴位
     *
     * @param axial 轴位
     * @return String 中文散光轴位
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
     * @return TwoTuple<> left-球镜中文 right-预警级别(重新封装的一层)
     */
    private TwoTuple<String, Integer> getSphTypeName(BigDecimal sph, BigDecimal cyl, Integer age) {
        if (sph.compareTo(new BigDecimal("0.00")) <= 0) {
            // 近视
            WarningLevel myopiaWarningLevel = StatUtil.getMyopiaWarningLevel(sph.floatValue(), cyl.floatValue());
            return new TwoTuple<>("近视" + sph.abs() + "度", warningLevel2Type(myopiaWarningLevel));
        } else {
            // 远视
            WarningLevel hyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(sph.floatValue(), cyl.floatValue(), age);
            return new TwoTuple<>("远视" + sph.abs() + "度", warningLevel2Type(hyperopiaWarningLevel));
        }
    }

    /**
     * 获取散光TypeName
     *
     * @param cyl 柱镜
     * @return String 散光中文名
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
     * @return Integer {@link ParentReportConst}
     */
    private Integer lowVisionType(BigDecimal nakedVision, Integer age) {
        boolean lowVision = StatUtil.isLowVision(nakedVision.floatValue(), age);
        if (lowVision) {
            return ParentReportConst.NAKED_LOW;
        }
        return ParentReportConst.NAKED_NORMAL;
    }

    /**
     * 预警级别转换成type
     * <p>预警级别 {@link WarningLevel}</p>
     *
     * @param warningLevel 预警级别
     * @return Integer {@link ParentReportConst}
     */
    private Integer warningLevel2Type(WarningLevel warningLevel) {
        if (null == warningLevel) {
            return ParentReportConst.LABEL_NORMAL;
        }
        // 预警-1或0则是正常
        if (warningLevel.code == WarningLevel.NORMAL.code || warningLevel.code == WarningLevel.ZERO.code) {
            return ParentReportConst.LABEL_NORMAL;
        }

        // 预警1是轻度
        if (warningLevel.code == WarningLevel.ONE.code) {
            return ParentReportConst.LABEL_MILD;
        }

        // 预警2是中度
        if (warningLevel.code == WarningLevel.TWO.code) {
            return ParentReportConst.LABEL_MODERATE;
        }

        // 预警3是重度
        if (warningLevel.code == WarningLevel.THREE.code) {
            return ParentReportConst.LABEL_SEVERE;
        }
        // 未知返回正常
        return ParentReportConst.LABEL_NORMAL;
    }

    /**
     * 获取最大值
     *
     * @param val 数值
     * @return Integer 最大值
     */
    private Integer getIntegerMax(Integer... val) {
        return Arrays.stream(val).max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * 矫正状态
     *
     * @param nakedVision     裸眼视力
     * @param correctedVision 矫正视力
     * @param glassesType     戴镜类型
     * @return Integer {@link ParentReportConst}
     */
    public Integer getCorrected2Type(BigDecimal nakedVision, BigDecimal correctedVision, Integer glassesType) {
        // 凡单眼裸眼视力＜4.9时，计入矫正人数
        if (nakedVision.compareTo(new BigDecimal("4.9")) < 0) {
            // 有问题但是没有佩戴眼镜,为未矫
            if (glassesType.equals(GlassesType.NOT_WEARING.code)) {
                return ParentReportConst.CORRECTED_NOT;
            } else {
                if (correctedVision.compareTo(new BigDecimal("4.9")) > 0) {
                    // 戴镜视力都＞4.9，正常
                    return ParentReportConst.CORRECTED_NORMAL;
                } else {
                    // 戴镜视力都<=4.9，欠矫
                    return ParentReportConst.CORRECTED_OWE;
                }
            }
        }
        return ParentReportConst.CORRECTED_NORMAL;
    }
}