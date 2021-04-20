package com.wupol.myopia.business.parent.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.parent.domain.model.Parent;
import com.wupol.myopia.business.parent.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @Author HaoHao
 * @Date 2021-02-26
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/parent/")
public class ParentController {

    @Autowired
    private ParentService parentService;
    
    /**
     * 获取当前登录家长基本信息
     *
     * @return com.wupol.myopia.business.parent.domain.model.Parent
     **/
    @GetMapping("/current/info")
    public Parent getCurrentLoginParentInfo() throws IOException {
        return parentService.getParentByUserId(CurrentUserUtil.getCurrentUser().getId());
    }
}
