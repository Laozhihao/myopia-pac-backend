package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.SchoolClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.SchoolGrade;

import java.util.List;

/**
 * 学校-班级表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolClassMapper extends BaseMapper<SchoolClass> {
    List<SchoolClass> getByIds(List<Integer> ids);
    List<SchoolClass> getBySchoolNameAndGradeName(String schoolName, String gradeName, Integer deptId);

}
