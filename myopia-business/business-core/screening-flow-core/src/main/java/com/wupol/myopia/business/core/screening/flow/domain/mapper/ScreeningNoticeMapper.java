package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 筛查通知表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningNoticeMapper extends BaseMapper<ScreeningNotice> {
    Integer release(Integer id);

    IPage<ScreeningNoticeDTO> selectPageByQuery(@Param("page") IPage<?> page, @Param("param") ScreeningNoticeQueryDTO query);

    List<ScreeningNotice> selectByTimePeriods(@Param("param") ScreeningNotice query);

    /**
     * 查找list
     *
     * @param type
     * @param taskId
     * @param govDeptIds
     * @return
     */
    Set<Integer> selectDistrictIds(Integer type, Integer taskId, Set<Integer> govDeptIds);

    ScreeningNotice getByTaskId(Integer taskId);

    List<ScreeningNotice> getByIdsOrderByCreateTime(@Param("ids") List<Integer> ids);

    List<ScreeningNotice> checkTitleExist(@Param("govDeptId") Integer govDeptId,
                                          @Param("title") String title,
                                          @Param("screeningNoticeId") Integer screeningNoticeId);
}
