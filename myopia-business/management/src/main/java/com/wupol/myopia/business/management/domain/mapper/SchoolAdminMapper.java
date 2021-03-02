package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.SchoolAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校-员工表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface SchoolAdminMapper extends BaseMapper<SchoolAdmin> {

    List<SchoolAdmin> getBySchoolIds(@Param("schoolIds") List<Integer> schoolIds);

}
