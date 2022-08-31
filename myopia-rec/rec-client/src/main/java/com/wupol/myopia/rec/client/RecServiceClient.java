package com.wupol.myopia.rec.client;

import com.wupol.myopia.rec.domain.RecExportDTO;
import com.wupol.myopia.rec.domain.RecExportVO;
import com.wupol.myopia.rec.feign.BusinessServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * rec服务调用客户端
 *
 * @author hang.yuan 2022/8/10 09:51
 */
@FeignClient(name = "myopia-rec", decode404 = true, fallbackFactory = RecServiceFallbackFactory.class,configuration = BusinessServiceFeignConfig.class)
public interface RecServiceClient {

    @PostMapping(value = "/rec/export")
    RecExportVO export(@RequestBody RecExportDTO exportDTO);
}
