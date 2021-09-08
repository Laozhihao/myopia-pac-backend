package com.wupol.myopia.business.core.school.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校-班级表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolClassMapper extends BaseMapper<SchoolClass> {

    List<SchoolClass> getBySchoolNameAndGradeName(String schoolName, String gradeName);

    List<SchoolClassExportDTO> getByGradeIds(@Param("ids") List<Integer> ids);

    List<SchoolClassDTO> selectVoList(@Param("param") SchoolClass schoolClass);

    List<SchoolClass> getByGradeIdAndStatus(@Param("gradeId") Integer gradeId, @Param("status") Integer status);

    List<SchoolClassDTO> getByGradeIdsAndSchoolIdAndStatus(@Param("gradeIds") List<Integer> gradeIds,
                                                        @Param("schoolId") Integer schoolId,
                                                        @Param("status") Integer status);
}
