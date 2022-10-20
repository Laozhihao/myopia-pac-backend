package com.wupol.myopia.business.core.screening.organization.domain.mapper;

import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewSchool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 总览机构学校关联表Mapper接口
 *
 * @Author Simple4H
 * @Date 2022-09-26
 */
public interface OverviewSchoolMapper extends BaseMapper<OverviewSchool> {

    /**
     * 批量插入总览机构医院绑定信息
     * @param overviewId 区域Id
     * @param schoolIds 学校Id
     * @return
     */
    int batchSave(@Param("overviewId") Integer overviewId, @Param("schoolIds") List<Integer> schoolIds);

    List<OverviewSchool> getListByOverviewIds(@Param("overviewIds") List<Integer> overviewIds);

}
