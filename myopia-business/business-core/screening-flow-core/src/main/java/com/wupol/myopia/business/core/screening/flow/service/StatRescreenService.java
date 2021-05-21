package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.StatRescreenMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatRescreen;
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

}
