package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentTrackWarningRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentTrackWarningResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 视力筛查
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/vision/screening")
public class VisionScreeningController {

    @Resource
    private VisionScreeningService visionScreeningService;

    @Resource
    private SchoolVisionStatisticService schoolVisionStatisticService;

    @Resource
    private ScreeningPlanSchoolStudentFacadeService screeningPlanSchoolStudentFacadeService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    /**
     * 获取学校计划
     *
     * @param pageRequest 分页请求
     * @return IPage<ScreeningListResponseDTO>
     */
    @GetMapping("list")
    public IPage<ScreeningListResponseDTO> getList(PageRequest pageRequest) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return visionScreeningService.getList(pageRequest, currentUser.getOrgId());
    }

    /**
     * 获取结果统计分析
     *
     * @param schoolStatisticId 结果统计
     * @return SchoolVisionStatistic
     */
    @GetMapping("{schoolStatisticId}")
    public SchoolVisionStatistic getSchoolStatistic(@PathVariable("schoolStatisticId") Integer schoolStatisticId) {
        return schoolVisionStatisticService.getById(schoolStatisticId);
    }

    /**
     * 获取计划学校的年级情况
     *
     * @param screeningPlanId 计划ID
     * @return List<SchoolGradeVo>
     */
    @GetMapping("grades/{screeningPlanId}")
    public List<SchoolGradeVO> queryGradesInfo(@PathVariable Integer screeningPlanId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return screeningPlanSchoolStudentFacadeService.getSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, currentUser.getOrgId());
    }

    /**
     * 获取学生跟踪预警列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  入参
     * @return IPage<StudentTrackWarningResponseDTO>
     */
    @GetMapping("statStudents/list")
    public IPage<StudentTrackWarningResponseDTO> queryStudentInfos(PageRequest pageRequest, @Valid StudentTrackWarningRequestDTO requestDTO) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return visionScreeningService.getTrackList(pageRequest, requestDTO, currentUser.getOrgId());
    }

    @GetMapping("/plan/{screeningPlanId}")
    public ScreeningPlan getPlanInfo(@PathVariable("screeningPlanId") Integer screeningPlanId) {
        return screeningPlanService.getById(screeningPlanId);
    }
}
