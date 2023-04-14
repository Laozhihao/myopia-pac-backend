package com.wupol.myopia.third.party.service;

import com.wupol.myopia.third.party.domain.VisionScreeningResultDTO;
import com.wupol.myopia.third.party.domain.model.StudentVisionScreeningResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 新疆业务处理类
 *
 * @Author lzh
 * @Date 2023/4/13
 **/
@Log4j2
@Service
public class XinJiangService {

    @Autowired
    private StudentVisionScreeningResultService studentVisionScreeningResultService;

    public void handleScreeningResultData(VisionScreeningResultDTO visionScreeningResultDTO) {
        log.info("handleScreeningResultData：" + visionScreeningResultDTO.toString());
        studentVisionScreeningResultService.findByList(new StudentVisionScreeningResult());
    }
}
