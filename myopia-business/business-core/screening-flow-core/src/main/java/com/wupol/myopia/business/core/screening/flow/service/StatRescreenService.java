package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.StatRescreenMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
import io.jsonwebtoken.lang.Assert;
import org.springframework.stereotype.Service;

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
        return super.list(new QueryWrapper(statRescreen));
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
        return baseMapper.countByPlanAndSchool(planId, schoolId);
    }

}
