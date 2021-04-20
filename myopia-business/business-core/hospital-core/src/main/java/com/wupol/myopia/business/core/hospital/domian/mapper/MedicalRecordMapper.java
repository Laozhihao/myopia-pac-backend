package com.wupol.myopia.business.core.hospital.domian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domian.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domian.query.MedicalRecordQuery;
import com.wupol.myopia.business.hospital.domain.vo.ReportAndRecordVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-检查单，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

    List<MedicalRecord> getBy(MedicalRecordQuery query);

    IPage<MedicalRecord> getByPage(@Param("page") Page<?> page, @Param("query") MedicalRecordQuery query);

    MedicalRecord getLastOneByStudentId(Integer studentId);
    MedicalRecord getTodayLastMedicalRecord(Integer hospitalId, Integer studentId);

    List<ReportAndRecordVo> getByStudentId(@Param("studentId") Integer studentId);
}
