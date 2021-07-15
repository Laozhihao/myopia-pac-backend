package com.wupol.myopia.business.api.screening.app.controller;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Classname TestController
 * @Description 测试
 * @Date 2021/7/14 5:56 下午
 * @Author Jacob
 * @Version
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/time")
    public Object tesfddsfgfegt(@RequestBody ScreeningStudentQueryDTO screeningStudentQueryDTO) {
       // Date startDate = screeningStudentQueryDTO.getStartDate();
        return screeningStudentQueryDTO;
    }


    @PostMapping("/time1")
    public Object tesfddsfgfegt() {
        Date date = new Date();
        return DateFormatUtil.format(date,DateFormatUtil.FORMAT_DETAIL_TIME);
    }

}
