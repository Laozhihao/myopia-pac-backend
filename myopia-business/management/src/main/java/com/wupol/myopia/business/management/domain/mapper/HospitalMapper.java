package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.query.HospitalQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院表Mapper接口
 *
 * @author HaoHao
 * @since 2020-12-21
 */
public interface HospitalMapper extends BaseMapper<Hospital> {

    IPage<Hospital> getHospitalListByCondition(@Param("page") Page<?> page, @Param("govDeptId") List<Integer> govDeptId,
                                               @Param("name") String name, @Param("type") Integer type, @Param("kind") Integer kind,
                                               @Param("level") Integer level, @Param("districtId") Integer districtId,
                                               @Param("status")Integer status);

    Hospital getLastHospitalByNo(@Param("code") Integer code);

    List<Hospital> getExportData(HospitalQuery query);

    Hospital getLastHospitalByNo(@Param("code") Long code);
}
