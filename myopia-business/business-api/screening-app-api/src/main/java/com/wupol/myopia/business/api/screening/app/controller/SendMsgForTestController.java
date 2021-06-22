package com.wupol.myopia.business.api.screening.app.controller;

import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.screening.app.schedules.SendWarningMsgScheduledTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 建档异常短信定时任务发送的触发接口
 * todo 给测试同学使用,测试完后删除
 * @Author jacob
 * @Date 2021-01-21
 */
@ResponseResultBody
@Controller
@RequestMapping("/app/msg")
@Slf4j
public class SendMsgForTestController {

    @Autowired
    private SendWarningMsgScheduledTask sendWarningMsgScheduledTask;

    /**
     * 触发每天的短信检查
     *
     * @return
     */
    @GetMapping("/today")
    public ApiResult sendWarningMsg() {
        sendWarningMsgScheduledTask.sendWarningMsg();
        return ApiResult.success("触发成功");
    }

    /**
     * 触发每日30天前的检查
     *
     * @return
     */
    @GetMapping("/30days")
    public ApiResult repeatNoticeWarningMsg() {
        sendWarningMsgScheduledTask.repeatNoticeWarningMsg();
        return ApiResult.success("触发成功");
    }

}
