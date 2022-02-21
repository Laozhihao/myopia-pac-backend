package com.wupol.myopia.business.core.screening.organization.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.organization.domain.model.OverviewHospital;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 总览机构医院关联表Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-02-17
 */
public interface OverviewHospitalMapper extends BaseMapper<OverviewHospital> {

    /**
     * 批量插入总览机构医院绑定信息
     * @param overviewId
     * @param hospitalIds
     * @return
     */
    int batchSave(@Param("overviewId") Integer overviewId, @Param("hospitalIds") List<Integer> hospitalIds);


}
