package com.wupol.myopia.third.party.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * kafka消费者
 *
 * @Author lzh
 * @Date 2023/4/14
 **/
@Component
@Slf4j
public class KafkaConsumer {

    // @KafkaListener(topics = {"myopia-xinjiang-error-data"})
    public void errorConsumer(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        Optional<ConsumerRecord<String, String>> kafkaMessage = Optional.ofNullable(record);
        log.info("kafkaMessage：{}", kafkaMessage.toString());
        if (kafkaMessage.isPresent()) {
            log.info("获取到Kafka通知消息，key：{}, value：{}", record.key(), record.value());
            ObjectMapper mapper = new ObjectMapper();
            Map errorInfo;
            try {
                //不一定是map，根据生产者
                errorInfo = mapper.readValue(record.value(),  Map.class);
                // ...处理业务逻辑
                // 提交-消费确认
                acknowledgment.acknowledge();
            } catch (Exception e) {
                log.error("Kafka通知消息解析失败");
            }
        }
    }
}