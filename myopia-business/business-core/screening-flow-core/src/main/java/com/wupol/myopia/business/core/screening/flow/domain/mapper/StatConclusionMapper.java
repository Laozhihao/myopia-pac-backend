package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
     *
     * @param query 查询条件
     * @return
     */
    List<StatConclusion> listByQuery(StatConclusionQueryDTO query);

    /**
     * 根据筛查计划ID获取Vo列表
     *
     * @param screeningPlanId
     * @return
     */
    List<StatConclusionDTO> selectVoByScreeningPlanId(@Param("screeningPlanId") Integer screeningPlanId);

    List<StatConclusionExportDTO> selectExportVoByScreeningNoticeIdAndDistrictIds(@Param("screeningNoticeId") Integer screeningNoticeId, @Param("districtIds") List<Integer> districtIds);

    List<StatConclusionExportDTO> selectExportVoByScreeningNoticeIdAndSchoolId(@Param("screeningNoticeId") Integer screeningNoticeId, @Param("schoolId") Integer schoolId,@Param("planId") Integer planId);

    List<StatConclusionExportDTO> selectExportVoByScreeningPlanIdAndSchoolId(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId);
    /**
    * @Description: 参考学校
    * @Param: [screeningPlanId, schoolId, gradeId, classId]
    * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO>
    * @Author: 钓猫的小鱼
    * @Date: 2021/12/31
    */
    List<StatConclusionExportDTO> selectExportVoByScreeningPlanIdAndSchoolIdAndGradeIdAndClassId(@Param("screeningPlanId") Integer screeningPlanId, @Param("schoolId") Integer schoolId, @Param("gradeId") Integer gradeId,@Param("classId") Integer classId);

    List<StatConclusionReportDTO> selectReportVoByQuery(
            @Param("screeningNoticeId") Integer screeningNoticeId,
            @Param("planId") Integer planId,
            @Param("schoolId") Integer schoolId);

    List<StatConclusionExportDTO> selectExportVoByScreeningNoticeIdAndScreeningOrgId(@Param("screeningNoticeId") Integer screeningNoticeId, @Param("screeningOrgId") Integer screeningOrgId);

    List<StatConclusionExportDTO> selectExportVoByScreeningPlanIdAndScreeningOrgId(@Param("screeningPlanId") Integer screeningPlanId, @Param("screeningOrgId") Integer screeningOrgId);

    /**
    * @Description: 查询学校、班级、年级 筛查数据
    * @Param: [screeningPlanId, screeningOrgId, 学校ID, 年级名称, 班级名称]
    * @return: java.util.List<com.wupol.myopia.business.core.screening.flow.domain.dto.StatConclusionExportDTO>
    * @Author: 钓猫的小鱼
    * @Date: 2021/12/30
    */
    List<StatConclusionExportDTO> selectExportVoBySPlanIdAndSOrgIdAndSChoolIdAndGradeNameAndClassanme(@Param("screeningPlanId") Integer screeningPlanId,
                                                                                                                      @Param("screeningOrgId") Integer screeningOrgId,
                                                                                                                      @Param("schoolId") Integer schoolId,
                                                                                                                      @Param("gradeId") Integer gradeId,
                                                                                                                      @Param("classId") Integer classId);


    /**
     * 根据筛查通知ID获取学校ID
     *
     * @param noticeId    筛查通知ID
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
    List<Integer> selectSchoolIdByPlanId(@Param("planId") Integer planId);

    /**
     * 获取指定时间内进行
     *
     * @param date
     * @param isRescreen
     * @return
     */
    List<ScreenPlanSchoolDTO> getPlanSchoolByDate(@Param("date") Date date, @Param("isRescreen") Boolean isRescreen);

    /**
     * 获取下一条筛查统计
     *
     * @param statConclusionId 表ID
     * @param studentId        学校ID
     * @return com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion
     **/
    StatConclusion getNextScreeningStat(@Param("statConclusionId") Integer statConclusionId, @Param("studentId") Integer studentId);

    List<StatConclusion> getByResultIds(@Param("resultIds") List<Integer> resultIds);

    List<StatConclusion> getByDate(@Param("start") Date start, @Param("end") Date end);

    IPage<StudentTrackWarningResponseDTO> getTrackList(@Param("page") Page<?> page, @Param("requestDTO") StudentTrackWarningRequestDTO requestDTO, @Param("schoolId") Integer schoolId);

    StatConclusion getByPlanStudentId(@Param("planStudentId") Integer planStudentId);
}
