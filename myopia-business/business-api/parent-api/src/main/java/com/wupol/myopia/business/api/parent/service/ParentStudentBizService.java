package com.wupol.myopia.business.api.parent.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.hospital.domain.dto.StudentVisitReportResponseDTO;
import com.wupol.myopia.business.aggregation.hospital.service.MedicalReportBizService;
import com.wupol.myopia.business.aggregation.hospital.service.OrgCooperationHospitalBizService;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.parent.domain.dos.*;
import com.wupol.myopia.business.api.parent.domain.dto.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.QrCodeCacheKey;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.dto.SuggestHospitalDTO;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.model.OrgCooperationHospital;
import com.wupol.myopia.business.core.hospital.service.HospitalService;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.hospital.service.OrgCooperationHospitalService;
import com.wupol.myopia.business.core.parent.domain.dto.CheckIdCardRequestDTO;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.service.ParentService;
import com.wupol.myopia.business.core.parent.service.ParentStudentService;
import com.wupol.myopia.business.core.school.domain.dto.CountParentStudentResponseDTO;
import com.wupol.myopia.business.core.school.domain.dto.ParentStudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.RefractoryResultItems;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionItems;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningResultUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HaoHao
 * Date 2021/4/21
 **/
@Service
@Log4j2
public class ParentStudentBizService {

    @Resource
    private ResourceFileService resourceFileService;
    @Resource
    private StudentService studentService;
    @Resource
    private MedicalReportService medicalReportService;
    @Resource
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private ParentService parentService;
    @Resource
    private SchoolService schoolService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ParentStudentService parentStudentService;
    @Resource
    private OrgCooperationHospitalService orgCooperationHospitalService;
    @Resource
    private HospitalService hospitalService;
    @Resource
    private MedicalReportBizService medicalReportBizService;
    @Resource
    private OrgCooperationHospitalBizService orgCooperationHospitalBizService;
    @Resource
    private StudentFacade studentFacade;
    @Resource
    private HospitalStudentService hospitalStudentService;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private DistrictService districtService;

    /**
     * 孩子统计、孩子列表
     *
     * @param currentUser 当前用户
     * @return CountParentStudentResponseDTO 家长端-统计家长绑定学生
     */
    public CountParentStudentResponseDTO countParentStudent(CurrentUser currentUser) {
        CountParentStudentResponseDTO responseDTO = new CountParentStudentResponseDTO();
        Parent parent = parentService.getParentByUserId(currentUser.getId());
        List<Integer> studentIds = parentStudentService.getStudentIdByParentId(parent.getId());
        if (studentIds.isEmpty()) {
            responseDTO.setTotal(0);
            responseDTO.setItem(new ArrayList<>());
            return responseDTO;
        }
        List<ParentStudentDTO> parentStudentDTOS = studentService.countParentStudent(studentIds);
        parentStudentDTOS.forEach(student -> {
            student.setAvatarUrl(Objects.isNull(student.getAvatarFileId()) ? StringUtils.EMPTY : resourceFileService.getResourcePath(student.getAvatarFileId()));
        });
        responseDTO.setTotal(parentStudentDTOS.size());
        responseDTO.setItem(parentStudentDTOS);
        return responseDTO;
    }

    /**
     * 检查身份证
     *
     * @param request 请求入参
     * @return Student 学生
     */
    public StudentDTO checkIdCard(CheckIdCardRequestDTO request) {
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
                log.error("身份证号与学生姓名不一致，身份证:{},身份证名称:{},系统学生姓名:{}", idCard, request.getName(), student.getName());
                throw new BusinessException("身份证号与学生姓名不一致");
            }
        }
        BeanUtils.copyProperties(student, studentDTO);
        if (Objects.nonNull(student.getSchoolId())) {
            // 学校编号不为空，则拼接学校信息
            School school = schoolService.getById(student.getSchoolId());
            studentDTO.setSchoolId(school.getId());
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

        if (StringUtils.isEmpty(idCard) || idCard.length() < 18) {
            log.error("身份证:[{}]异常", idCard);
            throw new BusinessException("身份证异常");
        }
        Integer gender = Integer.parseInt(idCard.substring(16, 17)) % 2 != 0 ? GenderEnum.MALE.type : GenderEnum.FEMALE.type;
        String birthdayStr = idCard.substring(6, 10) + "-" + idCard.substring(10, 12) + "-" + idCard.substring(12, 14);
        try {
            return new TwoTuple<>(DateFormatUtil.parseDate(birthdayStr, DateFormatUtil.FORMAT_ONLY_DATE), gender);
        } catch (ParseException e) {
            throw new BusinessException("通过身份证获取个人信息异常");
        }
    }

    /**
     * 更新孩子
     *
     * @param currentUser 当前用户
     * @param student     学生
     * @return StudentDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudent(CurrentUser currentUser, Student student) {
        // 查找家长ID
        Parent parent = parentService.getParentByUserId(currentUser.getId());
        if (null == parent) {
            throw new BusinessException("家长信息异常");
        }
        setStudentAddress(student);
        StudentDTO studentDTO = studentService.updateStudent(student);
        // 绑定孩子
        Integer studentId = student.getId();
        bindStudent(parent, studentId);
        updateOtherInfo(studentId, parent.getPhone());
        return studentDTO;
    }

    /**
     * 新增孩子
     *
     * @param student     学生
     * @param currentUser 当前登录用户
     * @return 学生ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(Student student, CurrentUser currentUser) {
        // 查找家长ID
        Parent parent = parentService.getParentByUserId(currentUser.getId());
        if (null == parent) {
            throw new BusinessException("家长信息异常");
        }
        // 保存孩子
        Integer studentId = studentService.saveStudent(student);
        // 绑定孩子
        bindStudent(parent, studentId);
        updateOtherInfo(studentId, parent.getPhone());
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
        // 更新医院学生家长手机号码
        hospitalStudentService.updateMpParentPhone(studentId, parent.getPhone());
        // 绑定孩子
        parentStudentService.parentBindStudent(studentId, parent.getId());
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
    public ReportCountResponseDO studentReportCount(Integer studentId) {
        ReportCountResponseDO response = new ReportCountResponseDO();

        Student student = studentService.getById(studentId);
        if (null == student) {
            throw new BusinessException("学生数据异常！");
        }
        response.setName(student.getName());

        // 学生筛查报告
        List<CountReportItemsDO> screeningLists = getStudentCountReportItems(studentId);
        ScreeningDetailDO screeningDetailDO = new ScreeningDetailDO();
        screeningDetailDO.setTotal(visionScreeningResultService.getByStudentId(studentId).stream().filter(r -> r.getIsDoubleScreen().equals(Boolean.FALSE)).count());
        screeningDetailDO.setItems(screeningLists);
        response.setScreeningDetailDO(screeningDetailDO);

        // 学生就诊档案统计
        VisitsDetailDO visitsDetailDO = new VisitsDetailDO();
        // 获取就诊记录
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentId(studentId);
        visitsDetailDO.setTotal(visitLists.size());
        visitsDetailDO.setItems(visitLists);
        response.setVisitsDetailDO(visitsDetailDO);
        return response;
    }

    /**
     * 学生筛查报告列表
     *
     * @param studentId 学生ID
     * @return List<CountReportItemsDTO>
     */
    public List<CountReportItemsDO> getStudentCountReportItems(Integer studentId) {
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByStudentId(studentId);
        return screeningResults.stream().filter(result -> result.getIsDoubleScreen().equals(Boolean.FALSE)).map(result -> {
            CountReportItemsDO items = new CountReportItemsDO();
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
            Student student = studentService.getById(studentId);
            ScreeningReportResponseDTO responseDTO = new ScreeningReportResponseDTO();
            ScreeningReportDetailDO detail = new ScreeningReportDetailDO();
            // 视力检查结果
            detail.setVisionResultItems(Lists.newArrayList(new VisionItems("矫正视力"), new VisionItems("裸眼视力")));
            // 验光仪检查结果
            detail.setRefractoryResultItems(Lists.newArrayList(new RefractoryResultItems("球镜SC"), new RefractoryResultItems("柱镜DC"), new RefractoryResultItems("轴位A"), new RefractoryResultItems("等效球镜SE")));
            responseDTO.setDetail(detail);
            responseDTO.setIsNewbornWithoutIdCard(student.getIsNewbornWithoutIdCard());
            return responseDTO;
        }
        return packageScreeningReport(visionScreeningResultService.getById(result.getId()), true);
    }

    /**
     * 获取筛查结果详情
     *
     * @param reportId   报告ID
     * @param isShowBind 是否展示重新绑定身份证
     * @return ScreeningReportResponseDTO 学生就诊记录档案卡
     */
    public ScreeningReportResponseDTO getScreeningReportDetail(Integer reportId, boolean isShowBind) {
        VisionScreeningResult result = visionScreeningResultService.getById(reportId);
        if (null == result) {
            return new ScreeningReportResponseDTO();
        }
        return packageScreeningReport(result, isShowBind);
    }

    /**
     * 获取最新的就诊报告
     *
     * @param studentId 学生ID
     * @return StudentVisitReportResponseDTO
     */
    public StudentVisitReportResponseDTO latestVisitsReport(Integer studentId) {
        // 查找学生最近的就诊记录
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(visitLists)) {
            return new StudentVisitReportResponseDTO();
        }
        ReportAndRecordDO reportAndRecordVo = visitLists.get(0);
        return medicalReportBizService.getStudentVisitReport(reportAndRecordVo.getReportId());
    }

    /**
     * 获取就诊报告详情
     *
     * @param request 请求入参
     * @return StudentVisitReportResponseDTO 学生就诊记录档案卡
     */
    public StudentVisitReportResponseDTO getVisitsReportDetails(VisitsReportDetailRequest request) {
        return medicalReportBizService.getStudentVisitReport(request.getReportId());
    }

    /**
     * 视力趋势
     *
     * @param studentId 学生ID
     * @return ScreeningVisionTrendsResponseDTO 视力趋势
     */
    public ScreeningVisionTrendsResponseDTO screeningVisionTrends(Integer studentId) {
        ScreeningVisionTrendsResponseDTO responseDTO = new ScreeningVisionTrendsResponseDTO();
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByStudentId(studentId).stream().filter(result -> result.getIsDoubleScreen().equals(Boolean.FALSE)).collect(Collectors.toList());
        // 矫正视力详情
        responseDTO.setCorrectedVisionDetails(ScreeningResultUtil.packageVisionTrendsByCorrected(resultList));
        // 柱镜详情
        responseDTO.setCylDetails(ScreeningResultUtil.packageVisionTrendsByCyl(resultList));
        // 球镜详情
        responseDTO.setSphDetails(ScreeningResultUtil.packageVisionTrendsBySph(resultList));
        // 裸眼视力详情
        responseDTO.setNakedVisionDetails(ScreeningResultUtil.packageVisionTrendsByNakedVision(resultList));
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
        String qrCodeContent = String.format(QrCodeCacheKey.PARENT_STUDENT_PREFIX, studentId);
        String key = String.format(QrCodeCacheKey.PARENT_STUDENT_QR_CODE, qrCodeContent);
        if (!redisUtil.set(key, studentId, RedisConstant.TOKEN_EXPIRE_TIME)) {
            throw new BusinessException("获取学生授权二维码失败");
        }
        return qrCodeContent;
    }

    /**
     * 封装筛查结果
     *
     * @param result     筛查结果
     * @param isShowBind 是否展示重新绑定身份证
     * @return ScreeningReportResponseDTO 筛查报告返回体
     */
    private ScreeningReportResponseDTO packageScreeningReport(VisionScreeningResult result, boolean isShowBind) {
        ScreeningReportResponseDTO response = new ScreeningReportResponseDTO();
        Integer screeningOrgId = result.getScreeningOrgId();
        // 查询学生
        Integer studentId = result.getStudentId();
        StudentDTO student = getStudentDTO(studentId);
        if (isShowBind) {
            response.setIsNewbornWithoutIdCard(student.getIsNewbornWithoutIdCard());
        } else {
            response.setIsNewbornWithoutIdCard(false);
        }
        response.setStudentInfo(student);
        int age = DateUtil.ageOfNow(student.getBirthday());

        ScreeningReportDetailDO responseDTO = new ScreeningReportDetailDO();

        responseDTO.setScreeningDate(result.getUpdateTime());
        responseDTO.setScreeningOrgId(screeningOrgId);

        VisionDataDO visionData = result.getVisionData();

        // 视力检查结果
        responseDTO.setVisionResultItems(ScreeningResultUtil.packageVisionResult(visionData, age));

        // 验光仪检查结果
        TwoTuple<List<RefractoryResultItems>, Integer> refractoryResult = ScreeningResultUtil.packageRefractoryResult(result.getComputerOptometry(), age);
        responseDTO.setRefractoryResultItems(refractoryResult.getFirst());

        // 生物测量
        responseDTO.setBiometricItems(ScreeningResultUtil.packageBiometricResult(result.getBiometricData(), result.getOtherEyeDiseases()));

        // 医生建议一
        responseDTO.setDoctorAdvice1(refractoryResult.getSecond());

        // 医生建议二
        responseDTO.setDoctorAdvice2(ScreeningResultUtil.getDoctorAdviceDetail(result, student.getGradeType(), age));
        if (null != visionData) {
            // 戴镜类型
            responseDTO.setGlassesType(visionData.getLeftEyeData().getGlassesType());
        }
        responseDTO.setSuggestHospital(orgCooperationHospitalBizService.packageSuggestHospital(screeningOrgId));
        response.setDetail(responseDTO);
        return response;
    }

    /**
     * 获取推荐医院列表
     *
     * @param screeningOrgId 筛查机构Id
     * @return 推荐医院列表
     */
    public List<SuggestHospitalDTO> getCooperationHospital(Integer screeningOrgId) {
        List<SuggestHospitalDTO> responseDTO = new ArrayList<>();
        List<OrgCooperationHospital> cooperationHospitalList = orgCooperationHospitalService.getCooperationHospitalList(screeningOrgId);
        if (cooperationHospitalList.isEmpty()) {
            return responseDTO;
        }
        cooperationHospitalList.forEach(c -> {
            SuggestHospitalDTO suggestHospitalDTO = new SuggestHospitalDTO();
            Hospital hospital = hospitalService.getById(c.getHospitalId());
            orgCooperationHospitalBizService.packageHospitalInfo(suggestHospitalDTO, hospital);
            responseDTO.add(suggestHospitalDTO);
        });
        return responseDTO;
    }

    /**
     * 更新其他信息
     *
     * @param studentId 学生Id
     * @param phone     手机号码
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOtherInfo(Integer studentId, String phone) {
        // 更新筛查统计信息
        studentFacade.updateStatConclusion(studentId);
        // 更新学校端学生绑定手机号码
        studentFacade.updateMpParentPhone(studentId, phone);
    }

    /**
     * 保存学生
     *
     * @param student     学生
     * @param currentUser 当前用户
     * @return Integer
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveRecordStudent(Student student, CurrentUser currentUser) {
        String recordNo = studentService.getRecordNo(student.getCommitteeCode());
        setStudentAddress(student);
        student.setRecordNo(recordNo);
        return saveStudent(student, currentUser);
    }

    /**
     * 设置学生地址
     *
     * @param student 学生
     */
    private void setStudentAddress(Student student) {
        if (Objects.isNull(student.getProvinceCode())) {
            List<District> districtDetail = districtService.getDistrictPositionDetail(student.getCommitteeCode());
            if (CollectionUtils.isEmpty(districtDetail)) {
                return;
            }
            student.setProvinceCode(districtDetail.get(0).getCode());
            student.setCityCode(districtDetail.get(1).getCode());
            student.setAreaCode(districtDetail.get(2).getCode());
            // 处理直辖市问题
            if (districtDetail.size() > 4) {
                student.setTownCode(districtDetail.get(3).getCode());
            }
        }
    }

    /**
     * 根据身份证获取学生
     *
     * @param idCard 身份证
     * @param userId 家长Id
     * @return StudentDTO
     */
    public StudentDTO getByIdCard(String idCard, Integer userId) {
        Student student = studentService.getByIdCard(idCard);
        if (Objects.isNull(student)) {
            Parent parent = parentService.getParentByUserId(userId);
            StudentDTO studentDTO = new StudentDTO();
            studentDTO.setParentPhone(parent.getPhone());
            return studentDTO;
        }
        StudentDTO studentDTO = new StudentDTO();
        BeanUtils.copyProperties(student, studentDTO);
        if (Objects.nonNull(student.getSchoolId())) {
            studentDTO.setSchoolName(schoolService.getById(student.getSchoolId()).getName());
        }
        if (Objects.nonNull(student.getCommitteeCode())) {
            studentDTO.setCommitteeLists(districtService.getDistrictPositionDetail(student.getCommitteeCode()));
        }
        if (StringUtils.isNotBlank(student.getParentPhone())) {
            return studentDTO;
        }
        Parent parent = parentService.getParentByUserId(userId);
        student.setParentPhone(parent.getPhone());
        return studentDTO;
    }

    /**
     * 通过条件获取筛查报告
     *
     * @param condition 条件
     * @param name      学生名称
     * @return 筛查条件
     */
    public ScreeningReportInfoResponseDTO getScreeningReportByCondition(String condition, String name) {
        ScreeningReportInfoResponseDTO responseDTO = new ScreeningReportInfoResponseDTO();

        // 查询学生
        Student student = studentService.getByCondition(condition, name);
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByCondition(condition, name);
        if (Objects.isNull(student) && CollectionUtils.isEmpty(planStudents)) {
            throw new BusinessException("该学生筛查编号/身份证/学籍号/姓名错误");
        }
        responseDTO.setStudentId(Objects.nonNull(student) ? student.getId() : planStudents.get(0).getStudentId());

        // 查询报告
        if (!CollectionUtils.isEmpty(planStudents)) {
            VisionScreeningResult result = visionScreeningResultService.getLatestByPlanStudentIds(planStudents.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList()));
            if (Objects.nonNull(result)) {
                responseDTO.setReportId(result.getId());
            }
        }
        return responseDTO;
    }

    /**
     * 更新学生身份证
     *
     * @param requestDTO 请求入参
     * @param studentId  学生Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentIdCard(BindStudentRequestDTO requestDTO, Integer studentId) {
        String idCard = requestDTO.getIdCard();
        String name = requestDTO.getName();
        if (!IdcardUtil.isValidCard(idCard)) {
            throw new BusinessException("身份证异常");
        }
        // 更新学生
        Student student = studentService.getById(studentId);
        // 检查学生身份证是否重复
        if (studentService.checkIdCardAndId(idCard, studentId)) {
            log.error("家长端更新学生身份证异常，学生Id:{}，身份证:{}", studentId, idCard);
            throw new BusinessException("学生身份证重复，请联系管理员");
        }
        studentService.updateById(student.setIdCard(idCard).setName(name).setIsNewbornWithoutIdCard(false));
        // 更新患者
        List<HospitalStudent> hospitalStudents = hospitalStudentService.getByStudentId(studentId);
        if (CollectionUtils.isEmpty(hospitalStudents)) {
            return;
        }
        hospitalStudents.forEach(hospitalStudent -> hospitalStudent.setIdCard(idCard).setName(name).setIsNewbornWithoutIdCard(false));
        hospitalStudentService.updateBatchById(hospitalStudents);
    }

    /**
     * 获取学生信息
     *
     * @param studentId 学生Id
     * @return StudentDTO
     */
    public StudentDTO getStudentInfo(Integer studentId) {
        return getStudentDTO(studentId);
    }

    /**
     * 获取学生信息
     *
     * @param studentId 学生Id
     * @return StudentDTO
     */
    private StudentDTO getStudentDTO(Integer studentId) {
        StudentDTO student = studentService.getStudentById(studentId);
        student.setScreeningCodes(screeningPlanSchoolStudentService.getByStudentId(studentId)
                .stream().map(ScreeningPlanSchoolStudent::getScreeningCode)
                .collect(Collectors.toList()));
        return student;
    }

}
