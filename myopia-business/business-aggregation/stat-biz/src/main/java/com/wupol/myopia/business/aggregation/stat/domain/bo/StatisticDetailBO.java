package com.wupol.myopia.business.aggregation.stat.domain.bo;

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
     /**
      * 筛查计划ID
      */
     private Integer screeningPlanId;
     /**
      * 筛查通知ID
      */
     private Integer screeningNoticeId;
     /**
      * 学校ID
      */
     private Integer schoolId;
     /**
      * 学校类型（幼儿园和小学及以上）
      */
     private Integer type;
     /**
      * 学校信息
      */
     private School school;
     /**
      * 当前用户
      */
     private CurrentUser user;
}
