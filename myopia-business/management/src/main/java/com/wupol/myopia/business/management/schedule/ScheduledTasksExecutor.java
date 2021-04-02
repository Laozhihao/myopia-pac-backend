package com.wupol.myopia.business.management.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.builder.DistrictBigScreenStatisticBuilder;
import com.wupol.myopia.business.management.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
import com.wupol.myopia.business.management.domain.vo.StudentVo;
import com.wupol.myopia.business.management.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @date 2021/02/19
 */
@Component
@Slf4j
public class ScheduledTasksExecutor {
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private DistrictAttentiveObjectsStatisticService districtAttentiveObjectsStatisticService;
    @Autowired
    private DistrictVisionStatisticService districtVisionStatisticService;
    @Autowired
    private DistrictMonitorStatisticService districtMonitorStatisticService;
    @Autowired
    private SchoolVisionStatisticService schoolVisionStatisticService;
    @Autowired
    private SchoolMonitorStatisticService schoolMonitorStatisticService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private BigScreenMapService bigScreenMapService;
    @Autowired
    private DistrictBigScreenStatisticService districtBigScreenStatisticService;

    /**
     * 筛查数据统计
     */
    //@Scheduled(cron = "0 5 0 * * ?", zone = "GMT+8:00")
//    @Scheduled(cron = "0 * * * * ?", zone = "GMT+8:00")
    public void statistic() {
        //1. 查询出需要统计的通知（根据筛查数据vision_screening_result的更新时间判断）
        List<Integer> yesterdayScreeningPlanIds = visionScreeningResultService.getYesterdayScreeningPlanIds();
        if (CollectionUtils.isEmpty(yesterdayScreeningPlanIds)) {
            log.info("筛查数据统计：前一天无筛查数据，无需统计");
            return;
        }
        statisticByPlanIds(yesterdayScreeningPlanIds);
    }

    /**
     * 根据筛查计划ID进行筛查统计
     *
     * @param yesterdayScreeningPlanIds
     */
    public void statisticByPlanIds(List<Integer> yesterdayScreeningPlanIds) {
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = new ArrayList<>();
        List<DistrictMonitorStatistic> districtMonitorStatistics = new ArrayList<>();
        List<DistrictVisionStatistic> districtVisionStatistics = new ArrayList<>();
        List<SchoolVisionStatistic> schoolVisionStatistics = new ArrayList<>();
        List<SchoolMonitorStatistic> schoolMonitorStatistics = new ArrayList<>();
        genDistrictStatistics(yesterdayScreeningPlanIds, districtMonitorStatistics, districtVisionStatistics);
        genSchoolStatistics(yesterdayScreeningPlanIds, schoolVisionStatistics, schoolMonitorStatistics);
        //重点视力对象需统计的是学校所在区域的所有数据，另外统计
        genAttentiveObjectsStatistics(yesterdayScreeningPlanIds, districtAttentiveObjectsStatistics);
        districtAttentiveObjectsStatisticService.batchSaveOrUpdate(districtAttentiveObjectsStatistics);
        districtMonitorStatisticService.batchSaveOrUpdate(districtMonitorStatistics);
        districtVisionStatisticService.batchSaveOrUpdate(districtVisionStatistics);
        schoolVisionStatisticService.batchSaveOrUpdate(schoolVisionStatistics);
        schoolMonitorStatisticService.batchSaveOrUpdate(schoolMonitorStatistics);
    }

    /**
     * 重点视力对象
     *
     * @param yesterdayScreeningPlanIds
     * @param districtAttentiveObjectsStatistics
     */
    private void genAttentiveObjectsStatistics(List<Integer> yesterdayScreeningPlanIds, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics) {
        // 1. 获取计划影响的学校的区域
        Set<Integer> schoolDistrictIds = schoolService.getAllSchoolDistrictIdsByScreeningPlanIds(yesterdayScreeningPlanIds);
        // 2. 根据学校的区域ID列表，组装需要重新计算的区域ID（省级到学校所在的区域所有层级）
        Set<Integer> districtIds = schoolDistrictIds.stream().map(districtId -> districtService.getDistrictPositionDetailById(districtId).stream().map(District::getId).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toSet());
        // 3. 组装省份的所有数据
        Map<Integer, Integer> districtIdProvinceIdMap = new HashMap<>(16);
        Set<Integer> provinceIdSet = new HashSet<>();
        Map<Integer, Map<Integer, List<StudentVo>>> provinceDistrictStudents = new HashMap<>(16);
        districtIds.forEach(districtId -> {
            Integer provinceId = districtService.getProvinceId(districtId);
            districtIdProvinceIdMap.put(districtId, provinceId);
            if (!provinceIdSet.contains(provinceId)) {
                provinceIdSet.add(provinceId);
                List<Integer> needGetStudentDistrictIds = CompareUtil.getRetain(districtService.getProvinceAllDistrictIds(provinceId), districtIds);
                provinceDistrictStudents.put(provinceId, studentService.getStudentsBySchoolDistrictIds(needGetStudentDistrictIds).stream().collect(Collectors.groupingBy(StudentVo::getDistrictId)));
            }
        });
        // 4. 循环districtIds，拿出所在省份的数据，然后分别统计自己/合计数据
        districtIds.forEach(districtId -> {
            Map<Integer, List<StudentVo>> districtStudentVos = provinceDistrictStudents.getOrDefault(districtIdProvinceIdMap.get(districtId), Collections.emptyMap());
            List<Integer> districtTreeAllIds = districtService.getDistrictTreeAllIds(districtId);
            List<StudentVo> totalStudents = districtStudentVos.keySet().stream().filter(districtTreeAllIds::contains).map(id -> districtStudentVos.getOrDefault(id, Collections.emptyList())).flatMap(Collection::stream).collect(Collectors.toList());
            if (districtStudentVos.keySet().contains(districtId)) {
                districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatistic.build(districtId, CommonConst.NOT_TOTAL, districtStudentVos.get(districtId)));
            }
            districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatistic.build(districtId, CommonConst.IS_TOTAL, totalStudents));
        });
    }

    /**
     * 按学校生成统计数据
     *
     * @param yesterdayScreeningPlanIds
     * @param schoolVisionStatistics
     */
    private void genSchoolStatistics(List<Integer> yesterdayScreeningPlanIds, List<SchoolVisionStatistic> schoolVisionStatistics, List<SchoolMonitorStatistic> schoolMonitorStatistics) {
        //3. 分别处理每个学校的统计
        yesterdayScreeningPlanIds.forEach(screeningPlanId -> {
            //3.1 查出计划对应的筛查数据(结果)
            List<StatConclusionVo> statConclusions = statConclusionService.getVoByScreeningPlanId(screeningPlanId);
            if (CollectionUtils.isEmpty(statConclusions)) {
                return;
            }
            Map<Integer, List<StatConclusionVo>> schoolIdStatConslusions = statConclusions.stream().collect(Collectors.groupingBy(StatConclusionVo::getSchoolId));
            ScreeningPlan screeningPlan = screeningPlanService.getById(screeningPlanId);
            Map<Integer, School> schoolIdMap = schoolService.getByIds(new ArrayList<>(schoolIdStatConslusions.keySet())).stream().collect(Collectors.toMap(School::getId, Function.identity()));
            ScreeningOrganization screeningOrg = screeningOrganizationService.getById(screeningPlan.getScreeningOrgId());
            Map<Integer, Long> planSchoolStudentNum = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
            //3.2 每个学校分别统计
            schoolIdStatConslusions.keySet().forEach(schoolId -> {
                List<StatConclusionVo> schoolStatConclusion = schoolIdStatConslusions.get(schoolId);
                Map<Boolean, List<StatConclusionVo>> isValidMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
                Map<Boolean, List<StatConclusionVo>> isRescreenTotalMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                List<StatConclusionVo> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
                Map<Boolean, List<StatConclusionVo>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                int planSchoolScreeningNumbers = planSchoolStudentNum.getOrDefault(schoolId, 0L).intValue();
                int reslScreeningNumbers = isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size();
                schoolVisionStatistics.add(SchoolVisionStatistic.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), screeningPlanId, isRescreenMap.getOrDefault(false, Collections.emptyList()), reslScreeningNumbers, planSchoolScreeningNumbers));
                schoolMonitorStatistics.add(SchoolMonitorStatistic.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), isRescreenMap.getOrDefault(true, Collections.emptyList()), planSchoolScreeningNumbers, reslScreeningNumbers));
            });
        });
    }

    /**
     * 按区域层级生成统计数据
     *
     * @param yesterdayScreeningPlanIds
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     */
    private void genDistrictStatistics(List<Integer> yesterdayScreeningPlanIds, List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics) {
        List<Integer> screeningNoticeIds = screeningPlanService.getSrcScreeningNoticeIdsByIds(yesterdayScreeningPlanIds);
        //2. 分别处理每个通知的区域层级统计
        screeningNoticeIds.forEach(screeningNoticeId -> {
            if (CommonConst.DEFAULT_ID.equals(screeningNoticeId)) {
                // 单点筛查机构创建的数据不需要统计
                return;
            }
            //2.1 查出对应的筛查数据(结果)
            List<StatConclusion> statConclusions = statConclusionService.getBySrcScreeningNoticeId(screeningNoticeId);
            if (CollectionUtils.isEmpty(statConclusions)) {
                return;
            }
            //2.2 层级维度统计
            Map<Integer, List<StatConclusion>> districtStatConclusions = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getDistrictId));
            //2.3 筛查通知中的学校所在层级的计划学生总数
            Map<Integer, Long> districtPlanStudentCountMap = screeningPlanSchoolStudentService.getDistrictPlanStudentCountBySrcScreeningNoticeId(screeningNoticeId);
            //2.4 查出通知对应的顶级层级：从任务所在省级开始（因为筛查计划可选全省学校）
            ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
            Integer provinceDistrictId = districtService.getProvinceId(screeningNotice.getDistrictId());
            genStatisticsByDistrictId(screeningNoticeId, provinceDistrictId, districtPlanStudentCountMap, districtMonitorStatistics, districtVisionStatistics, districtStatConclusions);
        });
    }

    /**
     * 生成层级的统计数据
     *
     * @param screeningNoticeId
     * @param districtId
     * @param districtPlanStudentCountMap
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param districtStatConclusions     所有的筛查数据
     */
    private void genStatisticsByDistrictId(Integer screeningNoticeId, Integer districtId, Map<Integer, Long> districtPlanStudentCountMap, List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics, Map<Integer, List<StatConclusion>> districtStatConclusions) {
        List<District> childDistricts = new ArrayList<>();
        List<Integer> childDistrictIds = new ArrayList<>();
        try {
            // 合计的要包括自己层级的筛查数据
            childDistricts = districtService.getChildDistrictByParentIdPriorityCache(districtId);
            childDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        } catch (IOException e) {
            log.error("获取区域层级失败", e);
        }
        //2.4 层级循环处理并添加到对应的统计中
        List<Integer> haveStatConclusionsChildDistrictIds = CompareUtil.getRetain(childDistrictIds, districtStatConclusions.keySet());
        List<Integer> haveStudentDistrictIds = CompareUtil.getRetain(childDistrictIds, districtPlanStudentCountMap.keySet());
        // 层级合计数据
        List<StatConclusion> totalStatConclusions = haveStatConclusionsChildDistrictIds.stream().map(districtStatConclusions::get).flatMap(Collection::stream).collect(Collectors.toList());
        Integer totalPlanStudentCount = (int) haveStudentDistrictIds.stream().mapToLong(districtPlanStudentCountMap::get).sum();
        // 层级自身数据
        List<StatConclusion> selfStatConclusions = districtStatConclusions.getOrDefault(districtId, Collections.emptyList());
        Integer selfPlanStudentCount = districtPlanStudentCountMap.getOrDefault(districtId, 0L).intValue();

        genTotalStatistics(screeningNoticeId, districtId, totalPlanStudentCount, districtMonitorStatistics, districtVisionStatistics, totalStatConclusions);
        genSelfStatistics(screeningNoticeId, districtId, selfPlanStudentCount, districtMonitorStatistics, districtVisionStatistics, selfStatConclusions);
        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genStatisticsByDistrictId(screeningNoticeId, childDistrict.getId(), districtPlanStudentCountMap, districtMonitorStatistics, districtVisionStatistics, districtStatConclusions));
        }
    }

    /**
     * 生成自己层级的筛查数据
     *
     * @param screeningNoticeId
     * @param districtId
     * @param planStudentNum
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param selfStatConclusions
     */
    private void genSelfStatistics(Integer screeningNoticeId, Integer districtId, Integer planStudentNum,
                                   List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics,
                                   List<StatConclusion> selfStatConclusions) {
        if (CollectionUtils.isEmpty(selfStatConclusions)) {
            return;
        }
        Map<Boolean, List<StatConclusion>> isValidMap = selfStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = selfStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        // 层级自己的筛查数据肯定属于同一个任务，所以只取第一个的就可以
        Integer screeningTaskId = selfStatConclusions.get(0).getTaskId();
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size();
        districtMonitorStatistics.add(DistrictMonitorStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(true, Collections.emptyList()), planStudentNum, realScreeningStudentNum));
        districtVisionStatistics.add(DistrictVisionStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), realScreeningStudentNum, planStudentNum));
    }

    /**
     * 生成层级所能看到的总的筛查数据
     *
     * @param screeningNoticeId
     * @param districtId
     * @param planStudentNum
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param totalStatConclusions
     */
    private void genTotalStatistics(Integer screeningNoticeId, Integer districtId, Integer planStudentNum,
                                    List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics,
                                    List<StatConclusion> totalStatConclusions) {
        if (CollectionUtils.isEmpty(totalStatConclusions)) {
            return;
        }
        Map<Boolean, List<StatConclusion>> isValidMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        // 层级总的筛查数据不一定属于同一个任务，所以取默认0
        Integer screeningTaskId = CommonConst.DEFAULT_ID;
        Integer realScreeningStudentNum = isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size();

        districtMonitorStatistics.add(DistrictMonitorStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(true, Collections.emptyList()), planStudentNum, realScreeningStudentNum));
        districtVisionStatistics.add(DistrictVisionStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), realScreeningStudentNum, planStudentNum));
    }

    /**
     * 筛查数据统计 测试环境暂时关闭
     */
    //@Scheduled(cron = "0 5 0 * * ?", zone = "GMT+8:00")
    public void statisticBigScreen() throws IOException {
        //找到所有省级部门
        List<GovDept> proviceGovDepts = govDeptService.getProviceGovDept();
        Set<Integer> govDeptIds = proviceGovDepts.stream().map(GovDept::getId).collect(Collectors.toSet());
        //通过所有省级部门查找所有通知
        List<ScreeningNotice> screeningNotices = screeningNoticeService.getNoticeByReleaseOrgId(govDeptIds, ScreeningNotice.TYPE_GOV_DEPT);
        //发布过的省级部门的省地区id
        Map<Integer, List<ScreeningNotice>> districtIdNoticeListMap = screeningNotices.stream().collect(Collectors.groupingBy(ScreeningNotice::getDistrictId));
        //将每个省最新发布的notice拿出来
        Set<Integer> provinceDistrictIds = districtIdNoticeListMap.keySet();
        Map<Integer, ScreeningNotice> districtIdNoticeMap = new HashMap<>();
        provinceDistrictIds.stream().forEach(districtId -> {
            List<ScreeningNotice> screeningNoticeList = districtIdNoticeListMap.get(districtId);
            ScreeningNotice screeningNotice = screeningNoticeList.stream().sorted(Comparator.comparing(ScreeningNotice::getReleaseTime).reversed()).findFirst().get();
            districtIdNoticeMap.put(districtId, screeningNotice);
        });
        for (Integer provinceDistrictId : provinceDistrictIds) {
            DistrictBigScreenStatistic districtBigScreenStatistic = this.generateResult(provinceDistrictId, districtIdNoticeMap.get(provinceDistrictId));
            if (districtBigScreenStatistic != null) {
                districtBigScreenStatisticService.saveOrUpdateByDistrictId(districtBigScreenStatistic);
            }
        }
    }

    /**
     * 生成某个省的数据
     *
     * @param provinceDistrictId
     * @param screeningNotice
     * @return
     * @throws IOException
     */
    private DistrictBigScreenStatistic generateResult(Integer provinceDistrictId, ScreeningNotice screeningNotice) throws IOException {
        //根据条件查找所有的元素：条件 cityDistrictIds 非复测 有效
        List<BigScreenStatDataDTO> bigScreenStatDataDTOs = statConclusionService.getByNoticeidAndDistrictIds(screeningNotice.getId());
        int realScreeningNum = CollectionUtils.size(bigScreenStatDataDTOs);
        //获取地图数据
        BigScreenMap bigScreenMap = bigScreenMapService.getByDistrictId(provinceDistrictId);
        //将基本数据放入构造器
        bigScreenStatDataDTOs = bigScreenStatDataDTOs.stream().filter(BigScreenStatDataDTO::getIsValid).collect(Collectors.toList());
        int realValidScreeningNum = CollectionUtils.size(bigScreenStatDataDTOs);
        DistrictBigScreenStatisticBuilder districtBigScreenStatisticBuilder = DistrictBigScreenStatisticBuilder.getBuilder()
                .setRealValidScreeningNum((long) realValidScreeningNum)
                .setRealScreeningNum((long) realScreeningNum)
                .setDistrictId(provinceDistrictId)
                .setMapJson(bigScreenMap.getJson())
                .setCityCenterMap(bigScreenMap.getCityCenterLocation())
                .setNoticeId(screeningNotice.getId())
                .setPlanScreeningNum(screeningPlanService.getAllPlanStudentNumByNoticeId(screeningNotice.getId()));
        if (realScreeningNum > 0 && realValidScreeningNum > 0) {
            //更新城市名
            this.updateCityName(bigScreenStatDataDTOs, districtService.getCityAllDistrictIds(provinceDistrictId));
            //构建数据
            districtBigScreenStatisticBuilder.setBigScreenStatDataDTOList(bigScreenStatDataDTOs);
        }
        return districtBigScreenStatisticBuilder.build();
    }

    /**
     * 更新大屏数据的城市名
     *
     * @param bigScreenStatDataDTOs
     * @param districtSetMap
     */
    private void updateCityName(List<BigScreenStatDataDTO> bigScreenStatDataDTOs, Map<District, Set<Integer>> districtSetMap) {
        bigScreenStatDataDTOs = bigScreenStatDataDTOs.stream().map(bigScreenStatDataDTO -> {
            Set<District> districtSet = districtSetMap.keySet();
            for (District cityDistrict : districtSet) {
                Set<Integer> districtIds = districtSetMap.get(cityDistrict);
                if (districtIds.contains(bigScreenStatDataDTO.getDistrictId()) || cityDistrict.getId().equals(bigScreenStatDataDTO.getDistrictId())) {
                    bigScreenStatDataDTO.setCityDistrictId(cityDistrict.getId());
                    bigScreenStatDataDTO.setCityDistrictName(cityDistrict.getName());
                    break;
                }
            }
            return bigScreenStatDataDTO;
        }).collect(Collectors.toList());
    }
}