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

    @PostMapping("bind/callback")
    public void bindCallBack(@RequestBody HybCallbackRequestDTO requestDTO) {


    }


    @PostMapping("push")
    public HuYangRequestDTO push(@RequestBody HuYangRequestDTO requestDTO) {
        requestDTO.setAccessToken(CurrentUserUtil.getUserToken());
        return requestDTO;
    }
}
