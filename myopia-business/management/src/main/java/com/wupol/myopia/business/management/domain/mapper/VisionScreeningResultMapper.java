package com.wupol.myopia.business.management.domain.mapper;

import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.management.domain.vo.StudentScreeningCountVO;

import java.util.List;

/**
 * Mapper接口
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
public interface VisionScreeningResultMapper extends BaseMapper<VisionScreeningResult> {

    List<Integer> getSchoolIdByTaskId(Integer taskId);

    List<Integer> getCreateUserIdByTaskId(Integer taskId);

    List<StudentScreeningCountVO> countScreeningTime();

}