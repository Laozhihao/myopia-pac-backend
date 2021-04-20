package com.wupol.myopia.business.core.hospital.domian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domian.model.Doctor;
import com.wupol.myopia.business.core.hospital.domian.query.DoctorQuery;
import com.wupol.myopia.business.hospital.domain.vo.DoctorVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-医生，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface DoctorMapper extends BaseMapper<Doctor> {


    List<Doctor> getBy(DoctorQuery query);
    List<DoctorVo> getDoctorVoList(DoctorQuery query);

    IPage<Doctor> getByPage(@Param("page") Page<?> page, @Param("query") Doctor query);

}
