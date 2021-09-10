package com.wupol.myopia.business.core.screening.flow.util;

import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 筛查编号生成器
 *
 * @Author HaoHao
 * @Date 2021/9/9
 **/
@Component
public class ScreeningCodeGenerator implements CommandLineRunner {

    /** 默认偏移量 */
    private static final long DEFAULT_OFFSET = 100000000000L;
    /** 生成器 */
    private static AtomicLong generator = new AtomicLong(DEFAULT_OFFSET);

    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Override
    public void run(String... args) {
        Long currentMaxCode = screeningPlanSchoolService.getCurrentMaxScreeningCode();
        initOffSetOnlyOnce(Objects.isNull(currentMaxCode) ? DEFAULT_OFFSET : currentMaxCode);
    }

    /**
     * 初始化偏移量
     *
     * @param offset 偏移量
     * @return void
     **/
    private static void initOffSetOnlyOnce(long offset) {
        boolean success = generator.compareAndSet(DEFAULT_OFFSET, offset);
        Assert.isTrue(success, "操作无效，已经初始化过生成器");
    }

    /**
     * 批量生成ID
     *
     * @param amount 数量
     * @return java.util.List<java.lang.Long>
     **/
    public static List<Long> getIdBatch(long amount) {
        long currentValue = generator.addAndGet(amount);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            ids.add(currentValue - i);
        }
        return ids;
    }

    /**
     * 获取下一个ID
     *
     * @return java.lang.Long
     **/
    public static Long nextId() {
        return generator.incrementAndGet();
    }
}
