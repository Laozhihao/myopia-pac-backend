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
import com.wupol.myopia.business.core.stat.service.ScreeningResultStatisticService;
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
    private final ScreeningResultStatisticService screeningResultStatisticService;

    /**
     * 按区域-获取幼儿园数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public KindergartenResultVO getKindergartenResult(Integer districtId, Integer noticeId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(currentUser)){
            return null;
        }
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException(BizMsgConstant.CAN_NOT_FIND_NOTICE);
        }
        return getKindergartenResultVO(screeningNotice,districtId,noticeId);
    }

    /**
     * 获取幼儿园统计结果
     * @param screeningNotice 筛查通知数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public KindergartenResultVO getKindergartenResultVO(ScreeningNotice screeningNotice, Integer districtId, Integer noticeId){

        //查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> visionStatistics = getKindergartenResultList(noticeId, districtId, screeningNotice.getScreeningType());
        if (CollectionUtils.isEmpty(visionStatistics)) {
            return null;
        }
        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, visionStatistics);
        //查找当前层级的数据（非合计数据）
        ScreeningResultStatistic currentVisionStatistic = currentVisionStatistic(districtId, noticeId, Boolean.TRUE,screeningNotice.getScreeningType());
        //构建数据
        KindergartenResultVO kindergartenResultVO = new KindergartenResultVO();
        kindergartenResultVO.setBasicData(districtId,districtInfo.getFirst());
        kindergartenResultVO.setCurrentData(currentVisionStatistic);
        kindergartenResultVO.setItemData(districtId,visionStatistics,districtInfo.getSecond());
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
            return null;
        }
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException(BizMsgConstant.CAN_NOT_FIND_NOTICE);
        }
        return getPrimarySchoolAndAboveResultVO(screeningNotice,districtId,noticeId);
    }

    /**
     * 获取小学及以上数据
     * @param screeningNotice 筛查通知数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    private PrimarySchoolAndAboveResultVO getPrimarySchoolAndAboveResultVO(ScreeningNotice screeningNotice, Integer districtId, Integer noticeId) {
        //查找合计数据（包括当前层级 和 下级的，不只页面的那一条“合计”数据）
        List<ScreeningResultStatistic> visionStatistics = getPrimarySchoolAndAboveResultList(noticeId, districtId, screeningNotice.getScreeningType());
        if (CollectionUtils.isEmpty(visionStatistics)) {
            return null;
        }
        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, visionStatistics);
        //获取当前级数据（该统计数据来源于该层级的所有学校，不包括下级的）
        ScreeningResultStatistic currentVisionStatistic = currentVisionStatistic(districtId, noticeId, Boolean.FALSE,screeningNotice.getScreeningType());
        //构建数据
        PrimarySchoolAndAboveResultVO primarySchoolAndAboveResultVO = new PrimarySchoolAndAboveResultVO();
        primarySchoolAndAboveResultVO.setBasicData(districtId,districtInfo.getFirst());
        primarySchoolAndAboveResultVO.setCurrentData(currentVisionStatistic);
        primarySchoolAndAboveResultVO.setItemData(districtId,visionStatistics,districtInfo.getSecond());
        return primarySchoolAndAboveResultVO;

    }

    /**
     * 获取当前筛查统计结果数据
     *
     * @param districtId 区域ID
     * @param noticeId 通知ID
     * @param isKindergarten 是否是幼儿园
     */
    private ScreeningResultStatistic currentVisionStatistic(Integer districtId, Integer noticeId, boolean isKindergarten,Integer screeningType) {
        return screeningResultStatisticService.getStatisticByNoticeIdAndCurrentDistrictId(noticeId, districtId, Boolean.FALSE, screeningType, isKindergarten);
    }


    /**
     * 按区域-获取数据详情（合计）
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    public ScreeningResultStatisticDetailVO getScreeningResultTotalDetail(Integer districtId, Integer noticeId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (Objects.isNull(currentUser)){
            return null;
        }
        ScreeningNotice screeningNotice = screeningNoticeService.getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException(BizMsgConstant.CAN_NOT_FIND_NOTICE);
        }

        return getScreeningResultStatisticDetailVO(screeningNotice,districtId,noticeId);
    }

    /**
     * 按区域-视力筛查-获取数据详情（合计）
     * @param screeningNotice  筛查通知数据
     * @param districtId 区域ID
     * @param noticeId 通知ID
     */
    private ScreeningResultStatisticDetailVO getScreeningResultStatisticDetailVO(ScreeningNotice screeningNotice, Integer districtId, Integer noticeId) {
        TwoTuple<List<ScreeningResultStatistic>, List<ScreeningResultStatistic>> screeningResult = getScreeningResult(noticeId, districtId, screeningNotice.getScreeningType());

        List<ScreeningResultStatistic> kindergartenVisionStatistics = screeningResult.getFirst();
        List<ScreeningResultStatistic> primarySchoolAndAboveVisionStatistics = screeningResult.getSecond();

        if (CollectionUtils.isEmpty(kindergartenVisionStatistics) && CollectionUtil.isEmpty(primarySchoolAndAboveVisionStatistics)) {
            return null;
        }

        List<ScreeningResultStatistic> statistics=Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(kindergartenVisionStatistics)){
            statistics.addAll(kindergartenVisionStatistics);
        }
        if (CollectionUtil.isNotEmpty(primarySchoolAndAboveVisionStatistics)){
            statistics.addAll(primarySchoolAndAboveVisionStatistics);
        }

        TwoTuple<String, Map<Integer, String>> districtInfo = districtInfo(districtId, statistics);

        ScreeningResultStatisticDetailVO screeningResultStatisticDetailVO = new ScreeningResultStatisticDetailVO();
        screeningResultStatisticDetailVO.setBasicData(districtId,districtInfo.getFirst(),screeningNotice);
        screeningResultStatisticDetailVO.setItemData(districtId, kindergartenVisionStatistics,primarySchoolAndAboveVisionStatistics);
        return screeningResultStatisticDetailVO;

    }

    /**
     * 地区信息
     * @param districtId 区域ID
     * @param screeningResultStatistics 筛查结果数据集合
     */
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

    /**
     * 获取幼儿园统计结果集合
     * @param noticeId 通知ID
     * @param districtId 区域ID
     * @param screeningType 筛查类型
     */
    private List<ScreeningResultStatistic> getKindergartenResultList(Integer noticeId,Integer districtId,Integer screeningType){
        return screeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,screeningType,Boolean.TRUE);
    }

    /**
     * 获取小学及以上统计结果集合
     * @param noticeId 通知ID
     * @param districtId 区域ID
     * @param screeningType 筛查类型
     */
    private List<ScreeningResultStatistic> getPrimarySchoolAndAboveResultList(Integer noticeId,Integer districtId,Integer screeningType){
        return screeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,screeningType,Boolean.FALSE);
    }

    /**
     * 获取筛查统计结果
     * @param noticeId 通知ID
     * @param districtId 区域ID
     * @param screeningType 筛查类型
     */
    private TwoTuple<List<ScreeningResultStatistic>,List<ScreeningResultStatistic>> getScreeningResult(Integer noticeId,Integer districtId,Integer screeningType){
        //幼儿园 查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> kindergartenVisionStatistics = screeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,screeningType,Boolean.TRUE);
        //小学及以上 查找合计数据（当前层级 + 下级）
        List<ScreeningResultStatistic> primarySchoolAndAboveVisionStatistics = screeningResultStatisticService.getStatisticByNoticeIdAndCurrentChildDistrictIds(noticeId,districtId,Boolean.TRUE,screeningType,Boolean.FALSE);
        return new TwoTuple<>(kindergartenVisionStatistics,primarySchoolAndAboveVisionStatistics);
    }
}
