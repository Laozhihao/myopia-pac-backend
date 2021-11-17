package com.wupol.myopia.business.core.school.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.mapper.SchoolStudentMapper;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学校端-学生服务
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class SchoolStudentService extends BaseService<SchoolStudentMapper, SchoolStudent> {

    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  入参
     * @param schoolId    学校Id
     * @return IPage<SchoolStudentListResponseDTO>
     */
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO, Integer schoolId) {
        return baseMapper.getList(pageRequest.toPage(), requestDTO, schoolId);
    }

    /**
     * 通过身份证和学号获取学生
     *
     * @param id       学生Id
     * @param idCard   身份证
     * @param sno      学号
     * @param schoolId 学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByIdCardAndSno(Integer id, String idCard, String sno, Integer schoolId) {
        return baseMapper.getByIdCardAndSno(id, idCard, sno, schoolId);
    }

    /**
     * 删除学生
     *
     * @param id 学生Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletedStudent(Integer id) {
        baseMapper.deletedStudent(id);
    }

    /**
     * 通过学生ids获取学校学生
     *
     * @param studentIds 学生ids
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByStudentIds(List<Integer> studentIds) {
        return baseMapper.getByStudentIds(studentIds);
    }

    /**
     * 通过学生id获取学校学生
     *
     * @param studentId 学生id
     * @return SchoolStudent
     */
    public SchoolStudent getByStudentId(Integer studentId) {
        return baseMapper.getByStudentId(studentId);
    }

    /**
     * 通过学校、班级Id获取学生
     *
     * @param schoolId 学校Id
     * @param gradeId  年级Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getBySchoolIdAndGradeId(Integer schoolId, Integer gradeId) {
        return baseMapper.getBySchoolIdAndGradeId(schoolId, gradeId);
    }

    /**
     * 通过身份证或学号获取学生
     *
     * @param idCards  身份证
     * @param snos     学号
     * @param schoolId 学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByIdCardOrSno(List<String> idCards, List<String> snos, Integer schoolId) {
        return baseMapper.getByIdCardOrSno(idCards, snos, schoolId);
    }

    /**
     * 通过身份证获取学生
     *
     * @param idCards  身份证
     * @param schoolId 学校Id
     * @return List<SchoolStudent>
     */
    public List<SchoolStudent> getByIdCards(List<String> idCards, Integer schoolId) {
        return baseMapper.getByIdCards(idCards, schoolId);
    }
}