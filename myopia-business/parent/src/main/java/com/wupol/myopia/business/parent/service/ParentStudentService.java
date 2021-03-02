package com.wupol.myopia.business.parent.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.hospital.domain.dto.StudentReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import com.wupol.myopia.business.hospital.service.MedicalReportService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.service.VisionScreeningResultService;
import com.wupol.myopia.business.parent.domain.dto.*;
import com.wupol.myopia.business.parent.domain.mapper.ParentStudentMapper;
import com.wupol.myopia.business.parent.domain.model.ParentStudent;
import com.wupol.myopia.business.parent.domain.vo.ParentStudentVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    public Integer checkIdCard(CheckIdCardRequest request) {
        Student student = studentService.getByIdCard(request.getIdCard());

        if (null == student) {
            // 为空说明是新增
            return null;
        } else {
            // 检查与姓名是否匹配
            if (!StringUtils.equals(request.getName(), student.getName())) {
                throw new BusinessException("身份证数据异常");
            }
            return student.getId();
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
        // 查找学生最近的就诊报告
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
        ScreeningReportResponseDTO responseDTO = new ScreeningReportResponseDTO();
        responseDTO.setScreeningDate(result.getCreateTime());
        responseDTO.setGlassesType("1");
        responseDTO.setNakedVisionItems(setNakedVision(result.getVisionData()));
        responseDTO.setRefractoryResultItems(setRefractoryResult(result.getComputerOptometry()));
        return responseDTO;
    }

    /**
     * 视力检查结果
     *
     * @param date 数据
     * @return List<NakedVisionItems>
     */
    private List<NakedVisionItems> setNakedVision(VisionDataDO date) {

        NakedVisionItems left = new NakedVisionItems();
        left.setLateriality(CommonConst.LEFT_EYE);
        left.setCorrectedVision(date.getLeftEyeData().getCorrectedVision());
        left.setNakedVision(date.getLeftEyeData().getNakedVision());

        NakedVisionItems right = new NakedVisionItems();
        right.setLateriality(CommonConst.RIGHT_EYE);
        right.setCorrectedVision(date.getRightEyeData().getCorrectedVision());
        right.setNakedVision(date.getRightEyeData().getNakedVision());

        return Lists.newArrayList(right, left);
    }

    /**
     * 验光仪检查结果
     *
     * @param date 数据
     * @return List<RefractoryResultItems>
     */
    private List<RefractoryResultItems> setRefractoryResult(ComputerOptometryDO date) {
        RefractoryResultItems left = new RefractoryResultItems();
        left.setLateriality(CommonConst.LEFT_EYE);
        left.setAxial(date.getLeftEyeData().getAxial());
        left.setSph(date.getLeftEyeData().getSph());
        left.setCyl(date.getLeftEyeData().getCyl());


        RefractoryResultItems right = new RefractoryResultItems();
        right.setLateriality(CommonConst.RIGHT_EYE);
        right.setAxial(date.getRightEyeData().getAxial());
        right.setSph(date.getRightEyeData().getSph());
        right.setCyl(date.getRightEyeData().getCyl());
        return Lists.newArrayList(right, left);
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
}
