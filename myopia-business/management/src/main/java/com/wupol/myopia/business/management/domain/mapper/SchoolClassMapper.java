package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import com.wupol.myopia.business.management.domain.vo.SchoolClassExportVO;
import com.wupol.myopia.business.management.domain.vo.SchoolClassVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校-班级表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolClassMapper extends BaseMapper<SchoolClass> {

    List<SchoolClass> getBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId);

    List<SchoolClassExportVO> getByGradeIds(@Param("ids") List<Integer> ids);

    List<SchoolClassVo> selectVoList(@Param("param") SchoolClass schoolClass);
}
