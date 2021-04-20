package com.wupol.myopia.business.core.screening.flow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.ScreeningNoticeDeptOrgMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQueryDTO;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
public class ScreeningNoticeDeptOrgService extends BaseService<ScreeningNoticeDeptOrgMapper, ScreeningNoticeDeptOrg> {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private GovDeptService govDeptService;

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

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningNoticeDTO> getPage(ScreeningNoticeQueryDTO query, PageRequest pageRequest) {
        Page<ScreeningNotice> page = (Page<ScreeningNotice>) pageRequest.toPage();
        IPage<ScreeningNoticeDTO> screeningNoticeIPage = baseMapper.selectPageByQuery(page, query);
        List<Integer> allGovDeptIds = screeningNoticeIPage.getRecords().stream().filter(vo -> ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())).map(ScreeningNoticeDTO::getAcceptOrgId).distinct().collect(Collectors.toList());
        Map<Integer, String> govDeptIdNameMap = CollectionUtils.isEmpty(allGovDeptIds) ? Collections.emptyMap() : govDeptService.getByIds(allGovDeptIds).stream().collect(Collectors.toMap(GovDept::getId, GovDept::getName));
        screeningNoticeIPage.getRecords().forEach(vo -> {
            List<District> districtPositionDetailById = districtService.getDistrictPositionDetailById(vo.getDistrictId());
            vo.setDistrictDetail(districtPositionDetailById).setDistrictName(districtService.getDistrictNameByDistrictPositionDetail(districtPositionDetailById));
            if (ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())) {
                vo.setGovDeptName(govDeptIdNameMap.getOrDefault(vo.getAcceptOrgId(), ""));
            }
        });
        return screeningNoticeIPage;
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
}
