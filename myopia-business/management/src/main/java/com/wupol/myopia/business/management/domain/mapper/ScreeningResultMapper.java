package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.ScreeningResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface ScreeningResultMapper extends BaseMapper<ScreeningResult> {

    List<Integer> getSchoolIdByTaskId(Integer taskId);

    List<Integer> getCreateUserIdByTaskId(Integer taskId);

}
