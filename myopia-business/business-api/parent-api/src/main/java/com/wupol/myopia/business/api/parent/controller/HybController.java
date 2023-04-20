package com.wupol.myopia.business.api.parent.controller;

import com.wupol.myopia.base.domain.HuYangRequestDTO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.parent.domain.dto.HybCallbackRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidResponseDTO;
import com.wupol.myopia.business.api.parent.service.HybService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 护眼宝
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/hyb/")
public class HybController {

    @Resource
    private HybService hybService;

    /**
     * 获取家长UID
     *
     * @param requestDTO 请求
     * @return ParentUidResponseDTO
     */
    @GetMapping("parent/uid")
    public ParentUidResponseDTO getParentUid(ParentUidRequestDTO requestDTO) {
        return hybService.getParentUid(requestDTO);
    }

    /**
     * 护眼宝回调
     *
     * @param requestDTO 请求
     */
    @PostMapping("bind/callback")
    public void bindCallBack(@RequestBody @Valid HybCallbackRequestDTO requestDTO) {
        hybService.bindCallBack(requestDTO);
    }


    /**
     * 接受推送护眼宝数据
     *
     * @param requestDTO 请求入参
     */
    @PostMapping("push")
    public void processHybData(@RequestBody @Valid HuYangRequestDTO requestDTO) {
        requestDTO.setAccessToken(CurrentUserUtil.getUserToken());
        hybService.processHybData(requestDTO);
    }
}
