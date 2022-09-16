package com.wupol.myopia.business.api.school.management.facade;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.aggregation.stat.domain.bo.StatisticDetailBO;
import com.wupol.myopia.business.aggregation.stat.domain.vo.KindergartenResultDetailVO;
import com.wupol.myopia.business.aggregation.stat.facade.StatSchoolFacade;
import com.wupol.myopia.business.api.school.management.domain.vo.SchoolScreeningResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 学校筛查统计门面
 *
 * @author hang.yuan 2022/9/16 20:33
 */
@Component
public class SchoolScreeningStatisticFacade {

    public SchoolScreeningResultVO screeningResult(CurrentUser currentUser) {
        return null;
    }

}
