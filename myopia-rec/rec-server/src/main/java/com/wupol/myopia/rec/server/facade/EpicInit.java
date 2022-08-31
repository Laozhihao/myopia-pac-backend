package com.wupol.myopia.rec.server.facade;

import com.wupol.myopia.rec.server.util.EpiDataUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Epic程序初始化
 *
 * @author hang.yuan 2022/8/23 14:57
 */
@Slf4j
@Component
@Order(1)
@Getter
@Setter
public class EpicInit implements CommandLineRunner {

    private Boolean initStatus = false;

    @Override
    public void run(String... args) throws Exception {
        setInitStatus(EpiDataUtil.initEpic());
        if(Objects.equals(initStatus,Boolean.TRUE)){
            log.info("EpiC initialized success!");
        }else {
            log.info("EpiC initialized fail!");
        }
    }
}
