package com.wupol.myopia.business.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.hospital.domain.model.Consultation;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-学生问诊，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface ConsultationMapper extends BaseMapper<Consultation> {


    List<Consultation> getBy(Consultation query);

    IPage<Consultation> getByPage(@Param("page") Page<?> page, @Param("query") Consultation query);

}
