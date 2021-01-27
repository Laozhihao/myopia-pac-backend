package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.domain.dto.ScreeningTaskResponse;
import com.wupol.myopia.business.management.domain.mapper.ScreeningTaskMapper;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
public class ScreeningTaskService extends BaseService<ScreeningTaskMapper, ScreeningTask> {

    /**
     * 通过ID获取ScreeningTask列表
     *
     * @param pageRequest 分页入参
     * @param ids         ids
     * @return {@link IPage} 统一分页返回体
     */
    public IPage<ScreeningTaskResponse> getTaskByIds(PageRequest pageRequest, List<Integer> ids) {
        return baseMapper.getTaskByIds(pageRequest.toPage(), ids);
    }

}
