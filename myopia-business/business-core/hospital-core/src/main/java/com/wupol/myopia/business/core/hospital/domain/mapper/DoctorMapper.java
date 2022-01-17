package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domain.dto.DoctorDTO;
import com.wupol.myopia.business.core.hospital.domain.model.Doctor;
import com.wupol.myopia.business.core.hospital.domain.query.DoctorQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 医院-医生，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface DoctorMapper extends BaseMapper<Doctor> {

    List<Doctor> getBy(DoctorQuery query);

    List<DoctorDTO> getDoctorVoList(DoctorQuery query);

    IPage<DoctorDTO> getByPage(@Param("page") Page<?> page, @Param("query") DoctorQuery query);

    DoctorDTO getById(@Param("id") Integer id);

    DoctorDTO getByUserId(@Param("userId") Integer userId);

    List<DoctorDTO> getAll();

    List<Doctor> getDoctorNameByIds(@Param("ids") Set<Integer> doctorIds);

    List<Doctor> getDoctorIdByName(@Param("hospitalId") Integer hospitalId, @Param("name") String name);

}
