package com.wupol.myopia.business.api.parent.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.parent.domain.dto.HybCallbackRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidResponseDTO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.HybBindStatusEnum;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.service.ParentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 护眼宝
 *
 * @author Simple4H
 */
@Service
@Slf4j
public class HybService {

    @Resource
    private ParentService parentService;

    @Resource
    private WxService wxService;

    public ParentUidResponseDTO getParentUid(ParentUidRequestDTO requestDTO) {
        Parent parent = parentService.getById(requestDTO.getId());
        if (Objects.isNull(parent)) {
            throw new BusinessException("家长数据异常");
        }
        String data = String.format(CommonConst.WX_SIGNATURE, wxService.getJsapiTicket(), requestDTO.getNoncestr(), requestDTO.getTimestamp(), requestDTO.getUrl());
        return new ParentUidResponseDTO(parent.getHashKey(), DigestUtil.sha1Hex(data));
    }

    /**
     * 护眼宝回调
     *
     * @param requestDTO 请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindCallBack(HybCallbackRequestDTO requestDTO) {
        String parentUid = requestDTO.getParentUid();
        Parent parent = parentService.getParentByHashKey(parentUid);
        if (Objects.isNull(parent)) {
            log.error("");
            throw new BusinessException("家长UID异常！");
        }
        parent.setHybBindStatus(HybBindStatusEnum.BIND.type);
        parentService.updateById(parent);
    }

}
