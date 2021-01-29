package com.wupol.myopia.business.management.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.ScreeningTaskDTO;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningTaskMapper;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.domain.query.ScreeningTaskQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningTaskVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alix
 * @Date 2021-01-20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ScreeningTaskService extends BaseService<ScreeningTaskMapper, ScreeningTask> {

    @Autowired
    private ScreeningNoticeService screeningNoticeService;
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private ScreeningTaskOrgService screeningTaskOrgService;
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 设置操作人再更新
     * @param entity
     * @param userId
     * @return
     */
    public boolean updateById(ScreeningTask entity, Integer userId) {
        entity.setOperateTime(new Date()).setOperatorId(userId);
        return updateById(entity);
    }

    /**
     * 通过ID获取ScreeningTask列表
     *
     * @param pageRequest 分页入参
     * @param ids         ids
     * @return {@link IPage} 统一分页返回体
     */
    public IPage<ScreeningTask> getTaskByIds(PageRequest pageRequest, List<Integer> ids) {
        return baseMapper.getTaskByIds(pageRequest.toPage(), ids);
    }

    /**
     * 分页查询
     * @param query
     * @param pageRequest
     * @return
     */
    public IPage<ScreeningTaskVo> getPage(ScreeningTaskQuery query, PageRequest pageRequest) {
        Page<ScreeningTask> page = (Page<ScreeningTask>) pageRequest.toPage();
        if (StringUtils.isNotBlank(query.getCreatorNameLike())) {
            UserDTOQuery userDTOQuery = new UserDTOQuery();
            userDTOQuery.setRealName(query.getCreatorNameLike());
            List<Integer> queryCreatorIds = oauthServiceClient.getUserList(userDTOQuery).getData().stream().map(UserDTO::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(queryCreatorIds)) {
                // 可以直接返回空
                return new Page<ScreeningTaskVo>().setRecords(Collections.EMPTY_LIST).setCurrent(pageRequest.getCurrent()).setSize(pageRequest.getSize()).setPages(0).setTotal(0);
            }
            query.setCreateUserIds(queryCreatorIds);
        }
        IPage<ScreeningTaskVo> screeningTaskIPage = baseMapper.selectPageByQuery(page, query);
        List<Integer> userIds = screeningTaskIPage.getRecords().stream().map(ScreeningTask::getCreateUserId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).getData().stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getRealName));
        screeningTaskIPage.getRecords().forEach(vo -> vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreateUserId(), "")));
        return screeningTaskIPage;
    }

    /**
     * 发布任务
     *
     * @param id
     * @return
     */
    public Boolean release(Integer id, CurrentUser user) {
        //1. 更新状态&发布时间
        ScreeningTask screeningTask = getById(id);
        ScreeningNotice screeningNotice = new ScreeningNotice();
        BeanUtils.copyProperties(screeningTask, screeningNotice);
        screeningTask.setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        if (updateById(screeningTask, user.getId())) {
            //2. 发布通知
            screeningNotice.setCreateUserId(user.getId()).setOperatorId(user.getId()).setOperateTime(new Date()).setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date()).setType(ScreeningNotice.TYPE_ORG);
            screeningNoticeService.save(screeningNotice);
            //3. 为筛查机构创建通知
            List<ScreeningTaskOrg> orgLists = screeningTaskOrgService.getOrgListsByTaskId(id);
            List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs = orgLists.stream().map(org -> new ScreeningNoticeDeptOrg().setScreeningNoticeId(screeningNotice.getId()).setDistrictId(screeningTask.getDistrictId()).setAcceptOrgId(org.getId()).setOperatorId(user.getId())).collect(Collectors.toList());
            return screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);
        }
        throw new BusinessException("发布失败");
    }

    /**
     * 新增或更新
     * @param screeningTaskDTO
     */
    public void saveOrUpdateWithScreeningOrgs(CurrentUser user, ScreeningTaskDTO screeningTaskDTO) {
        // 新增或更新筛查任务信息
        screeningTaskDTO.setCreateUserId(user.getId()).setOperatorId(user.getId());
        if (!saveOrUpdate(screeningTaskDTO)) {
            throw new BusinessException("创建失败");
        }
        // 新增或更新筛查机构信息
        screeningTaskOrgService.saveOrUpdateBatchByTaskId(screeningTaskDTO.getId(), screeningTaskDTO.getScreeningOrgs());
    }

    /**
     * 删除，并删除关联的机构信息
     * @param screeningTaskId
     * @return
     */
    public void removeWithOrgs(Integer screeningTaskId) {
        removeById(screeningTaskId);
        screeningTaskOrgService.deleteByTaskIdAndExcludeOrgIds(screeningTaskId, Collections.emptyList());
    }
}
