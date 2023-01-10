package com.wupol.myopia.business.api.management.service.stressTest;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@UtilityClass
public class IdCardGenerator {
    private static final Map<Integer, LineIterator> ID_CARD_ITERATOR_MAP = new ConcurrentHashMap<>(16);

    public String getIdCard(int age) throws IOException {
        LineIterator lineIterator = getLineIterator(age);
        return getIdCard(lineIterator);
    }

    private String getIdCard(LineIterator lineIterator) {
        if (!lineIterator.hasNext()) {
            throw new RuntimeException("已经到达文件结尾");
        }
        String idCard = lineIterator.nextLine();
        if (StringUtils.hasLength(idCard)) {
            return idCard;
        }
        log.warn("从txt文件获取IdCard为空! idCard= [{}]", idCard);
        return getIdCard(lineIterator);
    }

    /**
     * 容器式单例模式
     */
    private LineIterator getLineIterator(int age) throws IOException {
        synchronized (ID_CARD_ITERATOR_MAP) {
            if (ID_CARD_ITERATOR_MAP.containsKey(age)) {
                return ID_CARD_ITERATOR_MAP.get(age);
            }
            LineIterator lineIterator = FileUtils.lineIterator(new File("E:\\近视防控 - 压测\\身份证号码\\id-card-" + age + ".txt"), "UTF-8");
            ID_CARD_ITERATOR_MAP.put(age, lineIterator);
            return lineIterator;
        }
    }
}
