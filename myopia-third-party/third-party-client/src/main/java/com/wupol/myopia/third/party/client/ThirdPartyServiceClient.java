package com.wupol.myopia.third.party.client;

import com.wupol.myopia.base.config.feign.BusinessServiceFeignConfig;
import com.wupol.myopia.third.party.domain.VisionScreeningResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 *
 * @Author lzh
 * @Date 2023/4/14
 **/
@FeignClient(name = "myopia-third-party", decode404 = true, fallbackFactory = ThirdPartyServiceFallbackFactory.class, configuration = BusinessServiceFeignConfig.class)
public interface ThirdPartyServiceClient {

    /**
     * 管理端创建其他系统的用户(医院端、学校端、筛查端)
     *
     * @param visionScreeningResultDTO 用户数据
     * @return void
     **/
    @PostMapping("/xinjiang/screening/result/push")
    void pushScreeningResult(@RequestBody VisionScreeningResultDTO visionScreeningResultDTO);

    /**
     * 更新筛查数据学校名称
     *
     * @param oldSchoolName 旧名称
     * @param newSchoolName 新名称
     */
    @PutMapping("/screening/result/updateSchoolName")
    void updateSchoolName(@RequestParam("oldSchoolName") String oldSchoolName, @RequestParam("newSchoolName") String newSchoolName);

}
