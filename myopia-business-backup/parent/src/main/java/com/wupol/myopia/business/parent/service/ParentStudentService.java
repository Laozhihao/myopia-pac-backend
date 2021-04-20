package com.wupol.myopia.business.parent.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.constant.GlassesType;
import com.wupol.myopia.business.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.hospital.domain.vo.ReportAndRecordVo;
import com.wupol.myopia.business.hospital.service.MedicalReportService;
import com.wupol.myopia.business.management.constant.*;
import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.management.service.ResourceFileService;
import com.wupol.myopia.business.management.service.SchoolService;
import com.wupol.myopia.business.management.service.StudentService;
import com.wupol.myopia.business.management.service.VisionScreeningResultService;
import com.wupol.myopia.business.management.util.StatUtil;
import com.wupol.myopia.business.management.util.TwoTuple;
import com.wupol.myopia.business.parent.domain.dto.*;
import com.wupol.myopia.business.parent.domain.mapper.ParentStudentMapper;
import com.wupol.myopia.business.parent.domain.model.Parent;
import com.wupol.myopia.business.parent.domain.model.ParentStudent;
import com.wupol.myopia.business.parent.domain.vo.ParentStudentVO;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

    @Resource
    private ParentService parentService;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private RedisUtil redisUtil;

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
     * @return Student 学生
     */
    public StudentDTO checkIdCard(CheckIdCardRequest request) {
        String idCard = request.getIdCard();
        StudentDTO studentDTO = new StudentDTO();
        Student student = studentService.getByIdCard(idCard);

        if (null == student) {
            // 为空说明是新增
            TwoTuple<Date, Integer> idCardInfo = getIdCardInfo(idCard);
            studentDTO.setBirthday(idCardInfo.getFirst());
            studentDTO.setGender(idCardInfo.getSecond());
            return studentDTO;
        } else {
            // 检查与姓名是否匹配
            if (!StringUtils.equals(request.getName(), student.getName())) {
                throw new BusinessException("身份证号与学生姓名不一致");
            }
        }
        BeanUtils.copyProperties(student, studentDTO);
        if (StringUtils.isNotBlank(student.getSchoolNo())) {
            // 学校编号不为空，则拼接学校信息
            School school = schoolService.getBySchoolNo(student.getSchoolNo());
            studentDTO.setSchoolId(school.getId());
            studentDTO.setSchoolNo(school.getSchoolNo());
            studentDTO.setSchoolName(school.getName());
        }
        return studentDTO;
    }

    /**
     * 通过身份证获取个人信息
     *
     * @param idCard 身份证
     * @return TwoTuple<Date, Integer> 出生日期, 性别
     */
    private TwoTuple<Date, Integer> getIdCardInfo(String idCard) {
        String birthdayStr = null;
        Integer gender = null;

        char[] number = idCard.toCharArray();
        boolean flag = true;
        if (number.length == 15) {
            for (char c : number) {
                if (!flag) {
                    return new TwoTuple<>();
                }
                flag = Character.isDigit(c);
            }
        } else if (number.length == 18) {
            for (int x = 0; x < number.length - 1; x++) {
                if (!flag) {
                    return new TwoTuple<>();
                }
                flag = Character.isDigit(number[x]);
            }
        }
        if (flag && idCard.length() == 15) {
            birthdayStr = "19" + idCard.substring(6, 8) + "-"
                    + idCard.substring(8, 10) + "-"
                    + idCard.substring(10, 12);
            gender = Integer.parseInt(idCard.substring(idCard.length() - 3)) % 2 == 0 ? GenderEnum.FEMALE.type : GenderEnum.MALE.type;
        } else if (flag && idCard.length() == 18) {
            birthdayStr = idCard.substring(6, 10) + "-"
                    + idCard.substring(10, 12) + "-"
                    + idCard.substring(12, 14);
            gender = Integer.parseInt(idCard.substring(idCard.length() - 4, idCard.length() - 1)) % 2 == 0 ? GenderEnum.FEMALE.type : GenderEnum.MALE.type;
        }
        try {
            return new TwoTuple<>(DateFormatUtil.parseDate(birthdayStr, DateFormatUtil.FORMAT_ONLY_DATE), gender);
        } catch (ParseException e) {
            throw new BusinessException("身份证信息异常");
        }
    }

    /**
     * 更新孩子
     *
     * @param currentUser 当前用户
     * @param student     学生
     * @return StudentDTO
     * @throws IOException io异常
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudent(CurrentUser currentUser, Student student) throws IOException {
        // 查找家长ID
        Parent parent = parentService.getParentByUserId(currentUser.getId());
        if (null == parent) {
            throw new BusinessException("家长信息异常");
        }
        StudentDTO studentDTO = studentService.updateStudent(student);
        // 绑定孩子
        bindStudent(parent, student.getId());
        return studentDTO;
    }

    /**
     * 新增孩子
     *
     * @param student     学生
     * @param currentUser 当前登录用户
     * @return 学生ID
     * @throws IOException IO异常
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(Student student, CurrentUser currentUser) throws IOException {
        // 查找家长ID
        Parent parent = parentService.getParentByUserId(currentUser.getId());
        if (null == parent) {
            throw new BusinessException("家长信息异常");
        }
        // 保存孩子
        Integer studentId = studentService.saveStudent(student);
        // 绑定孩子
        bindStudent(parent, studentId);
        return studentId;
    }

    /**
     * 绑定孩子，更新孩子绑定家长手机号码
     *
     * @param parent    家长信息
     * @param studentId 学生ID
     */
    private void bindStudent(Parent parent, Integer studentId) {
        // 更新孩子绑定家长手机号码
        studentService.updateMpParentPhone(studentId, parent.getPhone());
        // 绑定孩子
        parentBindStudent(studentId, parent.getId());
    }

    /**
     * 获取学生信息
     *
     * @param studentId 学生ID
     * @return StudentDTO
     */
    public StudentDTO getStudentById(Integer studentId) {
        StudentDTO studentDTO = studentService.getStudentById(studentId);
        if (null != studentDTO.getAvatarFileId()) {
            studentDTO.setAvatar(resourceFileService.getResourcePath(studentDTO.getAvatarFileId()));
        }
        studentDTO.setToken(getQrCode(studentId));
        return studentDTO;
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
        List<CountReportItems> screeningLists = getStudentCountReportItems(studentId);
        ScreeningDetail screeningDetail = new ScreeningDetail();
        screeningDetail.setTotal(visionScreeningResultService.getByStudentId(studentId).stream().filter(r -> r.getIsDoubleScreen().equals(Boolean.FALSE)).count());
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
     * 学生筛查报告列表
     *
     * @param studentId 学生ID
     * @return List<CountReportItems>
     */
    public List<CountReportItems> getStudentCountReportItems(Integer studentId) {
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByStudentId(studentId);
        return screeningResults.stream().filter(result -> result.getIsDoubleScreen().equals(Boolean.FALSE))
                .map(result -> {
                    CountReportItems items = new CountReportItems();
                    items.setId(result.getId());
                    items.setCreateTime(result.getCreateTime());
                    items.setUpdateTime(result.getUpdateTime());
                    return items;
                }).collect(Collectors.toList());
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
            ScreeningReportResponseDTO responseDTO = new ScreeningReportResponseDTO();
            ScreeningReportDetail detail = new ScreeningReportDetail();
            // 视力检查结果
            detail.setVisionResultItems(Lists.newArrayList(new VisionItems("矫正视力"),
                    new VisionItems("裸眼视力")));
            // 验光仪检查结果
            detail.setRefractoryResultItems(Lists.newArrayList(new RefractoryResultItems("等效球镜SE"),
                    new RefractoryResultItems("柱镜DC"),
                    new RefractoryResultItems("轴位A")));
            responseDTO.setDetail(detail);
            return responseDTO;
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
     * @return StudentVisitReportResponseDTO
     */
    public StudentVisitReportResponseDTO latestVisitsReport(Integer studentId) {
        // 查找学生最近的就诊记录
        List<ReportAndRecordVo> visitLists = medicalReportService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(visitLists)) {
            return new StudentVisitReportResponseDTO();
        }
        ReportAndRecordVo reportAndRecordVo = visitLists.get(0);
        return medicalReportService.getStudentVisitReport(reportAndRecordVo.getReportId());
    }

    /**
     * 获取就诊报告详情
     *
     * @param request 请求入参
     * @return StudentVisitReportResponseDTO 学生就诊记录档案卡
     */
    public StudentVisitReportResponseDTO getVisitsReportDetails(VisitsReportDetailRequest request) {
        return medicalReportService.getStudentVisitReport(request.getReportId());
    }

    /**
     * 视力趋势
     *
     * @param studentId 学生ID
     * @return ScreeningVisionTrendsResponseDTO 视力趋势
     */
    public ScreeningVisionTrendsResponseDTO screeningVisionTrends(Integer studentId) {
        ScreeningVisionTrendsResponseDTO responseDTO = new ScreeningVisionTrendsResponseDTO();
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByStudentId(studentId)
                .stream()
                .filter(result -> result.getIsDoubleScreen().equals(Boolean.FALSE))
                .collect(Collectors.toList());
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
     * 获取学生授权二维码
     *
     * @param studentId 学生Id
     * @return ApiResult<String>
     */
    public String getQrCode(Integer studentId) {
        Student student = studentService.getById(studentId);
        if (Objects.isNull(student)) {
            throw new BusinessException("学生信息异常");
        }
        String md5 = StringUtils.upperCase(SecureUtil.md5(student.getIdCard() + studentId + IdUtil.simpleUUID()));
        String key = String.format(CacheKey.PARENT_STUDENT_QR_CODE, md5);
        if (!redisUtil.set(key, studentId, 60 * 60)) {
            throw new BusinessException("获取学生授权二维码失败");
        }
        return md5;
    }

    /**
     * 家长绑定学生
     *
     * @param studentId 学生ID
     * @param parentId  家长ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void parentBindStudent(Integer studentId, Integer parentId) {
        ParentStudent parentStudent = new ParentStudent();
        if (null == parentId || null == studentId) {
            throw new BusinessException("数据异常");
        }
        ParentStudent checkResult = baseMapper.getByParentIdAndStudentId(parentId, studentId);
        if (null != checkResult) {
            return;
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
        int age = DateUtil.ageOfNow(student.getBirthday());

        ScreeningReportDetail responseDTO = new ScreeningReportDetail();
        responseDTO.setScreeningDate(result.getUpdateTime());
        VisionDataDO visionData = result.getVisionData();
        // 视力检查结果
        responseDTO.setVisionResultItems(packageVisionResult(visionData, age));
        // 验光仪检查结果
        TwoTuple<List<RefractoryResultItems>, Integer> refractoryResult = packageRefractoryResult(result.getComputerOptometry(), age);
        responseDTO.setRefractoryResultItems(refractoryResult.getFirst());
        // 生物测量
        responseDTO.setBiometricItems(packageBiometricResult(result.getBiometricData(), result.getOtherEyeDiseases()));
        // 医生建议一（这里-5是为了type的偏移量）
        Integer doctorAdvice1 = refractoryResult.getSecond() - 5;
        if (doctorAdvice1.equals(-5)) {
            responseDTO.setDoctorAdvice1(null);
        } else {
            responseDTO.setDoctorAdvice1(doctorAdvice1);
        }
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
        if (Objects.isNull(leftNakedVision) && Objects.isNull(rightNakedVision)) {
            return "";
        }

        // 获取左右眼的矫正视力
        BigDecimal leftCorrectedVision = visionData.getLeftEyeData().getCorrectedVision();
        BigDecimal rightCorrectedVision = visionData.getRightEyeData().getCorrectedVision();

        BigDecimal leftSph = computerOptometry.getLeftEyeData().getSph();
        BigDecimal leftCyl = computerOptometry.getLeftEyeData().getCyl();
        BigDecimal rightSph = computerOptometry.getRightEyeData().getSph();
        BigDecimal rightCyl = computerOptometry.getRightEyeData().getCyl();

        return packageDoctorAdvice(leftNakedVision, rightNakedVision,
                leftCorrectedVision, rightCorrectedVision,
                leftSph, rightSph, leftCyl, rightCyl,
                glassesType, gradeType);
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
            if (Objects.nonNull(leftNakedVisionValue)) {
                nakedVision.setOs(packageNakedVision(leftNakedVision, leftNakedVisionValue, age));
            }

            // 右裸眼视力
            VisionItems.Item rightNakedVision = new VisionItems.Item();
            BigDecimal rightNakedVisionValue = date.getRightEyeData().getNakedVision();
            if (Objects.nonNull(rightNakedVisionValue)) {
                nakedVision.setOd(packageNakedVision(rightNakedVision, rightNakedVisionValue, age));
            }

            // 左矫正视力
            VisionItems.Item leftCorrectedVision = new VisionItems.Item();
            BigDecimal leftCorrectedVisionValue = date.getLeftEyeData().getCorrectedVision();
            if (Objects.nonNull(leftCorrectedVisionValue)) {
                correctedVision.setOs(packageCorrectedVision(leftCorrectedVision, leftCorrectedVisionValue,
                        leftNakedVisionValue, glassesType));
            }

            // 右矫正视力
            VisionItems.Item rightCorrectedVision = new VisionItems.Item();
            BigDecimal rightCorrectedVisionValue = date.getRightEyeData().getCorrectedVision();
            if (Objects.nonNull(rightCorrectedVisionValue)) {
                correctedVision.setOd(packageCorrectedVision(rightCorrectedVision, rightCorrectedVisionValue,
                        rightNakedVisionValue, glassesType));
            }
        }
        itemsList.add(correctedVision);
        itemsList.add(nakedVision);
        return itemsList;
    }

    /**
     * 封装裸眼视力
     *
     * @param nakedVision      返回的实体
     * @param nakedVisionValue 裸眼视力值
     * @param age              年龄
     * @return VisionItems.Item
     */
    private VisionItems.Item packageNakedVision(VisionItems.Item nakedVision, BigDecimal nakedVisionValue, Integer age) {
        nakedVision.setVision(nakedVisionValue);
        nakedVision.setType(lowVisionType(nakedVisionValue, age));
        return nakedVision;
    }

    /**
     * 封装矫正视力
     *
     * @param correctedVision      返回的实体
     * @param correctedVisionValue 矫正视力值
     * @param nakedVisionValue     裸眼视力值
     * @param glassesType          戴镜类型
     * @return VisionItems.Item
     */
    private VisionItems.Item packageCorrectedVision(VisionItems.Item correctedVision, BigDecimal correctedVisionValue,
                                                    BigDecimal nakedVisionValue, Integer glassesType) {
        correctedVision.setVision(correctedVisionValue);
        if (Objects.nonNull(nakedVisionValue)) {
            correctedVision.setType(getCorrected2Type(nakedVisionValue, correctedVisionValue, glassesType));
        }
        return correctedVision;
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
        Integer maxType = 0;

        RefractoryResultItems sphItems = new RefractoryResultItems();
        sphItems.setTitle("等效球镜SE");

        RefractoryResultItems cylItems = new RefractoryResultItems();
        cylItems.setTitle("柱镜DC");

        RefractoryResultItems axialItems = new RefractoryResultItems();
        axialItems.setTitle("轴位A");

        if (Objects.nonNull(date)) {
            // 左眼数据
            ComputerOptometryDO.ComputerOptometry leftEyeData = date.getLeftEyeData();
            BigDecimal leftSph = leftEyeData.getSph();
            BigDecimal leftCyl = leftEyeData.getCyl();

            BigDecimal rightSph = date.getRightEyeData().getSph();
            BigDecimal rightCyl = date.getRightEyeData().getCyl();

            BigDecimal leftAxial = leftEyeData.getAxial();
            BigDecimal rightAxial = date.getRightEyeData().getAxial();

            // 左眼等效球镜SE
            if (Objects.nonNull(leftSph) && Objects.nonNull(leftCyl)) {
                TwoTuple<Integer, RefractoryResultItems.Item> result = packageSpnItem(leftSph, leftCyl, age, maxType);
                maxType = result.getFirst();
                sphItems.setOs(result.getSecond());
            }
            // 右眼等效球镜SE
            if (Objects.nonNull(rightSph) && Objects.nonNull(rightCyl)) {
                TwoTuple<Integer, RefractoryResultItems.Item> result = packageSpnItem(rightSph, rightCyl, age, maxType);
                maxType = result.getFirst();
                sphItems.setOd(result.getSecond());
            }
            items.add(sphItems);

            // 左眼柱镜DC
            if (Objects.nonNull(leftCyl)) {
                TwoTuple<Integer, RefractoryResultItems.Item> result = packageCylItem(leftCyl, maxType);
                maxType = result.getFirst();
                cylItems.setOs(result.getSecond());
            }
            // 右眼柱镜DC
            if (Objects.nonNull(rightCyl)) {
                TwoTuple<Integer, RefractoryResultItems.Item> result = packageCylItem(rightCyl, maxType);
                maxType = result.getFirst();
                cylItems.setOd(result.getSecond());
            }
            items.add(cylItems);

            // 左眼轴位A
            if (Objects.nonNull(leftAxial)) {
                axialItems.setOs(packageAxialItem(leftAxial));
            }
            // 右眼轴位A
            if (Objects.nonNull(rightAxial)) {
                axialItems.setOs(packageAxialItem(rightAxial));
            }
            items.add(axialItems);
            return new TwoTuple<>(items, maxType);
        }
        items.add(sphItems);
        items.add(cylItems);
        items.add(axialItems);
        return new TwoTuple<>(items, maxType);
    }

    /**
     * 封装等效球镜SE
     *
     * @param spn     球镜
     * @param cyl     柱镜
     * @param age     年龄
     * @param maxType 最大类型
     * @return TwoTuple<Integer, RefractoryResultItems.Item>
     */
    private TwoTuple<Integer, RefractoryResultItems.Item> packageSpnItem(BigDecimal spn, BigDecimal cyl, Integer age, Integer maxType) {
        RefractoryResultItems.Item sphItems = new RefractoryResultItems.Item();
        // 等效球镜SE
        sphItems.setVision(calculationSE(spn, cyl));
        TwoTuple<String, Integer> leftSphType = getSphTypeName(spn, cyl, age);
        sphItems.setTypeName(leftSphType.getFirst());
        Integer type = leftSphType.getSecond();
        // 取最大的type
        maxType = maxType > type ? maxType : type;
        sphItems.setType(type);
        return new TwoTuple<>(maxType, sphItems);
    }

    /**
     * 封装柱镜DC
     *
     * @param cyl     柱镜
     * @param maxType 最大类型
     * @return TwoTuple<Integer, RefractoryResultItems.Item>
     */
    private TwoTuple<Integer, RefractoryResultItems.Item> packageCylItem(BigDecimal cyl, Integer maxType) {
        RefractoryResultItems.Item cylItems = new RefractoryResultItems.Item();
        cylItems.setVision(cyl);
        TwoTuple<String, Integer> leftCylType = getCylTypeName(cyl);
        Integer type = leftCylType.getSecond();
        cylItems.setType(type);
        // 取最大的type
        maxType = maxType > type ? maxType : type;
        cylItems.setTypeName(leftCylType.getFirst());
        return new TwoTuple<>(maxType, cylItems);
    }

    /**
     * 封装轴位
     *
     * @param axial 轴位
     * @return RefractoryResultItems.Item {@link RefractoryResultItems.Item}
     */
    private RefractoryResultItems.Item packageAxialItem(BigDecimal axial) {
        RefractoryResultItems.Item axialItems = new RefractoryResultItems.Item();
        axialItems.setVision(axial);
        axialItems.setTypeName(getAxialTypeName(axial));
        return axialItems;
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

            CorrectedVisionDetails.Item left = new CorrectedVisionDetails.Item();
            CorrectedVisionDetails.Item right = new CorrectedVisionDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            VisionDataDO visionData = result.getVisionData();
            if (Objects.nonNull(visionData)) {
                // 左眼
                left.setVision(visionData.getLeftEyeData().getCorrectedVision());
                // 右眼
                right.setVision(visionData.getRightEyeData().getCorrectedVision());
            }
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

            CylDetails.Item left = new CylDetails.Item();
            CylDetails.Item right = new CylDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            ComputerOptometryDO computerOptometry = result.getComputerOptometry();
            if (Objects.nonNull(computerOptometry)) {
                // 左眼
                left.setVision(computerOptometry.getLeftEyeData().getCyl().multiply(new BigDecimal("100")));
                // 右眼
                right.setVision(computerOptometry.getRightEyeData().getCyl().multiply(new BigDecimal("100")));
            }
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

            SphDetails.Item left = new SphDetails.Item();
            SphDetails.Item right = new SphDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            ComputerOptometryDO computerOptometry = result.getComputerOptometry();
            if (Objects.nonNull(computerOptometry)) {
                // 左眼
                left.setVision(calculationSE(computerOptometry.getLeftEyeData().getSph(),
                        computerOptometry.getLeftEyeData().getCyl()));
                // 右眼
                right.setVision(calculationSE(computerOptometry.getRightEyeData().getSph(),
                        computerOptometry.getRightEyeData().getCyl()));
            }
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

            NakedVisionDetails.Item left = new NakedVisionDetails.Item();
            NakedVisionDetails.Item right = new NakedVisionDetails.Item();

            left.setLateriality(CommonConst.LEFT_EYE);
            left.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));
            right.setLateriality(CommonConst.RIGHT_EYE);
            right.setCreateTime(DateFormatUtil.format(result.getCreateTime(), DateFormatUtil.FORMAT_ONLY_DATE));

            VisionDataDO visionData = result.getVisionData();
            if (Objects.nonNull(visionData)) {
                // 左眼
                left.setVision(visionData.getLeftEyeData().getNakedVision());
                // 右眼
                right.setVision(visionData.getRightEyeData().getNakedVision());
            }
            details.setItem(Lists.newArrayList(left, right));
            return details;
        }).collect(Collectors.toList());
    }

    /**
     * 取视力值低的眼球
     *
     * @param left  左眼
     * @param right 右眼
     * @return TwoTuple<BigDecimal, Integer> left-视力 right-左右眼
     */
    private TwoTuple<BigDecimal, Integer> getResultVision(BigDecimal left, BigDecimal right) {
        if (Objects.isNull(left) || Objects.isNull(right)) {
            // 左眼为空取右眼
            if (Objects.isNull(left)) {
                return new TwoTuple<>(right, CommonConst.RIGHT_EYE);
            }
            // 右眼为空取左眼
            return new TwoTuple<>(left, CommonConst.LEFT_EYE);
        }
        if (left.compareTo(right) == 0) {
            return new TwoTuple<>(left, CommonConst.SAME_EYE);
        }
        if (left.compareTo(right) < 0) {
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
     * 判断是否在某个区间，左闭右比区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    private Boolean isBetweenAll(BigDecimal val, BigDecimal start, BigDecimal end) {
        return val.compareTo(start) >= 0 && val.compareTo(end) <= 0;
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
        BigDecimal se = calculationSE(sph, cyl);
        BigDecimal seVal = se.abs().multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN);
        if (sph.compareTo(new BigDecimal("0.00")) <= 0) {
            // 近视
            WarningLevel myopiaWarningLevel = StatUtil.getMyopiaWarningLevel(sph.floatValue(), cyl.floatValue());
            String str;
            if (sph.compareTo(new BigDecimal("-0.50")) < 0) {
                str = "近视" + seVal + "度";
            } else {
                str = seVal + "度";
            }
            return new TwoTuple<>(str, warningLevel2Type(myopiaWarningLevel));
        } else {
            // 远视
            WarningLevel hyperopiaWarningLevel = StatUtil.getHyperopiaWarningLevel(sph.floatValue(), cyl.floatValue(), age);
            String str;
            if (sph.compareTo(new BigDecimal("0.50")) > 0) {
                str = "远视" + seVal + "度";
            } else {
                str = seVal + "度";
            }
            return new TwoTuple<>(str, warningLevel2Type(hyperopiaWarningLevel));
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
        BigDecimal cylVal = cyl.abs().multiply(new BigDecimal("100")).setScale(0, BigDecimal.ROUND_DOWN);
        if (isBetweenAll(cyl, new BigDecimal("-0.5"), new BigDecimal("0.5"))) {
            return new TwoTuple<>(cylVal + "度", warningLevel2Type(astigmatismWarningLevel));
        }
        return new TwoTuple<>("散光" + cylVal + "度", warningLevel2Type(astigmatismWarningLevel));
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
        if (warningLevel.code.equals(WarningLevel.NORMAL.code) || warningLevel.code.equals(WarningLevel.ZERO.code)) {
            return ParentReportConst.LABEL_NORMAL;
        }

        // 预警1是轻度
        if (warningLevel.code.equals(WarningLevel.ONE.code)) {
            return ParentReportConst.LABEL_MILD;
        }

        // 预警2是中度
        if (warningLevel.code.equals(WarningLevel.TWO.code)) {
            return ParentReportConst.LABEL_MODERATE;
        }

        // 预警3是重度
        if (warningLevel.code.equals(WarningLevel.THREE.code)) {
            return ParentReportConst.LABEL_SEVERE;
        }
        // 未知返回正常
        return ParentReportConst.LABEL_NORMAL;
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

    /**
     * 获取医生建议
     *
     * @param leftNakedVision      左-裸眼
     * @param rightNakedVision     右-裸眼
     * @param leftCorrectedVision  左-矫正视力
     * @param rightCorrectedVision 右-矫正视力
     * @param leftSph              左-柱镜
     * @param rightSph             右-柱镜
     * @param leftCyl              左-球镜
     * @param rightCyl             右-球镜
     * @param glassesType          戴镜类型
     * @param schoolAge            学龄段
     * @return 医生建议
     */
    private String packageDoctorAdvice(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                       BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                       BigDecimal leftSph, BigDecimal rightSph,
                                       BigDecimal leftCyl, BigDecimal rightCyl,
                                       Integer glassesType, Integer schoolAge) {

        TwoTuple<BigDecimal, Integer> nakedVisionResult = getResultVision(leftNakedVision, rightNakedVision);
        BigDecimal leftSe = calculationSE(leftSph, leftCyl);
        BigDecimal rightSe = calculationSE(rightSph, rightCyl);

        // 裸眼视力是否小于4.9
        if (nakedVisionResult.getFirst().compareTo(new BigDecimal("4.9")) < 0) {
            // 是否佩戴眼镜
            if (glassesType >= 1) {
                return getIsWearingGlasses(leftCorrectedVision, rightCorrectedVision,
                        leftNakedVision, rightNakedVision, nakedVisionResult);
            } else {
                // 没有佩戴眼镜
                TwoTuple<Integer, String> left = getNotWearingGlasses(leftCyl, leftSe,
                        schoolAge, leftNakedVision);
                TwoTuple<Integer, String> right = getNotWearingGlasses(rightCyl, rightSe,
                        schoolAge, rightNakedVision);
                // 取结论严重的那只眼
                if (left.getFirst() >= right.getFirst()) {
                    return left.getSecond();
                } else {
                    return right.getSecond();
                }
            }
        } else {
            // 裸眼视力大于4.9
            return nakedVisionNormal(leftNakedVision, rightNakedVision, leftSe, rightSe, nakedVisionResult);
        }
    }

    /**
     * 两眼的值是否都在4.9的同侧
     *
     * @param leftNakedVision  左裸眼视力
     * @param rightNakedVision 右裸眼数据
     * @return Boolean
     */
    private Boolean isNakedVisionMatch(BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        return ((leftNakedVision.compareTo(new BigDecimal("4.9")) < 0) &&
                (rightNakedVision.compareTo(new BigDecimal("4.9")) < 0))
                ||
                ((leftNakedVision.compareTo(new BigDecimal("4.9")) >= 0) &&
                        (rightNakedVision.compareTo(new BigDecimal("4.9")) >= 0));
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
    private String getIsWearingGlasses(BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                       BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                       TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        if (Objects.isNull(leftCorrectedVision) || Objects.isNull(rightCorrectedVision)) {
            return "";
        }
        BigDecimal visionVal;
        // 判断两只眼睛的裸眼视力是否都小于4.9或大于等于4.9
        if (isNakedVisionMatch(leftNakedVision, rightNakedVision)) {
            // 获取矫正视力低的眼球
            visionVal = getResultVision(leftCorrectedVision, rightCorrectedVision).getFirst();
        } else {
            if (nakedVisionResult.getSecond().equals(CommonConst.LEFT_EYE)) {
                // 取左眼数据
                visionVal = leftCorrectedVision;
            } else {
                // 取右眼数据
                visionVal = rightCorrectedVision;
            }
        }
        if (visionVal.compareTo(new BigDecimal("4.9")) < 0) {
            // 矫正视力小于4.9
            return "裸眼远视力下降，戴镜远视力下降。建议：请及时到医疗机构复查。";
        } else {
            // 矫正视力大于4.9
            return "裸眼远视力下降，戴镜远视力≥4.9。建议：请3个月或半年1次检查裸眼视力和戴镜视力。";
        }
    }

    /**
     * 没有戴镜情况下获取结论
     *
     * @param cyl         柱镜
     * @param se          等效球镜
     * @param schoolAge   学龄段
     * @param nakedVision 裸眼视力
     * @return TwoTuple<Integer, String>
     */
    private TwoTuple<Integer, String> getNotWearingGlasses(BigDecimal cyl, BigDecimal se,
                                                           Integer schoolAge, BigDecimal nakedVision) {
        // 是否大于4.9，大于4.9直接返回
        if (nakedVision.compareTo(new BigDecimal("4.90")) >= 0) {
            return new TwoTuple<>(0, "");
        }
        boolean checkCyl = cyl.abs().compareTo(new BigDecimal("1.5")) < 0;
        // (小学生 && 0<=SE<2 && Cyl <1.5) || (初中生、高中、职业高中 && -0.5<=SE<3 && Cyl <1.5)
        if ((SchoolAge.PRIMARY.code.equals(schoolAge) && isBetweenLeft(se, "0.00", "2.00") && checkCyl)
                ||
                (SchoolAge.isMiddleSchool(schoolAge) && isBetweenLeft(se, "-0.50", "3.00") && checkCyl)
        ) {
            return new TwoTuple<>(1, "裸眼远视力下降，视功能可能异常。建议：请到医疗机构接受检查，明确诊断并及时采取措施。");
            // (小学生 && !(0 <= SE < 2)) || (初中生、高中、职业高中 && (Cyl >= 1.5 || !(-0.5 <= SE < 3)))
        } else if ((SchoolAge.PRIMARY.code.equals(schoolAge) && !isBetweenLeft(se, "0.00", "2.00"))
                ||
                (SchoolAge.isMiddleSchool(schoolAge) && (!isBetweenLeft(se, "-0.50", "3.00") || !checkCyl))) {
            return new TwoTuple<>(2, "裸眼远视力下降，屈光不正筛查阳性。建议：请到医疗机构接受检查，明确诊断并及时采取措施。");
        }
        return new TwoTuple<>(0, "");
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
        BigDecimal se;
        // 判断两只眼睛的裸眼视力是否都在4.9的同侧
        if (isNakedVisionMatch(leftNakedVision, rightNakedVision)) {
            // 取等效球镜严重的眼别
            if (leftSe.compareTo(new BigDecimal("0.00")) <= 0
                    || rightSe.compareTo(new BigDecimal("0.00")) <= 0) {
                if (leftSe.compareTo(new BigDecimal("0.00")) <= 0) {
                    // 取左眼
                    se = leftSe;
                } else {
                    // 取右眼
                    se = rightSe;
                }
            } else {
                // 取等效球镜值大的眼别
                if (leftSe.compareTo(rightSe) >= 0) {
                    // 取左眼
                    se = leftSe;
                } else {
                    // 取右眼
                    se = rightSe;
                }
            }
        } else {
            // 裸眼视力不同，取视力低的眼别
            if (nakedVisionResult.getSecond().equals(CommonConst.LEFT_EYE)) {
                // 左眼的等效球镜
                se = leftSe;
            } else {
                // 右眼的等效球镜
                se = rightSe;
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
}