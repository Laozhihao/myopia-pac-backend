package com.wupol.myopia.business.api.hospital.app.facade;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.hospital.domain.vo.HospitalStudentVO;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dos.HospitalStudentDO;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private HospitalStudentService hospitalStudentService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;

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
        String idCard = studentVo.getIdCard();
        if (StringUtils.isBlank(idCard)) {
            throw new BusinessException("缺少学生身份证信息");
        }

        // 数据库中保存的学生信息
        // 优先使用studentId查询
        Student oldStudent = Objects.nonNull(studentVo.getStudentId()) ?
                studentService.getById(studentVo.getStudentId()) :
                studentService.getByIdCard(idCard);
        if ((Objects.nonNull(oldStudent) && isCheckNameAndIDCard)
                && (!(oldStudent.getIdCard().equals(idCard)
                && oldStudent.getName().equals(studentVo.getName())))) {
            throw new BusinessException("学生的身份证与姓名不匹配");
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
        if (Objects.nonNull(studentVo.getCommittee())) {
            studentVo.setCommitteeCode(districtService.getById(studentVo.getCommittee().getId()).getCode());
        }

        // 如果管理端没有该学生信息, 则先到管理端创建,再到医院端创建
        if (Objects.isNull(oldStudent)) {
            Student tmpStudent = new Student();
            BeanUtils.copyProperties(studentVo, tmpStudent);
            tmpStudent.setId(null);
            // 转换地址与学校数据
            if (Objects.nonNull(studentVo.getSchoolId())) {
                tmpStudent.setSchoolId(studentVo.getSchoolId());
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
            if (Objects.nonNull(studentVo.getCommitteeCode())) {
                tmpStudent.setCommitteeCode(studentVo.getCommitteeCode());
                tmpStudent.setRecordNo(studentService.getRecordNo(studentVo.getCommitteeCode()));
            }
            Integer studentId = studentService.saveStudent(tmpStudent);
            studentVo.setStudentId(studentId);
        } else {
            studentVo.setRecordNo(oldStudent.getRecordNo());
        }

        // 如果是新增学生，则将创建时间与更新时间设置成当前
        if (Objects.isNull(studentVo.getId())) {
            Date now = new Date();
            studentVo.setCreateTime(now).setUpdateTime(now);
        }

        hospitalStudentService.saveOrUpdate(studentVo);
        return studentVo.getStudentId();
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
     * 设置医院端的学生信息的学校及地址信息
     *
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

}
