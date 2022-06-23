package com.wupol.myopia.business.api.management.domain.bo;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.school.domain.model.School;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统计详情业务流转实体
 *
 * @author hang.yuan 2022/5/11 17:37
 */
@Data
@Accessors(chain = true)
public class StatisticDetailBO {
     private Integer screeningPlanId;
     private Integer screeningNoticeId;
     private Integer schoolId;
     private Integer type;
     private School school;
     private CurrentUser user;
}
