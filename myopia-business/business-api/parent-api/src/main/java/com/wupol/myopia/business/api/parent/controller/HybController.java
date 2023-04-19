package com.wupol.myopia.business.api.parent.controller;

import com.wupol.myopia.base.domain.HuYangRequestDTO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.parent.domain.dto.HybCallbackRequestDTO;
import com.wupol.myopia.business.api.parent.domain.dto.ParentUidResponseDTO;
import com.wupol.myopia.business.api.parent.service.WxService;
import com.wupol.myopia.business.core.parent.domain.model.Parent;
import com.wupol.myopia.business.core.parent.service.ParentService;
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
    private ParentService parentService;

    @Resource
    private WxService wxService;

    /**
     * 获取家长UID
     *
     * @param id 家长Id
     * @return ParentUidResponseDTO
     */
    @GetMapping("parent/uid/{id}")
    public ParentUidResponseDTO getParentUid(@PathVariable("id") Integer id) {
        Parent parent = parentService.getById(id);
        return new ParentUidResponseDTO(parent.getHashKey(), wxService.getJsapiTicket());
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
