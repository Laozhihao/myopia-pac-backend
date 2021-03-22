package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.mapper.DistrictBigScreenStatisticMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.vo.bigscreening.BigScreeningVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2021-03-07
 */
@Service
public class DistrictBigScreenStatisticService extends BaseService<DistrictBigScreenStatisticMapper, DistrictBigScreenStatistic> {

    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private DistrictService districtService;

    /**
     * 获取最新的数据
     *
     * @param currentUser
     */
    public BigScreeningVO getLatestData(CurrentUser currentUser) {
        if (!currentUser.isGovDeptUser()) {
            //todo 日志 异常
            return null;
        }
        //根据角色获取当前的id
        District district = districtService.getNotPlatformAdminUserDistrict(currentUser);
        if (district == null) {
            //todo 日志 异常
            return null;
        }
        //查找最新的noticeId
        ScreeningNotice screeningNotice = screeningNoticeService.getLatestNoticeByUser(currentUser);
        if (screeningNotice == null) {
            return null;
        }
        //根据noticeId 和 districtId 查找数据
        DistrictBigScreenStatistic districtBigScreenStatistic = this.getByNoticeIdAndDistrictId(screeningNotice.getId(), district.getId());
        if (districtBigScreenStatistic == null) {
            return null;
        }
        //对数据进行整合
        return BigScreeningVO.getNewInstance(screeningNotice, districtBigScreenStatistic);
    }

    /**
     * 根据通知id和地区id获取数据
     *
     * @param noticeId
     * @param districtId
     * @return
     */
    private DistrictBigScreenStatistic getByNoticeIdAndDistrictId(Integer noticeId, Integer districtId) {
        LambdaQueryWrapper<DistrictBigScreenStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictBigScreenStatistic::getScreeningNoticeId, noticeId).eq(DistrictBigScreenStatistic::getDistrictId, districtId);
        return baseMapper.selectOne(queryWrapper);
    }
}
