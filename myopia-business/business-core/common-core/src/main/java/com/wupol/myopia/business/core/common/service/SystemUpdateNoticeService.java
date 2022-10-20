package com.wupol.myopia.business.core.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.dto.SystemNoticeSaveRequestDTO;
import com.wupol.myopia.business.core.common.domain.dto.SystemUpdateNoticeListResponse;
import com.wupol.myopia.business.core.common.domain.mapper.SystemUpdateNoticeMapper;
import com.wupol.myopia.business.core.common.domain.model.SystemCodeDO;
import com.wupol.myopia.business.core.common.domain.model.SystemUpdateNotice;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Simple4H
 */
@Service
public class SystemUpdateNoticeService extends BaseService<SystemUpdateNoticeMapper, SystemUpdateNotice> {

    /**
     * 获取列表
     *
     * @param pageRequest 分页请求
     *
     * @return IPage<SystemUpdateNotice>
     */
    public IPage<SystemUpdateNoticeListResponse> getList(PageRequest pageRequest) {
        IPage<SystemUpdateNoticeListResponse> returnPage = new Page<>();
        IPage<SystemUpdateNotice> page = findByPage(new SystemUpdateNotice(), pageRequest.getCurrent(), pageRequest.getSize());
        List<SystemUpdateNotice> noticeList = page.getRecords();
        if (CollectionUtils.isEmpty(noticeList)) {
            return returnPage;
        }
        BeanUtils.copyProperties(page, returnPage);
        return returnPage.setRecords(noticeList.stream().map(systemUpdateNotice -> {
            SystemUpdateNoticeListResponse listResponse = new SystemUpdateNoticeListResponse();
            BeanUtils.copyProperties(systemUpdateNotice, listResponse);
            listResponse.setPlatform(systemUpdateNotice.getSystemCode().stream().map(SystemCodeDO::getSystemCode).collect(Collectors.toList()));
            return listResponse;
        }).collect(Collectors.toList()));
    }

    /**
     * 保存通知
     *
     * @param requestDTO 请求入参
     * @param userId     用户Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveNotice(SystemNoticeSaveRequestDTO requestDTO, Integer userId) {
        SystemUpdateNotice systemUpdateNotice = new SystemUpdateNotice();
        systemUpdateNotice.setCreateUserId(userId);
        systemUpdateNotice.setComment(requestDTO.getComment());
        systemUpdateNotice.setSystemCode(requestDTO.platform2SystemCode());
        baseMapper.insert(systemUpdateNotice);
    }

    /**
     * 下线通知
     *
     * @param id id
     */
    @Transactional(rollbackFor = Exception.class)
    public void offlineNotice(Integer id) {
        SystemUpdateNotice systemUpdateNotice = baseMapper.selectById(id);
        systemUpdateNotice.setStatus(StatusConstant.DISABLE);
        baseMapper.updateById(systemUpdateNotice);
    }

    /**
     * 获取登录用户最新的一条更新通知
     *
     * @param systemCode 系统编码
     *
     * @return 更新通知
     */
    public String getLastSystemUpdateNotice(Integer systemCode) {
        SystemUpdateNotice systemUpdateNotice = baseMapper.getLastSystemUpdateNotice(systemCode);
        if (Objects.isNull(systemUpdateNotice)) {
            return StringUtils.EMPTY;
        }
        return systemUpdateNotice.getComment();
    }

}
