package com.wupol.myopia.business.core.school.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.school.domain.model.SchoolStaff;
import org.apache.ibatis.annotations.Param;

/**
 * 学校员工表Mapper接口
 *
 * @author Simple4H
 */
public interface SchoolStaffMapper extends BaseMapper<SchoolStaff> {

    /**
     * 获取学校员工
     *
     * @param page     分页
     * @param schoolId 学校Id
     *
     * @return IPage<SchoolStaff>
     */
    IPage<SchoolStaff> getSchoolStaff(@Param("page") Page<?> page, @Param("schoolId") Integer schoolId);

}
