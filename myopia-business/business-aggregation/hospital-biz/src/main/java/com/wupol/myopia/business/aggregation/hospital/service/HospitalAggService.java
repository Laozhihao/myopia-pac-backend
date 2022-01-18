package com.wupol.myopia.business.aggregation.hospital.service;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author wulizhou
 * @Date 2022/1/17 19:35
 */
@Service
public class HospitalAggService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 根据学生二维码的token, 从管理端获取学生信息
     *
     * @param hospitalId 医院Id
     * @param token
     * @return com.wupol.myopia.business.api.hospital.app.domain.vo.HospitalStudentDTO
     **/
    public TwoTuple<HospitalStudentVO, Boolean> getStudentByToken(Integer hospitalId, String token) {
        Integer studentId = studentService.parseToken2StudentId(token);
        return getStudentById(hospitalId, studentId);
    }

    /**
     * 从管理端获取学生信息
     *
     * @param hospitalId 医院Id
     * @param idCard     学生的身份证
     * @param name       学生的姓名
     * @return
     */
    public HospitalStudentVO getStudent(Integer hospitalId, String idCard, String name) {
        return getHospitalStudent(hospitalId, null, idCard, name).getFirst();
    }

    /**
     * 从管理端获取学生信息
     *
     * @param hospitalId 医院Id
     * @param id         学生id
     * @return
     */
    public TwoTuple<HospitalStudentVO, Boolean> getStudentById(Integer hospitalId, Integer id) {
        TwoTuple<HospitalStudentVO, Boolean> studentInfo = getHospitalStudent(hospitalId, id, null, null);
        if (Objects.isNull(studentInfo) || Objects.isNull(studentInfo.getFirst())) {
            throw new BusinessException("未找到该学生");
        }
        return studentInfo;
    }

    /**
     * 获取学生详情,先从医院端获取，如果没有，则从管理端获取
     *
     * @param hospitalId 医院Id
     * @param studentId  学生ID
     * @param idCard     身份证
     * @param name       姓名
     * @return TwoTuple<学生信息, 是否在医院建档>
     */
    public TwoTuple<HospitalStudentVO, Boolean> getHospitalStudent(Integer hospitalId, Integer studentId, String idCard, String name) {
        HospitalStudentVO studentVO = new HospitalStudentVO();
        HospitalStudent student;
        if (null != studentId) {
            HospitalStudentQuery query = new HospitalStudentQuery();
            query.setStudentId(studentId).setHospitalId(hospitalId);
            student = hospitalStudentService.getBy(query).stream().findFirst().orElse(null);
        } else {
            if (StringUtils.isBlank(idCard) || StringUtils.isBlank(name)) {
                throw new BusinessException("数据异常，请确认");
            }
            HospitalStudentQuery query = new HospitalStudentQuery();
            query.setIdCard(idCard).setName(name);
            student = hospitalStudentService.getBy(query).stream().findFirst().orElse(null);
        }
        // 医院端没有该学生信息，则从管理端获取
        if (null == student) {
            return TwoTuple.of(getHospitalStudentFromManagement(studentId, idCard, name), false);
        }
        BeanUtils.copyProperties(student, studentVO);
        studentVO.setStudentId(studentId);

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
        return TwoTuple.of(studentVO, true);
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
        studentVO.setId(null).setStudentId(studentId);

        // 地区Maps
        Map<Long, District> districtMaps = getDistrictMap(Lists.newArrayList(student));
        packageStudentDistrict(districtMaps, studentVO, student);

        if (Objects.nonNull(student.getSchoolId())) {
            studentVO.setSchool(schoolService.getById(student.getSchoolId()));
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
        if (Objects.nonNull(student.getCommitteeCode())) {
            dto.setCommittee(districtService.getDistrictByCode(student.getCommitteeCode()));
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
        if (Objects.nonNull(student.getCommitteeCode())) {
            dto.setCommittee(districtService.getDistrictByCode(student.getCommitteeCode()));
        }
    }

}
