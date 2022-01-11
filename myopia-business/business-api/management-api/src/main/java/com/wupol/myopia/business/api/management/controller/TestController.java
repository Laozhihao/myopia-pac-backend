package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.BusinessUtil;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.api.management.service.PreSchoolNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

/**
 * 测试
 *
 * @author Simple4H
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/test")
public class TestController {

    @Autowired
    private PreSchoolNoticeService preSchoolNoticeService;

    @GetMapping("getMonthAge")
    public Object getMonthAge(String date) throws ParseException {
        return BusinessUtil.getMonthAgeByBirthday(DateFormatUtil.parseDate(date, DateFormatUtil.FORMAT_ONLY_DATE));
    }

    @GetMapping("send")
    public void test() {
        preSchoolNoticeService.timedTaskSendMsg();
    }


}
