package com.wupol.myopia.business.core.school.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.common.utils.domain.dto.StatusRequest;
import com.wupol.myopia.business.core.school.domain.dos.SimpleSchoolDO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.vo.SchoolGradeClassVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 学校表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolMapper extends BaseMapper<School> {

    IPage<SchoolResponseDTO> getSchoolListByCondition(@Param("page") Page<?> page, @Param("query") SchoolQueryDTO query,
                                                      @Param("districtId") Integer districtId, @Param("districtCode") Integer districtCode,
                                                      @Param("userIds") List<Integer> userIds);

    List<School> getByQuery(SchoolQueryDTO query);
    List<SimpleSchoolDO> getSimpleSchool(SchoolQueryDTO query);

    IPage<School> getByPage(@Param("page") Page<?> page, @Param("schoolQueryDTO") SchoolQueryDTO schoolQueryDTO);

    List<School> getByName(@Param("name") String name);

    List<School> getByNoNeId(@Param("schoolNo") String schoolNo, @Param("id") Integer id);

    School getBySchoolNo(@Param("schoolNo") String schoolNo);

    List<School> getBySchoolNos(@Param("schoolNos") List<String> schoolNos);

    List<School> getByDistrictId(@Param("districtId") Integer districtId);

    List<School> getByNameNeId(@Param("name") String name, @Param("id") Integer id);

    Set<Integer> selectDistrictIdsBySchoolIds(@Param("schoolIds") Set<Integer> schoolIds);

    void updateStatus(@Param("request") StatusRequest request);

    School getBySchoolId(@Param("id") Integer id);

    List<School> getByCooperationTimeAndStatus(@Param("date") Date date);

    int updateSchoolStatus(@Param("id") Integer id, @Param("targetStatus") Integer targetStatus, @Param("sourceStatus") Integer sourceStatus);

    List<School> getByCooperationEndTime(@Param("start") Date start, @Param("end") Date end);

    SchoolGradeClassVO getBySchoolIdAndGradeIdAndClassId(@Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId, @Param("classId") Integer classId);

    List<School> getListByProvinceCodeAndNameLike(@Param("name") String name, @Param("provinceDistrictCode") Long provinceDistrictCode);

    List<School> getBySchoolIds(@Param("ids") List<Integer> ids);

}
