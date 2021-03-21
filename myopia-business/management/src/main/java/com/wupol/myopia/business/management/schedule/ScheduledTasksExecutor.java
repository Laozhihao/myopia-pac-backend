package com.wupol.myopia.business.management.schedule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.CompareUtil;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.builder.DistrictBigScreenStatisticBuilder;
import com.wupol.myopia.business.management.domain.dto.BigScreenStatDataDTO;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
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
//    @Scheduled(cron = "*/20 * * * * ?", zone = "GMT+8:00")
    public void statistic() {
        //1. 查询出需要统计的通知（根据筛查数据vision_screening_result的更新时间判断）
        List<Integer> yesterdayScreeningPlanIds = visionScreeningResultService.getYesterdayScreeningPlanIds();
        if (CollectionUtils.isEmpty(yesterdayScreeningPlanIds)) {
            log.info("筛查数据统计：前一天无筛查数据，无需统计");
            return;
        }
        List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics = new ArrayList<>();
        List<DistrictMonitorStatistic> districtMonitorStatistics = new ArrayList<>();
        List<DistrictVisionStatistic> districtVisionStatistics = new ArrayList<>();
        List<SchoolVisionStatistic> schoolVisionStatistics = new ArrayList<>();
        List<SchoolMonitorStatistic> schoolMonitorStatistics = new ArrayList<>();
        genDistrictStatistics(yesterdayScreeningPlanIds, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics);
        genSchoolStatistics(yesterdayScreeningPlanIds, schoolVisionStatistics, schoolMonitorStatistics);
        districtAttentiveObjectsStatisticService.batchSaveOrUpdate(districtAttentiveObjectsStatistics);
        districtMonitorStatisticService.batchSaveOrUpdate(districtMonitorStatistics);
        districtVisionStatisticService.batchSaveOrUpdate(districtVisionStatistics);
        schoolVisionStatisticService.batchSaveOrUpdate(schoolVisionStatistics);
        schoolMonitorStatisticService.batchSaveOrUpdate(schoolMonitorStatistics);
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
            //3.2 每个学校分别统计
            schoolIdStatConslusions.keySet().forEach(schoolId -> {
                List<StatConclusionVo> schoolStatConclusion = schoolIdStatConslusions.get(schoolId);
                Map<Boolean, List<StatConclusionVo>> isValidMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
                Map<Boolean, List<StatConclusionVo>> isRescreenTotalMap = schoolStatConclusion.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                List<StatConclusionVo> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
                Map<Boolean, List<StatConclusionVo>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
                schoolVisionStatistics.add(SchoolVisionStatistic.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), screeningPlanId, screeningPlan.getDistrictId(), isRescreenMap.getOrDefault(false, Collections.emptyList()), screeningPlan.getStudentNumbers(), isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
                schoolMonitorStatistics.add(SchoolMonitorStatistic.build(schoolIdMap.get(schoolId), screeningOrg, screeningPlan.getSrcScreeningNoticeId(), screeningPlan.getScreeningTaskId(), screeningPlan.getDistrictId(), isRescreenMap.getOrDefault(true, Collections.emptyList()), screeningPlan.getStudentNumbers(), isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
            });
        });
    }

    /**
     * 按区域层级生成统计数据
     *
     * @param yesterdayScreeningPlanIds
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     */
    private void genDistrictStatistics(List<Integer> yesterdayScreeningPlanIds, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics) {
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
            List<ScreeningPlan> screeningPlans = screeningPlanService.getBySrcScreeningNoticeId(screeningNoticeId);
            //2.2 层级维度统计
            Map<Integer, List<StatConclusion>> districtStatConclusions = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getDistrictId));
            Map<Integer, List<ScreeningPlan>> districtScreeningPlans = screeningPlans.stream().collect(Collectors.groupingBy(ScreeningPlan::getDistrictId));
            //2.3 查出通知对应的顶级层级
            ScreeningNotice screeningNotice = screeningNoticeService.getById(screeningNoticeId);
            genStatisticsByDistrictId(screeningNoticeId, screeningNotice.getDistrictId(), districtScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics, districtStatConclusions);
        });
    }

    /**
     * 生成层级的统计数据
     *
     * @param screeningNoticeId
     * @param districtId
     * @param districtScreeningPlans
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param districtStatConclusions            所有的筛查数据
     */
    private void genStatisticsByDistrictId(Integer screeningNoticeId, Integer districtId, Map<Integer, List<ScreeningPlan>> districtScreeningPlans, List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics, List<DistrictVisionStatistic> districtVisionStatistics, Map<Integer, List<StatConclusion>> districtStatConclusions) {
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
        List<Integer> haveScreeningPlansChildDistrictIds = CompareUtil.getRetain(childDistrictIds, districtScreeningPlans.keySet());
        List<StatConclusion> totalStatConclusions = haveStatConclusionsChildDistrictIds.stream().map(districtStatConclusions::get).flatMap(Collection::stream).collect(Collectors.toList());
        List<ScreeningPlan> totalScreeningPlans = haveScreeningPlansChildDistrictIds.stream().map(districtScreeningPlans::get).flatMap(Collection::stream).collect(Collectors.toList());
        List<StatConclusion> selfStatConclusions = districtStatConclusions.getOrDefault(districtId, Collections.emptyList());
        List<ScreeningPlan> selfScreeningPlans = districtScreeningPlans.getOrDefault(districtId, Collections.emptyList());

        genTotalStatistics(screeningNoticeId, districtId, totalScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics, totalStatConclusions);
        genSelfStatistics(screeningNoticeId, districtId, selfScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics, selfStatConclusions);
        if (totalStatConclusions.size() != selfStatConclusions.size()) {
            //递归统计下层级数据
            childDistricts.forEach(childDistrict -> genStatisticsByDistrictId(screeningNoticeId, childDistrict.getId(), districtScreeningPlans, districtAttentiveObjectsStatistics, districtMonitorStatistics, districtVisionStatistics, districtStatConclusions));
        }
    }

    /**
     * 生成自己层级的筛查数据
     *
     * @param screeningNoticeId
     * @param districtId
     * @param screeningPlans
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param selfStatConclusions
     */
    private void genSelfStatistics(Integer screeningNoticeId, Integer districtId, List<ScreeningPlan> screeningPlans,
                                   List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics,
                                   List<DistrictVisionStatistic> districtVisionStatistics, List<StatConclusion> selfStatConclusions) {
        if (CollectionUtils.isEmpty(selfStatConclusions)) {
            return;
        }
        Map<Boolean, List<StatConclusion>> isValidMap = selfStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = selfStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        // 层级自己的筛查数据肯定属于同一个任务，所以只取第一个的就可以
        Integer screeningTaskId = selfStatConclusions.get(0).getTaskId();
        Integer totalPlanStudentNum = screeningPlans.stream().mapToInt(ScreeningPlan::getStudentNumbers).sum();
        districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), totalPlanStudentNum));
        districtMonitorStatistics.add(DistrictMonitorStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(true, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
        districtVisionStatistics.add(DistrictVisionStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.NOT_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
    }

    /**
     * 生成层级所能看到的总的筛查数据
     *
     * @param screeningNoticeId
     * @param districtId
     * @param screeningPlans
     * @param districtAttentiveObjectsStatistics
     * @param districtMonitorStatistics
     * @param districtVisionStatistics
     * @param totalStatConclusions
     */
    private void genTotalStatistics(Integer screeningNoticeId, Integer districtId, List<ScreeningPlan> screeningPlans,
                                    List<DistrictAttentiveObjectsStatistic> districtAttentiveObjectsStatistics, List<DistrictMonitorStatistic> districtMonitorStatistics,
                                    List<DistrictVisionStatistic> districtVisionStatistics, List<StatConclusion> totalStatConclusions) {
        if (CollectionUtils.isEmpty(totalStatConclusions)) {
            return;
        }
        Map<Boolean, List<StatConclusion>> isValidMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsValid));
        Map<Boolean, List<StatConclusion>> isRescreenTotalMap = totalStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        List<StatConclusion> validStatConclusions = isValidMap.getOrDefault(true, Collections.emptyList());
        Map<Boolean, List<StatConclusion>> isRescreenMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getIsRescreen));
        // 层级总的筛查数据不一定属于同一个任务，所以取默认0
        Integer screeningTaskId = CommonConst.DEFAULT_ID;
        Integer totalPlanStudentNum = screeningPlans.stream().mapToInt(ScreeningPlan::getStudentNumbers).sum();
        districtAttentiveObjectsStatistics.add(DistrictAttentiveObjectsStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), totalPlanStudentNum));
        districtMonitorStatistics.add(DistrictMonitorStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(true, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
        districtVisionStatistics.add(DistrictVisionStatistic.build(screeningNoticeId, screeningTaskId, districtId, CommonConst.IS_TOTAL, isRescreenMap.getOrDefault(false, Collections.emptyList()), totalPlanStudentNum, isRescreenTotalMap.getOrDefault(false, Collections.emptyList()).size()));
    }

    /**
     * 筛查数据统计
     */
    //@Scheduled(cron = "0 5 0 * * ?", zone = "GMT+8:00")
    //@Scheduled(cron = "*/20 * * * * ?", zone = "GMT+8:00")
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
                districtBigScreenStatisticService.getBaseMapper().insert(districtBigScreenStatistic);
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
        District district = districtService.getById(provinceDistrictId);
        List<District> cityDistrictList = districtService.getChildDistrictByParentIdPriorityCache(district.getCode());
        Set<Integer> cityDistrictIdList = cityDistrictList.stream().map(District::getId).collect(Collectors.toSet());
        //根据条件查找所有的元素：条件 cityDistrictIds 非复测 有效
        List<BigScreenStatDataDTO> bigScreenStatDataDTOs = statConclusionService.getByNoticeidAndDistrictIds(cityDistrictIdList, screeningNotice.getId());
        if (CollectionUtils.isEmpty(bigScreenStatDataDTOs)) {
            return null;
        }
        //将所在区域按城市分
        Map<District, Set<Integer>> districtSetMap = districtService.getCityAllDistrictIds(provinceDistrictId);
        this.updateCityName(bigScreenStatDataDTOs, districtSetMap);
        //Map<Integer, String> districtIdNameMap = districtSet.stream().filter(Objects::nonNull).collect(Collectors.toMap(e -> e.getId(), e -> e.getName()));
        //获取地图数据
        BigScreenMap bigScreenMap = bigScreenMapService.getByDistrictId(provinceDistrictId);
        DistrictBigScreenStatistic districtBigScreenStatistic = DistrictBigScreenStatisticBuilder.getBuilder()
                .setRealScreeningNum((long) CollectionUtils.size(bigScreenStatDataDTOs))
                .setDistrictId(provinceDistrictId)
                .setBigScreenStatDataDTOList(bigScreenStatDataDTOs)
                .setMapJson(bigScreenMap.getJson())
                .setCityCenterMap(bigScreenMap.getCityCenterLocation())
                .setNoticeId(screeningNotice.getId()).build();
        //进行统
        return districtBigScreenStatistic;
    }

    /**
     * 获取城市名
     *
     * @param bigScreenStatDataDTOs
     * @param districtSetMap
     */
    private void updateCityName(List<BigScreenStatDataDTO> bigScreenStatDataDTOs, Map<District, Set<Integer>> districtSetMap) {
        bigScreenStatDataDTOs = bigScreenStatDataDTOs.stream().map(bigScreenStatDataDTO -> {
            districtSetMap.forEach((cityDistrict, districtIds) -> {
                if (districtIds.contains(bigScreenStatDataDTO.getDistrictId()) || cityDistrict.getId().equals(bigScreenStatDataDTO.getDistrictId())) {
                    bigScreenStatDataDTO.setCityDistrictId(cityDistrict.getId());
                    bigScreenStatDataDTO.setCityDistrictName(cityDistrict.getName());
                }
            });
            return bigScreenStatDataDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 生成地图
     */
    //@Scheduled(cron = "*/20 * * * * ?", zone = "GMT+8:00")
    public void generator() {
        // 找到所有省级的地区
        LambdaQueryWrapper<District> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(District::getParentCode,"100000000");
        List<District> districts = districtService.getBaseMapper().selectList(queryWrapper);
        // 转换成地区code 访问json
        districts.stream().forEach(district -> {
            Long code = district.getCode();
            Object jsonObject = getJSONObject(code);
            BigScreenMap bigScreenMap = new BigScreenMap();
            //请求高德
            bigScreenMap.setDistrictId(district.getId()).setJson(jsonObject).setCreateTime(new Date());
            bigScreenMapService.getBaseMapper().insert(bigScreenMap);
        });
        // 保存下来
    }

    /**
     * 定时更新城市的坐标（目前只包含大陆）
     * todo 待确认再删除
     */
    //@Scheduled(cron = "*/20 * * * * ?", zone = "GMT+8:00")
    public void city() {
        // 找到所有省级的地区
        LambdaQueryWrapper<BigScreenMap> queryWrapper = new LambdaQueryWrapper<>();
        List<BigScreenMap> bigScreenMaps = bigScreenMapService.getBaseMapper().selectList(queryWrapper);
        // 转换成地区code 访问json

        bigScreenMaps.stream().forEach(bigScreenMap -> {
            Map<Integer, JSONArray> longJSONArrayHashMap = new HashMap<>();
            Object json = bigScreenMap.getJson();
            Object read = JSONPath.read(JSON.toJSONString(json), "$.features");
            JSONArray features = (JSONArray)read;
            features.stream().forEach(feature->{
                String name = (String)JSONPath.read(JSON.toJSONString(feature), "$.properties.name");
                Integer code = (Integer)JSONPath.read(JSON.toJSONString(feature), "$.properties.adcode");
                District district = districtService.getByCode(code * 1000L);
                if (district == null) {
                    try {
                        district = districtService.findOne(new District().setName(name));
                    } catch (Exception e) {
                        System.err.println(name);
                        e.printStackTrace();
                    }
                }
                JSONArray center = (JSONArray)JSONPath.read(JSON.toJSONString(feature), "$.properties.center");
                //string 转换成long
                longJSONArrayHashMap.put(district.getId(),center);
            });
            // todo 待修改 bigScreenMap.setCityCenterLocation(longJSONArrayHashMap);
             bigScreenMapService.getBaseMapper().updateById(bigScreenMap);
        });
    }

    /**
     * todo 待确认删除
     * @return
     */
    public Object getJSONObject(Long code)     {
        code = code / 1000;
        if (code.equals(830000)) {
            //高德的台湾省是710000
            code = 710000L;
        }
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建http GET请求
        HttpGet httpGet = new HttpGet("https://geo.datav.aliyun.com/areas_v2/bound/" + code + "_full.json");
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                return JSONObject.toJSON(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //相当于关闭浏览器
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}