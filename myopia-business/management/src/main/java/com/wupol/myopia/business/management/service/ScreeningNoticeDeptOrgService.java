package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.mapper.ScreeningNoticeDeptOrgMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
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
        QueryWrapper<ScreeningNoticeDeptOrg> query = new QueryWrapper<>();
        query.eq("screening_notice_id", screeningNoticeId).eq("accept_org_id", acceptOrgId);
        return baseMapper.selectOne(query);
    }

    /**
     * 根据通知ID查询
     *
     * @param screeningNoticeId
     * @return
     */
    public List<ScreeningNoticeDeptOrg> getByScreeningNoticeId(Integer screeningNoticeId) {
        QueryWrapper<ScreeningNoticeDeptOrg> query = new QueryWrapper<>();
        query.eq("screening_notice_id", screeningNoticeId);
        return baseMapper.selectList(query);
    }

    /**
     * 分页查询
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningNoticeVo> getPage(ScreeningNoticeQuery query, PageRequest pageRequest) {
        Page<ScreeningNotice> page = (Page<ScreeningNotice>) pageRequest.toPage();
        IPage<ScreeningNoticeVo> screeningNoticeIPage = baseMapper.selectPageByQuery(page, query);
        List<Integer> allGovDeptIds = screeningNoticeIPage.getRecords().stream().filter(vo -> ScreeningNotice.TYPE_GOV_DEPT.equals(vo.getType())).map(ScreeningNoticeVo::getAcceptOrgId).distinct().collect(Collectors.toList());
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
