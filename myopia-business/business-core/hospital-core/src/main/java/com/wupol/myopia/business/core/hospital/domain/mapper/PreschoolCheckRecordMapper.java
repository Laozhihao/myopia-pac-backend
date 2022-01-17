package com.wupol.myopia.business.core.hospital.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.hospital.domain.dto.EyeHealthyReportResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.StudentPreschoolCheckDTO;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 筛查结果表Mapper接口
 *
 * @Author wulizhou
 * @Date 2022-01-04
 */
public interface PreschoolCheckRecordMapper extends BaseMapper<PreschoolCheckRecord> {

    /**
     * 获取眼保健详情
     *
     * @param id
     * @return
     */
    PreschoolCheckRecordDTO getDetail(@Param("id") Integer id);

    /**
     * 获取眼保健列表
     *
     * @param page
     * @param query
     * @return
     */
    IPage<PreschoolCheckRecordDTO> getListByCondition(@Param("page") Page<?> page, @Param("query") PreschoolCheckRecordQuery query);


    PreschoolCheckRecord getByOne(PreschoolCheckRecordQuery query);
    /**
     * 通过学生Id获取列表
     *
     * @param studentId 学生Id
     * @return List<EyeHealthyReportResponseDTO>
     */
    List<EyeHealthyReportResponseDTO> getByStudentId(@Param("studentId") Integer studentId);

    List<PreschoolCheckRecord> getByStudentIds(@Param("studentIds") List<Integer> studentIds);

    /**
     * 通过学生获取学生月龄检查数
     * @param studentIds
     * @return
     */
    List<StudentPreschoolCheckDTO> getStudentCheckCount(@Param("studentIds") List<Integer> studentIds);

}
