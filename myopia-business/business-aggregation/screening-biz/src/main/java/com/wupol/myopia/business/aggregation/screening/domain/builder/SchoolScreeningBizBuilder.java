package com.wupol.myopia.business.aggregation.screening.domain.builder;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.aggregation.screening.constant.MergeStatusEnum;
import com.wupol.myopia.business.aggregation.screening.constant.SchoolConstant;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolStatisticVO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.ScreeningPlanVO;
import com.wupol.myopia.business.aggregation.screening.domain.vos.ScreeningStudentListVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalReport;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningBizTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningListResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentTrackWarningResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查业务
 *
 * @author hang.yuan 2022/9/25 15:11
 */
@UtilityClass
public class SchoolScreeningBizBuilder {

    /**
     * 获取筛查状态
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param releaseStatus   计划状态
     * @param screeningResultNum   筛查结果数
     * @return 筛查状态 0-未开始 1-进行中 2-已结束
     */
    public Integer getScreeningStatus(Date startDate, Date endDate, Integer releaseStatus , Integer screeningResultNum) {
        if (CommonConst.STATUS_ABOLISH.equals(releaseStatus)) {
            return 3;
        }
        Date nowDate = new Date();
        if (nowDate.before(startDate)) {
            return 0;
        }
        if (nowDate.after(startDate) && nowDate.before(endDate) && screeningResultNum > 0) {
            return 1;
        }
        if (nowDate.after(endDate)) {
            return 2;
        }
        return 0;
    }

    /**
     * 设置筛查计划信息
     * @param responseDTO 返回对象
     * @param planMap 筛查计划集合
     */
    public void setScreeningPlanInfo(ScreeningListResponseDTO responseDTO, Map<Integer, ScreeningPlan> planMap, Map<Integer,Integer> visionScreeningResultMap) {
        ScreeningPlan screeningPlan = planMap.get(responseDTO.getPlanId());
        if (Objects.isNull(screeningPlan)) {
            return;
        }
        responseDTO.setTitle(screeningPlan.getTitle());
        responseDTO.setStartTime(screeningPlan.getStartTime());
        responseDTO.setEndTime(screeningPlan.getEndTime());
        responseDTO.setReleaseStatus(screeningPlan.getReleaseStatus());
        responseDTO.setScreeningStatus(SchoolScreeningBizBuilder.getScreeningStatus(screeningPlan.getStartTime(), screeningPlan.getEndTime(), screeningPlan.getReleaseStatus(),visionScreeningResultMap.get(screeningPlan.getId())));
        responseDTO.setReleaseTime(screeningPlan.getReleaseTime());
        responseDTO.setContent(screeningPlan.getContent());
        responseDTO.setScreeningBizType(ScreeningBizTypeEnum.getInstanceByOrgType(responseDTO.getScreeningOrgType()).getType());
        responseDTO.setStatus(setMergeStatus(responseDTO.getReleaseStatus(),responseDTO.getScreeningStatus()));
        responseDTO.setSrcScreeningNoticeId(screeningPlan.getSrcScreeningNoticeId());
        responseDTO.setScreeningTaskId(screeningPlan.getScreeningTaskId());
    }

    /**
     * 筛查状态与发布状态合并(0-未发布,1-未开始 2-进行中 3-已结束)
     * @param releaseStatus 发布状态
     * @param screeningStatus 筛查状态
     */
    public Integer setMergeStatus(Integer releaseStatus,Integer screeningStatus) {
        if(Objects.equals(CommonConst.STATUS_NOT_RELEASE,releaseStatus)){
            return MergeStatusEnum.NOT_RELEASE.getCode();
        }
        Integer status;
        switch (screeningStatus){
            default:
            case 0:
                status = MergeStatusEnum.NOT_START.getCode();
                break;
            case 1:
                status = MergeStatusEnum.PROCESSING.getCode();
                break;
            case 2:
                status = MergeStatusEnum.END.getCode();
                break;
        }
        return status;
    }

    /**
     * 设置统计信息
     * @param responseDTO 返回对象
     * @param schoolStatisticMap 统计信息集合
     */
    public void setStatisticInfo(ScreeningListResponseDTO responseDTO, Map<Integer, SchoolStatisticVO> schoolStatisticMap) {
        SchoolStatisticVO schoolStatisticVO = schoolStatisticMap.get(responseDTO.getPlanId());
        if (Objects.nonNull(schoolStatisticVO)) {
            responseDTO.setPlanScreeningNumbers(schoolStatisticVO.getPlanScreeningNum());
            responseDTO.setRealScreeningNumbers(schoolStatisticVO.getRealScreeningNum());
        } else {
            responseDTO.setRealScreeningNumbers(0);
        }
    }

    /**
     * 设置机构信息
     * @param responseDTO 返回对象
     * @param orgMap 机构信息集合
     */
    public void setOrgInfo(ScreeningListResponseDTO responseDTO,Map<Integer, ScreeningOrganization> orgMap) {
        ScreeningOrganization screeningOrganization = orgMap.get(responseDTO.getScreeningOrgId());
        if (Objects.isNull(screeningOrganization)){
            responseDTO.setScreeningOrgName(SchoolConstant.OUR_SCHOOL);
            return;
        }
        responseDTO.setScreeningOrgName(screeningOrganization.getName());
        responseDTO.setQrCodeConfig(screeningOrganization.getQrCodeConfig());
    }

    /**
     * 设置告知书配置
     * @param responseDTO 返回对象
     * @param notificationInfo 告知书配置
     */
    public void setNotificationInfo(ScreeningListResponseDTO responseDTO, TwoTuple<NotificationConfig, String> notificationInfo) {
        if (Objects.isNull(notificationInfo)){
            return;
        }
        // 设置告知书配置
        responseDTO.setNotificationConfig(notificationInfo.getFirst());
        responseDTO.setQrCodeFileUrl(notificationInfo.getSecond());
    }

    /**
     * 设置学生跟踪预警信息
     * @param reportMap
     * @param schoolStudentMap
     * @param track
     */
    public void setStudentTrackWarningInfo(Map<Integer, MedicalReport> reportMap, Map<Integer, Integer> schoolStudentMap, StudentTrackWarningResponseDTO track) {
        track.setSchoolStudentId(schoolStudentMap.get(track.getStudentId()));
        track.setIsBindMp(track.getIsBindMp());
        if (Objects.nonNull(track.getReportId())) {
            MedicalReport report = reportMap.get(track.getReportId());
            track.setIsReview(true);
            track.setVisitResult(report.getMedicalContent());
            track.setGlassesSuggest(report.getGlassesSituation());
        }
    }

    /**
     * 获取筛查学生信息
     * @param schoolGradeAndClass 年级和班级
     * @param visionScreeningResultMap 筛查结果集合
     * @param screeningPlanSchoolStudent 筛查学生信息
     * @param schoolStudentIdMap 学校学生信息
     */
    public ScreeningStudentListVO getScreeningStudentListVO(TwoTuple<Map<Integer, SchoolGrade>, Map<Integer, SchoolClass>> schoolGradeAndClass,
                                                            Map<Integer, VisionScreeningResult> visionScreeningResultMap,
                                                            ScreeningPlanSchoolStudent screeningPlanSchoolStudent,
                                                            Map<Integer, Integer> schoolStudentIdMap) {
        VisionScreeningResult visionScreeningResult = visionScreeningResultMap.get(screeningPlanSchoolStudent.getId());
        SchoolGrade schoolGrade = schoolGradeAndClass.getFirst().get(screeningPlanSchoolStudent.getGradeId());
        SchoolClass schoolClass = schoolGradeAndClass.getSecond().get(screeningPlanSchoolStudent.getClassId());
        return buildScreeningStudentListVO(screeningPlanSchoolStudent, visionScreeningResult, schoolGrade, schoolClass,schoolStudentIdMap);
    }

    /**
     * 构建筛查学生列表对象
     * @param screeningPlanSchoolStudent 筛查学生对象
     * @param visionScreeningResult 筛查结果集合
     */
    private ScreeningStudentListVO buildScreeningStudentListVO(ScreeningPlanSchoolStudent screeningPlanSchoolStudent,
                                                               VisionScreeningResult visionScreeningResult,
                                                               SchoolGrade schoolGrade,SchoolClass schoolClass,
                                                               Map<Integer, Integer> schoolStudentIdMap) {
        ScreeningStudentListVO screeningStudentListVO = new ScreeningStudentListVO()
                .setPlanStudentId(screeningPlanSchoolStudent.getId())
                .setId(schoolStudentIdMap.get(screeningPlanSchoolStudent.getStudentId()))
                .setStudentId(screeningPlanSchoolStudent.getStudentId())
                .setScreeningCode(screeningPlanSchoolStudent.getScreeningCode())
                .setSno(screeningPlanSchoolStudent.getStudentNo())
                .setName(screeningPlanSchoolStudent.getStudentName())
                .setGender(screeningPlanSchoolStudent.getGender())
                .setGradeName(schoolGrade.getName())
                .setClassName(schoolClass.getName())
                .setState(screeningPlanSchoolStudent.getState());

        setStudentVisionScreeningResult(screeningStudentListVO,visionScreeningResult);
        return screeningStudentListVO;
    }

    /**
     * 设置学生的筛查数据
     * @param screeningStudentListVO 筛查学生
     * @param visionScreeningResult 筛查学生的筛查结果
     */
    private void setStudentVisionScreeningResult(ScreeningStudentListVO screeningStudentListVO, VisionScreeningResult  visionScreeningResult) {
        screeningStudentListVO.setHasScreening(Objects.nonNull(visionScreeningResult))
                //是否戴镜情况
                .setGlassesTypeDes(EyeDataUtil.glassesTypeString(visionScreeningResult))
                //裸视力
                .setNakedVision(EyeDataUtil.visionRightDataToStr(visionScreeningResult)+"/"+EyeDataUtil.visionLeftDataToStr(visionScreeningResult))
                //矫正 视力
                .setCorrectedVision(EyeDataUtil.correctedRightDataToStr(visionScreeningResult)+"/"+EyeDataUtil.correctedLeftDataToStr(visionScreeningResult))
                //球镜
                .setSph(EyeDataUtil.computerRightSph(visionScreeningResult)+"/"+EyeDataUtil.computerLeftSph(visionScreeningResult))
                //柱镜
                .setCyl(EyeDataUtil.computerRightCyl(visionScreeningResult)+"/"+EyeDataUtil.computerLeftCyl(visionScreeningResult))
                //眼轴
                .setAxial(EyeDataUtil.computerRightAxial(visionScreeningResult)+"/"+EyeDataUtil.computerLeftAxial(visionScreeningResult));

        //是否数据完整性
        if (Objects.isNull(visionScreeningResult)) {
            screeningStudentListVO.setDataIntegrity(CommonConst.DATA_INTEGRITY_MISS);
            return;
        }
        boolean completedData = StatUtil.isCompletedData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
        screeningStudentListVO.setDataIntegrity(Objects.equals(completedData,Boolean.TRUE)?CommonConst.DATA_INTEGRITY_FINISH:CommonConst.DATA_INTEGRITY_MISS);
    }

    /**
     * 构建学校统计信息
     * @param screeningPlanId
     * @param screeningPlanSchoolStudentMap
     * @param visionScreeningResultMap
     */
    public SchoolStatisticVO buildSchoolStatistic(Integer screeningPlanId, Map<Integer, List<ScreeningPlanSchoolStudent>> screeningPlanSchoolStudentMap, Map<Integer, List<VisionScreeningResult>> visionScreeningResultMap) {
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentMap.getOrDefault(screeningPlanId, Lists.newArrayList());
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultMap.getOrDefault(screeningPlanId, Lists.newArrayList());

        return new SchoolStatisticVO()
                .setScreeningPlanId(screeningPlanId)
                .setPlanScreeningNum(screeningPlanSchoolStudentList.size())
                .setRealScreeningNum(visionScreeningResultList.size());
    }

    /**
     * 构建筛查计划信息
     * @param screeningPlan
     * @param screeningOrg
     * @param schoolGradeList
     */
    public ScreeningPlanVO buildScreeningPlanVO(ScreeningPlan screeningPlan, TwoTuple<Integer,String> screeningOrg, List<SchoolGrade> schoolGradeList, Integer screeningStatus) {
        List<Integer> optionTabs = schoolGradeList.stream().map(SchoolScreeningBizBuilder::getSchoolType).filter(Objects::nonNull).distinct().sorted(Comparator.comparing(Integer::intValue).reversed()).collect(Collectors.toList());

        return new ScreeningPlanVO()
                .setId(screeningPlan.getId())
                .setTitle(screeningPlan.getTitle())
                .setStartTime(screeningPlan.getStartTime())
                .setEndTime(screeningPlan.getEndTime())
                .setScreeningType(screeningPlan.getScreeningType())
                .setScreeningBizType(ScreeningBizTypeEnum.getInstanceByOrgType(screeningPlan.getScreeningOrgType()).getType())
                .setStatus(SchoolScreeningBizBuilder.setMergeStatus(screeningPlan.getReleaseStatus(),screeningStatus))
                .setScreeningOrgName(screeningOrg.getSecond())
                .setScreeningOrgId(screeningOrg.getFirst())
                .setOptionTabs(optionTabs);
    }

    /**
     * 获取学校类型
     * @param schoolGrade 学校年级
     */
    private Integer getSchoolType(SchoolGrade schoolGrade) {
        if (GradeCodeEnum.primaryAbove().contains(schoolGrade.getGradeCode())) {
            return SchoolEnum.TYPE_PRIMARY.getType();
        }
        if (GradeCodeEnum.kindergartenSchoolCode().contains(schoolGrade.getGradeCode())) {
            return SchoolEnum.TYPE_KINDERGARTEN.getType();
        }
        return null;
    }
}
