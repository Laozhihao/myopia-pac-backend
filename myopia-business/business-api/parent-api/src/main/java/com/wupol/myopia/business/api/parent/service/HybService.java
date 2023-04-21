package com.wupol.myopia.business.api.parent.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.domain.HuYangRequestDTO;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.MD5Util;
import com.wupol.myopia.business.api.parent.domain.dto.HybCallbackRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidResponseDTO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.HybBindStatusEnum;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.service.ParentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private RedisUtil redisUtil;

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

    /**
     * 接受推送护眼宝数据
     *
     * @param requestDTO 请求入参
     */
    public void processHybData(HuYangRequestDTO requestDTO) {
        preCheck(requestDTO);

    }

    /**
     * 前置校验
     *
     * @param requestDTO 请求入参
     */
    private void preCheck(HuYangRequestDTO requestDTO) {
        // 检查签名是否有效
        String generateSign = MD5Util.generate(requestDTO.getAccessToken() + requestDTO.getTimestamp());
        if (!StringUtils.equals(generateSign, requestDTO.getSign())) {
            throw new BusinessException("签名存在问题，请确认");
        }
        // 检查是否重复请求
        String redisKey = String.format(RedisConstant.HYB_PUSH_DATA, generateSign);
        if (Objects.nonNull(redisUtil.get(redisKey))) {
            // 重新登录产生的Key不会重复
            throw new BusinessException("重复请求，请确认！");
        }
        // 设置1小时缓存
        redisUtil.set(redisKey, requestDTO.getTimestamp(), 60 * 60);

        // 检查家长HashKey是否存在
        List<HuYangRequestDTO.ParentStudentData> parentStudentData = requestDTO.getData();
        if (CollectionUtils.isEmpty(parentStudentData)) {
            throw new BusinessException("数据为空，请确认!");
        }

        Set<String> parentHashKey = parentStudentData.stream().map(HuYangRequestDTO.ParentStudentData::getParentUid).collect(Collectors.toSet());
        List<Parent> parents = parentService.getParentByHashKeys(parentHashKey);
        if (CollectionUtils.isEmpty(parents)) {
            throw new BusinessException("根据家长HashKey查询不到信息");
        }

        List<String> hashKeys = parentHashKey.stream()
                .filter(e -> !parents.stream().map(Parent::getHashKey).collect(Collectors.toSet()).contains(e))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(hashKeys)) {
            throw new BusinessException("存在错误的HashKey:" + String.join("、", hashKeys));
        }
    }

}
