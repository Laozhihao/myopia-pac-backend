package com.wupol.myopia.business.core.school.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学校端-学生表Mapper接口
 *
 * @author Simple4H
 */
public interface SchoolStudentMapper extends BaseMapper<SchoolStudent> {

    IPage<SchoolStudentListResponseDTO> getList(@Param("page") Page<?> page, @Param("requestDTO") SchoolStudentRequestDTO requestDTO);

    List<SchoolStudent> getByIdCardAndSno(@Param("id") Integer id, @Param("idCard") String idCard, @Param("sno") String sno);


}
