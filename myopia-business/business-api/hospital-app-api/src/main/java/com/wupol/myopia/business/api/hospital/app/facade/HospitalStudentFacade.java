package com.wupol.myopia.business.api.hospital.app.facade;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.hospital.app.domain.vo.HospitalStudentVO;
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
     * 根据学生二维码的token, 从管理端获取学生信息
     *
     * @param token
     * @return com.wupol.myopia.business.api.hospital.app.domain.vo.HospitalStudentDTO
     **/
    public HospitalStudentVO getStudentByToken(String token) {
        Integer studentId = studentService.parseToken2StudentId(token);
        return getStudentById(studentId);
    }

    /**
     * 从管理端获取学生信息
     *
     * @param idCard 学生的身份证
     * @param name   学生的姓名
     * @return
     */
    public HospitalStudentVO getStudent(String idCard, String name) {
        return getHospitalStudent(null, idCard, name);
    }

    /**
     * 从管理端获取学生信息
     *
     * @param id 学生id
     * @return
     */
    public HospitalStudentVO getStudentById(Integer id) {
        HospitalStudentVO studentDTO = getHospitalStudent(id, null, null);
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
     * 获取最近6条的学生信息.
     * 今天建档的患者姓名【前3名】+今天眼健康检查【前3名】的患者姓名，最新时间排在最前面 。最多显示6个。
     *
     * @param hospitalId 医院id
     * @return
     */
    public List<HospitalStudentVO> getRecentList(Integer hospitalId) throws IOException {
        // 今天建档的患者姓名【前3名】
        List<Integer> idList = hospitalStudentService.findByPage(new HospitalStudent().setHospitalId(hospitalId), 0, 3)
                .getRecords().stream()
                .filter(item -> DateUtils.isSameDay(item.getCreateTime(), new Date()))
                .map(HospitalStudent::getId).collect(Collectors.toList());

        // 今天眼健康检查【前3名】的患者
        idList.addAll(medicalRecordService.getTodayLastThreeStudentList(hospitalId));
        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setStudentIdList(idList).setHospitalId(hospitalId);
        return CollectionUtils.isEmpty(idList) ? Collections.emptyList() : getHospitalStudentVoList(query);
    }

    /**
     * 保存学生信息, 带id是更新,不带是新增
     *
     * @param studentVo            学生信息
     * @param isCheckNameAndIDCard 是否校验名称与身份证的匹配性
     * @return 学生的id
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(HospitalStudentVO studentVo, Boolean isCheckNameAndIDCard) {
        if (Objects.isNull(studentVo)) {
            throw new BusinessException("学生信息不能为空");
        }

        // 数据库中保存的学生信息
        // 优先使用studentId查询
        Student oldStudent = Objects.nonNull(studentVo.getId()) ?
                studentService.getById(studentVo.getId()) :
                studentService.getByIdCard(studentVo.getIdCard());
        if (Objects.nonNull(oldStudent) && isCheckNameAndIDCard) {
            if (!(oldStudent.getIdCard().equals(studentVo.getIdCard()) && oldStudent.getName().equals(studentVo.getName()))) {
                throw new BusinessException("学生的身份证与姓名不匹配");
            }
        }

        // 设置学校信息
        if (Objects.nonNull(studentVo.getSchool())) {
            School school = schoolService.getById(studentVo.getSchool().getId());
            studentVo.setSchoolId(school.getId());
        }
        if (Objects.nonNull(studentVo.getSchoolGrade())) {
            studentVo.setGradeId(studentVo.getSchoolGrade().getId());
        }
        if (Objects.nonNull(studentVo.getSchoolClass())) {
            studentVo.setClassId(studentVo.getSchoolClass().getId());
        }
        if (Objects.nonNull(studentVo.getProvince())) {
            studentVo.setProvinceId(studentVo.getProvince().getId());
        }
        if (Objects.nonNull(studentVo.getCity())) {
            studentVo.setCityId(studentVo.getCity().getId());
        }
        if (Objects.nonNull(studentVo.getArea())) {
            studentVo.setAreaId(studentVo.getArea().getId());
        }
        if (Objects.nonNull(studentVo.getTown())) {
            studentVo.setTownId(studentVo.getTown().getId());
        }

        // 如果医院端没有该学生信息, 则先到管理端创建,再到医院端创建
        if (Objects.isNull(oldStudent)) {
            Student tmpStudent = new Student();
            BeanUtils.copyProperties(studentVo, tmpStudent);
            // 转换地址与学校数据
            if (Objects.nonNull(studentVo.getSchoolId())) {
                tmpStudent.setSchoolNo(schoolService.getById(studentVo.getSchoolId()).getSchoolNo());
            }
            if (Objects.nonNull(studentVo.getProvinceId())) {
                tmpStudent.setProvinceCode(districtService.getById(studentVo.getProvinceId()).getCode());
            }
            if (Objects.nonNull(studentVo.getCityId())) {
                tmpStudent.setCityCode(districtService.getById(studentVo.getCityId()).getCode());
            }
            if (Objects.nonNull(studentVo.getAreaId())) {
                tmpStudent.setAreaCode(districtService.getById(studentVo.getAreaId()).getCode());
            }
            if (Objects.nonNull(studentVo.getTownId())) {
                tmpStudent.setTownCode(districtService.getById(studentVo.getTownId()).getCode());
            }
            Integer studentId = studentService.saveStudent(tmpStudent);
            studentVo.setId(studentId);
        }
        hospitalStudentService.saveOrUpdate(studentVo);
        return studentVo.getId();
    }

    /**
     * 获取医院的学生的详细信息
     *
     * @param query
     **/
    public List<HospitalStudentVO> getHospitalStudentVoList(HospitalStudentQuery query) {
        // 该医院已建档的学生
        List<HospitalStudentDO> studentDOList = hospitalStudentService.getHospitalStudentDoList(query);
        // 获取学生的带地址和学校的详细信息
        return updateStudentVoInfo(studentDOList);
    }

    /**
     * 获取学生详情,先从医院端获取，如果没有，则从管理端获取
     *
     * @param studentId 学生ID
     * @param idCard    身份证
     * @param name      姓名
     * @return HospitalStudentDTO
     */
    public HospitalStudentVO getHospitalStudent(Integer studentId, String idCard, String name) {
        HospitalStudentVO studentVO = new HospitalStudentVO();
        HospitalStudent student;
        if (null != studentId) {
            student = hospitalStudentService.getById(studentId);
        } else {
            if (StringUtils.isBlank(idCard) || StringUtils.isBlank(name)) {
                throw new BusinessException("数据异常，请确认");
            }
            HospitalStudentQuery query = new HospitalStudentQuery();
            query.setIdCard(idCard).setName(name);
            student = hospitalStudentService.getBy(query).stream().findFirst().orElse(null);
        }
        // 医院端没有该学生信息，从则管理端获取
        if (null == student) {
            return getHospitalStudentFromManagement(studentId, idCard, name);
        }
        BeanUtils.copyProperties(student, studentVO);

        // 地区Maps
        Map<Integer, District> districtMaps = getDistrictMapByDistrictId(Lists.newArrayList(student));
        packageStudentDistrict(districtMaps, studentVO, student);

        if (Objects.nonNull(student.getSchoolId())) {
            studentVO.setSchool(schoolService.getById(student.getSchoolId()));
        }
        if (Objects.nonNull(student.getGradeId())) {
            studentVO.setSchoolGrade(schoolGradeService.getById(student.getGradeId()));
        }
        if (Objects.nonNull(student.getClassId())) {
            studentVO.setSchoolClass(schoolClassService.getById(student.getClassId()));
        }
        if (Objects.nonNull(student.getNation())) {
            studentVO.setNationName(NationEnum.getName(studentVO.getNation()));
        }
        return studentVO;
    }

    /**
     * 获取学生详情
     *
     * @param studentId 学生ID
     * @param idCard    身份证
     * @param name      姓名
     * @return HospitalStudentDTO
     */
    private HospitalStudentVO getHospitalStudentFromManagement(Integer studentId, String idCard, String name) {

        HospitalStudentVO studentVO = new HospitalStudentVO();
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
            return studentVO;
        }
        BeanUtils.copyProperties(student, studentVO);

        // 地区Maps
        Map<Long, District> districtMaps = getDistrictMap(Lists.newArrayList(student));
        packageStudentDistrict(districtMaps, studentVO, student);

        if (StringUtils.isNotBlank(student.getSchoolNo())) {
            studentVO.setSchool(schoolService.getBySchoolNo(student.getSchoolNo()));
        }
        if (null != student.getGradeId()) {
            studentVO.setSchoolGrade(schoolGradeService.getById(student.getGradeId()));
        }
        if (null != student.getClassId()) {
            studentVO.setSchoolClass(schoolClassService.getById(student.getClassId()));
        }
        if (null != student.getNation()) {
            studentVO.setNationName(NationEnum.getName(studentVO.getNation()));
        }
        return studentVO;
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
     * 获取学生地区Maps
     *
     * @param students 学生列表
     * @return Map<Long, District>
     */
    private Map<Integer, District> getDistrictMapByDistrictId(List<HospitalStudent> students) {
        List<Integer> districtCode = new ArrayList<>();
        students.forEach(student -> {
            if (null != student.getProvinceId()) {
                districtCode.add(student.getProvinceId());
            }
            if (null != student.getCityId()) {
                districtCode.add(student.getCityId());
            }
            if (null != student.getAreaId()) {
                districtCode.add(student.getAreaId());
            }
            if (null != student.getTownId()) {
                districtCode.add(student.getTownId());
            }
        });

        if (CollectionUtils.isEmpty(districtCode)) {
            return new HashMap<>();
        }
        // 地区Maps
        return districtService.getDistrictByIds(districtCode)
                .stream().distinct().collect(Collectors
                        .toMap(District::getId, Function.identity()));
    }

    /**
     * 设置医院端的学生信息的学校及地址信息
     * @param studentList 学生信息列表
     */
    private List<HospitalStudentVO> updateStudentVoInfo(List<HospitalStudentDO> studentList) {
        List<HospitalStudentVO> voList = new ArrayList<>();

        if (CollectionUtils.isEmpty(studentList)) {
            return voList;
        }

        // 学校Maps
        List<School> schoolList = schoolService.getSchoolByIds(studentList
                .stream().distinct().map(HospitalStudent::getSchoolId).collect(Collectors.toList()));
        Map<Integer, School> schoolMaps = schoolList.stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        // 班级Maps
        Map<Integer, SchoolClass> classMaps = schoolClassService.getClassMapByIds(studentList
                .stream().map(HospitalStudent::getClassId).collect(Collectors.toList()));

        // 年级Maps
        Map<Integer, SchoolGrade> gradeMaps = schoolGradeService.getGradeMapByIds(studentList
                .stream().map(HospitalStudent::getGradeId).collect(Collectors.toList()));

        studentList.forEach(student -> {
            HospitalStudentVO dto = new HospitalStudentVO();
            BeanUtils.copyProperties(student, dto);

            if (Objects.nonNull(student.getSchoolId())) {
                dto.setSchool(schoolMaps.get(student.getSchoolId()));
            }
            if (null != student.getClassId()) {
                dto.setSchoolClass(classMaps.get(student.getClassId()));
            }
            if (null != student.getGradeId()) {
                dto.setSchoolGrade(gradeMaps.get(student.getGradeId()));
            }
            voList.add(dto);
        });
        return voList;
    }

    /**
     * 封装学生区域
     *
     * @param districtMaps 区域Maps
     * @param dto          dto
     * @param student      学生
     */
    private void packageStudentDistrict(Map<Long, District> districtMaps, HospitalStudentVO dto, Student student) {
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

    /**
     * 封装学生区域
     *
     * @param districtMaps 区域Maps
     * @param dto          dto
     * @param student      学生
     */
    private void packageStudentDistrict(Map<Integer, District> districtMaps, HospitalStudentVO dto, HospitalStudent student) {
        if (null != student.getProvinceId()) {
            dto.setProvince(districtMaps.get(student.getProvinceId()));
        }
        if (null != student.getCityId()) {
            dto.setCity(districtMaps.get(student.getCityId()));
        }
        if (null != student.getAreaId()) {
            dto.setArea(districtMaps.get(student.getAreaId()));
        }
        if (null != student.getTownId()) {
            dto.setTown(districtMaps.get(student.getTownId()));
        }
    }
}
