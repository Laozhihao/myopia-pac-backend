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
        requestDTO.setAccessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6eyJpZCI6MTU2NCwib3JnSWQiOi0xLCJxdWVzdGlvbm5haXJlVXNlcklkIjpudWxsLCJzY3JlZW5pbmdPcmdJZCI6bnVsbCwicmVhbE5hbWUiOm51bGwsInN5c3RlbUNvZGUiOjUsInJvbGVUeXBlcyI6W10sInVzZXJUeXBlIjotMSwiY2xpZW50SWQiOiI1IiwidG9rZW4iOm51bGwsInBsYXRmb3JtQWRtaW5Vc2VyIjpmYWxzZX0sImV4cCI6MTY4MjA2NjQ4MSwidXNlcl9uYW1lIjoiMTg1NzgyMzA0NjEiLCJqdGkiOiI5YjEyY2Q5Ni0wYTAwLTQyOWQtOTBhZC1mZWYwZTVjOWNjZDciLCJjbGllbnRfaWQiOiI1Iiwic2NvcGUiOlsiYWxsIl19.puaZy4po9TUmhY4vLDOCTJNyDVXSWnP2vwE9qNrx02ND7k1Y-bUPxdv8KQVPWs-NAgjYrH0m4oVDsgEU4DEW64fcvGGmD76QES1i_7K2bLo4ASF4NTk-OFa5ofvkyjeVwjBc_rgEOj7VFP1taMvBH1ug-k5JXSAc9cGkARf7Z73YcKfXJF9saB2h9hu6GJcfOO3Dav_NV9Hq0opLHMFyiSXQcfMljLcXytjf07KpNrmiIvOHExu7L_yWApbpPxFLsB-WrB1W-cnGcgiyvhAXNAoKMOJ_X04MTya4QCI-iwDyWW2Z_9YQWDrsqUq-OLnGQeOAueMyspYNA-ejg8_znw");
        hybService.processHybData(requestDTO);
    }
}
