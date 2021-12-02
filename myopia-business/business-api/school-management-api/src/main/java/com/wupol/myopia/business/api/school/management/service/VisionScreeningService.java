package com.wupol.myopia.business.api.school.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentTrackWarningRequestDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentTrackWarningResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.organization.domain.dto.ScreeningOrgResponseDTO;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.business.core.stat.domain.model.SchoolMonitorStatistic;
import com.wupol.myopia.business.core.stat.service.SchoolMonitorStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校端-视力筛查
 *
 * @author Simple4H
 */
@Service
public class VisionScreeningService {

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private ScreeningPlanService screeningPlanService;

    @Resource
    private SchoolMonitorStatisticService schoolMonitorStatisticService;

    @Resource
    private StatConclusionService statConclusionService;

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private ScreeningOrganizationService screeningOrganizationService;

    @Resource
    private ResourceFileService resourceFileService;

    @Resource
    private MedicalReportService medicalReportService;

    @Resource
    private SchoolService schoolService;

    /**
     * 获取视力筛查列表
     *
     * @param pageRequest 分页请求
     * @param schoolId    学校Id
     * @return IPage<ScreeningListResponseDTO>
     */
    public IPage<ScreeningListResponseDTO> getList(PageRequest pageRequest, Integer schoolId) {
        IPage<ScreeningListResponseDTO> responseDTO = screeningPlanSchoolService.getResponseBySchoolId(pageRequest, schoolId);
        List<ScreeningListResponseDTO> schoolPlanList = responseDTO.getRecords();

        // 获取筛查计划
        List<Integer> planIds = schoolPlanList.stream().map(ScreeningListResponseDTO::getPlanId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(planIds)) {
            return responseDTO;
        }
        List<ScreeningPlan> screeningPlans = screeningPlanService.getByIds(planIds);
        Map<Integer, ScreeningPlan> planMap = screeningPlans.stream().collect(Collectors.toMap(ScreeningPlan::getId, Function.identity()));

        // 获取统计信息
        List<SchoolMonitorStatistic> statisticList = schoolMonitorStatisticService.getBySchoolId(schoolId);
        Map<Integer, SchoolMonitorStatistic> schoolStatisticMap = statisticList.stream()
                .collect(Collectors.toMap(SchoolMonitorStatistic::getScreeningPlanId, Function.identity(), (o, n) -> o));

        // 筛查机构
        List<Integer> orgIds = schoolPlanList.stream().map(ScreeningListResponseDTO::getScreeningOrgId).collect(Collectors.toList());
        List<ScreeningOrgResponseDTO> screeningOrgDetails = screeningOrganizationService.getScreeningOrgDetails(orgIds);
        Map<Integer, String> orgMap = screeningOrgDetails.stream().collect(Collectors
                .toMap(ScreeningOrganization::getId, ScreeningOrganization::getName));

        // 获取学校告知书
        School school = schoolService.getBySchoolId(schoolId);

        schoolPlanList.forEach(schoolPlan -> {
            ScreeningPlan screeningPlan = planMap.get(schoolPlan.getPlanId());
            if (Objects.nonNull(screeningPlan)) {
                schoolPlan.setTitle(screeningPlan.getTitle());
                schoolPlan.setStartTime(screeningPlan.getStartTime());
                schoolPlan.setEndTime(screeningPlan.getEndTime());
                schoolPlan.setReleaseStatus(screeningOrganizationService.getScreeningStatus(screeningPlan.getStartTime(), screeningPlan.getEndTime()));
                schoolPlan.setReleaseTime(screeningPlan.getReleaseTime());
                schoolPlan.setPlanScreeningNumbers(screeningPlan.getStudentNumbers());
                schoolPlan.setContent(screeningPlan.getContent());
            }

            SchoolMonitorStatistic schoolMonitorStatistic = schoolStatisticMap.get(schoolPlan.getPlanId());
            if (Objects.nonNull(schoolMonitorStatistic)) {
                schoolPlan.setSchoolStatisticId(schoolMonitorStatistic.getId());
                schoolPlan.setRealScreeningNumbers(schoolMonitorStatistic.getRealScreeningNumbers());
            } else {
                schoolPlan.setRealScreeningNumbers(0);
            }

            schoolPlan.setScreeningOrgName(orgMap.get(schoolPlan.getScreeningOrgId()));

            // 设置告知书配置
            NotificationConfig notificationConfig = school.getNotificationConfig();
            if (Objects.nonNull(notificationConfig)) {
                schoolPlan.setNotificationConfig(notificationConfig);
                // 设置图片
                Integer qrCodeFileId = notificationConfig.getQrCodeFileId();
                if (Objects.nonNull(qrCodeFileId)) {
                    schoolPlan.setQrCodeFileUrl(resourceFileService.getResourcePath(qrCodeFileId));
                }
            } else {
                notificationConfig = new NotificationConfig();
                notificationConfig.setTitle("学生视力筛查告家长书");
                notificationConfig.setCall("亲爱的家长");
                notificationConfig.setContent("您好！");
                notificationConfig.setSubTitle(school.getName());
                schoolPlan.setNotificationConfig(notificationConfig);
            }
        });
        return responseDTO;
    }

    /**
     * 获取学生跟踪预警列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  入参
     * @param schoolId    学校Id
     * @return IPage<StudentTrackWarningResponseDTO>
     */
    public IPage<StudentTrackWarningResponseDTO> getTrackList(PageRequest pageRequest, StudentTrackWarningRequestDTO requestDTO, Integer schoolId) {
        IPage<StudentTrackWarningResponseDTO> responseDTO = statConclusionService.getTrackList(pageRequest, requestDTO, schoolId);
        List<StudentTrackWarningResponseDTO> trackList = responseDTO.getRecords();
        if (CollectionUtils.isEmpty(trackList)) {
            return responseDTO;
        }
        List<Integer> studentIds = trackList.stream().map(StudentTrackWarningResponseDTO::getStudentId).collect(Collectors.toList());
        List<Integer> reportIds = trackList.stream().map(StudentTrackWarningResponseDTO::getReportId).collect(Collectors.toList());

        Map<Integer, MedicalReport> reportMap = medicalReportService.getByIds(reportIds).stream()
                .collect(Collectors.toMap(MedicalReport::getId, Function.identity()));

        // 学校端学生
        Map<Integer, Integer> schoolStudentMap = schoolStudentService.getByStudentIds(studentIds).stream()
                .collect(Collectors.toMap(SchoolStudent::getStudentId, SchoolStudent::getId));

        trackList.forEach(track -> {
            track.setSchoolStudentId(schoolStudentMap.get(track.getStudentId()));
            track.setIsBindMp(track.getIsBindMp());
            if (Objects.nonNull(track.getReportId())) {
                MedicalReport report = reportMap.get(track.getReportId());
                track.setIsReview(true);
                track.setVisitResult(report.getMedicalContent());
                track.setGlassesSuggest(report.getGlassesSituation());
            }
        });
        return responseDTO;
    }
}
