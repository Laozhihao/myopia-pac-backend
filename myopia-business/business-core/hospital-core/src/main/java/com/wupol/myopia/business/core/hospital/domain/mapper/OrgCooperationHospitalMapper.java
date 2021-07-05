package com.wupol.myopia.business.core.hospital.domain.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domain.dto.CooperationHospitalDTO;
import com.wupol.myopia.business.core.hospital.domain.model.OrgCooperationHospital;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查机构合作医院表Mapper接口
 *
 * @author Simple4H
 */
public interface OrgCooperationHospitalMapper extends BaseMapper<OrgCooperationHospital> {

    Integer batchSaveOrgCooperationHospital(@Param("screeningOrgId") Integer screeningOrgId, @Param("hospitalIds") List<Integer> hospitalIds);

    void updateByScreeningOrgId(@Param("screeningOrgId") Integer screeningOrgId);

    IPage<CooperationHospitalDTO> getByScreeningOrgId(@Param("page") Page<?> page, @Param("screeningOrgId") Integer screeningOrgId);

    Integer countByScreeningOrgId(@Param("screeningOrgId") Integer screeningOrgId);

    Integer countByOrgIdAndHospitalIds(@Param("screeningOrgId") Integer screeningOrgId, @Param("hospitalIds") List<Integer> hospitalIds);

    Integer getSuggestHospital(@Param("screeningOrgId") Integer screeningOrgId);

    List<OrgCooperationHospital> getListByScreeningOrgId(@Param("screeningOrgId") Integer screeningOrgId);

    void deletedByHospitalId(@Param("hospitalId") Integer hospitalId);
}
