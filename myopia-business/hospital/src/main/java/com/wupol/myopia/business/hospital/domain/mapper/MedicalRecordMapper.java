package com.wupol.myopia.business.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.hospital.domain.query.MedicalRecordQuery;
import com.wupol.myopia.business.management.domain.dto.StudentDTO;
import com.wupol.myopia.business.management.domain.model.Student;
import com.wupol.myopia.business.management.domain.query.StudentQuery;
import com.wupol.myopia.business.management.domain.vo.StudentCountVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
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

}
