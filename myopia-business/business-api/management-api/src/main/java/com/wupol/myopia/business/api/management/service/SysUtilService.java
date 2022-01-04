package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.cache.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/04/14:45
 * @Description:
 */
@Service
public class SysUtilService {
    @Autowired
    private RedisUtil redisUtil;

    public boolean isExport(String key, Map<String,Object> param){
        Map<String,Object> result = (Map<String, Object>) redisUtil.get(key);
        if (result!=null&&(Integer) result.get("count")>=2){

            return false;
        }
        boolean isSave =  redisUtil.cSet(key,param);
        return isSave;
    }
}
