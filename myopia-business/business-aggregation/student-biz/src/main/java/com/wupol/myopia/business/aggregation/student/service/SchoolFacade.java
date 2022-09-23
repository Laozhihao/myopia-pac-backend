package com.wupol.myopia.business.aggregation.student.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.student.domain.vo.GradeInfoVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.*;
import com.wupol.myopia.business.core.screening.flow.domain.dto.CommonDiseasePlanStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 多端管理学校
 *
 * @author Simple4H
 */
@Service
public class SchoolFacade {

    @Resource
    private SchoolService schoolService;

    @Resource
    private DistrictService districtService;

    @Resource
    private StudentService studentService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Resource
    private ResourceFileService resourceFileService;
    @Resource
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Resource
    private StudentCommonDiseaseIdService studentCommonDiseaseIdService;
    @Resource
    private SchoolClassService schoolClassService;
    @Resource
    private SchoolGradeService schoolGradeService;

    /**
     * 获取学校详情
     *
     * @param id                 学校ID
     * @param isSchoolManagement 是否学校管理端
     * @return SchoolResponseDTO
     */
    public SchoolResponseDTO getBySchoolId(Integer id, boolean isSchoolManagement) {
        SchoolResponseDTO responseDTO = new SchoolResponseDTO();
        School school = schoolService.getBySchoolId(id);
        BeanUtils.copyProperties(school, responseDTO);
        // 填充地址
        responseDTO.setAddressDetail(districtService.getAddressDetails(school.getProvinceCode(), school.getCityCode(), school.getAreaCode(), school.getTownCode(), school.getAddress()));
        int studentCount;
        if (isSchoolManagement) {
            studentCount = schoolStudentService.count(new SchoolStudent().setSchoolId(school.getId()).setStatus(CommonConst.STATUS_NOT_DELETED));
        } else {
            studentCount = studentService.count(new Student().setSchoolId(school.getId()).setStatus(CommonConst.STATUS_NOT_DELETED));
        }
        // 统计学生数
        responseDTO.setStudentCount(studentCount);
        ResultNoticeConfig resultNoticeConfig = school.getResultNoticeConfig();
        if (Objects.nonNull(resultNoticeConfig) && Objects.nonNull(resultNoticeConfig.getQrCodeFileId())) {
            responseDTO.setNoticeResultFileUrl(resourceFileService.getResourcePath(resultNoticeConfig.getQrCodeFileId()));
        }
        return responseDTO;
    }

    /**
     * 更新学校
     *
     * @param schoolRequestDTO 学校实体类
     * @return 学校实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolResponseDTO updateSchool(SaveSchoolRequestDTO schoolRequestDTO) {
        Integer schoolId = schoolRequestDTO.getId();
        if (schoolService.checkSchoolName(schoolRequestDTO.getName(), schoolId)) {
            throw new BusinessException("学校名称重复，请确认");
        }
        School oldSchool = schoolService.getById(schoolId);
        District district = districtService.getById(schoolRequestDTO.getDistrictId());
        schoolRequestDTO.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        //更新学校
        schoolService.updateById(schoolRequestDTO);
        // 新增
        schoolService.generateGradeAndClass(schoolRequestDTO.getId(), schoolRequestDTO.getCreateUserId(), schoolRequestDTO.getBatchSaveGradeList());
        // 同步到oauth机构状态
        if (Objects.nonNull(schoolRequestDTO.getStatus())) {
            oauthServiceClient.updateOrganization(new Organization(schoolRequestDTO.getId(), SystemCode.SCHOOL_CLIENT,
                    UserType.OTHER, schoolRequestDTO.getStatus()));
        }
        // 更新筛查计划中的学校
        screeningPlanSchoolService.updateSchoolNameBySchoolId(schoolId, schoolRequestDTO.getName());
        // 更新关联的筛查学生的常见病ID
        School newSchool = schoolService.getById(schoolId);
        updateStudentCommonDiseaseId(oldSchool, newSchool);
        // 组装返回数据
        SchoolResponseDTO schoolResponseDTO = new SchoolResponseDTO();
        BeanUtils.copyProperties(newSchool, schoolResponseDTO);
        schoolResponseDTO.setDistrictName(districtService.getDistrictName(newSchool.getDistrictDetail()));
        schoolResponseDTO.setAddressDetail(districtService.getAddressDetails(newSchool.getProvinceCode(), newSchool.getCityCode(), newSchool.getAreaCode(), newSchool.getTownCode(), newSchool.getAddress()));
        // 判断是否能更新
        schoolResponseDTO.setCanUpdate(newSchool.getGovDeptId().equals(schoolRequestDTO.getGovDeptId()));
        schoolResponseDTO.setStudentCount(schoolRequestDTO.getStudentCount())
                .setScreeningCount(schoolRequestDTO.getScreeningCount())
                .setCreateUser(schoolRequestDTO.getCreateUser());
        return schoolResponseDTO;
    }

    /**
     * 更新学生常见病ID
     *
     * @param oldSchool  旧学校
     * @param newSchool  新学校
     **/
    private void updateStudentCommonDiseaseId(School oldSchool, School newSchool) {
        // 行政区域地址的区/县、片区、监测点若没有变动，则不需要更新
        District oldDistrict = districtService.getById(oldSchool.getDistrictId());
        District newDistrict = districtService.getById(newSchool.getDistrictId());
        if (Objects.equals(String.valueOf(oldDistrict.getCode()).substring(0, 6), String.valueOf(newDistrict.getCode()).substring(0, 6)) &&
                Objects.equals(oldSchool.getAreaType(), newSchool.getAreaType()) &&
                Objects.equals(oldSchool.getMonitorType(), newSchool.getMonitorType())) {
            return;
        }
        // 获取所有需要更新的计划学生
        List<CommonDiseasePlanStudent> commonDiseasePlanStudentList = screeningPlanSchoolStudentService.getCommonDiseaseScreeningPlanStudent(newSchool.getId());
        if (CollectionUtils.isEmpty(commonDiseasePlanStudentList)) {
            return;
        }
        Map<Integer, ScreeningPlanSchoolStudent> planStudentMap = screeningPlanSchoolStudentService.getByIds(commonDiseasePlanStudentList.stream()
                .map(CommonDiseasePlanStudent::getId).collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        List<ScreeningPlanSchoolStudent> planStudentList = commonDiseasePlanStudentList.stream()
                .map(x -> {
                    ScreeningPlanSchoolStudent planStudent = planStudentMap.get(x.getId());
                    return new ScreeningPlanSchoolStudent()
                            .setId(planStudent.getId())
                            .setPassport(planStudent.getPassport())
                            .setIdCard(planStudent.getIdCard())
                            .setProvinceCode(planStudent.getProvinceCode())
                            .setCityCode(planStudent.getCityCode())
                            .setAreaCode(planStudent.getAreaCode())
                            .setTownCode(planStudent.getTownCode())
                            .setAddress(planStudent.getAddress())
                            .setCommonDiseaseId(studentCommonDiseaseIdService.getStudentCommonDiseaseId(newSchool.getDistrictId(), newSchool.getId(), x.getGradeId(), x.getStudentId(), x.getPlanStartTime()));

                })
                .collect(Collectors.toList());
        // 批量更新
        screeningPlanSchoolStudentService.updateBatchById(planStudentList);
    }

    /**
     * 获取班级信息，并带有学校和年级名称
     * @param classIds 班级ID集合
     */
    public List<SchoolClassDTO> getClassWithSchoolAndGradeName(List<Integer> classIds){
        //班级
        List<SchoolClass> schoolClassList = schoolClassService.listByIds(classIds);

        //年级
        Set<Integer> gradeIds = schoolClassList.stream().map(SchoolClass::getGradeId).collect(Collectors.toSet());
        List<SchoolGrade> schoolGradeList = schoolGradeService.listByIds(gradeIds);
        Map<Integer, SchoolGrade> schoolGradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity(), (v1, v2) -> v2));

        //学校
        Set<Integer> schoolIds = schoolGradeList.stream().map(SchoolGrade::getSchoolId).collect(Collectors.toSet());
        List<School> schoolList = schoolService.listByIds(schoolIds);
        Map<Integer, School> schoolMap = schoolList.stream().collect(Collectors.toMap(School::getId, Function.identity(), (v1, v2) -> v2));

        return schoolClassList.stream().map(schoolClass -> buildSchoolClassDTO(schoolGradeMap, schoolMap, schoolClass)).collect(Collectors.toList());
    }

    /**
     * 根据学校ID查询学校年级信息
     * @param schoolId 学校ID
     */
    public List<GradeInfoVO.GradeInfo> getGradeInfoBySchoolId(Integer schoolId){

        //学生
        List<SchoolStudent> schoolStudentList = schoolStudentService.listBySchoolId(schoolId);
        Map<Integer, List<SchoolStudent>> gradeStudentMap = schoolStudentList.stream().collect(Collectors.groupingBy(SchoolStudent::getGradeId));

        //年级
        List<SchoolGrade> schoolGradeList = schoolGradeService.listBySchoolId(schoolId);

        List<GradeInfoVO.GradeInfo> gradeInfoVOList = Lists.newArrayList();

        //幼儿园
        List<SchoolGrade> kindergartenList = schoolGradeList.stream().filter(schoolGrade -> GradeCodeEnum.kindergartenSchoolCode().contains(schoolGrade.getGradeCode())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(kindergartenList)){
            kindergartenList.sort(Comparator.comparing(SchoolGrade::getGradeCode));
            kindergartenList.forEach(schoolGrade -> gradeInfoVOList.add(buildGradeInfo(gradeStudentMap, schoolGrade)));
        }

        //小学及以上
        List<SchoolGrade> notKindergartenList = schoolGradeList.stream().filter(schoolGrade -> !GradeCodeEnum.kindergartenSchoolCode().contains(schoolGrade.getGradeCode())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(notKindergartenList)){
            notKindergartenList.sort(Comparator.comparing(SchoolGrade::getGradeCode));
            notKindergartenList.forEach(schoolGrade -> gradeInfoVOList.add(buildGradeInfo(gradeStudentMap, schoolGrade)));
        }

        return gradeInfoVOList;
    }

    /**
     * 构建年级信息
     * @param gradeStudentMap 年级学生集合
     * @param schoolGrade 学校年级
     */
    private GradeInfoVO.GradeInfo buildGradeInfo(Map<Integer, List<SchoolStudent>> gradeStudentMap, SchoolGrade schoolGrade) {
        List<SchoolStudent> schoolStudentList = gradeStudentMap.getOrDefault(schoolGrade.getId(), Lists.newArrayList());
        GradeInfoVO.GradeInfo gradeInfoVO = new GradeInfoVO.GradeInfo();
        gradeInfoVO.setGradeId(schoolGrade.getId());
        gradeInfoVO.setGradeName(schoolGrade.getName());
        gradeInfoVO.setStudentNum(schoolStudentList.size());
        return gradeInfoVO;
    }

    /**
     * 构建班级信息
     * @param schoolGradeMap 年级集合
     * @param schoolMap 学校集合
     * @param schoolClass 班级对象
     */
    private SchoolClassDTO buildSchoolClassDTO(Map<Integer, SchoolGrade> schoolGradeMap, Map<Integer, School> schoolMap, SchoolClass schoolClass) {

        SchoolGrade schoolGrade = schoolGradeMap.get(schoolClass.getGradeId());
        School school = schoolMap.get(schoolGrade.getSchoolId());
        SchoolClassDTO schoolClassDTO = new SchoolClassDTO()
                .setGradeName(schoolGrade.getName())
                .setSchoolName(school.getName())
                .setSchoolDistrictDetail(school.getDistrictDetail())
                .setSchoolDistrictId(school.getDistrictId())
                .setSchoolAreaType(school.getAreaType())
                .setSchoolMonitorType(school.getMonitorType());

        schoolClassDTO.setId(schoolClass.getId())
                .setCreateUserId(schoolClass.getCreateUserId())
                .setName(schoolClass.getName())
                .setSeatCount(schoolClass.getSeatCount())
                .setStatus(schoolClass.getStatus())
                .setSchoolId(school.getId())
                .setGradeId(schoolGrade.getId())
                .setCreateTime(schoolClass.getCreateTime())
                .setUpdateTime(schoolClass.getUpdateTime());

        return schoolClassDTO;
    }

    /**
     * 获取年级和班级
     * @param gradeIds 年级ID集合
     */
    public TwoTuple<Map<Integer,SchoolGrade>,Map<Integer,SchoolClass>> getSchoolGradeAndClass(List<Integer> gradeIds){
        TwoTuple<Map<Integer,SchoolGrade>,Map<Integer,SchoolClass>> twoTuple = new TwoTuple<>();
        if (CollUtil.isEmpty(gradeIds)){
            return TwoTuple.of(Maps.newHashMap(),Maps.newHashMap());
        }
        List<SchoolGrade> schoolGradeList = schoolGradeService.listByIds(gradeIds);
        if (CollUtil.isEmpty(schoolGradeList)){
            twoTuple.setFirst(Maps.newHashMap());
        }else {
            Map<Integer, SchoolGrade> schoolGradeMap = schoolGradeList.stream().collect(Collectors.toMap(SchoolGrade::getId, Function.identity()));
            twoTuple.setFirst(schoolGradeMap);
        }

        List<SchoolClass> schoolClassList = schoolClassService.listByGradeIds(gradeIds);
        if (CollUtil.isEmpty(schoolClassList)){
            twoTuple.setSecond(Maps.newHashMap());
        }else {
            Map<Integer, SchoolClass> schoolGradeMap = schoolClassList.stream().collect(Collectors.toMap(SchoolClass::getId, Function.identity()));
            twoTuple.setSecond(schoolGradeMap);
        }
        return twoTuple;
    }

}
