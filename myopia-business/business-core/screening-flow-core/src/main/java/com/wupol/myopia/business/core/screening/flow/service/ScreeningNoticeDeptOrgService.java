package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningNoticeDeptOrgMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
public class ScreeningNoticeDeptOrgService extends BaseService<ScreeningNoticeDeptOrgMapper, ScreeningNoticeDeptOrg> {

    @Autowired
    private ScreeningNoticeService screeningNoticeService;

    /**
     * 设置操作人再更新
     *
     * @param entity
     * @param userId
     * @return
     */
    public boolean updateById(ScreeningNoticeDeptOrg entity, Integer userId) {
        entity.setOperatorId(userId);
        return updateById(entity);
    }

    /**
     * 根据通知ID和接收机构ID获取通知
     *
     * @param screeningNoticeId
     * @param acceptOrgId
     * @return
     */
    public ScreeningNoticeDeptOrg getByScreeningNoticeIdAndAcceptOrgId(Integer screeningNoticeId, Integer acceptOrgId) {
        return baseMapper.getByNoticeIdAndOrgId(screeningNoticeId, acceptOrgId);
    }

    /**
     * 查找通过 某个orgId 接受的通知
     *
     * @param type
     * @param acceptOrgId
     * @return
     */
    public List<ScreeningNotice> selectByAcceptIdAndType(Integer acceptOrgId, Integer type) {
        return baseMapper.selectByAcceptIdAndType(type,acceptOrgId);
    }

    /**
     * 根据通知ID查询
     *
     * @param screeningNoticeId 通知ID
     * @return List<ScreeningNoticeDeptOrg>
     */
    public List<ScreeningNoticeDeptOrg> getByScreeningNoticeId(Integer screeningNoticeId) {
        return baseMapper.getByNoticeId(screeningNoticeId);
    }

    public IPage<ScreeningNoticeDTO> selectPageByQuery(IPage<ScreeningNotice> page, ScreeningNoticeQueryDTO query) {
        return baseMapper.selectPageByQuery(page, query);
    }

    /**
     * 已读处理
     *
     * @param noticeDeptOrgId
     * @param user
     */
    public void read(Integer noticeDeptOrgId, CurrentUser user) {
        //1. 更新状态
        ScreeningNoticeDeptOrg noticeDeptOrg = new ScreeningNoticeDeptOrg();
        noticeDeptOrg.setId(noticeDeptOrgId).setOperationStatus(CommonConst.STATUS_NOTICE_READ);
        if (!updateById(noticeDeptOrg, user.getId())) {
            throw new BusinessException("已读失败");
        }
    }

    /**
     * 根据通知ID和机构ID设置已读
     *
     * @param screeningNoticeId
     * @param acceptOrgId
     * @param user
     */
    public void read(Integer screeningNoticeId, Integer acceptOrgId, CurrentUser user) {
        // 1. 查找关联的通知
        ScreeningNoticeDeptOrg noticeDeptOrg = getByScreeningNoticeIdAndAcceptOrgId(screeningNoticeId, acceptOrgId);
        if (Objects.isNull(noticeDeptOrg)) {
            return;
        }
        // 2. 更新状态
        read(noticeDeptOrg.getId(), user);
    }

    /**
     * 已读已创建通知状态处理
     *
     * @param noticeId
     * @param acceptOrgId
     * @param genTaskOrPlanId 生成的任务或计划Id
     * @param user
     */
    public void statusReadAndCreate(Integer noticeId, Integer acceptOrgId, Integer genTaskOrPlanId, CurrentUser user) {
        //1. 更新状态与任务/计划ID
        baseMapper.updateStatusAndTaskPlanIdByNoticeIdAndAcceptOrgId(noticeId, acceptOrgId, genTaskOrPlanId, user.getId(), CommonConst.STATUS_NOTICE_CREATED);
    }

    /**
     * 添加上级部门历史通知
     * @param govDeptId
     * @param districtId
     */
    public void saveScreeningNotice(Integer govDeptId, Integer districtId) {
        ScreeningNotice screeningNotice = new ScreeningNotice();
        screeningNotice.setGovDeptId(govDeptId);
        screeningNotice.setReleaseStatus(1);
        screeningNotice.setType(0);
        List<ScreeningNotice> list = screeningNoticeService.findByDeptId(screeningNotice);
        if (!list.isEmpty()){
            for (ScreeningNotice screeningNotice1 :list){
                ScreeningNoticeDeptOrg screeningNoticeDeptOrg  = new ScreeningNoticeDeptOrg();
                screeningNoticeDeptOrg.setScreeningNoticeId(screeningNotice1.getId());
                screeningNoticeDeptOrg.setDistrictId(districtId);
                screeningNoticeDeptOrg.setAcceptOrgId(govDeptId);
                screeningNoticeDeptOrg.setOperationStatus(0);
                screeningNoticeDeptOrg.setScreeningTaskPlanId(screeningNotice1.getScreeningTaskId());
                saveOrUpdate(screeningNoticeDeptOrg);
            }
        }
    }
}
