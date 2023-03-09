package com.wupol.myopia.business.api.management.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitType;
import com.wupol.myopia.business.aggregation.screening.constant.DataSubmitTypeEnum;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据上报
 *
 * @author Simple4H
 */
@CrossOrigin
@ResponseResultBody
@RestController
@RequestMapping("/management/dataSubmit")
public class DataSubmitController {

    /**
     * 获取数据上报模版
     *
     * @return 模版
     */
    @GetMapping("/template")
    public List<DataSubmitType> getDataSubmitTemplate() {
        return DataSubmitTypeEnum.getDataSubmitTypeList();
    }
}
