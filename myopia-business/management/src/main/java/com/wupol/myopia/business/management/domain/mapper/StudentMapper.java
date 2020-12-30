package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.model.Student;
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

    IPage<Student> getStudentListByCondition(@Param("page") Page<?> page, @Param("schoolId") Integer schoolId,
                                             @Param("sno") Integer sno, @Param("idCard") String idCard,
                                             @Param("name") String name, @Param("parentPhone") String parentPhone,
                                             @Param("gender") Integer gender, @Param("gradeIds") List<Integer> gradeIds,
                                             @Param("labels") String labels, @Param("startScreeningTime") Date startScreeningTime,
                                             @Param("endScreeningTime") Date endScreeningTime);

}
