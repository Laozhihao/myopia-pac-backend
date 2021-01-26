package com.wupol.myopia.business.management.controller;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.query.PageRequest;
import com.wupol.myopia.business.management.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @PostMapping("read")
    public Object readNotice(@RequestBody List<Integer> ids) {
        return noticeService.readNotice(ids);
    }

    @PostMapping("deleted")
    public Object deletedNotice(@RequestBody List<Integer> ids) {
        return noticeService.deletedNotice(ids);
    }

    @GetMapping("unreadCount")
    public Object unreadCount() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return noticeService.unreadCount(currentUser);
    }
}