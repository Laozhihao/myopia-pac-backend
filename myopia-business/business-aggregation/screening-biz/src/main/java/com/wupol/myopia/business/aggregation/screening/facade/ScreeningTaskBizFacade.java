package com.wupol.myopia.business.aggregation.screening.facade;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTask;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningTaskOrgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 筛查任务业务
 *
 * @author hang.yuan 2022/10/12 12:06
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ScreeningTaskBizFacade {

    private final ScreeningTaskOrgService screeningTaskOrgService;

    /**
     * 获取发布通知
     * @param id
     * @param user
     * @param screeningTask
     */
    public List<ScreeningNotice> getScreeningNoticeList(Integer id, CurrentUser user, ScreeningTask screeningTask) {
        List<ScreeningTaskOrg> screeningTaskOrgList = screeningTaskOrgService.getOrgListsByTaskId(screeningTask.getId());
        return getScreeningNoticeList(id,user,screeningTask,screeningTaskOrgList);
    }

    /**
     * 获取发布通知
     * @param id
     * @param user
     * @param screeningTask
     */
    public List<ScreeningNotice> getScreeningNoticeList(Integer id, CurrentUser user, ScreeningTask screeningTask ,List<ScreeningTaskOrg> screeningTaskOrgList) {
        List<ScreeningNotice> screeningNoticeList = Lists.newArrayList();
        Map<Integer, List<ScreeningTaskOrg>> orgTypeMap = screeningTaskOrgList.stream().collect(Collectors.groupingBy(ScreeningTaskOrg::getScreeningOrgType));
        if (orgTypeMap.containsKey(ScreeningOrgTypeEnum.ORG.getType())){
            screeningNoticeList.add(buildScreeningNotice(id, user, screeningTask,ScreeningNotice.TYPE_ORG));
        }
        if (orgTypeMap.containsKey(ScreeningOrgTypeEnum.SCHOOL.getType())){
            screeningNoticeList.add(buildScreeningNotice(id, user, screeningTask,ScreeningNotice.TYPE_SCHOOL));
        }
        return screeningNoticeList;
    }


    /**
     * 构建筛查通知
     * @param id
     * @param user
     * @param screeningTask
     * @param type
     */
    private ScreeningNotice buildScreeningNotice(Integer id, CurrentUser user, ScreeningTask screeningTask,Integer type) {
        ScreeningNotice screeningNotice = new ScreeningNotice();
        BeanUtils.copyProperties(screeningTask, screeningNotice);
        screeningNotice.setCreateUserId(user.getId())
                .setOperatorId(user.getId())
                .setOperateTime(new Date())
                .setScreeningTaskId(id)
                .setGovDeptId(CommonConst.DEFAULT_ID)
                .setType(type)
                .setReleaseStatus(CommonConst.STATUS_RELEASE)
                .setReleaseTime(new Date());
        return screeningNotice;
    }

}
