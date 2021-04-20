package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.SchoolGradeItems;
import com.wupol.myopia.business.management.domain.dto.StudentClazzDTO;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.query.SchoolGradeQuery;
import com.wupol.myopia.business.management.domain.vo.SchoolGradeExportVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校-年级表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolGradeMapper extends BaseMapper<SchoolGrade> {
    List<SchoolGrade> getByIds(@Param("ids") List<Integer> ids);

    IPage<SchoolGradeItems> getGradeBySchool(@Param("page") Page<?> page,
                                             @Param("schoolId") Integer schoolId);

    List<SchoolGrade> getBy(SchoolGradeQuery query);

    IPage<SchoolGrade> getByPage(@Param("page") Page<?> page, @Param("query") SchoolGradeQuery query);

    List<SchoolGradeExportVO> getBySchoolIds(@Param("ids") List<Integer> ids);

    List<SchoolGradeItems> getAllBySchoolId(@Param("schoolId") Integer schoolId);

    /**
     * 通过学校ID和年级id查找
     *
     * @param schoolId
     * @param gradeName
     * @param clazzName
     * @return
     */
    StudentClazzDTO selectListBySchoolIdAndGradeId(Integer schoolId, String gradeName, String clazzName);

    List<SchoolGrade> getBySchoolId(@Param("schoolId") Integer schoolId);

    Integer countBySchoolIdAndCode(@Param("schoolId") Integer schoolId, @Param("code") String code);

}
