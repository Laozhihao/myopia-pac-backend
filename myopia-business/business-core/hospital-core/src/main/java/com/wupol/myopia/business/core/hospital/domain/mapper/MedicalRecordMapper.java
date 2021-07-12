package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecordDate;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

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

    List<ReportAndRecordDO> getByStudentId(@Param("studentId") Integer studentId);
    List<MedicalRecordDate> getMedicalRecordDateList(Integer hospitalId, Integer studentId);

    /**
     * 批量查询特定条件下,学生是否存在筛查记录
     * @param medicalRecordQueries 查询条件集,请在使用前确保集合里的每个对象,每个参数都不能为空
     * @return 返回存在的学生id
     */
    Set<Integer> selectBatchQuerys(Set<MedicalRecordQuery> medicalRecordQueries);
}
