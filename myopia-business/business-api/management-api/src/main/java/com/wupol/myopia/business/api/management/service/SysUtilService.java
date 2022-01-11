package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    /**
    * @Description: 查看用户是否多次导出，
    * @Param: [key值, 参数结果]
    * @return: boolean true:允许导出  false：禁止导出
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/4
    */
    public boolean isExport(String key){
        Map<String,Object> result = (Map<String, Object>) redisUtil.get(key);
        if (result==null){
            Map<String,Object> param  = new HashMap<>();
            param.put("count",1);
            redisUtil.cSet(key,param);
            return true;
        }
       int count = (Integer)result.get("count");
        if (count>=2){
            throw new BusinessException("今天的次数已用完，请明天再操作！！！");
        }
        count = count+1;
        result.put("count",count);
        redisUtil.cSet(key,result);
        return true;
    }
}
