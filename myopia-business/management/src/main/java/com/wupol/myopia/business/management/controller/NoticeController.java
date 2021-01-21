package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 消息表控制层
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @GetMapping("list")
    public Object getLists(PageRequest pageRequest) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return noticeService.getLists(pageRequest, currentUser);
    }

    @PutMapping("read/{ids}")
    public Object readNotice(@PathVariable("ids") String ids) {
        return noticeService.readNotice(ids);
    }

    @PutMapping("deleted/{ids}")
    public Object deletedNotice(@PathVariable("ids") String ids) {
        return noticeService.deletedNotice(ids);
    }
}