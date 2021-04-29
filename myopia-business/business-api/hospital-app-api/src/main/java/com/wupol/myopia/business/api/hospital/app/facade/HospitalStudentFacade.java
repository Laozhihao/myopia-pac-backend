package com.wupol.myopia.business.api.hospital.app.facade;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.api.hospital.app.domain.dto.HospitalStudentDTO;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dos.HospitalStudentDO;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/21
 **/
@Service
public class HospitalStudentFacade {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private MedicalReportService medicalReportService;
    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 根据学生二维码的token, 获取学生信息
     *
     * @param token
     * @return com.wupol.myopia.business.api.hospital.app.domain.dto.HospitalStudentDTO
     **/
    public HospitalStudentDTO getStudentByToken(String token) {
        Integer studentId = studentService.parseToken2StudentId(token);
        return getStudentById(studentId);
    }

    /**
     * 获取学生信息
     * @param idCard    学生的身份证
     * @param name    学生的姓名
     * @return
     */
    public HospitalStudentDTO getStudent(String idCard, String name) {
        return getHospitalStudentDetail(null, idCard, name);
    }

    /**
     * 获取学生信息
     * @param id     学生id
     * @return
     */
    public HospitalStudentDTO getStudentById(Integer id) {
        HospitalStudentDTO studentDTO = getHospitalStudentDetail(id, null, null);
        if (Objects.isNull(studentDTO)) {
            throw new BusinessException("未找到该学生");
        }
        // 设置最后的就诊日期
        MedicalReport medicalReport = medicalReportService.getLastOneByStudentId(studentDTO.getId());
        if (Objects.nonNull(medicalReport)) {
            studentDTO.setLastVisitDate(medicalReport.getCreateTime());
        }
        return studentDTO;
    }

    /**
     * 获取学生列表, 带就诊信息的
     * @param hospitalId 医院id
     * @return
     */
    public List<HospitalStudentDTO> getStudentList(Integer hospitalId, String nameLike) {
        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setNameLike(nameLike).setHospitalId(hospitalId);
        // 获取学生的详细信息
        return getHospitalStudentDTOList(query);
    }


    /**
     * 获取最近6条的学生信息.
     * 今天建档的患者姓名【前3名】+今天眼健康检查【前3名】的患者姓名，最新时间排在最前面 。最多显示6个。
     * @param hospitalId 医院id
     * @return
     */
    public List<HospitalStudentDTO> getRecentList(Integer hospitalId) throws IOException {
        // 今天建档的患者姓名【前3名】
        List<Integer> idList = hospitalStudentService.findByPage(new HospitalStudent().setHospitalId(hospitalId), 0, 3)
                .getRecords().stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date()))
                .map(HospitalStudent::getStudentId).collect(Collectors.toList());

        // 今天眼健康检查【前3名】的患者
        idList.addAll(medicalRecordService.getTodayLastThreeStudentList(hospitalId));
        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setStudentIdList(idList).setHospitalId(hospitalId);
        return CollectionUtils.isEmpty(idList) ? Collections.EMPTY_LIST : getHospitalStudentDTOList(query);
    }

    /**
     * 保存学生信息, 带id是更新,不带是新增
     * @param studentVo 学生信息
     * @param isCheckNameAndIDCard 是否校验名称与身份证的匹配性
     * @return  学生的id
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(HospitalStudentDTO studentVo, Boolean isCheckNameAndIDCard) {
        Student student = BeanCopyUtil.copyBeanPropertise(studentVo, HospitalStudentDTO.class);
        if (Objects.isNull(student)) {
            throw new BusinessException("学生信息不能为空");
        }

        // 数据库中保存的学生信息
        // 优先使用studentId查询
        Student oldStudent = Objects.nonNull(student.getId()) ? studentService.getById(student.getId()) : studentService.getByIdCard(student.getIdCard());
        if (Objects.nonNull(oldStudent) && isCheckNameAndIDCard) {
            if(!(oldStudent.getIdCard().equals(student.getIdCard()) && oldStudent.getName().equals(student.getName()))) {
                throw new BusinessException("学生的身份证与姓名不匹配");
            }
        }

        // 设置学校信息
        if (Objects.nonNull(studentVo.getSchool())) {
            School school = schoolService.getById(studentVo.getSchool().getId());
            student.setSchoolNo(school.getSchoolNo());
        }
        if (Objects.nonNull(studentVo.getSchoolGrade())) {
            student.setGradeId(studentVo.getSchoolGrade().getId());
        }
        if (Objects.nonNull(studentVo.getSchoolClass())) {
            student.setClassId(studentVo.getSchoolClass().getId());
        }
        if (Objects.nonNull(studentVo.getProvince())) {
            student.setProvinceCode(districtService.getById(studentVo.getProvince().getId()).getCode());
        }
        if (Objects.nonNull(studentVo.getCity())) {
            student.setCityCode(districtService.getById(studentVo.getCity().getId()).getCode());
        }
        if (Objects.nonNull(studentVo.getArea())) {
            student.setAreaCode(districtService.getById(studentVo.getArea().getId()).getCode());
        }
        if (Objects.nonNull(studentVo.getTown())) {
            student.setTownCode(districtService.getById(studentVo.getTown().getId()).getCode());
        }

        Integer studentId;

        // 存在则更新,不存在则新增
        if (Objects.nonNull(oldStudent)) {
            updateStudentInfoByAnotherStudent(oldStudent, student);
            studentId = studentService.updateStudent(oldStudent).getId();
        } else{
            studentId = studentService.saveStudent(student);
        }
        return studentId;
    }

    /**
     * 从一个学生信息, 更新到另一个学生信息
     *  只更新来源的数据不为空的数据
     */
    private void updateStudentInfoByAnotherStudent(Student target, Student source) {
        if (Objects.nonNull(source.getId())) target.setId(source.getId());
        if (Objects.nonNull(source.getGradeId())) target.setGradeId(source.getGradeId());
        if (Objects.nonNull(source.getClassId())) target.setClassId(source.getClassId());
        if (!StringUtils.isEmpty(source.getIdCard())) target.setIdCard(source.getIdCard());
        if (!StringUtils.isEmpty(source.getName())) target.setName(source.getName());
        if (Objects.nonNull(source.getGender())) target.setGender(source.getGender());
        if (Objects.nonNull(source.getBirthday())) target.setBirthday(source.getBirthday());
        if (!StringUtils.isEmpty(source.getParentPhone())) target.setParentPhone(source.getParentPhone());
        if (!StringUtils.isEmpty(source.getMpParentPhone())) target.setMpParentPhone(source.getMpParentPhone());
        if (!StringUtils.isEmpty(source.getSchoolNo())) target.setSchoolNo(source.getSchoolNo());
        if (Objects.nonNull(source.getProvinceCode())) target.setProvinceCode(source.getProvinceCode());
        if (Objects.nonNull(source.getCityCode())) target.setCityCode(source.getCityCode());
        if (Objects.nonNull(source.getAreaCode())) target.setAreaCode(source.getAreaCode());
        if (Objects.nonNull(source.getTownCode())) target.setTownCode(source.getTownCode());
        if (!StringUtils.isEmpty(source.getAddress())) target.setAddress(source.getAddress());

    }


    /**
     * 获取医院的学生的详细信息
     *
     * @param query
     * @return java.util.List<com.wupol.myopia.business.api.hospital.app.domain.dto.HospitalStudentDTO>
     **/
    private List<HospitalStudentDTO> getHospitalStudentDTOList(HospitalStudentQuery query) {
        // 该医院已建档的学生
        Map<Integer, HospitalStudentDO> studentVoMap = hospitalStudentService.getHospitalStudentVoMap(query);
        // 获取学生的详细信息
        List<HospitalStudentDTO> studentList = getHospitalStudentList(new ArrayList<>(studentVoMap.keySet()), query.getNameLike());
        // 设置就诊信息
        studentList.forEach(item-> {
            HospitalStudentDO hospitalStudentDO = studentVoMap.get(item.getId());
            // 就诊次数
            item.setNumOfVisits(hospitalStudentDO.getNumOfVisits());
            // 获取最后一检查的创建时间
            item.setLastVisitDate(hospitalStudentDO.getLastVisitDate());
        });
        return studentList;
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
            student = studentService.getById(studentId);
        } else {
            if (StringUtils.isBlank(idCard) || StringUtils.isBlank(name)) {
                throw new BusinessException("数据异常，请确认");
            }
            student = studentService.getByIdCardAndName(idCard, name);
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
        if (null != student.getNation()) {
            studentDTO.setNationName(NationEnum.getName(studentDTO.getNation()));
        }
        return studentDTO;
    }

    /**
     * 获取学生地区Maps
     *
     * @param students 学生列表
     * @return Map<Long, District>
     */
    private Map<Long, District> getDistrictMap(List<Student> students) {
        List<Long> districtCode = new ArrayList<>();
        students.forEach(student -> {
            if (null != student.getProvinceCode()) {
                districtCode.add(student.getProvinceCode());
            }
            if (null != student.getCityCode()) {
                districtCode.add(student.getCityCode());
            }
            if (null != student.getAreaCode()) {
                districtCode.add(student.getAreaCode());
            }
            if (null != student.getTownCode()) {
                districtCode.add(student.getTownCode());
            }
        });

        // 地区Maps
        return districtService.getByCodes(districtCode)
                .stream().distinct().collect(Collectors
                        .toMap(District::getCode, Function.identity()));
    }

    /**
     * 医院端学生信息
     *
     * @param studentIds 学生ids
     * @param name       学生姓名
     * @return List<HospitalStudentDTO>
     */
    public List<HospitalStudentDTO> getHospitalStudentList(List<Integer> studentIds, String name) {
        List<HospitalStudentDTO> dtoList = new ArrayList<>();

        if (CollectionUtils.isEmpty(studentIds)) {
            return dtoList;
        }

        List<Student> students = studentService.getByIdsAndName(studentIds, name);
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

        students.forEach(student -> {
            HospitalStudentDTO dto = new HospitalStudentDTO();
            BeanUtils.copyProperties(student, dto);

            if (StringUtils.isNotBlank(student.getSchoolNo())) {
                dto.setSchool(schoolMaps.get(student.getSchoolNo()));
            }
            if (null != student.getClassId()) {
                dto.setSchoolClass(classMaps.get(student.getClassId()));
            }
            if (null != student.getGradeId()) {
                dto.setSchoolGrade(gradeMaps.get(student.getGradeId()));
            }
            dtoList.add(dto);
        });
        return dtoList;
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
