package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 市区创建任务
 *
 * @author Simple4H
 */
@Service
public class UrbanAreaTaskService {

    @Resource
    private ScreeningNoticeBizService screeningNoticeBizService;

    @Resource
    private ScreeningTaskBizService screeningTaskBizService;

    /**
     * 创建任务
     *
     * @param screeningTaskDTO 请求入参
     * @param user             登陆用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTask(ScreeningTaskDTO screeningTaskDTO, CurrentUser user) {
        ScreeningNotice screeningNotice = screeningNoticeBizService.saveNotice(screeningTaskDTO, user.getId());
        screeningNoticeBizService.publishNotice(screeningNotice, user);
        screeningTaskBizService.createTask(screeningNotice, screeningTaskDTO, user);;
    }

}
