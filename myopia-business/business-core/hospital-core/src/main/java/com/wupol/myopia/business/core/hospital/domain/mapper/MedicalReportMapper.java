package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domain.dos.MedicalReportDO;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalReportRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalReportQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医院-报告 ，Mapper接口
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
public interface MedicalReportMapper extends BaseMapper<MedicalReport> {

    List<MedicalReport> getMedicalReportList(MedicalReportQuery query);

    IPage<MedicalReport> getByPage(@Param("page") Page<?> page, @Param("query") MedicalReportQuery query);

    List<MedicalReportDO> getMedicalReportDoList(MedicalReportQuery query);

    Integer countReportBySchoolId(@Param("studentId") Integer studentId);

    MedicalReport getLastOneByStudentId(@Param("studentId") Integer studentId);

    MedicalReport getLastOne(MedicalReportQuery query);

    MedicalReportDO getTodayLastMedicalReportDO(Integer hospitalId, Integer studentId);

    MedicalReport getTodayLastMedicalReport(Integer hospitalId, Integer studentId);

    IPage<ReportAndRecordDO> getByStudentIdWithPage(@Param("page") Page<?> page, @Param("studentId") Integer studentId);

    List<ReportAndRecordDO> getStudentId(@Param("studentId") Integer studentId);

    List<ReportAndRecordDO> getByStudentIds(@Param("studentIds") List<Integer> studentIds);

    List<MedicalReport> getInconclusiveReportList();

    List<MedicalReport> getByIds(@Param("ids") List<Integer> ids);

    IPage<ReportAndRecordDO> getByHospitalId(@Param("page") Page<?> page, @Param("requestDTO") HospitalReportRequestDTO requestDTO);

}
