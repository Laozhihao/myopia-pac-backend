package com.wupol.myopia.business.hospital.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.hospital.domain.mapper.HospitalStudentMapper;
import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.management.domain.dto.HospitalStudentDTO;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
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
        List<Integer> idList = baseMapper.getBy(query).stream().map(HospitalStudent::getStudentId).collect(Collectors.toList());
        Map<Integer, List<MedicalReport>> studentReportMap = medicalReportService.findByList(new MedicalReport().setHospitalId(hospitalId))
                .stream().collect(Collectors.groupingBy(MedicalReport::getStudentId));
       List<HospitalStudentDTO> studentList = studentService.getHospitalStudentLists(idList, nameLike);
        // 设置就诊信息
        studentList.forEach(item-> {
           List<MedicalReport> reportList = studentReportMap.get(item.getId());
           if (CollectionUtils.isEmpty(reportList)) {
               item.setNumOfVisits(0);
               item.setLastScreeningTime(null);
           } else {
               item.setNumOfVisits(reportList.size()); // 就诊次数
               item.setLastScreeningTime(reportList.get(reportList.size()-1).getCreateTime()); // 获取最后一条的创建时间
           }
       });
        return studentList;
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
     * @param hospitalId 医院id
     * @return  学生的id
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveStudent(HospitalStudentDTO studentVo, Integer hospitalId) {
        Student student = BeanCopyUtil.copyBeanPropertise(studentVo, HospitalStudentDTO.class);
        if (Objects.isNull(student)) {
            throw new BusinessException("学生信息不能为空");
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

        Student oldStudent = studentService.getByIdCard(student.getIdCard());
        Integer studentId;
        // 存在则更新,不存在则新增
        if (Objects.nonNull(oldStudent)) {
            studentId = studentService.updateStudent(student).getId();
        } else{
            studentId = studentService.saveStudent(student);
        }
        // 保存医院与学生的关系
        saveOrUpdate(new HospitalStudent(hospitalId, studentId));
    }

    /** 保存学生与医院关系 */
    public Boolean existHospitalAndStudentRelationship(Integer hospitalId, Integer studentId) throws IOException {
        HospitalStudent student = findOne(new HospitalStudent(hospitalId, studentId));
        return Objects.nonNull(student);
    }

    public List<HospitalStudent> getBy(HospitalStudentQuery query) {
        return baseMapper.getBy(query);
    }

}