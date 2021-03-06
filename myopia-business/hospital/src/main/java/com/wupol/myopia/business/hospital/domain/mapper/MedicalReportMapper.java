package com.wupol.myopia.business.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.hospital.domain.query.MedicalReportQuery;
import com.wupol.myopia.business.hospital.domain.vo.MedicalReportVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-报告 ，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface MedicalReportMapper extends BaseMapper<MedicalReport> {

    List<MedicalReport> getBy(MedicalReportQuery query);

    IPage<MedicalReport> getByPage(@Param("page") Page<?> page, @Param("query") MedicalReportQuery query);

    List<MedicalReportVo> getVoBy(MedicalReportQuery query);

    Integer countReportBySchoolId(@Param("studentId") Integer studentId);

    MedicalReport getLatestVisitsReport(@Param("studentId") Integer studentId);
}
