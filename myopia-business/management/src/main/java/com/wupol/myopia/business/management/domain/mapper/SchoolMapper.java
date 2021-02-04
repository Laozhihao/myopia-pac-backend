package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.management.domain.model.School;
import com.wupol.myopia.business.management.domain.query.SchoolQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolMapper extends BaseMapper<School> {

    IPage<SchoolResponseDTO> getSchoolListByCondition(@Param("page") Page<?> page, @Param("name") String name,
                                                      @Param("schoolNo") String schoolNo, @Param("type") Integer type,
                                                      @Param("districtId") Integer districtId, @Param("userIds") List<Integer> userIds);

    School getLastSchoolByNo(@Param("code") Integer code);

    List<School> getBy(SchoolQuery query);

    School getLastSchoolByNo(@Param("code") Long code);

    IPage<School> getByPage(@Param("page") Page<?> page, @Param("schoolQuery") SchoolQuery schoolQuery);
}
