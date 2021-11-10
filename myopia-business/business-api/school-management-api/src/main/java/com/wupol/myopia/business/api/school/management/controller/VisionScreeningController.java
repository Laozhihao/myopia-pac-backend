package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.aggregation.screening.service.ScreeningPlanSchoolStudentFacadeService;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.api.school.management.service.VisionScreeningService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolVisionStatisticService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private VisionScreeningBizService visionScreeningBizService;

    @Resource
    private StatConclusionService statConclusionService;

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
     * 学生预警跟踪
     *
     * @param query 查询参数
     * @param page  分页数据
     * @return IPage<StudentDTO>
     */
    @GetMapping("statStudents/list")
    public IPage<ScreeningStudentDTO> queryStudentInfos(PageRequest page, ScreeningStudentQueryDTO query) {
        return screeningPlanSchoolStudentFacadeService.getPage(query, page);
    }
}
