package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningNoticeMapper;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeNameVO;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningNoticeService extends BaseService<ScreeningNoticeMapper, ScreeningNotice> {
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private GovDeptService govDeptService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private OauthServiceClient oauthServiceClient;
    @Autowired
    private NoticeService noticeService;

    /**
     * 设置操作人再更新
     *
     * @param entity
     * @param userId
     * @return
     */
    public boolean updateById(ScreeningNotice entity, Integer userId) {
        entity.setOperateTime(new Date()).setOperatorId(userId);
        return updateById(entity);
    }

    /**
     * 分页查询
     *
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningNoticeVo> getPage(ScreeningNoticeQuery query, PageRequest pageRequest) {
        Page<ScreeningNotice> page = (Page<ScreeningNotice>) pageRequest.toPage();
        if (StringUtils.isNotBlank(query.getCreatorNameLike())) {
            UserDTOQuery userDTOQuery = new UserDTOQuery();
            userDTOQuery.setRealName(query.getCreatorNameLike());
            List<Integer> queryCreatorIds = oauthServiceClient.getUserList(userDTOQuery).getData().stream().map(UserDTO::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(queryCreatorIds)) {
                // 可以直接返回空
                return new Page<ScreeningNoticeVo>().setRecords(Collections.EMPTY_LIST).setCurrent(pageRequest.getCurrent()).setSize(pageRequest.getSize()).setPages(0).setTotal(0);
            }
            query.setCreateUserIds(queryCreatorIds);
        }
        IPage<ScreeningNoticeVo> screeningNoticeIPage = baseMapper.selectPageByQuery(page, query);
        List<Integer> userIds = screeningNoticeIPage.getRecords().stream().map(ScreeningNotice::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).getData().stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getRealName));
        screeningNoticeIPage.getRecords().forEach(vo -> vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), "")).setDistrictDetail(districtService.getDistrictPositionDetailById(vo.getDistrictId())));
        return screeningNoticeIPage;
    }

    /**
     * 发布通知
     *
     * @param id
     * @return
     */
    public Boolean release(Integer id, CurrentUser user) {
        //1. 更新状态&发布时间
        ScreeningNotice notice = getById(id);
        notice.setId(id).setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        if (updateById(notice, user.getId())) {
            List<GovDept> govDepts = govDeptService.getAllSubordinateWithDistrictId(notice.getGovDeptId());
            List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs = govDepts.stream().map(govDept -> new ScreeningNoticeDeptOrg().setScreeningNoticeId(id).setDistrictId(govDept.getDistrictId()).setAcceptOrgId(govDept.getId()).setOperatorId(user.getId())).collect(Collectors.toList());
            //2. 为下属部门创建通知
            boolean result = screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);
            // 3. 为消息中心创建通知
            List<Integer> govOrgIds = govDepts.stream().map(GovDept::getId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(govOrgIds)){
                ApiResult<List<UserDTO>> userBatchByOrgIds = oauthServiceClient.getUserBatchByOrgIds(govOrgIds, SystemCode.MANAGEMENT_CLIENT.getCode());
                List<Integer> toUserIds = userBatchByOrgIds.getData().stream().map(UserDTO::getId).collect(Collectors.toList());
                noticeService.batchCreateScreeningNotice(user.getId(), id, toUserIds, CommonConst.NOTICE_SCREENING_NOTICE, notice.getTitle(), notice.getTitle(), notice.getStartTime(), notice.getEndTime());
            }
            return result;
        }
        throw new BusinessException("发布失败");
    }

    /**
     * 部门是否已存在该标题
     *
     * @param screeningNoticeId 已有的ID，更新时使用。新增时可为null
     * @param govDeptId         部门ID
     * @param title             标题
     * @return
     */
    public boolean checkTitleExist(Integer screeningNoticeId, Integer govDeptId, String title) {
        QueryWrapper<ScreeningNotice> queryWrapper = new QueryWrapper<ScreeningNotice>().eq("gov_dept_id", govDeptId).eq("title", title).eq("release_status", CommonConst.STATUS_RELEASE).eq("type", ScreeningNotice.TYPE_GOV_DEPT);
        if (Objects.nonNull(screeningNoticeId)) {
            queryWrapper.ne("id", screeningNoticeId);
        }
        return baseMapper.selectList(queryWrapper).size() > 0;
    }

    /**
     * 发布筛查通知时，判断时间段是否合法（只查看已发布的且校验type为0）
     * 一个部门在一个时间段内只能发布一个筛查通知【即时间不允许重叠，且只能创建今天之后的时间段】
     *
     * @param screeningNotice：必须存在govDeptId、startTime、endTime
     * @return
     */
    public boolean checkTimeLegal(ScreeningNotice screeningNotice) {
        return baseMapper.selectByTimePeriods(screeningNotice).size() > 0;
    }

    /**
     * 通过筛查通知ids查找
     *
     * @param ids ids
     * @return List<ScreeningNotice>
     */
    public List<ScreeningNotice> getByIds(List<Integer> ids) {
        return baseMapper
                .selectList(new QueryWrapper<ScreeningNotice>()
                        .in("id", ids)
                        .orderByDesc("create_time"));
    }

    /**
     * 根据任务ID获取通知（type为1）
     *
     * @param screeningTaskId
     * @return
     */
    public ScreeningNotice getByScreeningTaskId(Integer screeningTaskId) {
        QueryWrapper<ScreeningNotice> queryWrapper = new QueryWrapper<ScreeningNotice>().eq("screening_task_id", screeningTaskId).eq("type", ScreeningNotice.TYPE_ORG);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 根据任务ID获取通知（type为1）
     *
     * @param screeningTaskId
     * @return
     */
    public Set<Integer> listByScreeningTaskId(Integer screeningTaskId, Set<Integer> govDeptIds) {
        return baseMapper.selectDistrictIds(screeningTaskId, ScreeningNotice.TYPE_GOV_DEPT, govDeptIds);
    }

    /**
     * 获取该用户所在部门参与的筛查通知（发布筛查通知，或者接受过筛查通知）
     *
     * @param user
     * @return
     */
    public List<ScreeningNotice> getRelatedNoticeByUser(CurrentUser user) {
        List<ScreeningNotice> screeningNotices = new ArrayList<>();
        if (user.isGovDeptUser()) {
            //查找所有的上级部门
            Set<Integer> superiorGovIds = govDeptService.getSuperiorGovIds(user.getOrgId());
            superiorGovIds.add(user.getOrgId());
            //查找政府发布的通知
            screeningNotices = this.getNoticeByReleaseOrgId(superiorGovIds, ScreeningNotice.TYPE_GOV_DEPT);
        } else if (user.isPlatformAdminUser()) {
            screeningNotices = this.getAllReleaseNotice();
        } else if (user.isScreeningUser()) {
            //该部门发布的通知
            Set<Integer> screeningOrgs = new HashSet<>();
            screeningOrgs.add(user.getOrgId());
            screeningNotices = this.getNoticeByReleaseOrgId(screeningOrgs, ScreeningNotice.TYPE_ORG);
            //该部门接收到的通知
            screeningNotices.addAll(screeningNoticeDeptOrgService.selectByAcceptIdAndType(user.getOrgId(), ScreeningNotice.TYPE_ORG));
        }
        return screeningNotices;
    }

    /**
     * 获取所有已经发布的政府通知
     *
     * @return
     */
    private List<ScreeningNotice> getAllReleaseNotice() {
        LambdaQueryWrapper<ScreeningNotice> screeningNoticeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningNoticeLambdaQueryWrapper
                .eq(ScreeningNotice::getReleaseStatus, CommonConst.STATUS_RELEASE)
        .eq(ScreeningNotice::getType, ScreeningNotice.TYPE_GOV_DEPT);
        List<ScreeningNotice> screeningNotices = baseMapper.selectList(screeningNoticeLambdaQueryWrapper);
        return screeningNotices;
    }

    /**
     * 根据发布部门获取通知
     *
     * @param orgIds
     * @param orgType
     * @return
     */
    private List<ScreeningNotice> getNoticeByReleaseOrgId(Set<Integer> orgIds, Integer orgType) {
        if (CollectionUtils.isEmpty(orgIds) || orgType == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScreeningNotice> screeningNoticeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        screeningNoticeLambdaQueryWrapper.eq(ScreeningNotice::getType, orgType);
        screeningNoticeLambdaQueryWrapper.in(ScreeningNotice::getGovDeptId, orgIds);
        screeningNoticeLambdaQueryWrapper.eq(ScreeningNotice::getReleaseStatus, CommonConst.STATUS_RELEASE);
        return baseMapper.selectList(screeningNoticeLambdaQueryWrapper);
    }

    /**
     * 获取年度
     *
     * @return
     */
    public List<Integer> getYears(List<ScreeningNotice> screeningNotices) {
        Set<Integer> yearSet = new HashSet<>();
        screeningNotices.forEach(screeningTask -> {
            Integer startYear = this.getYear(screeningTask.getStartTime());
            Integer endYear = this.getYear(screeningTask.getEndTime());
            yearSet.add(startYear);
            yearSet.add(endYear);
        });
        List<Integer> yearList = new ArrayList<>(yearSet);
        yearList.stream().sorted();
        Collections.reverse(yearList);
        return yearList;
    }


    /**
     * 根据时间获取年份 todo 待抽取
     *
     * @param date
     * @return
     */
    public Integer getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        return year;
    }

    /**
     * 获取已经发布的通知
     * @param noticeId
     * @return
     */
    public ScreeningNotice getReleasedNoticeById(Integer noticeId){
        ScreeningNotice screeningNotice = getById(noticeId);
        if (screeningNotice == null) {
            throw new BusinessException("无法找到该通知");
        }
        if (screeningNotice.getReleaseStatus() != CommonConst.STATUS_RELEASE) {
            throw new BusinessException("该通知未发布");
        }
        return screeningNotice;
    }


    /**
     * 获取筛查任务名字
     * @param screeningNoticeIds
     * @param year
     */
    public  Set<ScreeningNoticeNameVO> getScreeningNoticeNameVO(Set<Integer> screeningNoticeIds, Integer year) {
        List<ScreeningNotice> screeningNotices = listByIds(screeningNoticeIds);
        Set<ScreeningNoticeNameVO> screeningNoticeNameVOS = screeningNotices.stream().filter(screeningNotice ->
                year.equals(getYear(screeningNotice.getStartTime())) || year.equals(getYear(screeningNotice.getEndTime()))
        ).map(screeningNotice -> {
            ScreeningNoticeNameVO screeningNoticeNameVO = new ScreeningNoticeNameVO();
            screeningNoticeNameVO.setNoticeTitle(screeningNotice.getTitle()).setNoticeId(screeningNotice.getId()).setScreeningStartTime(screeningNotice.getStartTime()).setScreeningEndTime(screeningNotice.getEndTime());
            return screeningNoticeNameVO;
        }).collect(Collectors.toSet());
        return screeningNoticeNameVOS;
    }
}
