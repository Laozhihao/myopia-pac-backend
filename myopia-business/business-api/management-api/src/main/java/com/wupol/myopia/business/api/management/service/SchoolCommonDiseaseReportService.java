package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseaseReportVO;
import com.wupol.myopia.business.api.management.domain.vo.report.DistrictCommonDiseasesAnalysisVO;
import com.wupol.myopia.business.api.management.domain.vo.report.SchoolCommonDiseaseReportVO;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 按学校常见病报告
 *
 * @author hang.yuan 2022/5/19 14:09
 */
@Service
public class SchoolCommonDiseaseReportService {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    public DistrictCommonDiseaseReportVO districtCommonDiseaseReport(Integer districtId,Integer noticeId){
        DistrictCommonDiseaseReportVO districtCommonDiseaseReportVO = new DistrictCommonDiseaseReportVO();

        //全局变量
        DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO = getGlobalVariableVO(districtId, noticeId);
        districtCommonDiseaseReportVO.setGlobalVariableVO(globalVariableVO);
        
        return districtCommonDiseaseReportVO;
    }

    private DistrictCommonDiseaseReportVO.GlobalVariableVO getGlobalVariableVO(Integer districtId,Integer noticeId){
        DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO = new DistrictCommonDiseaseReportVO.GlobalVariableVO();
        String format="YYYY";
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (Objects.isNull(screeningNotice)){
            throw new BusinessException(String.format("不存在筛查通知: noticeId=%s",noticeId));
        }
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getAllPlanByNoticeId(noticeId);
        if (CollectionUtil.isEmpty(screeningPlanList)){
            throw new BusinessException(String.format("该筛查通知不存在筛查计划: noticeId=%s",noticeId));
        }

        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> screeningResults = visionScreeningResultService.getByPlanIds(Lists.newArrayList(planIds));
        if (CollectionUtil.isEmpty(screeningResults)){
            return globalVariableVO;
        }

        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByScreeningPlanIds(Lists.newArrayList(planIds));

        long totalSum = planSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getSchoolId).distinct().count();
        Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap = planSchoolStudentList.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeType));

        setSchoolItemData(planSchoolStudentMap,globalVariableVO);

        //获取行政区域
        String districtName = districtService.getDistrictNameByDistrictId(districtId);
        Set<String> years= Sets.newHashSet(DateUtil.format(screeningNotice.getStartTime(), format),
                DateUtil.format(screeningNotice.getEndTime(), format));
        String year = CollectionUtil.join(years, "-");
        if (years.size()==1){
            Set<String> yearPeriod= Sets.newHashSet(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(screeningNotice.getEndTime(), "MM月dd日"));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);
            globalVariableVO.setDataYear(year);
        }else {
            Set<String> yearPeriod= Sets.newHashSet(DateUtil.format(screeningNotice.getStartTime(), DatePattern.CHINESE_DATE_PATTERN),
                    DateUtil.format(screeningNotice.getEndTime(), DatePattern.CHINESE_DATE_PATTERN));
            String screeningTimePeriod = CollectionUtil.join(yearPeriod, StrUtil.DASHED);
            globalVariableVO.setScreeningTimePeriod(screeningTimePeriod);

            VisionScreeningResult visionScreeningResult = screeningResults.get(0);
            String dataYear = DateUtil.format(visionScreeningResult.getCreateTime(),format);
            globalVariableVO.setDataYear(dataYear);
        }

        globalVariableVO.setAreaName(districtName);
        globalVariableVO.setYear(year);
        globalVariableVO.setTotalSchoolNum((int)totalSum);

        return globalVariableVO;
    }

    private void setSchoolItemData(Map<Integer, List<ScreeningPlanSchoolStudent>> planSchoolStudentMap, DistrictCommonDiseaseReportVO.GlobalVariableVO globalVariableVO) {
        String format = "%s所%s";
        List<String> itemList= Lists.newArrayList();
        List<ScreeningPlanSchoolStudent> primary = planSchoolStudentMap.get(SchoolAge.PRIMARY.code);
        if (CollectionUtil.isNotEmpty(primary)){
            itemList.add(String.format(format,primary.size(),SchoolAge.PRIMARY.desc));
        }
        List<ScreeningPlanSchoolStudent> junior = planSchoolStudentMap.get(SchoolAge.JUNIOR.code);
        if (CollectionUtil.isNotEmpty(junior)){
            itemList.add(String.format(format,junior.size(),SchoolAge.JUNIOR.desc));
        }
        List<ScreeningPlanSchoolStudent> high = planSchoolStudentMap.get(SchoolAge.HIGH.code);
        if (CollectionUtil.isNotEmpty(high)){
            itemList.add(String.format(format,high.size(),SchoolAge.HIGH.desc));
        }
        List<ScreeningPlanSchoolStudent> vocationalHigh = planSchoolStudentMap.get(SchoolAge.VOCATIONAL_HIGH.code);
        if (CollectionUtil.isNotEmpty(vocationalHigh)){
            itemList.add(String.format(format,vocationalHigh.size(),SchoolAge.VOCATIONAL_HIGH.desc));
        }
        List<ScreeningPlanSchoolStudent> university = planSchoolStudentMap.get(SchoolAge.UNIVERSITY.code);
        if (CollectionUtil.isNotEmpty(university)){
            itemList.add(String.format(format,university.size(),SchoolAge.UNIVERSITY.desc));
        }
        List<ScreeningPlanSchoolStudent> kindergarten = planSchoolStudentMap.get(SchoolAge.KINDERGARTEN.code);
        if (CollectionUtil.isNotEmpty(kindergarten)){
            itemList.add(String.format(format,kindergarten.size(),SchoolAge.KINDERGARTEN.desc));
        }

        if (CollectionUtil.isNotEmpty(itemList)){
            globalVariableVO.setSchoolItem(itemList);
        }

    }

    private DistrictCommonDiseaseReportVO.VisionAnalysisVO getVisionAnalysisVO(Integer districtId, Integer noticeId){
        DistrictCommonDiseaseReportVO.VisionAnalysisVO visionAnalysisVO = new DistrictCommonDiseaseReportVO.VisionAnalysisVO();

        return visionAnalysisVO;
    }

    private DistrictCommonDiseasesAnalysisVO getDistrictCommonDiseasesAnalysisVO(Integer districtId, Integer noticeId){
        DistrictCommonDiseasesAnalysisVO districtCommonDiseasesAnalysisVO = new DistrictCommonDiseasesAnalysisVO();

        return districtCommonDiseasesAnalysisVO;
    }


    public SchoolCommonDiseaseReportVO schoolCommonDiseaseReport(Integer districtId,Integer noticeId,Integer planId){

        return null;
    }
}
