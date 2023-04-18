package com.wupol.myopia.business.api.parent.controller;

import com.wupol.myopia.base.domain.HuYangRequestDTO;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.parent.domain.dto.HybCallbackRequestDTO;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("bind/callback")
    public void bindCallBack(@RequestBody HybCallbackRequestDTO requestDTO) {


    }


    @PostMapping("push")
    public HuYangRequestDTO push(@RequestBody HuYangRequestDTO requestDTO) {
        requestDTO.setAccessToken(CurrentUserUtil.getUserToken());
        return requestDTO;
    }
}
