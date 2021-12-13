package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 医院表Mapper接口
 *
 * @author HaoHao
 * @since 2020-12-21
 */
public interface HospitalMapper extends BaseMapper<Hospital> {

    IPage<HospitalResponseDTO> getHospitalListByCondition(@Param("page") Page<?> page, @Param("govDeptId") List<Integer> govDeptId,
                                                          @Param("query") HospitalQuery query);

    List<Hospital> getBy(HospitalQuery query);

    IPage<Hospital> getByPage(@Param("page") Page<?> page, @Param("hospitalQuery") HospitalQuery hospitalQuery);

    List<Hospital> getByNameNeId(@Param("name") String name, @Param("id") Integer id);

    List<HospitalResponseDTO> getHospitalByName(@Param("name") String name, @Param("codePre") Integer codePre);

    List<Hospital> getByCooperationTimeAndStatus(@Param("date") Date date, @Param("status") Integer status);

    int updateHospitalStatus(@Param("id") Integer id, @Param("targetStatus") Integer targetStatus, @Param("stopDate") Date stopDate, @Param("sourceStatus")Integer sourceStatus);

    List<Hospital> getByCooperationEndTime(@Param("date") Date date);

}
