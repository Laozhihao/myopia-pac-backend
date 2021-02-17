package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.dto.ScreeningResultSearchDTO;
import com.wupol.myopia.business.management.domain.dto.StudentScreeningInfoWithResultDTO;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 参与筛查计划的学生表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningPlanSchoolStudentMapper extends BaseMapper<ScreeningPlanSchoolStudent> {

    List<StudentScreeningInfoWithResultDTO> selectStudentInfoWithResult(@Param("data") ScreeningResultSearchDTO screeningResultSearchDTO);
}
