package com.wupol.myopia.third.party.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;

/**
 * kafka生产者
 *
 * @Author lzh
 * @Date 2023/4/14
 **/
@Component
public class KafkaProducer {

    @Resource
    private KafkaTemplate<String,String> kafkaTemplate;

    /**
     * 发送字符串信息（指定主题）
     * @param topic     主题
     * @param message   信息
     */
    public ListenableFuture<SendResult<String, String>> send(String topic, String message){
        return kafkaTemplate.send(topic, message);
    }

    /**
     * 发送字符串信息（指定主题）
     * @param topic     主题
     * @param key       信息键
     * @param message   信息值
     */
    public ListenableFuture<SendResult<String, String>> send(String topic, String key, String message){
        return kafkaTemplate.send(topic, key, message);
    }

    /**
     * 发送对象信息（指定主题）
     * @param topic 主题
     * @param message 数据
     **/
    public ListenableFuture<SendResult<String, String>> sendObject(String topic, Object message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(message);
        return kafkaTemplate.send(topic, json);
    }

    /**
     * 发送对象信息（指定主题）
     * @param topic 主题
     * @param key 信息键
     * @param message 数据
     **/
    public ListenableFuture<SendResult<String, String>> sendObject(String topic, String key, Object message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(message);
        return kafkaTemplate.send(topic, key, json);
    }

    /**
     * 发送字符串信息(默认主题)
     * @param message   信息
     */
    public ListenableFuture<SendResult<String, String>> sendDefaultTopic(String message){
        return kafkaTemplate.sendDefault(message);
    }

    /**
     * 发送字符串信息(默认主题)
     * @param key       键
     * @param message   信息
     */
    public ListenableFuture<SendResult<String, String>> sendDefaultTopic(String key, String message){
        return kafkaTemplate.sendDefault(key, message);
    }

    /**
     * 发送对象信息（默认主题）
     * @param message 数据
     **/
    public ListenableFuture<SendResult<String, String>> sendObjectDefaultTopic(Object message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(message);
        return kafkaTemplate.sendDefault(json);
    }

}