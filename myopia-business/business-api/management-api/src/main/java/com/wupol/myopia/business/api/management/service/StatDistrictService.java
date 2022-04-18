package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.vo.KindergartenResultVO;
import com.wupol.myopia.business.api.management.domain.vo.PrimarySchoolAndAboveResultVO;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningResultStatisticDetailVO;
import com.wupol.myopia.business.common.utils.constant.BizMsgConstant;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.stat.domain.model.ScreeningResultStatistic;
import com.wupol.myopia.business.core.stat.service.CommonDiseaseScreeningResultStatisticService;
import com.wupol.myopia.business.core.stat.service.VisionScreeningResultStatisticService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 按区域统计
 *
 * @author hang.yuan 2022/4/15 16:16
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class StatDistrictService {

    private final DistrictService districtService;
    private final ScreeningNoticeService screeningNoticeService;
    private final VisionScreeningResultStatisticService visionScreeningResultStatisticService;
    private final CommonDiseaseScreeningResultStatisticService commonDiseaseScreeningResultStatisticService;

    /**
     * 按区域-获取幼儿园数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public KindergartenResultVO getKindergartenResult(Integer districtId, Integer noticeId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(currentUser)){
            return new KindergartenResultVO();
        }
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException(BizMsgConstant.CAN_NOT_FIND_NOTICE);
        }
        if (Objects.equals(0,screeningNotice.getScreeningType())){
            return getVisionKindergartenResultVO(screeningNotice,districtId,noticeId);
        }else {
            return getCommonDiseaseKindergartenResultVO(screeningNotice,districtId,noticeId);
        }
    }

    /**
     * 按区域-视力筛查-幼儿园
     */
    public KindergartenResultVO getVisionKindergartenResultVO(ScreeningNotice screeningNotice,Integer districtId, Integer noticeId){

        //查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> visionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,0,Boolean.TRUE);
        if (CollectionUtils.isEmpty(visionStatistics)) {
            return new KindergartenResultVO();
        }
        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, visionStatistics);

        //查找当前层级的数据（非合计数据）
        List<ScreeningResultStatistic> currentVisionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentDistrictId(noticeId, districtId, Boolean.FALSE,0,Boolean.TRUE);
        ScreeningResultStatistic currentVisionStatistic = null;
        if (CollectionUtils.isNotEmpty(currentVisionStatistics)) {
            currentVisionStatistic = currentVisionStatistics.stream().findFirst().orElse(null);
        }
        //构建数据
        return getKindergartenResultVO(visionStatistics,districtId,districtInfo.getFirst(),screeningNotice,districtInfo.getSecond(),currentVisionStatistic);
    }


    /**
     * 按区域-常见病-幼儿园
     */
    public KindergartenResultVO getCommonDiseaseKindergartenResultVO(ScreeningNotice screeningNotice,Integer districtId, Integer noticeId){
        //查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> visionStatistics = commonDiseaseScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,1,Boolean.TRUE);
        if (CollectionUtils.isEmpty(visionStatistics)) {
            return new KindergartenResultVO();
        }
        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, visionStatistics);

        //查找当前层级的数据（非合计数据）
        List<ScreeningResultStatistic> currentVisionStatistics = commonDiseaseScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentDistrictId(noticeId, districtId, Boolean.FALSE,1,Boolean.TRUE);
        ScreeningResultStatistic currentVisionStatistic = null;
        if (CollectionUtils.isNotEmpty(currentVisionStatistics)) {
            currentVisionStatistic = currentVisionStatistics.stream().findFirst().orElse(null);
        }
        //构建数据
        return getKindergartenResultVO(visionStatistics,districtId,districtInfo.getFirst(),screeningNotice,districtInfo.getSecond(),currentVisionStatistic);
    }

    private KindergartenResultVO getKindergartenResultVO(List<ScreeningResultStatistic> visionStatistics,Integer districtId,
                                                         String currentRangeName,ScreeningNotice screeningNotice,Map<Integer, String> districtIdNameMap,
                                                         ScreeningResultStatistic currentVisionStatistic){

        KindergartenResultVO kindergartenResultVO = new KindergartenResultVO();
        if(CollectionUtil.isEmpty(visionStatistics)){
            return kindergartenResultVO;
        }
        kindergartenResultVO.setBasicData(districtId,currentRangeName,screeningNotice);
        kindergartenResultVO.setCurrentData(currentVisionStatistic);
        kindergartenResultVO.setItemData(districtId,visionStatistics,districtIdNameMap);
        return kindergartenResultVO;
    }

    /**
     * 按区域-获取小学及以上数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public PrimarySchoolAndAboveResultVO getPrimarySchoolAndAboveResult(Integer districtId, Integer noticeId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(currentUser)){
            return new PrimarySchoolAndAboveResultVO();
        }
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException(BizMsgConstant.CAN_NOT_FIND_NOTICE);
        }
        if (Objects.equals(0,screeningNotice.getScreeningType())){
            return getVisionPrimarySchoolAndAboveResultVO(screeningNotice,districtId,noticeId);
        }else {
            return getCommonDiseasePrimarySchoolAndAboveResultVO(screeningNotice,districtId,noticeId);
        }
    }

    /**
     * 按区域-视力筛查-小学及以上
     */
    private PrimarySchoolAndAboveResultVO getVisionPrimarySchoolAndAboveResultVO(ScreeningNotice screeningNotice, Integer districtId, Integer noticeId) {
        //查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> visionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,0,Boolean.FALSE);
        if (CollectionUtils.isEmpty(visionStatistics)) {
            return new PrimarySchoolAndAboveResultVO();
        }
        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, visionStatistics);

        //查找当前层级的数据（非合计数据）
        List<ScreeningResultStatistic> currentVisionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentDistrictId(noticeId, districtId, Boolean.FALSE,0,Boolean.FALSE);
        ScreeningResultStatistic currentVisionStatistic = null;
        if (CollectionUtils.isNotEmpty(currentVisionStatistics)) {
            currentVisionStatistic = currentVisionStatistics.stream().findFirst().orElse(null);
        }
        //构建数据
        return getPrimarySchoolAndAboveResultVO(visionStatistics,districtId,districtInfo.getFirst(),screeningNotice,districtInfo.getSecond(),currentVisionStatistic);
    }

    /**
     * 按区域-常见病-小学及以上
     */
    private PrimarySchoolAndAboveResultVO getCommonDiseasePrimarySchoolAndAboveResultVO(ScreeningNotice screeningNotice, Integer districtId, Integer noticeId) {
        //查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> visionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,0,Boolean.FALSE);
        if (CollectionUtils.isEmpty(visionStatistics)) {
            return new PrimarySchoolAndAboveResultVO();
        }
        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, visionStatistics);

        //查找当前层级的数据（非合计数据）
        List<ScreeningResultStatistic> currentVisionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentDistrictId(noticeId, districtId, Boolean.FALSE,0,Boolean.FALSE);
        ScreeningResultStatistic currentVisionStatistic = null;
        if (CollectionUtils.isNotEmpty(currentVisionStatistics)) {
            currentVisionStatistic = currentVisionStatistics.stream().findFirst().orElse(null);
        }
        //构建数据
        return getPrimarySchoolAndAboveResultVO(visionStatistics,districtId,districtInfo.getFirst(),screeningNotice,districtInfo.getSecond(),currentVisionStatistic);
    }



    private PrimarySchoolAndAboveResultVO getPrimarySchoolAndAboveResultVO(List<ScreeningResultStatistic> visionStatistics,Integer districtId,
                                                         String currentRangeName,ScreeningNotice screeningNotice,Map<Integer, String> districtIdNameMap,
                                                         ScreeningResultStatistic currentVisionStatistic){
        PrimarySchoolAndAboveResultVO primarySchoolAndAboveResultVO = new PrimarySchoolAndAboveResultVO();
        if(CollectionUtil.isEmpty(visionStatistics)){
            return primarySchoolAndAboveResultVO;
        }
        primarySchoolAndAboveResultVO.setBasicData(districtId,currentRangeName,screeningNotice);
        primarySchoolAndAboveResultVO.setCurrentData(currentVisionStatistic);
        primarySchoolAndAboveResultVO.setItemData(districtId,visionStatistics,districtIdNameMap);
        return primarySchoolAndAboveResultVO;
    }

    /**
     * 按区域-获取数据详情（合计）
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public ScreeningResultStatisticDetailVO getScreeningResultTotalDetail(Integer districtId, Integer noticeId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(currentUser)){
            return new ScreeningResultStatisticDetailVO();
        }
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException(BizMsgConstant.CAN_NOT_FIND_NOTICE);
        }
        if (Objects.equals(0,screeningNotice.getScreeningType())){
            return getVisionScreeningResultStatisticDetailVO(screeningNotice,districtId,noticeId);
        }else {
            return getCommonDiseaseScreeningResultStatisticDetailVO(screeningNotice,districtId,noticeId);
        }
    }

    /**
     * 按区域-视力筛查-获取数据详情（合计）
     */
    private ScreeningResultStatisticDetailVO getVisionScreeningResultStatisticDetailVO(ScreeningNotice screeningNotice, Integer districtId, Integer noticeId) {
        //幼儿园 查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> kindergartenVisionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,0,Boolean.TRUE);

        //小学及以上 查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> primarySchoolAndAboveVisionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,0,Boolean.FALSE);
        if (CollectionUtils.isEmpty(kindergartenVisionStatistics) && CollectionUtil.isEmpty(primarySchoolAndAboveVisionStatistics)) {
            return new ScreeningResultStatisticDetailVO();
        }

        List<ScreeningResultStatistic> statistics=Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(kindergartenVisionStatistics)){
            statistics.addAll(kindergartenVisionStatistics);
        }
        if (CollectionUtil.isNotEmpty(primarySchoolAndAboveVisionStatistics)){
            statistics.addAll(primarySchoolAndAboveVisionStatistics);
        }

        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, statistics);

        return getScreeningResultStatisticDetailVO(kindergartenVisionStatistics,primarySchoolAndAboveVisionStatistics,districtId,districtInfo.getFirst(),screeningNotice,districtInfo.getSecond());
    }

    /**
     * 按区域-常见病筛查-获取数据详情（合计）
     */
    private ScreeningResultStatisticDetailVO getCommonDiseaseScreeningResultStatisticDetailVO(ScreeningNotice screeningNotice, Integer districtId, Integer noticeId) {
        //幼儿园 查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> kindergartenVisionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,1,Boolean.TRUE);

        //小学及以上 查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> primarySchoolAndAboveVisionStatistics = visionScreeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,1,Boolean.FALSE);
        if (CollectionUtils.isEmpty(kindergartenVisionStatistics) && CollectionUtil.isEmpty(primarySchoolAndAboveVisionStatistics)) {
            return new ScreeningResultStatisticDetailVO();
        }
        List<ScreeningResultStatistic> statistics=Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(kindergartenVisionStatistics)){
            statistics.addAll(kindergartenVisionStatistics);
        }
        if (CollectionUtil.isNotEmpty(primarySchoolAndAboveVisionStatistics)){
            statistics.addAll(primarySchoolAndAboveVisionStatistics);
        }

        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, statistics);

        return getScreeningResultStatisticDetailVO(kindergartenVisionStatistics,primarySchoolAndAboveVisionStatistics,districtId,districtInfo.getFirst(),screeningNotice,districtInfo.getSecond());
    }

    private ScreeningResultStatisticDetailVO getScreeningResultStatisticDetailVO(List<ScreeningResultStatistic> kindergartenVisionStatistics,
                                                                                 List<ScreeningResultStatistic> primarySchoolAndAboveVisionStatistics,
                                                                                 Integer districtId,String currentRangeName,ScreeningNotice screeningNotice,
                                                                                 Map<Integer, String> districtIdNameMap){

        ScreeningResultStatisticDetailVO screeningResultStatisticDetailVO = new ScreeningResultStatisticDetailVO();
        if(CollectionUtil.isEmpty(kindergartenVisionStatistics) && CollectionUtil.isEmpty(primarySchoolAndAboveVisionStatistics)){
            return screeningResultStatisticDetailVO;
        }
        screeningResultStatisticDetailVO.setBasicData(districtId,currentRangeName,screeningNotice);
        screeningResultStatisticDetailVO.setItemData(districtId,kindergartenVisionStatistics,primarySchoolAndAboveVisionStatistics);
        return screeningResultStatisticDetailVO;

    }


    private TwoTuple<String,Map<Integer, String>> districtInfo(Integer districtId,List<ScreeningResultStatistic> screeningResultStatistics){
        //获取当前范围名
        String currentRangeName = districtService.getDistrictNameByDistrictId(districtId);
        // 获取districtIds 的所有名字
        Set<Integer> districtIds = screeningResultStatistics.stream().map(ScreeningResultStatistic::getDistrictId).collect(Collectors.toSet());
        List<District> districts = districtService.getDistrictByIds(Lists.newArrayList(districtIds));
        Map<Integer, String> districtIdNameMap = districts.stream().collect(Collectors.toMap(District::getId, District::getName));
        districtIdNameMap.put(districtId, currentRangeName);
        return new TwoTuple<>(currentRangeName,districtIdNameMap);
    }
}
