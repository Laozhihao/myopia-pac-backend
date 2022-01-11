package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domain.dos.HospitalStudentDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.query.HospitalStudentQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-学生，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface HospitalStudentMapper extends BaseMapper<HospitalStudent> {

    List<HospitalStudent> getBy(HospitalStudentQuery query);

    IPage<HospitalStudent> getByPage(@Param("page") Page<?> page, @Param("query") HospitalStudentQuery query);

    List<HospitalStudentDO> getHospitalStudentDoList(HospitalStudentQuery query);

    IPage<HospitalStudentResponseDTO> getByList(@Param("page") Page<?> page, @Param("requestDTO") HospitalStudentRequestDTO requestDTO);

    void deletedById(@Param("id") Integer id);

    HospitalStudentResponseDTO getByHospitalStudentId(@Param("id") Integer id);

    List<HospitalStudent> getByStudentId(@Param("studentId") Integer studentId);

    List<HospitalStudent> getPreschoolByStudentType();
}
