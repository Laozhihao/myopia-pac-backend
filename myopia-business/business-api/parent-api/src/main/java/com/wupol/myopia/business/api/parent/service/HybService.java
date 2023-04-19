package com.wupol.myopia.business.api.parent.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidResponseDTO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.service.ParentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 护眼宝
 *
 * @author Simple4H
 */
@Service
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

}
