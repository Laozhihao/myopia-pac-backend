package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-员工表Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface HospitalAdminMapper extends BaseMapper<HospitalAdmin> {

    HospitalAdmin getByHospitalId(@Param("hospitalId") Integer hospitalId);

    List<HospitalAdmin> getHospitalAdminByOrgIds(@Param("orgIds") List<Integer> orgIds);

    List<HospitalAdmin> getByHospitalIds(@Param("hospitalIds") List<Integer> hospitalIds);

}
