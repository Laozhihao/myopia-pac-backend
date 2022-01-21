package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 钓猫的小鱼
 * @Date: 2022/01/04/14:45
 * @Description:
 */
@Service
public class SysUtilService {

    /**
     * 次数扩展字段
     */
    private final String COUNT = "count";
    /**
     * 每天下载次数
     */
    private final int CALL_COUNT = 2;
    /**
     * 非平台管理员
     */
    private final int NOPLATFORM = 1;

    private final RedisUtil redisUtil;

    public SysUtilService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /***
     * @Description: 非平台管理员一天只能下载2次
     * @Param: [key]
     * @return: void
     * @Author: 钓猫的小鱼
     * @Date: 2022/1/17
     */
    public void isNoPlatformRepeatExport(String key){
        if (CurrentUserUtil.getCurrentUser().getUserType()==NOPLATFORM){
            isExport(key);
        }
    }

    /**
    * @Description: 查看用户是否多次导出，
    * @Param: [key值, 参数结果]
    * @return: boolean true:允许导出  false：禁止导出
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/4
    */
    public void isExport(String key){
        Map<String,Object> result = JSON.parseObject(redisUtil.get(key).toString(),HashMap.class);

        if (Objects.isNull(result) || result.isEmpty()){
            Map<String,Object> param  = new HashMap<>(2);
            param.put(COUNT,1);
            redisUtil.cSet(key,param);
            return;
        }
        int count = (Integer)result.get(COUNT);
        if (count>=CALL_COUNT){
            throw new BusinessException("今天的次数已用完，请明天再操作！！！");
        }
        count = count+1;
        result.put(COUNT,count);
        redisUtil.cSet(key,result);
    }

}
