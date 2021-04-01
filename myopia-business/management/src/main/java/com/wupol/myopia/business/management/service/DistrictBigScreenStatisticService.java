package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.exceptions.ManagementUncheckedException;
import com.wupol.myopia.business.management.domain.mapper.DistrictBigScreenStatisticMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.DistrictBigScreenStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.vo.bigscreening.BigScreeningVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

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
        //根据角色获取当前的id
        District district = districtService.getNotPlatformAdminUserDistrict(currentUser);
        if (district == null) {
            throw new ManagementUncheckedException("无法找到该用户的找到所在区域，user = " + JSON.toJSONString(currentUser));
        }
        //查找最新的noticeId
        ScreeningNotice screeningNotice = screeningNoticeService.getLatestNoticeByUser(currentUser);
        if (screeningNotice == null) {
            throw new ManagementUncheckedException("无法找到该用户的找到筛查通知，user = " + JSON.toJSONString(currentUser));
        }
        //根据noticeId 和 districtId 查找数据
        DistrictBigScreenStatistic districtBigScreenStatistic = this.getByNoticeIdAndDistrictId(screeningNotice.getId(), district.getId());
        if (districtBigScreenStatistic == null) {
            return BigScreeningVO.getImmutableEmptyInstance();
        }
        //对数据进行整合
        return BigScreeningVO.getNewInstance(screeningNotice, districtBigScreenStatistic, district.getName());
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

    /**
     * 保存 或 通过id更新
     *
     * @param districtBigScreenStatistic
     * @return
     */
    public boolean saveOrUpdateByDistrictId(DistrictBigScreenStatistic districtBigScreenStatistic) throws IOException {
        if (null == districtBigScreenStatistic) {
            return false;
        } else {
            Integer districtId = districtBigScreenStatistic.getDistrictId();
            LambdaQueryWrapper<DistrictBigScreenStatistic> districtBigScreenStatisticLambdaQueryWrapper = new LambdaQueryWrapper<>();
            districtBigScreenStatisticLambdaQueryWrapper.eq(DistrictBigScreenStatistic::getDistrictId, districtId);
            return Objects.nonNull(districtId) && !Objects.isNull(this.findOne(new DistrictBigScreenStatistic().setDistrictId(districtId))) ? this.update(districtBigScreenStatistic, districtBigScreenStatisticLambdaQueryWrapper) : this.save(districtBigScreenStatistic);
        }
    }
}
