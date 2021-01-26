package com.wupol.myopia.business.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.controller.BaseController;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
import com.wupol.myopia.business.management.domain.query.ScreeningNoticeQuery;
import com.wupol.myopia.business.management.service.ScreeningNoticeService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
     * 分页查询创建的通知
     * 1. 管理员：所有
     * 2. 政府机构：自己部门创建的
     *
     * @param query 查询参数
     * @param pageNum 页码
     * @param pageSize 条数
     * @return Object
     */
    @GetMapping("dept/page")
    public IPage queryDeptPage(ScreeningNoticeQuery query,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize) throws IOException {
//        query.setDistrictId()
        return baseService.findByPage(query, pageNum, pageSize);
    }

    /**
     * 发布
     * @param id ID
     * @return void
     */
    @PostMapping("{id}")
    public void release(@PathVariable Integer id) {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        baseService.release(id, currentUser);
    }
}
