package com.wupol.myopia.business.sdk.client;

import com.wupol.myopia.base.config.feign.BusinessServiceFeignConfig;
import com.wupol.myopia.business.sdk.domain.response.QuestionnaireUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author wulizhou
 * @Date 2022/6/29 17:01
 */
@FeignClient(name = "myopia-business", decode404 = true, fallbackFactory = BusinessServiceFallbackFactory.class, configuration = BusinessServiceFeignConfig.class)
public interface BusinessServiceClient {

    /**
     * 获取学生信息
     *
     * @param credentialNo
     * @param studentName
     * @return
     */
    @GetMapping("/management/screeningPlan/student/")
    QuestionnaireUser getStudent(@RequestParam("credentialNo") String credentialNo, @RequestParam("studentName") String studentName);

    /**
     * 获取学校信息
     *
     * @param schoolNo
     * @return
     */
    @GetMapping("/management/screeningPlan/school/")
    QuestionnaireUser getSchool(@RequestParam("schoolNo") String schoolNo, @RequestParam("password") String password);

    /**
     * 获取政府信息
     * @param orgId
     * @return
     */
    @GetMapping("/management/screeningPlan/government/")
    QuestionnaireUser checkGovernmentLogin(@RequestParam("orgId") Integer orgId);
}
