package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.collection.CollUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.domain.vo.OnlineUserStatisticVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户在线统计
 *
 * @author hang.yuan 2022/5/10 10:00
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/onlineUsers")
public class OnlineUsersStatisticController {
    @Autowired
    private RedisUtil redisUtil;

    @GetMapping()
    public OnlineUserStatisticVO getOnlineNum(){
        OnlineUserStatisticVO onlineUserStatisticVO= new OnlineUserStatisticVO();
        Set<String> keys = redisUtil.getOnline();
        if (CollUtil.isNotEmpty(keys)){
            Stream<String> managementClientUser = keys.stream().filter(key->key.contains("online:1") || key.contains("online:6") );
            Stream<String> schoolClientUser = keys.stream().filter(key->key.contains("online:2"));
            Stream<String> screeningClientUser = keys.stream().filter(key->key.contains("online:3"));
            Stream<String> hospitalClientUser = keys.stream().filter(key->key.contains("online:4") );
            Stream<String> parentClientUser = keys.stream().filter(key->key.contains("online:5"));
            Stream<String> zeroToSixClientUser = keys.stream().filter(key->key.contains("online:7"));
            Stream<String> questionnaireClientUser = keys.stream().filter(key->key.contains("online:8"));

            onlineUserStatisticVO.setManagementClientNum(managementClientUser.count())
                    .setSchoolClientNum(schoolClientUser.count())
                    .setScreeningClientNum(screeningClientUser.count())
                    .setHospitalClientNum(hospitalClientUser.count())
                    .setParentClientNum(parentClientUser.count())
                    .setZeroToSixClientNum(zeroToSixClientUser.count())
                    .setQuestionnaireClientNum(questionnaireClientUser.count());

            onlineUserStatisticVO.setManagementClientUserList(redisUtil.batchGet(managementClientUser.collect(Collectors.toList())))
                    .setSchoolClientUserList(redisUtil.batchGet(schoolClientUser.collect(Collectors.toList())))
                    .setScreeningClientUserList(redisUtil.batchGet(screeningClientUser.collect(Collectors.toList())))
                    .setHospitalClientUserList(redisUtil.batchGet(hospitalClientUser.collect(Collectors.toList())))
                    .setParentClientUserList(redisUtil.batchGet(parentClientUser.collect(Collectors.toList())))
                    .setZeroToSixClientUserList(redisUtil.batchGet(zeroToSixClientUser.collect(Collectors.toList())))
                    .setQuestionnaireClientUserList(redisUtil.batchGet(questionnaireClientUser.collect(Collectors.toList())));
        }else {
            onlineUserStatisticVO.setManagementClientNum(0L).setSchoolClientNum(0L)
                    .setScreeningClientNum(0L).setHospitalClientNum(0L).setParentClientNum(0L)
                    .setZeroToSixClientNum(0L);
        }
        return onlineUserStatisticVO;
    }
}
