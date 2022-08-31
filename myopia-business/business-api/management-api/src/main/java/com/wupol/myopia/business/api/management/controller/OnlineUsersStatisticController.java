package com.wupol.myopia.business.api.management.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.api.management.domain.vo.OnlineUserStatisticVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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
            long managementClientNum = keys.stream().filter(key->key.contains("online:1") || key.contains("online:6") ).count();
            long schoolClientNum = keys.stream().filter(key->key.contains("online:2")).count();
            long screeningClientNum = keys.stream().filter(key->key.contains("online:3")).count();
            long hospitalClientNum = keys.stream().filter(key->key.contains("online:4") ).count();
            long parentClientNum = keys.stream().filter(key->key.contains("online:5")).count();
            long zeroToSixClientNum = keys.stream().filter(key->key.contains("online:7")).count();
            long questionnaireClientNum = keys.stream().filter(key->key.contains("online:8")).count();
            onlineUserStatisticVO.setManagementClientNum(managementClientNum)
                    .setSchoolClientNum(schoolClientNum)
                    .setScreeningClientNum(screeningClientNum)
                    .setHospitalClientNum(hospitalClientNum)
                    .setParentClientNum(parentClientNum)
                    .setZeroToSixClientNum(zeroToSixClientNum)
                    .setQuestionnaireClientNum(questionnaireClientNum);
        }else {
            onlineUserStatisticVO.setManagementClientNum(0L).setSchoolClientNum(0L)
                    .setScreeningClientNum(0L).setHospitalClientNum(0L).setParentClientNum(0L)
                    .setZeroToSixClientNum(0L);
        }
        return onlineUserStatisticVO;
    }
}
