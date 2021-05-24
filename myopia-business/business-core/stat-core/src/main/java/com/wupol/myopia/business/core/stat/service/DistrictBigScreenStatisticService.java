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
