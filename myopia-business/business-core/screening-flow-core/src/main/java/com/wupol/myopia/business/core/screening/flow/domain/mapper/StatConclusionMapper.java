package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionReportDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查数据结论Mapper接口
 *
 * @Author Jacob
 * @Date 2021-02-22
 */
public interface StatConclusionMapper extends BaseMapper<StatConclusion> {
    /**
     * 获取行政区域内最后一条数据
     */
    StatConclusion selectLastOne(StatConclusionQueryDTO query);

    /**
     * 获取统计结论数据
     * @param noticeId 政府通知ID
     * @param districtIds 行政区域ID列表
     * @return
     */
    List<StatConclusion> listByQuery(StatConclusionQueryDTO query);

    /**
     * 根据筛查计划ID获取Vo列表
     * @param screeningPlanId
     * @return
     */
    List<StatConclusionDTO> selectVoByScreeningPlanId(@Param("screeningPlanId") Integer screeningPlanId);

    List<StatConclusionExportDTO> selectExportVoByScreeningNoticeIdAndDistrictIds(@Param("screeningNoticeId") Integer screeningNoticeId, @Param("districtIds") List<Integer> districtIds);

    List<StatConclusionExportDTO> selectExportVoByScreeningNoticeIdAndSchoolId(@Param("screeningNoticeId") Integer screeningNoticeId, @Param("schoolId") Integer schoolId);

    List<StatConclusionExportDTO> selectExportVoByScreeningPlanIdAndSchoolId(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId);

    List<StatConclusionReportDTO> selectReportVoByQuery(
            @Param("screeningNoticeId") Integer screeningNoticeId,
            @Param("planId") Integer planId,
            @Param("schoolId") Integer schoolId);

    List<StatConclusionExportDTO> selectExportVoByScreeningNoticeIdAndScreeningOrgId(@Param("screeningNoticeId") Integer screeningNoticeId, @Param("screeningOrgId") Integer screeningOrgId);

    List<StatConclusionExportDTO> selectExportVoByScreeningPlanIdAndScreeningOrgId(@Param("screeningPlanId") Integer screeningPlanId, @Param("screeningOrgId") Integer screeningOrgId);

    /**
     * 根据筛查通知ID获取学校ID
     *
     * @param noticeId 筛查通知ID
     * @param districtIds 行政区域ID集
     * @return java.util.List<java.lang.Integer>
     **/
    List<Integer> selectSchoolIdsByScreeningNoticeIdAndDistrictIds(@Param("screeningNoticeId") Integer noticeId, @Param("districtIds") List<Integer> districtIds);

    /**
     * 根据筛查计划ID获取学校ID
     *
     * @param planId 筛查计划ID
     * @return java.util.List<java.lang.Integer>
     **/
    List<Integer> selectSchoolIdByPlanId(Integer planId);
}