package com.wupol.myopia.business.management.controller;

import org.springframework.web.bind.annotation.*;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.service.ScreeningNoticeService;

/**
 * @Author HaoHao
 * @Date 2021-01-20
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/screeningNotice")
public class ScreeningNoticeController extends BaseController<ScreeningNoticeService, ScreeningNotice> {
    /**
     * 发布
     * @param id ID
     * @return void
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) {
        //发布：1. 更新状态&发布时间 2. 为下属部门创建通知
    }
}
