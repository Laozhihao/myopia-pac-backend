package com.wupol.myopia.business.core.school.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校端-学生表Mapper接口
 *
 * @author Simple4H
 */
public interface SchoolStudentMapper extends BaseMapper<SchoolStudent> {

    IPage<SchoolStudentListResponseDTO> getList(@Param("page") Page<?> page, @Param("requestDTO") SchoolStudentRequestDTO requestDTO, @Param("schoolId") Integer schoolId);

    List<SchoolStudent> getByIdCardAndSno(@Param("id") Integer id, @Param("idCard") String idCard, @Param("sno") String sno, @Param("schoolId") Integer schoolId);

    void deletedStudent(@Param("id") Integer id);

    List<SchoolStudent> getByStudentIds(@Param("studentIds") List<Integer> studentIds);

    List<SchoolStudent> getByStudentId(@Param("studentId") Integer studentId);

    List<SchoolStudent> getBySchoolIdAndGradeId(@Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId);

    List<SchoolStudent> getByIdCardOrSno(@Param("idCards") List<String> idCards, @Param("snos") List<String> snos, @Param("schoolId") Integer schoolId);

    List<SchoolStudent> getByIdCards(@Param("idCards") List<String> idCards, @Param("schoolId") Integer schoolId);

    List<SchoolStudent> getBySchoolId(@Param("schoolId") Integer schoolId);

    SchoolStudent getDeletedByIdCardAndSno(@Param("idCard") String idCard, @Param("sno") String sno, @Param("schoolId") Integer schoolId);
}
