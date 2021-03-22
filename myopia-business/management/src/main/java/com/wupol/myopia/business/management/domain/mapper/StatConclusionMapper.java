package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;
import com.wupol.myopia.business.management.domain.vo.StatConclusionExportVo;
import com.wupol.myopia.business.management.domain.vo.StatConclusionReportVo;
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
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
    StatConclusion selectLastOne(StatConclusionQuery query);

    /**
     * 获取统计结论数据
     * @param noticeId 政府通知ID
     * @param districtIds 行政区域ID列表
     * @return
     */
    List<StatConclusion> listByQuery(StatConclusionQuery query);

    /**
     * 根据筛查计划ID获取Vo列表
     * @param screeningPlanId
     * @return
     */
    List<StatConclusionVo> selectVoByScreeningPlanId(@Param("screeningPlanId") Integer screeningPlanId);

    List<StatConclusionExportVo> selectExportVoByScreeningNoticeIdAndDistrictIds(@Param("screeningNoticeId") Integer screeningNoticeId,@Param("districtIds") List<Integer> districtIds);

    List<StatConclusionExportVo> selectExportVoByScreeningNoticeIdAndSchoolId(@Param("screeningNoticeId") Integer screeningNoticeId,@Param("schoolId") Integer schoolId);

    List<StatConclusionExportVo> selectExportVoByScreeningPlanIdAndSchoolId(@Param("screeningPlanId") Integer screeningPlanId,@Param("schoolId") Integer schoolId);

    List<StatConclusionReportVo> selectReportVoByScreeningNoticeIdAndSchoolId(
            @Param("screeningNoticeId") Integer screeningNoticeId,
            @Param("schoolId") Integer schoolId);

    List<StatConclusionExportVo> selectExportVoByScreeningNoticeIdAndScreeningOrgId(@Param("screeningNoticeId") Integer screeningNoticeId,@Param("screeningOrgId") Integer screeningOrgId);

    List<StatConclusionExportVo> selectExportVoByScreeningPlanIdAndScreeningOrgId(@Param("screeningPlanId") Integer screeningPlanId,@Param("screeningOrgId") Integer screeningOrgId);
}
