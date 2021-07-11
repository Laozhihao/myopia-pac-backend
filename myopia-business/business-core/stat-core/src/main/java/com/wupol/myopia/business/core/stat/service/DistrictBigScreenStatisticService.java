package com.wupol.myopia.business.core.stat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.stat.domain.mapper.DistrictBigScreenStatisticMapper;
import com.wupol.myopia.business.core.stat.domain.model.DistrictBigScreenStatistic;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021-03-07
 */
@Service
public class DistrictBigScreenStatisticService extends BaseService<DistrictBigScreenStatisticMapper, DistrictBigScreenStatistic> {

    /**
     * 根据通知id和地区id获取数据
     *
     * @param noticeId
     * @param districtId
     * @return
     */
    public DistrictBigScreenStatistic getByNoticeIdAndDistrictId(Integer noticeId, Integer districtId) {
        LambdaQueryWrapper<DistrictBigScreenStatistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DistrictBigScreenStatistic::getScreeningNoticeId, noticeId).eq(DistrictBigScreenStatistic::getDistrictId, districtId);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 保存 或 通过id更新
     * 关于缓存驱逐: 缓存驱逐会在方法执行成功后执行,失败则不执行
     * @param districtBigScreenStatistic
     * @return
     */
    public boolean saveOrUpdateByDistrictIdAndNoticeId(DistrictBigScreenStatistic districtBigScreenStatistic) throws IOException {
        if (null == districtBigScreenStatistic) {
            return false;
        }
        Integer districtId = districtBigScreenStatistic.getDistrictId();
        LambdaQueryWrapper<DistrictBigScreenStatistic> districtBigScreenStatisticLambdaQueryWrapper = new LambdaQueryWrapper<>();
        districtBigScreenStatisticLambdaQueryWrapper.eq(DistrictBigScreenStatistic::getDistrictId, districtId);
        districtBigScreenStatisticLambdaQueryWrapper.eq(DistrictBigScreenStatistic::getScreeningNoticeId,districtBigScreenStatistic.getScreeningNoticeId());
        DistrictBigScreenStatistic districtBigScreenStatistic1 = new DistrictBigScreenStatistic().setDistrictId(districtId).setScreeningNoticeId(districtBigScreenStatistic.getScreeningNoticeId());
        if (Objects.nonNull(districtId) && Objects.nonNull(this.findOne(districtBigScreenStatistic1))) {
            return  this.update(districtBigScreenStatistic, districtBigScreenStatisticLambdaQueryWrapper);
        } else {
            return this.save(districtBigScreenStatistic);
        }
    }
}
