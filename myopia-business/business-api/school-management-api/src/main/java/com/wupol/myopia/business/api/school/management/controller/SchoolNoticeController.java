package com.wupol.myopia.business.api.school.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.system.domain.dto.UnreadNoticeResponse;
import com.wupol.myopia.business.core.system.domain.model.Notice;
import com.wupol.myopia.business.core.system.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 学校端-消息中心
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/school/notice")
public class SchoolNoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 获取通知列表
     *
     * @param pageRequest 分页请求
     * @return 通知列表
     */
    @GetMapping("list")
    public IPage<Notice> getLists(PageRequest pageRequest) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return noticeService.getLists(pageRequest, currentUser);
    }

    /**
     * 消息已读
     *
     * @param ids 消息id
     * @return 是否更新成功
     */
    @PostMapping("read")
    public Boolean readNotice(@RequestBody List<Integer> ids) {
        return noticeService.readNotice(ids);
    }

    /**
     * 删除消息
     *
     * @param ids 消息id
     * @return 是否删除成功
     */
    @PostMapping("deleted")
    public Boolean deletedNotice(@RequestBody List<Integer> ids) {
        return noticeService.deletedNotice(ids);
    }

    /**
     * 未读消息统计
     *
     * @return 未读列表
     */
    @GetMapping("unreadCount")
    public UnreadNoticeResponse unreadCount() {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return noticeService.unreadCount(currentUser);
    }

    /**
     * 筛查通知已读
     *
     * @param screeningNoticeId 筛查通知ID
     * @return 是否成功
     */
    @PostMapping("screeningNotice/{screeningNoticeId}")
    public Boolean readScreeningNotice(@PathVariable("screeningNoticeId") Integer screeningNoticeId) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        return noticeService.readNotice(currentUser, screeningNoticeId);
    }
}
