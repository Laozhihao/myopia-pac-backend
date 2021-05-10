package com.wupol.myopia.business.management.schedule;

import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.builder.DistrictBigScreenStatisticBuilder;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
import com.wupol.myopia.business.management.domain.vo.StudentVo;
import com.wupol.myopia.business.management.service.*;
import com.wupol.myopia.business.management.util.TwoTuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;
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
    @Resource
    private VistelToolsService vistelToolsService;

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
            List<Integer> districtTreeAllIds = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
            List<StudentVo> totalStudents = districtStudentVos.keySet().stream().filter(districtTreeAllIds::contains).map(id -> districtStudentVos.getOrDefault(id, Collections.emptyList())).flatMap(Collection::stream).collect(Collectors.toList());
            if (districtStudentVos.containsKey(districtId)) {
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
        provinceDistrictIds.forEach(districtId -> {
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

    /**
     * 每天9点执行，发送短信
     */
//    @Scheduled(cron = "0 0 9 * * ?", zone = "GMT+8:00")
    public void sendSMSNotice() {
        // TODO: 需要添加一个已经发送过的条件
        List<VisionScreeningResult> studentResult = visionScreeningResultService.getStudentResults();
        if (CollectionUtils.isEmpty(studentResult)) {
            return;
        }
        // 获取学生信息
        List<Integer> studentIds = studentResult.stream()
                .map(VisionScreeningResult::getStudentId).collect(Collectors.toList());
        List<Student> students = studentService.getByIds(studentIds);
        Map<Integer, Student> studentMaps = students.stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        studentResult.forEach(getVisionScreeningResultConsumer(studentMaps));
        // TODO: 更新筛查记录
        log.info("更新筛查数据");
    }

    /**
     * 消费
     *
     * @param studentMaps 学生Maps
     * @return Consumer<VisionScreeningResult>
     */
    private Consumer<VisionScreeningResult> getVisionScreeningResultConsumer(Map<Integer, Student> studentMaps) {
        return result -> {
            Student student = studentMaps.get(result.getStudentId());
            VisionDataDO visionData = result.getVisionData();
            ComputerOptometryDO computerOptometry = result.getComputerOptometry();
            if (Objects.isNull(visionData)) {
                return;
            }
            VisionDataDO.VisionData leftEyeData = visionData.getLeftEyeData();
            VisionDataDO.VisionData rightEyeData = visionData.getRightEyeData();

            BigDecimal leftNakedVision = leftEyeData.getNakedVision();
            BigDecimal leftCorrectedVision = leftEyeData.getCorrectedVision();
            BigDecimal rightNakedVision = rightEyeData.getNakedVision();
            BigDecimal rightCorrectedVision = rightEyeData.getCorrectedVision();

            // 左右眼的裸眼视力都是为空直接返回
            if (Objects.isNull(leftNakedVision) && Objects.isNull(rightNakedVision)) {
                return;
            }

            TwoTuple<BigDecimal, Integer> nakedVisionResult = getResultVision(leftNakedVision, rightNakedVision);
            Integer glassesType = leftEyeData.getGlassesType();

            // 裸眼视力是否小于4.9
            if (nakedVisionResult.getFirst().compareTo(new BigDecimal("4.9")) < 0) {
                // 是否佩戴眼镜
                if (glassesType >= 1) {
                    String noticeInfo = getSMSNoticeInfo(student.getName(),
                            leftNakedVision, rightNakedVision,
                            getIsWearingGlasses(leftCorrectedVision, rightCorrectedVision,
                                    leftNakedVision, rightNakedVision, nakedVisionResult));
                    // 发送短信
                    sendSMS(str2List(student.getMpParentPhone()), student.getParentPhone(), noticeInfo);
                } else {
                    // 没有佩戴眼镜
                    String noticeInfo = getSMSNoticeInfo(student.getName(),
                            leftNakedVision, rightNakedVision,
                            "裸眼视力下降，建议：请到医疗机构接受检查，明确诊断并及时采取措施。");
                    // 发送短信
                    sendSMS(str2List(student.getMpParentPhone()), student.getParentPhone(), noticeInfo);
                }
            } else {
                if (Objects.isNull(computerOptometry)) {
                    return;
                }
                BigDecimal leftSph = computerOptometry.getLeftEyeData().getSph();
                BigDecimal leftCyl = computerOptometry.getLeftEyeData().getCyl();
                BigDecimal rightSph = computerOptometry.getRightEyeData().getSph();
                BigDecimal rightCyl = computerOptometry.getRightEyeData().getCyl();
                BigDecimal leftSe = calculationSE(leftSph, leftCyl);
                BigDecimal rightSe = calculationSE(rightSph, rightCyl);
                // 裸眼视力大于4.9
                String noticeInfo = getSMSNoticeInfo(student.getName(),
                        leftNakedVision, rightNakedVision,
                        nakedVisionNormal(leftNakedVision, rightNakedVision,
                                leftSe, rightSe, nakedVisionResult));
                // 发送短信
                sendSMS(str2List(student.getMpParentPhone()), student.getParentPhone(), noticeInfo);
            }
        };
    }

    /**
     * 取视力值低的眼球
     *
     * @param left  左眼
     * @param right 右眼
     * @return TwoTuple<BigDecimal, Integer> left-视力 right-左右眼
     */
    private TwoTuple<BigDecimal, Integer> getResultVision(BigDecimal left, BigDecimal right) {
        if (Objects.isNull(left) || Objects.isNull(right)) {
            // 左眼为空取右眼
            if (Objects.isNull(left)) {
                return new TwoTuple<>(right, CommonConst.RIGHT_EYE);
            }
            // 右眼为空取左眼
            return new TwoTuple<>(left, CommonConst.LEFT_EYE);
        }
        if (left.compareTo(right) == 0) {
            return new TwoTuple<>(left, CommonConst.SAME_EYE);
        }
        if (left.compareTo(right) < 0) {
            return new TwoTuple<>(left, CommonConst.LEFT_EYE);
        }
        return new TwoTuple<>(right, CommonConst.RIGHT_EYE);
    }

    /**
     * 戴镜获取结论
     *
     * @param leftCorrectedVision  左眼矫正视力
     * @param rightCorrectedVision 右眼矫正视力
     * @param leftNakedVision      左眼裸眼视力
     * @param rightNakedVision     右眼裸眼视力
     * @param nakedVisionResult    取视力值低的眼球
     * @return 结论
     */
    private String getIsWearingGlasses(BigDecimal leftCorrectedVision, BigDecimal rightCorrectedVision,
                                       BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                       TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        if (Objects.isNull(leftCorrectedVision) || Objects.isNull(rightCorrectedVision)) {
            return "";
        }
        BigDecimal visionVal;
        // 判断两只眼睛的裸眼视力是否都小于4.9或大于等于4.9
        if (isNakedVisionMatch(leftNakedVision, rightNakedVision)) {
            // 获取矫正视力低的眼球
            visionVal = getResultVision(leftCorrectedVision, rightCorrectedVision).getFirst();
        } else {
            if (nakedVisionResult.getSecond().equals(CommonConst.LEFT_EYE)) {
                // 取左眼数据
                visionVal = leftCorrectedVision;
            } else {
                // 取右眼数据
                visionVal = rightCorrectedVision;
            }
        }
        if (visionVal.compareTo(new BigDecimal("4.9")) < 0) {
            // 矫正视力小于4.9
            return "裸眼视力下降，建议：请及时到医疗机构复查。";
        } else {
            // 矫正视力大于4.9
            return "裸眼视力下降，建议：3个月或半年复查视力。";
        }
    }

    /**
     * 两眼的值是否都在4.9的同侧
     *
     * @param leftNakedVision  左裸眼视力
     * @param rightNakedVision 右裸眼数据
     * @return Boolean
     */
    private Boolean isNakedVisionMatch(BigDecimal leftNakedVision, BigDecimal rightNakedVision) {
        return ((leftNakedVision.compareTo(new BigDecimal("4.9")) < 0) &&
                (rightNakedVision.compareTo(new BigDecimal("4.9")) < 0))
                ||
                ((leftNakedVision.compareTo(new BigDecimal("4.9")) >= 0) &&
                        (rightNakedVision.compareTo(new BigDecimal("4.9")) >= 0));
    }

    /**
     * 计算 等效球镜
     *
     * @param sph 球镜
     * @param cyl 柱镜
     * @return 等效球镜
     */
    private BigDecimal calculationSE(BigDecimal sph, BigDecimal cyl) {
        return sph.add(cyl.multiply(new BigDecimal("0.5")))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 正常裸眼视力获取结论
     *
     * @param leftNakedVision   左眼裸眼视力
     * @param rightNakedVision  右眼裸眼视力
     * @param leftSe            左眼等效球镜
     * @param rightSe           右眼等效球镜
     * @param nakedVisionResult 取视力值低的眼球
     * @return 结论
     */
    private String nakedVisionNormal(BigDecimal leftNakedVision, BigDecimal rightNakedVision,
                                     BigDecimal leftSe, BigDecimal rightSe,
                                     TwoTuple<BigDecimal, Integer> nakedVisionResult) {
        BigDecimal se;
        // 判断两只眼睛的裸眼视力是否都在4.9的同侧
        if (isNakedVisionMatch(leftNakedVision, rightNakedVision)) {
            // 取等效球镜严重的眼别
            if (leftSe.compareTo(new BigDecimal("0.00")) <= 0
                    || rightSe.compareTo(new BigDecimal("0.00")) <= 0) {
                if (leftSe.compareTo(new BigDecimal("0.00")) <= 0) {
                    // 取左眼
                    se = leftSe;
                } else {
                    // 取右眼
                    se = rightSe;
                }
            } else {
                // 取等效球镜值大的眼别
                if (leftSe.compareTo(rightSe) >= 0) {
                    // 取左眼
                    se = leftSe;
                } else {
                    // 取右眼
                    se = rightSe;
                }
            }
        } else {
            // 裸眼视力不同，取视力低的眼别
            if (nakedVisionResult.getSecond().equals(CommonConst.LEFT_EYE)) {
                // 左眼的等效球镜
                se = leftSe;
            } else {
                // 右眼的等效球镜
                se = rightSe;
            }
        }
        // SE >= 0
        if (se.compareTo(new BigDecimal("0.00")) >= 0) {
            return "目前尚无近视高危风险";
        } else {
            // SE < 0
            return "可能存在近视高危因素，建议严格注意用眼卫生，到医疗机构检查了解是否可能发展为近视。";
        }
    }

    /**
     * 获取短信通知详情
     *
     * @param studentName      学校名称
     * @param leftNakedVision  左眼裸眼视力
     * @param rightNakedVision 右眼裸眼视力
     * @param advice           建议
     * @return 短信通知详情
     */
    private String getSMSNoticeInfo(String studentName, BigDecimal leftNakedVision, BigDecimal rightNakedVision, String advice) {
        return String.format(CommonConst.SEND_SMS_TO_PARENT_MESSAGE, packageStudentName(studentName),
                leftNakedVision.toString(), rightNakedVision.toString(), advice);
    }

    /**
     * 封装短信内容需要的学生姓名
     * <p>超过4个字符以上：显示前5个字符，其中前3个字符正常回显，后2个字符用*代替。
     * 如陈旭格->陈旭格、陈旭格力->陈旭格力、陈旭格力哈->陈旭格**、陈旭格力哈特->陈旭格**
     * </p>
     *
     * @param studentName 学生姓名
     * @return 学生姓名
     */
    private String packageStudentName(String studentName) {
        if (studentName.length() < 5) {
            return studentName;
        }
        return StringUtils.overlay(studentName, "**", 3, studentName.length());
    }

    /**
     * String 转换成List
     *
     * @param string 字符串
     * @return 字符串
     */
    public static List<String> str2List(String string) {
        if (StringUtils.isNotBlank(string)) {
            return Arrays.stream(string.split(",")).map(String::valueOf)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 发送短信
     *
     * @param mpParentPhone 家长端绑定的手机号码
     * @param parentPhone   多端绑定的手机号码
     * @param noticeInfo    短信内容
     */
    private void sendSMS(List<String> mpParentPhone, String parentPhone, String noticeInfo) {
        log.info("noticeInfo:{}", noticeInfo);
        // 优先家长端绑定的手机号码
//        if (CollectionUtils.isNotEmpty(mpParentPhone)) {
//            mpParentPhone.forEach(phone -> vistelToolsService.sendMsg(new MsgData(phone, "+86", noticeInfo)));
//            return;
//        }
//        if (StringUtils.isNotBlank(parentPhone)) {
//            vistelToolsService.sendMsg(new MsgData(parentPhone, "+86", noticeInfo));
//        }
    }
}