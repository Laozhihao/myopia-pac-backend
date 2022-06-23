package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.StatRescreenMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
import io.jsonwebtoken.lang.Assert;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author wulizhou
 * @Date 2021/5/20 10:12
 */
@Service
public class StatRescreenService extends BaseService<StatRescreenMapper, StatRescreen> {

    public List<StatRescreen> getList(Integer planId, Integer schoolId) {
        StatRescreen statRescreen = new StatRescreen();
        statRescreen.setPlanId(planId)
                .setSchoolId(schoolId);
        return super.list(new QueryWrapper<>(statRescreen));
    }

    /**
     * 是否存在复测报告
     * @param planId
     * @param schoolId
     * @return
     */
    public boolean hasRescreenReport(Integer planId, Integer schoolId) {
        return countByPlanAndSchool(planId, schoolId) > 0;
    }

    public int countByPlanAndSchool(Integer planId, Integer schoolId) {
        Assert.notNull(planId);
        Assert.notNull(schoolId);
        return baseMapper.countByPlanAndSchool(planId, schoolId, DateUtil.getYesterdayEndTime());
    }

    public List<StatRescreen> getByPlanIdAndSchoolId(List<Integer> planIds,List<Integer> schoolIds){
        Assert.isTrue(CollectionUtil.isNotEmpty(planIds));
        Assert.isTrue(CollectionUtil.isNotEmpty(planIds));
        LambdaQueryWrapper<StatRescreen> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(StatRescreen::getPlanId,planIds);
        queryWrapper.in(StatRescreen::getSchoolId,schoolIds);
        return baseMapper.selectList(queryWrapper);
    }

    public int deleteByScreeningTime(Date screeningTime) {
        return baseMapper.deleteByScreeningTime(screeningTime);
    }

    /**
     * 获取学校日期
     *
     * @param planId   计划Id
     * @param schoolId 学校Id
     * @return 日期
     */
    public List<Date> getSchoolDate(Integer planId, Integer schoolId) {
        return baseMapper.getSchoolDate(planId, schoolId, DateUtil.getYesterdayEndTime());
    }

    public List<StatRescreen> getByPlanAndSchool(Integer planId, Integer schoolId, Date screeningTime) {
        return baseMapper.getByPlanAndSchool(planId, schoolId, screeningTime);
    }

}
