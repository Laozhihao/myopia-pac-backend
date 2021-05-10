package com.wupol.myopia.business.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.hospital.domain.query.HospitalStudentQuery;
import com.wupol.myopia.business.hospital.domain.vo.HospitalStudentVo;
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
    List<HospitalStudentVo> getHospitalStudentVoList(HospitalStudentQuery query);

    IPage<HospitalStudent> getByPage(@Param("page") Page<?> page, @Param("query") HospitalStudentQuery query);

}
