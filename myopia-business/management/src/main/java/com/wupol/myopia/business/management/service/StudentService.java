package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.myopia.common.constant.WearingGlassesSituation;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CacheKey;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.dto.*;
import com.wupol.myopia.business.management.domain.mapper.StudentMapper;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.StudentCountVO;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;
import com.wupol.myopia.business.management.util.TwoTuple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author HaoHao
 * Date 2020-12-22
 */
@Service
@Log4j2
public class StudentService extends BaseService<StudentMapper, Student> {

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private VisionScreeningResultService visionScreeningResultService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private DistrictService districtService;

    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 根据学生id列表获取学生信息
     *
     * @param ids id列表
     * @return List<Student>
     */
    public List<Student> getByIds(List<Integer> ids) {
        return baseMapper.selectBatchIds(ids);
    }

    /**
     * 通过年级id查找学生
     *
     * @param gradeId 年级Id
     * @return 学生列表
     */
    public List<Student> getStudentsByGradeId(Integer gradeId) {
        return baseMapper.getByGradeIdAndStatus(gradeId, CommonConst.STATUS_IS_DELETED);
    }

    /**
     * 通过班级id查找学生
     *
     * @param classId 班级Id
     * @return 学生列表
     */
    public List<Student> getStudentsByClassId(Integer classId) {
        return baseMapper.getByClassIdAndStatus(classId, CommonConst.STATUS_IS_DELETED);
    }

    /**
     * 新增学生
     *
     * @param student 学生实体类
     * @return 新增数量
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(Student student) {

        Integer createUserId = student.getCreateUserId();
        String idCard = student.getIdCard();

        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }
        // 检查学生身份证是否重复
        if (checkIdCard(student.getIdCard(), null)) {
            throw new BusinessException("学生身份证重复");
        }

        RLock rLock = redissonClient.getLock(String.format(CacheKey.LOCK_STUDENT_REDIS, idCard));
        try {
            boolean tryLock = rLock.tryLock(2, 4, TimeUnit.SECONDS);
            if (tryLock) {
                return baseMapper.insert(student);
            }
        } catch (InterruptedException e) {
            log.error("用户:{}创建学生获取锁异常,e:{}", createUserId, e);
            throw new BusinessException("系统繁忙，请稍后再试");
        } finally {
            if (rLock.isLocked()) {
                rLock.unlock();
            }
        }
        log.warn("用户id:{}新增学生获取不到锁，新增学生身份证:{}", createUserId, idCard);
        throw new BusinessException("请重试");
    }

    /**
     * 更新学生
     *
     * @param student 学生实体类
     * @return 学生实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudent(Student student) {

        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }

        // 检查学生身份证是否重复
        if (checkIdCard(student.getIdCard(), student.getId())) {
            throw new BusinessException("学生身份证重复");
        }

        // 更新学生
        baseMapper.updateById(student);
        Student resultStudent = baseMapper.selectById(student.getId());
        StudentDTO studentDTO = new StudentDTO();
        BeanUtils.copyProperties(resultStudent, studentDTO);
        if (StringUtils.isNotBlank(studentDTO.getSchoolNo())) {
            School school = schoolService.getBySchoolNo(studentDTO.getSchoolNo());
            studentDTO.setSchoolName(school.getName());
            studentDTO.setSchoolId(school.getId());

            // 查询年级和班级
            SchoolGrade schoolGrade = schoolGradeService.getById(resultStudent.getGradeId());
            SchoolClass schoolClass = schoolClassService.getById(resultStudent.getClassId());
            studentDTO.setGradeName(schoolGrade.getName()).setClassName(schoolClass.getName());
        }
        studentDTO.setScreeningCount(student.getScreeningCount())
                .setQuestionnaireCount(student.getQuestionnaireCount())
                // TODO: 就诊次数
                .setNumOfVisits(0);
        return studentDTO;
    }

    /**
     * 删除学生
     *
     * @param id 学生id
     * @return 删除个数
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer deletedStudent(Integer id) {
        if (checkStudentHavePlan(id)) {
            throw new BusinessException("该学生有对应的筛查计划，无法进行删除");
        }
        Student student = new Student();
        student.setId(id);
        student.setStatus(CommonConst.STATUS_IS_DELETED);
        return baseMapper.updateById(student);
    }

    /**
     * 获取学生列表
     *
     * @param pageRequest  分页
     * @param studentQuery 请求体
     * @return IPage<Student> {@link IPage}
     */
    public IPage<StudentDTO> getStudentLists(PageRequest pageRequest, StudentQuery studentQuery) {

        TwoTuple<List<Integer>, List<Integer>> conditionalFilter = conditionalFilter(
                studentQuery.getGradeIds(), studentQuery.getVisionLabels());

        IPage<StudentDTO> pageStudents = baseMapper.getStudentListByCondition(pageRequest.toPage(),
                studentQuery.getSno(), studentQuery.getIdCard(), studentQuery.getName(),
                studentQuery.getParentPhone(), studentQuery.getGender(), conditionalFilter.getFirst(),
                conditionalFilter.getSecond(), studentQuery.getStartScreeningTime(), studentQuery.getEndScreeningTime(),
                studentQuery.getSchoolName());
        List<StudentDTO> students = pageStudents.getRecords();

        // 为空直接放回
        if (CollectionUtils.isEmpty(students)) {
            return pageStudents;
        }

        // 筛查次数
        List<StudentScreeningCountVO> studentScreeningCountVOS = visionScreeningResultService.countScreeningTime();
        Map<Integer, Integer> countMaps = studentScreeningCountVOS.stream().collect(Collectors
                .toMap(StudentScreeningCountVO::getStudentId,
                        StudentScreeningCountVO::getCount));

        // 封装DTO
        for (StudentDTO s : students) {
            // 筛查次数
            s.setScreeningCount(countMaps.getOrDefault(s.getId(), 0));
            // TODO: 就诊次数
            s.setNumOfVisits(0);
            // TODO: 设置问卷数
            s.setQuestionnaireCount(0);
        }
        return pageStudents;
    }

    /**
     * 通过条件查询
     * @param query StudentQuery
     * @return List<Student>
     */
    public List<Student> getBy(StudentQuery query) {
        return baseMapper.getBy(query);
    }

    /**
     * 条件过滤
     *
     * @param gradeIdsStr     年级ID字符串
     * @param visionLabelsStr 视力标签字符串
     * @return {@link TwoTuple} <p>TwoTuple.getFirst-年级list, TwoTuple.getSecond-视力标签list</p>
     */
    public TwoTuple<List<Integer>, List<Integer>> conditionalFilter(String gradeIdsStr,
                                                                    String visionLabelsStr) {
        TwoTuple<List<Integer>, List<Integer>> result = new TwoTuple<>();

        // 年级条件
        if (StringUtils.isNotBlank(gradeIdsStr)) {
            result.setFirst(Arrays.stream(gradeIdsStr.split(","))
                    .map(Integer::valueOf).collect(Collectors.toList()));
        }

        // 视力标签条件
        if (StringUtils.isNotBlank(visionLabelsStr)) {
            result.setSecond(Arrays.stream(visionLabelsStr.split(","))
                    .map(Integer::valueOf).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 获取学生筛查档案
     *
     * @param studentId 学生ID
     * @return StudentScreeningResultResponse
     */
    public StudentScreeningResultResponseDTO getScreeningList(Integer studentId) {
        StudentScreeningResultResponseDTO responseDTO = new StudentScreeningResultResponseDTO();
        List<StudentScreeningResultItems> items = new ArrayList<>();

        // 通过学生id查询结果
        List<VisionScreeningResult> resultList = visionScreeningResultService.getByStudentId(studentId);

        for (VisionScreeningResult r : resultList) {
            StudentScreeningResultItems item = new StudentScreeningResultItems();
            List<StudentResultDetails> result = packageDTO(r);
            item.setDetails(result);
            item.setScreeningDate(r.getCreateTime());
            // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样的
            item.setGlassesType(WearingGlassesSituation.getType(r.getVisionData().getLeftEyeData().getGlassesType()));
            item.setResultId(r.getId());
            items.add(item);
        }
        responseDTO.setTotal(resultList.size());
        responseDTO.setItems(items);
        return responseDTO;
    }

    /**
     * 分页查询
     *
     * @param page  分页
     * @param query 条件
     * @return {@link IPage} 分页结果
     */
    public IPage<Student> getByPage(Page<?> page, StudentQuery query) {
        return baseMapper.getByPage(page, query);
    }

    /**
     * 通过id获取学生信息
     *
     * @param id 学生ID
     * @return StudentDTO
     */
    public StudentDTO getStudentById(Integer id) {
        StudentDTO student = baseMapper.getStudentById(id);

        if (StringUtils.isNotBlank(student.getSchoolNo())) {
            // 学校编号不为空，则拼接学校信息
            School school = schoolService.getBySchoolNo(student.getSchoolNo());
            student.setSchoolId(school.getId());
            student.setSchoolNo(school.getSchoolNo());
            student.setSchoolName(school.getName());
            if (null != student.getClassId() && null != student.getGradeId()) {
                SchoolGrade schoolGrade = schoolGradeService.getById(student.getGradeId());
                SchoolClass schoolClass = schoolClassService.getById(student.getClassId());
                student.setClassName(schoolClass.getName());
                student.setGradeName(schoolGrade.getName());
            }
        }
        return student;
    }


    /**
     * 通过学校ID、班级ID、年级ID查找学生
     *
     * @param schoolId 学校Id
     * @return 学生列表
     */
    public List<Student> getBySchoolIdAndGradeIdAndClassId(Integer schoolId, Integer classId, Integer gradeId) {
        return baseMapper.getByOtherId(schoolId, classId, gradeId);
    }

    /**
     * 统计学生人数
     *
     * @return List<StudentCountVO>
     */
    public List<StudentCountVO> countStudentBySchoolNo() {
        return baseMapper.countStudentBySchoolNo();
    }


    /**
     * 检查学生身份证号码是否重复
     *
     * @param IdCard 身份证号码
     * @param id     学生ID
     * @return 是否重复
     */
    public Boolean checkIdCard(String IdCard, Integer id) {
        return baseMapper.getByIdCardNeIdAndStatus(IdCard, id, CommonConst.STATUS_IS_DELETED).size() > 0;
    }

    /**
     * 根据身份证列表获取学生
     *
     * @param idCardList 身份证list
     * @return List<Student>
     */
    public List<Student> getByIdCards(List<String> idCardList) {
        StudentQuery studentQuery = new StudentQuery();
        return Lists.partition(idCardList, 50).stream().map(list -> {
            studentQuery.setIdCardList(list);
            return baseMapper.getBy(studentQuery);
        }).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 批量检查学生身份证号码是否重复
     *
     * @param IdCards 身份证号码
     * @return 是否重复
     */
    public Boolean checkIdCards(List<String> IdCards) {
        return baseMapper.getByIdCardsAndStatus(IdCards, CommonConst.STATUS_IS_DELETED).size() > 0;
    }

    /**
     * 封装结果
     *
     * @param result 结果表
     * @return List<StudentResultDetails>
     */
    private List<StudentResultDetails> packageDTO(VisionScreeningResult result) {

        // 设置左眼
        StudentResultDetails leftDetails = new StudentResultDetails();
        leftDetails.setGlassesType(WearingGlassesSituation.getType(result.getVisionData().getLeftEyeData().getGlassesType()));
        leftDetails.setCorrectedVision(result.getVisionData().getLeftEyeData().getCorrectedVision());
        leftDetails.setNakedVision(result.getVisionData().getLeftEyeData().getNakedVision());
        leftDetails.setAxial(result.getComputerOptometry().getLeftEyeData().getAxial());
        leftDetails.setSph(result.getComputerOptometry().getLeftEyeData().getSph());
        leftDetails.setCyl(result.getComputerOptometry().getLeftEyeData().getCyl());
        leftDetails.setAD(result.getBiometricData().getLeftEyeData().getAd());
        leftDetails.setAL(result.getBiometricData().getLeftEyeData().getAl());
        leftDetails.setCCT(result.getBiometricData().getLeftEyeData().getCct());
        leftDetails.setLT(result.getBiometricData().getLeftEyeData().getLt());
        leftDetails.setWTW(result.getBiometricData().getLeftEyeData().getWtw());
        leftDetails.setEyeDiseases(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases());
        leftDetails.setLateriality(CommonConst.LEFT_EYE);

        //设置右眼
        StudentResultDetails rightDetails = new StudentResultDetails();
        rightDetails.setGlassesType(WearingGlassesSituation.getType(result.getVisionData().getRightEyeData().getGlassesType()));
        rightDetails.setCorrectedVision(result.getVisionData().getRightEyeData().getCorrectedVision());
        rightDetails.setNakedVision(result.getVisionData().getRightEyeData().getNakedVision());
        rightDetails.setAxial(result.getComputerOptometry().getRightEyeData().getAxial());
        rightDetails.setSph(result.getComputerOptometry().getRightEyeData().getSph());
        rightDetails.setCyl(result.getComputerOptometry().getRightEyeData().getCyl());
        rightDetails.setAD(result.getBiometricData().getRightEyeData().getAd());
        rightDetails.setAL(result.getBiometricData().getRightEyeData().getAl());
        rightDetails.setCCT(result.getBiometricData().getRightEyeData().getCct());
        rightDetails.setLT(result.getBiometricData().getRightEyeData().getLt());
        rightDetails.setWTW(result.getBiometricData().getRightEyeData().getWtw());
        rightDetails.setEyeDiseases(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases());
        rightDetails.setLateriality(CommonConst.RIGHT_EYE);
        return Lists.newArrayList(rightDetails, leftDetails);
    }

    /**
     * 获取学生档案卡
     *
     * @param resultId 筛查结果
     * @return StudentCardResponseDTO
     */
    public StudentCardResponseDTO getCardDetails(Integer resultId) {
        VisionScreeningResult visionScreeningResult = visionScreeningResultService.getById(resultId);
        return getStudentCardResponseDTO(visionScreeningResult);
    }

    /**
     * 根据筛查接口获取档案卡所需要的数据
     *
     * @param visionScreeningResult 筛查结果
     * @return StudentCardResponseDTO
     */
/*    public List<VisionScreeningResult> getScreeningList(Integer studentId) {
        // 通过计划Ids查询学生的结果
        return visionScreeningResultService.getByStudentIds(studentId);
    }*/

    public StudentCardResponseDTO getStudentCardResponseDTO(VisionScreeningResult visionScreeningResult) {
        StudentCardResponseDTO responseDTO = new StudentCardResponseDTO();
        Integer studentId = visionScreeningResult.getStudentId();
        Student student = baseMapper.selectById(studentId);
        // 获取学生基本信息
        CardInfo cardInfo = getCardInfo(student);
        cardInfo.setScreeningDate(visionScreeningResult.getCreateTime());
        responseDTO.setInfo(cardInfo);

        // 获取结果记录
        CardDetails cardDetails = getCardDetails(visionScreeningResult);
        responseDTO.setDetails(cardDetails);
        return responseDTO;
    }

    /**
     * 设置学生基本信息
     *
     * @param student 学生
     * @return CardInfo
     */
    private CardInfo getCardInfo(Student student) {
        CardInfo cardInfo = new CardInfo();

        cardInfo.setName(student.getName());
        cardInfo.setBirthday(student.getBirthday());
        cardInfo.setIdCard(student.getIdCard());
        cardInfo.setGender(student.getGender());

        String schoolNo = student.getSchoolNo();
        if (StringUtils.isNotBlank(schoolNo)) {
            School school = schoolService.getBySchoolNo(schoolNo);
            SchoolClass schoolClass = schoolClassService.getById(student.getClassId());
            SchoolGrade schoolGrade = schoolGradeService.getById(student.getGradeId());
            cardInfo.setSchoolName(school.getName());
            cardInfo.setClassName(schoolClass.getName());
            cardInfo.setGradeName(schoolGrade.getName());
            cardInfo.setProvinceName(districtService.getDistrictName(school.getProvinceCode()));
            cardInfo.setCityName(districtService.getDistrictName(school.getCityCode()));
            cardInfo.setAreaName(districtService.getDistrictName(school.getAreaCode()));
            cardInfo.setTownName(districtService.getDistrictName(school.getTownCode()));
        }
        return cardInfo;
    }

    /**
     * 设置视力信息
     *
     * @param result 筛查结果
     * @return CardDetails
     */
    private CardDetails getCardDetails(VisionScreeningResult result) {
        CardDetails details = new CardDetails();
        // 佩戴眼镜的类型随便取一个都行，两只眼睛的数据是一样
        CardDetails.GlassesTypeObj glassesTypeObj = new CardDetails.GlassesTypeObj();
        //todo result.getVisionData().getLeftEyeData().getGlassesType()
        glassesTypeObj.setType("1");
        glassesTypeObj.setLeftVision(new BigDecimal("4.5"));
        glassesTypeObj.setRightVision(new BigDecimal("4.7"));
        details.setGlassesTypeObj(glassesTypeObj);
        details.setVisionResults(setVisionResult(result.getVisionData()));
        details.setRefractoryResults(setRefractoryResults(result.getComputerOptometry()));
        details.setCrossMirrorResults(setCrossMirrorResults(result));
        details.setEyeDiseasesResult(setEyeDiseasesResult(result.getOtherEyeDiseases()));
        return details;
    }

    /**
     * 设置视力检查结果
     *
     * @param result 筛查结果
     * @return List<VisionResult>
     */
    private List<VisionResult> setVisionResult(VisionDataDO result) {
        VisionResult left = new VisionResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        left.setCorrectedVision(result.getLeftEyeData().getCorrectedVision());
        left.setNakedVision(result.getLeftEyeData().getNakedVision());

        VisionResult right = new VisionResult();
        right.setLateriality(CommonConst.RIGHT_EYE);
        right.setCorrectedVision(result.getRightEyeData().getCorrectedVision());
        right.setNakedVision(result.getRightEyeData().getNakedVision());
        return Lists.newArrayList(right, left);
    }

    /**
     * 设置验光仪检查结果
     *
     * @param result 筛查结果
     * @return List<RefractoryResult>
     */
    private List<RefractoryResult> setRefractoryResults(ComputerOptometryDO result) {
        RefractoryResult left = new RefractoryResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        left.setAxial(result.getLeftEyeData().getAxial());
        left.setSph(result.getLeftEyeData().getSph());
        left.setCyl(result.getLeftEyeData().getCyl());

        RefractoryResult right = new RefractoryResult();
        right.setLateriality(CommonConst.RIGHT_EYE);
        right.setAxial(result.getRightEyeData().getAxial());
        right.setSph(result.getRightEyeData().getSph());
        right.setCyl(result.getRightEyeData().getCyl());
        return Lists.newArrayList(right, left);
    }

    /**
     * 设置串镜检查结果
     *
     * @return List<CrossMirrorResult>
     */
    private List<CrossMirrorResult> setCrossMirrorResults(VisionScreeningResult result) {
        CrossMirrorResult left = new CrossMirrorResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        left.setMyopia(true);
        left.setFarsightedness(true);
        if (CollectionUtils.isEmpty(result.getOtherEyeDiseases().getLeftEyeData().getEyeDiseases())) {
            left.setOther(true);
        }

        CrossMirrorResult right = new CrossMirrorResult();
        right.setLateriality(CommonConst.RIGHT_EYE);
        right.setMyopia(true);
        right.setFarsightedness(true);
        if (CollectionUtils.isEmpty(result.getOtherEyeDiseases().getRightEyeData().getEyeDiseases())) {
            right.setOther(true);
        }
        return Lists.newArrayList(right, left);
    }

    /**
     * 其他眼部疾病
     *
     * @param result 其他眼部疾病
     * @return List<EyeDiseasesResult>
     */
    private List<EyeDiseasesResult> setEyeDiseasesResult(OtherEyeDiseasesDO result) {
        EyeDiseasesResult left = new EyeDiseasesResult();
        left.setLateriality(CommonConst.LEFT_EYE);
        left.setEyeDiseases(result.getLeftEyeData().getEyeDiseases());

        EyeDiseasesResult right = new EyeDiseasesResult();
        right.setLateriality(CommonConst.RIGHT_EYE);
        right.setEyeDiseases(result.getRightEyeData().getEyeDiseases());
        return Lists.newArrayList(right, left);
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
     * 通过身份证查找学生
     *
     * @param idCard 身份证
     * @return Student
     */
    public Student getByIdCard(String idCard) {
        return baseMapper.getByIdCard(idCard);
    }

    /**
     * 医院端获取学生详情
     *
     * @param studentId 学生ID
     * @param idCard    身份证
     * @param name      姓名
     * @return HospitalStudentDTO
     */
    public HospitalStudentDTO getHospitalStudentDetail(Integer studentId, String idCard, String name) {

        HospitalStudentDTO studentDTO = new HospitalStudentDTO();
        Student student;
        if (null != studentId) {
            student = baseMapper.selectById(studentId);
        } else {
            if (StringUtils.isBlank(idCard) || StringUtils.isBlank(name)) {
                throw new BusinessException("数据异常，请确认");
            }
            student = baseMapper.getByIdCardAndName(idCard, name);
        }
        if (null == student) {
            return studentDTO;
        }

        BeanUtils.copyProperties(student, studentDTO);

        // 地区Maps
        Map<Long, District> districtMaps = getDistrictMap(Lists.newArrayList(student));
        packageStudentDistrict(districtMaps, studentDTO, student);

        if (StringUtils.isNotBlank(student.getSchoolNo())) {
            studentDTO.setSchool(schoolService.getBySchoolNo(student.getSchoolNo()));
        }
        if (null != student.getGradeId()) {
            studentDTO.setSchoolGrade(schoolGradeService.getById(student.getGradeId()));
        }
        if (null != student.getClassId()) {
            studentDTO.setSchoolClass(schoolClassService.getById(student.getClassId()));
        }
        return studentDTO;
    }

    /**
     * 医院端学生信息
     *
     * @param studentIds 学生ids
     * @param name       学生姓名
     * @return List<HospitalStudentDTO>
     */
    public List<HospitalStudentDTO> getHospitalStudentLists(List<Integer> studentIds, String name) {
        List<HospitalStudentDTO> dtoList = new ArrayList<>();

        List<Student> students = baseMapper.getByIdsAndName(studentIds, name);
        if (CollectionUtils.isEmpty(students)) {
            return new ArrayList<>();
        }

        // 学校Maps
        List<School> schoolList = schoolService.getBySchoolNos(students
                .stream().distinct().map(Student::getSchoolNo).collect(Collectors.toList()));
        Map<String, School> schoolMaps = schoolList.stream()
                .collect(Collectors.toMap(School::getSchoolNo, Function.identity()));

        // 班级Maps
        Map<Integer, SchoolClass> classMaps = schoolClassService.getClassMapByIds(students
                .stream().map(Student::getClassId).collect(Collectors.toList()));

        // 年级Maps
        Map<Integer, SchoolGrade> gradeMaps = schoolGradeService.getGradeMapByIds(students
                .stream().map(Student::getGradeId).collect(Collectors.toList()));

        students.forEach(s -> {
            HospitalStudentDTO dto = new HospitalStudentDTO();
            BeanUtils.copyProperties(s, dto);

            if (StringUtils.isNotBlank(s.getSchoolNo())) {
                dto.setSchool(schoolMaps.get(s.getSchoolNo()));
            }
            if (null != s.getClassId()) {
                dto.setSchoolClass(classMaps.get(s.getClassId()));
            }
            if (null != s.getGradeId()) {
                dto.setSchoolGrade(gradeMaps.get(s.getGradeId()));
            }
            dtoList.add(dto);
        });
        return dtoList;
    }

    /**
     * 获取学生地区Maps
     *
     * @param students 学生列表
     * @return Map<Long, District>
     */
    private Map<Long, District> getDistrictMap(List<Student> students) {
        List<Long> districtCode = new ArrayList<>();
        students.forEach(d -> {
            if (null != d.getProvinceCode()) {
                districtCode.add(d.getProvinceCode());
            }
            if (null != d.getCityCode()) {
                districtCode.add(d.getCityCode());
            }
            if (null != d.getAreaCode()) {
                districtCode.add(d.getAreaCode());
            }
            if (null != d.getTownCode()) {
                districtCode.add(d.getTownCode());
            }
        });

        // 地区Maps
        return districtService.getByCodes(districtCode)
                .stream().distinct().collect(Collectors
                        .toMap(District::getCode, Function.identity()));
    }

    /**
     * 封装学生区域
     *
     * @param districtMaps 区域Maps
     * @param dto          dto
     * @param student      学生
     */
    private void packageStudentDistrict(Map<Long, District> districtMaps, HospitalStudentDTO dto, Student student) {
        if (null != student.getProvinceCode()) {
            dto.setProvince(districtMaps.get(student.getProvinceCode()));
        }
        if (null != student.getCityCode()) {
            dto.setCity(districtMaps.get(student.getCityCode()));
        }
        if (null != student.getAreaCode()) {
            dto.setArea(districtMaps.get(student.getAreaCode()));
        }
        if (null != student.getTownCode()) {
            dto.setTown(districtMaps.get(student.getTownCode()));
        }
    }
}