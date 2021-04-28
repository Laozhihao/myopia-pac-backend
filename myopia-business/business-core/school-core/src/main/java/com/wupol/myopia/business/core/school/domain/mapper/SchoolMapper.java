package com.wupol.myopia.business.core.school.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 学校表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolMapper extends BaseMapper<School> {

    IPage<SchoolResponseDTO> getSchoolListByCondition(@Param("page") Page<?> page, @Param("name") String name,
                                                      @Param("schoolNo") String schoolNo, @Param("type") Integer type,
                                                      @Param("districtId") Integer districtId, @Param("userIds") List<Integer> userIds,
                                                      @Param("districtIdPre") Integer districtIdPre);

    List<School> getByQuery(SchoolQueryDTO query);

    IPage<School> getByPage(@Param("page") Page<?> page, @Param("SchoolQueryDTO") SchoolQueryDTO SchoolQueryDTO);

    List<School> getByName(@Param("name") String name);

    List<School> getByNoNeId(@Param("schoolNo") String schoolNo, @Param("id") Integer id);

    School getBySchoolNo(@Param("schoolNo") String schoolNo);

    List<School> getBySchoolNos(@Param("schoolNos") List<String> schoolNos);

    List<School> getByDistrictId(@Param("districtId") Integer districtId);

    List<School> getByNameNeId(@Param("name") String name, @Param("id") Integer id);

    Set<Integer> selectDistrictIdsByScreeningPlanIds(@Param("screeningPlanIds") List<Integer> screeningPlanIds);

    Set<Integer> selectDistrictIdsBySchoolIds(@Param("schoolIds") Set<Integer> schoolIds);

}
