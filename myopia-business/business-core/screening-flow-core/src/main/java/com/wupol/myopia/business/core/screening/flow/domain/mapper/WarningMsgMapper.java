package com.wupol.myopia.business.core.screening.flow.domain.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg;

import java.util.List;
import java.util.Set;

/**
 * Mapper接口
 *
 * @Author jacob
 * @Date 2021-06-08
 */
public interface WarningMsgMapper extends BaseMapper<WarningMsg> {

    /**
     * 查找某种状态的
     *
     * @param sendDate       某一天的数据
     * @param sendStatus 状态 发送的状态
     * @return
     */
    List<WarningMsg> selectNeedToNotice(Integer studentId, Long sendDate, Integer sendStatus);
}
