package com.wupol.myopia.business.management.domain.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 筛查通知任务或者计划表Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningTaskMapper extends BaseMapper<ScreeningTask> {

    IPage<ScreeningTask> getTaskByIds(@Param("page") Page<?> page, @Param("ids") List<Integer> ids);

    IPage<ScreeningTaskVo> selectPageByQuery(@Param("page") Page<ScreeningTask> page, @Param("param") ScreeningTaskQuery query);
}
