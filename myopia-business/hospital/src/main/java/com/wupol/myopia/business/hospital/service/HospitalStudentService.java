package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.hospital.domain.mapper.HospitalStudentMapper;
import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.hospital.domain.vo.HospitalStudentVo;
import com.wupol.myopia.business.management.domain.dto.HospitalStudentDTO;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.log4j.Log4j2;
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
 * 医院的学生管理的App接口
 *
 * @Author Chikong
 * @date 2021-02-10
 */
@Service
@Log4j2
public class HospitalStudentService extends BaseService<HospitalStudentMapper, HospitalStudent> {

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


    /**
     * 获取学生信息
     * @param id     学生id
     * @param idCard    学生的身份证
     * @param name    学生的姓名
     * @return
     */
    public HospitalStudentDTO getStudent(Integer id, String idCard, String name) {
        return studentService.getHospitalStudentDetail(id, idCard, name);
    }

    /**
     * 获取学生信息
     * @param id     学生id
     * @return
     */
    public HospitalStudentDTO getStudentById(Integer id) {
        HospitalStudentDTO studentDTO = studentService.getHospitalStudentDetail(id, null, null);
        if (Objects.isNull(studentDTO)) {
            throw new BusinessException("未找到该学生");
        }
        // 设置最后的就诊日期
        MedicalRecord medicalRecord = medicalRecordService.getLastOneByStudentId(studentDTO.getId());
        if (Objects.nonNull(medicalRecord)) {
            studentDTO.setLastVisitDate(medicalRecord.getCreateTime());
        }
        return studentDTO;
    }

    /**
     * 获取学生列表, 带就诊信息的
     * @param hospitalId 医院id
     * @return
     */
    public List<HospitalStudentDTO> getStudentList(Integer hospitalId, String nameLike) throws IOException {
        HospitalStudentQuery query = new HospitalStudentQuery();
        query.setHospitalId(hospitalId);

        // 该医院已建档的学生
        Map<Integer, HospitalStudentVo> studentVoMap = getHospitalStudentVoList(query).stream()
                .collect(Collectors.toMap(HospitalStudentVo::getStudentId, Function.identity()));
        // 获取学生的详细信息
       List<HospitalStudentDTO> studentList = studentService.getHospitalStudentLists(new ArrayList<>(studentVoMap.keySet()), nameLike);
        // 设置就诊信息
        studentList.forEach(item-> {
            HospitalStudentVo hospitalStudentVo = studentVoMap.get(item.getId());
           item.setNumOfVisits(hospitalStudentVo.getNumOfVisits()); // 就诊次数
           item.setLastVisitDate(hospitalStudentVo.getLastVisitDate()); // 获取最后一检查的创建时间
       });
        return studentList;
    }

    /** 获取HospitalStudentDTO的数据 */
    public List<HospitalStudentVo> getHospitalStudentVoList(HospitalStudentQuery query) {
        return baseMapper.getHospitalStudentVoList(query);
    }

    /**
     * 获取最近6条的学生信息.
     * 今天建档的患者姓名【前3名】+今天眼健康检查【前3名】的患者姓名，最新时间排在最前面 。最多显示6个。
     * @param hospitalId 医院id
     * @return
     */
    public List<Student> getRecentList(Integer hospitalId) throws IOException {
        // 今天建档的患者姓名【前3名】
        List<Integer> idList = findByPage(new HospitalStudent().setHospitalId(hospitalId), 0, 3)
                .getRecords().stream()
                .filter(item-> DateUtils.isSameDay(item.getCreateTime(), new Date()))
                .map(HospitalStudent::getStudentId).collect(Collectors.toList());

        // 今天眼健康检查【前3名】的患者
        idList.addAll(medicalRecordService.getTodayLastThreeStudentList(hospitalId));
       return CollectionUtils.isEmpty(idList) ? Collections.EMPTY_LIST : studentService.getByIds(idList);
    }

    /**
     * 保存学生信息, 带id是更新,不带是新增
     * @param studentVo 学生信息
     * @return  学生的id
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveStudent(HospitalStudentDTO studentVo) {
        Student student = BeanCopyUtil.copyBeanPropertise(studentVo, HospitalStudentDTO.class);
        if (Objects.isNull(student)) {
            throw new BusinessException("学生信息不能为空");
        }

        // 数据库中保存的学生信息
        Student oldStudent = studentService.getByIdCard(student.getIdCard());
        if (Objects.nonNull(oldStudent)) {
                if(!(oldStudent.getIdCard().equals(student.getIdCard()) && oldStudent.getName().equals(student.getName()))) {
                    throw new BusinessException("学生的身份证与姓名不匹配");
                }
        }

        // 设置学校信息
        if (Objects.nonNull(studentVo.getSchool())) {
            School school = schoolService.getBySchoolId(studentVo.getSchool().getId());
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
            updateStudentInfoByAnotherStudent(student, oldStudent);
            studentId = studentService.updateStudent(student).getId();
        } else{
            studentId = studentService.saveStudent(student);
        }
        return studentId;
    }

    /** 从一个学生信息, 更新到另一个学生信息 */
    private void updateStudentInfoByAnotherStudent(Student target, Student source) {
        if (Objects.nonNull(source.getId())) target.setId(source.getId());
        if (Objects.nonNull(source.getIdCard())) target.setIdCard(source.getIdCard());
        if (Objects.nonNull(source.getName())) target.setName(source.getName());
        if (Objects.nonNull(source.getGender())) target.setGender(source.getGender());
        if (Objects.nonNull(source.getBirthday())) target.setBirthday(source.getBirthday());
        if (Objects.nonNull(source.getParentPhone())) target.setParentPhone(source.getParentPhone());
        if (Objects.nonNull(source.getSchoolNo())) target.setSchoolNo(source.getSchoolNo());
        if (Objects.nonNull(source.getGradeId())) target.setGradeId(source.getGradeId());
        if (Objects.nonNull(source.getClassId())) target.setClassId(source.getClassId());
        if (Objects.nonNull(source.getProvinceCode())) target.setProvinceCode(source.getProvinceCode());
        if (Objects.nonNull(source.getCityCode())) target.setCityCode(source.getCityCode());
        if (Objects.nonNull(source.getAreaCode())) target.setAreaCode(source.getAreaCode());
        if (Objects.nonNull(source.getTownCode())) target.setTownCode(source.getTownCode());
        if (Objects.nonNull(source.getAddress())) target.setAddress(source.getAddress());

    }

    /** 保存医院与学生的 */
    public void saveHospitalStudentArchive(Integer hospitalId, Integer studentId) {
        // 保存医院与学生的关系
        saveOrUpdate(new HospitalStudent(hospitalId, studentId));
    }

    /** 校验学生与医院关系 */
    public Boolean existHospitalAndStudentRelationship(Integer hospitalId, Integer studentId) throws IOException {
        HospitalStudent student = findOne(new HospitalStudent(hospitalId, studentId));
        return Objects.nonNull(student);
    }

    public List<HospitalStudent> getBy(HospitalStudentQuery query) {
        return baseMapper.getBy(query);
    }

}