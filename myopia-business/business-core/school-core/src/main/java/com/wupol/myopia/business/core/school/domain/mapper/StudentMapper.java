package com.wupol.myopia.business.core.school.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.school.domain.dto.ParentStudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentCountDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 学校-学生表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface StudentMapper extends BaseMapper<Student> {

    IPage<StudentDTO> getStudentListByCondition(@Param("page") Page<?> page,
                                                @Param("sno") String sno, @Param("idCard") String idCard,
                                                @Param("name") String name, @Param("parentPhone") String parentPhone,
                                                @Param("gender") Integer gender, @Param("gradeIds") List<Integer> gradeIds,
                                                @Param("visionLabels") List<Integer> visionLabels, @Param("startScreeningTime") Date startScreeningTime,
                                                @Param("endScreeningTime") Date endScreeningTime, @Param("schoolName") String schoolName);

    List<Student> getBy(StudentQueryDTO query);

    IPage<Student> getByPage(@Param("page") Page<?> page, @Param("StudentQueryDTO") StudentQueryDTO StudentQueryDTO);

    StudentDTO getStudentById(@Param("id") Integer id);

    List<StudentDTO> getByOtherId(@Param("schoolId") Integer schoolId, @Param("classId") Integer classId, @Param("gradeId") Integer gradeId);

    List<StudentCountDTO> countStudentBySchoolNo();

    Student getByIdCard(@Param("idCard") String idCard);

    Student getByIdCardAndName(@Param("idCard") String idCard, @Param("name") String name);

    List<Student> getByIdsAndName(@Param("ids") List<Integer> ids, @Param("name") String name);

    List<Student> getByGradeIdAndStatus(@Param("gradeId") Integer gradeId, @Param("status") Integer status);

    List<Student> getByClassIdAndStatus(@Param("classId") Integer classId, @Param("status") Integer status);

    List<Student> getByIdCardNeIdAndStatus(@Param("cardId") String cardId, @Param("id") Integer id, @Param("status") Integer status);

    List<Student> getByIdCardsAndStatus(@Param("idCards") List<String> idCards, @Param("status") Integer status);

    List<StudentDTO> selectBySchoolDistrictIds(@Param("districtIds") List<Integer> districtIds);

    List<ParentStudentDTO> countParentStudent(@Param("studentIds") List<Integer> studentIds);
}
