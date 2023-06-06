package com.wupol.myopia.third.party.controller;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.third.party.domain.VisionScreeningResultDTO;
import com.wupol.myopia.third.party.service.XinJiangService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * 新疆业务控制器
 *
 * @Author lzh
 * @Date 2023-04-13
 */
@Validated
@Log4j2
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/xinjiang")
public class XinJiangController {

    @Autowired
    private XinJiangService xinJiangService;

    /**
     * 接收business服务推送过来的筛查数据
     *
     * @param visionScreeningResultDTO 筛查数据
     */
    @PostMapping("/screening/result/push")
    public void receiveScreeningResultData(@RequestBody @Validated VisionScreeningResultDTO visionScreeningResultDTO) {
        log.info("推送数据到新疆中间库：" + JSON.toJSONString(visionScreeningResultDTO));
        xinJiangService.handleScreeningResultData(visionScreeningResultDTO);
    }

    /**
     * 更新筛查数据学校名称
     *
     * @param oldSchoolName 旧名称
     * @param newSchoolName 新名称
     */
    @PutMapping("/screening/result/updateSchoolName")
    public void updateScreeningResultSchoolName(@NotBlank(message = "oldSchoolName不能为空") String oldSchoolName, @NotBlank(message = "newSchoolName不能为空") String newSchoolName) {
        xinJiangService.updateSchoolName(oldSchoolName, newSchoolName);
    }
}
