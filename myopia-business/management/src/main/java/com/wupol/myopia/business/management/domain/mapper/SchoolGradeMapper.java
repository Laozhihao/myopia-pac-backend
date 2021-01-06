package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.dto.SchoolGradeItems;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;
import org.apache.ibatis.annotations.Param;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;

import java.util.List;

/**
 * 学校-年级表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolGradeMapper extends BaseMapper<SchoolGrade> {
    List<SchoolGrade> getByIds(List<Integer> ids);

    IPage<SchoolGradeItems> getGradeBySchool(@Param("page") Page<?> page,
                                             @Param("schoolId") Integer schoolId);

}
