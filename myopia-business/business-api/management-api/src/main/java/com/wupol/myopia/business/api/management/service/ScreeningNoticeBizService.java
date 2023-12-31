package com.wupol.myopia.business.api.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.vo.ScreeningNoticeVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import com.wupol.myopia.business.core.government.service.GovDeptService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.core.screening.flow.facade.ScreeningRelatedFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeDeptOrgService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.system.service.NoticeService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScreeningNoticeBizService {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private ScreeningRelatedFacade screeningRelatedFacade;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningNoticeVO> getPage(ScreeningNoticeQueryDTO query, PageRequest pageRequest) {
        Page<ScreeningNotice> page = (Page<ScreeningNotice>) pageRequest.toPage();
        if (StringUtils.isNotBlank(query.getCreatorNameLike()) && screeningRelatedFacade.initCreateUserIdsAndReturnIsEmpty(query)) {
            return new Page<>();
        }
        IPage<ScreeningNoticeDTO> screeningNoticeIPage = screeningNoticeService.selectPageByQuery(page, query);
        List<Integer> userIds = screeningNoticeIPage.getRecords().stream().map(ScreeningNotice::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).stream().collect(Collectors.toMap(User::getId, User::getRealName));
        // 设置位址和创造者信息
        return screeningNoticeIPage.convert(dto -> {
            ScreeningNoticeVO vo = new ScreeningNoticeVO(dto);
            vo.setDistrictDetail(districtService.getDistrictPositionDetailById(vo.getDistrictId())).setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), ""));
            return vo;
        });
    }

    /**
     * 发布通知
     *
     * @param id
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void release(Integer id, CurrentUser user, Boolean isTaskCreate) {
        //1. 更新状态&发布时间
        ScreeningNotice notice = screeningNoticeService.getById(id);
        notice.setId(id).setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        if (!screeningNoticeService.updateById(notice, user.getId())) {
            throw new BusinessException("发布失败");
        }
        // 任务创建任务，不需要向下发布任务
        if (Objects.equals(isTaskCreate, Boolean.TRUE)) {
            return;
        }
        List<GovDept> govDepts = govDeptService.getAllSubordinateWithDistrictId(notice.getGovDeptId());
        List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs = govDepts.stream().map(govDept -> new ScreeningNoticeDeptOrg().setScreeningNoticeId(id).setDistrictId(govDept.getDistrictId()).setAcceptOrgId(govDept.getId()).setOperatorId(user.getId())).collect(Collectors.toList());
        //2. 为下属部门创建通知
        screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);
        // 3. 为消息中心创建通知
        List<Integer> govOrgIds = govDepts.stream().map(GovDept::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(govOrgIds)) {
            return;
        }
        List<User> userBatchByOrgIds = oauthServiceClient.getUserBatchByOrgIds(govOrgIds, SystemCode.MANAGEMENT_CLIENT.getCode(), UserType.GOVERNMENT_ADMIN.getType());
        List<Integer> toUserIds = userBatchByOrgIds.stream().map(User::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(toUserIds)) {
            noticeService.batchCreateNotice(user.getId(), id, toUserIds, CommonConst.NOTICE_SCREENING_NOTICE, notice.getTitle(), notice.getTitle(), notice.getStartTime(), notice.getEndTime());
        }
    }

    /**
     * 获取该用户所在部门参与的筛查通知（发布筛查通知，或者接受过筛查通知）
     *
     * @param user
     * @return
     */
    public List<ScreeningNotice> getRelatedNoticeByUser(CurrentUser user) {
        if (user.isGovDeptUser()) {
            //查找所有的上级部门
            Set<Integer> superiorGovIds = govDeptService.getSuperiorGovIds(user.getOrgId());
            superiorGovIds.add(user.getOrgId());
            //查找政府发布的通知
            return screeningNoticeService.getNoticeByReleaseOrgId(superiorGovIds, ScreeningNotice.TYPE_GOV_DEPT);
        }
        if (user.isPlatformAdminUser()) {
            //这里只是查找政府的通知
            return screeningNoticeService.getAllReleaseNotice();
        }
        if (user.isScreeningUser() || (user.isHospitalUser() && (Objects.nonNull(user.getScreeningOrgId())))) {
            //该机构发布的通知
            return screeningNoticeService.getNoticeBySreeningUser(user.getScreeningOrgId());
        }
        return Collections.emptyList();
    }

    /**
     * 新增通知
     * @param screeningTaskDTO 入参
     * @param userId 用户Id
     * @return 通知
     */
    public ScreeningNotice saveNotice(ScreeningTaskDTO screeningTaskDTO, Integer userId) {

        ScreeningNotice screeningNotice = new ScreeningNotice();
        screeningNotice.setTitle(screeningTaskDTO.getTitle())
                .setContent(screeningTaskDTO.getContent())
                .setStartTime(screeningTaskDTO.getStartTime())
                .setEndTime(screeningTaskDTO.getEndTime())
                .setReleaseTime(new Date())
                .setCreateUserId(userId)
                .setCreateTime(new Date())
                .setOperatorId(userId)
                .setOperateTime(new Date())
                .setDistrictId(screeningTaskDTO.getDistrictId())
                .setGovDeptId(screeningTaskDTO.getGovDeptId())
                .setScreeningType(screeningTaskDTO.getScreeningType())
                .setCreateUserId(userId)
                .setOperatorId(userId);

        if (!screeningNoticeService.save(screeningNotice)) {
            throw new BusinessException("创建失败");
        }
        screeningNoticeDeptOrgService.save(new ScreeningNoticeDeptOrg().
                setScreeningNoticeId(screeningNotice.getId())
                .setDistrictId(screeningNotice.getDistrictId())
                .setAcceptOrgId(screeningNotice.getGovDeptId())
                .setOperatorId(screeningNotice.getCreateUserId()));
        return screeningNotice;
    }

    /**
     * 推送通知
     *
     * @param screeningNotice 通知
     * @param user            用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(ScreeningNotice screeningNotice, CurrentUser user) {
        screeningNoticeService.createOrReleaseValidate(screeningNotice);
        if (user.isPlatformAdminUser() || user.isGovDeptUser() && user.getOrgId().equals(screeningNotice.getGovDeptId())) {
            release(screeningNotice.getId(), user, true);
        } else {
            throw new ValidationException("无权限");
        }
    }


}
