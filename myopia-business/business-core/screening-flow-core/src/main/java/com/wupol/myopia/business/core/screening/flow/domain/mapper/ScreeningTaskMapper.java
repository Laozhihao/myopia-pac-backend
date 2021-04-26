package com.wupol.myopia.business.core.screening.flow.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningOrgPlanResponseDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskPageDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查通知任务或者计划表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningTaskMapper extends BaseMapper<ScreeningTask> {

    IPage<ScreeningOrgPlanResponseDTO> getTaskByIds(@Param("page") Page<?> page, @Param("ids") List<Integer> ids);

    IPage<ScreeningTaskPageDTO> selectPageByQuery(@Param("page") Page<ScreeningTask> page, @Param("param") ScreeningTaskQueryDTO query);

    Integer countByNoticeIdAndGovId(@Param("noticeId") Integer noticeId, @Param("govId") Integer govId);
}
