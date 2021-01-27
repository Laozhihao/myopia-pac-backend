package com.wupol.myopia.business.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.client.OauthServiceClient;
import com.wupol.myopia.business.management.constant.CommonConst;
import com.wupol.myopia.business.management.domain.dto.UserDTO;
import com.wupol.myopia.business.management.domain.mapper.ScreeningNoticeMapper;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.model.ScreeningNoticeDeptOrg;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.domain.query.UserDTOQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningNoticeVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Service
@Transactional
public class ScreeningNoticeService extends BaseService<ScreeningNoticeMapper, ScreeningNotice> {
    @Autowired
    private ScreeningNoticeDeptOrgService screeningNoticeDeptOrgService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private OauthServiceClient oauthServiceClient;

    /**
     * 设置操作人再更新
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
     * @param query
     * @param pageNum
     * @param pageSize
     * @return
     */
    public IPage<ScreeningNoticeVo> getPage(ScreeningNoticeQuery query, Integer pageNum, Integer pageSize) {
        Page<ScreeningNotice> page = new Page<>(pageNum, pageSize);
        if (StringUtils.isNotBlank(query.getCreatorNameLike())) {
            //TODO 优化查询，目前人员查询有问题
            UserDTOQuery userDTOQuery = new UserDTOQuery();
            userDTOQuery.setUsername(query.getCreatorNameLike()).setCurrent(1).setSize(1000);
            List<Integer> queryCreatorIds = oauthServiceClient.getUserListPage(userDTOQuery).getData().getRecords().stream().map(UserDTO::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(queryCreatorIds)) {
                // 可以直接返回空
                return new Page<ScreeningNoticeVo>().setRecords(Collections.EMPTY_LIST).setCurrent(pageNum).setSize(pageSize).setPages(0).setTotal(0);
            }
            query.setCreatorIds(queryCreatorIds);
        }
        IPage<ScreeningNoticeVo> screeningNoticeIPage = baseMapper.selectPageByQuery(page, query);
        List<Integer> userIds = screeningNoticeIPage.getRecords().stream().map(ScreeningNotice::getCreatorId).distinct().collect(Collectors.toList());
        Map<Integer, String> userIdNameMap = oauthServiceClient.getUserBatchByIds(userIds).getData().stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getRealName));
        screeningNoticeIPage.getRecords().forEach(vo -> vo.setCreatorName(userIdNameMap.getOrDefault(vo.getCreatorId(), "")));
        return screeningNoticeIPage;
    }

    /**
     * 发布通知
     * @param id
     * @return
     */
    public Boolean release(Integer id, CurrentUser user) {
        //1. 更新状态&发布时间
        ScreeningNotice notice = new ScreeningNotice();
        notice.setId(id).setReleaseStatus(CommonConst.STATUS_RELEASE).setReleaseTime(new Date());
        if (updateById(notice, user.getId())) {
            //TODO 待测试
            List<District> currentUserDistrictTree = districtService.getCurrentUserDistrictTree(user);
            List<ScreeningNoticeDeptOrg> screeningNoticeDeptOrgs = currentUserDistrictTree.stream().map(district -> new ScreeningNoticeDeptOrg().setScreeningNoticeId(id).setDistrictId(district.getId())).collect(Collectors.toList());
            //2. 为下属部门创建通知
            return screeningNoticeDeptOrgService.saveBatch(screeningNoticeDeptOrgs);
        }
        throw new BusinessException("发布失败");
    }
}
