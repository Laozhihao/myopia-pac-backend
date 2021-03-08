package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;
<<<<<<< HEAD
import com.wupol.myopia.business.management.domain.vo.StatConclusionVo;
import org.apache.ibatis.annotations.Param;
=======
>>>>>>> 1.0.0.0-manager-app

import java.util.List;

/**
 * 筛查数据结论Mapper接口
 *
 * @Author Jacob
 * @Date 2021-02-22
 */
public interface StatConclusionMapper extends BaseMapper<StatConclusion> {
    /**
     * 获取行政区域内最后一条数据
     */
    StatConclusion selectLastOne(StatConclusionQuery query);

    /**
     * 获取统计结论数据
     * @param noticeId 政府通知ID
     * @param districtIds 行政区域ID列表
<<<<<<< HEAD
     * @return
     */
    List<StatConclusion> listByQuery(StatConclusionQuery query);

    /**
     * 根据筛查计划ID获取Vo列表
     * @param screeningPlanId
     * @return
     */
    List<StatConclusionVo> selectValidVoByScreeningPlanId(@Param("screeningPlanId") Integer screeningPlanId);
=======
     * @return
     */
    List<StatConclusion> listByQuery(StatConclusionQuery query);
>>>>>>> 1.0.0.0-manager-app
}
