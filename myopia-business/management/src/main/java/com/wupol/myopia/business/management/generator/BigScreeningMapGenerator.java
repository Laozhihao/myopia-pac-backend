package com.wupol.myopia.business.management.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.management.domain.model.BigScreenMap;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.service.BigScreenMapService;
import com.wupol.myopia.business.management.service.DistrictService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jacob
 * @date 2021/02/19
 * 用于更新大屏某个前端地图插件的基本json数据
 */
@Component
@Slf4j
public class BigScreeningMapGenerator {

    @Autowired
    private DistrictService districtService;
    @Autowired
    private BigScreenMapService bigScreenMapService;

    /**
     * 生成地图
     */
    public void generator() {
        // 找到所有省级的地区
        LambdaQueryWrapper<District> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(District::getParentCode, "100000000");
        List<District> districts = districtService.getBaseMapper().selectList(queryWrapper);
        // 转换成地区code 访问json
        districts.stream().forEach(district -> {
            Long code = district.getCode();
            Object jsonObject = getJSONObject(code);
            BigScreenMap bigScreenMap = new BigScreenMap();
            //请求高德
            bigScreenMap.setDistrictId(district.getId()).setJson(jsonObject).setCreateTime(new Date());
            bigScreenMapService.getBaseMapper().insert(bigScreenMap);
        });
    }

    /**
     * 定时更新城市的坐标（目前只包含大陆）
     */
    public void city() {
        // 找到所有省级的地区
        LambdaQueryWrapper<BigScreenMap> queryWrapper = new LambdaQueryWrapper<>();
        List<BigScreenMap> bigScreenMaps = bigScreenMapService.getBaseMapper().selectList(queryWrapper);
        // 转换成地区code 访问json
        bigScreenMaps.stream().forEach(bigScreenMap -> {
            Map<Integer, JSONArray> longJSONArrayHashMap = new HashMap<>();
            Object json = bigScreenMap.getJson();
            Object read = JSONPath.read(JSON.toJSONString(json), "$.features");
            JSONArray features = (JSONArray) read;
            features.stream().forEach(feature -> {
                String name = (String) JSONPath.read(JSON.toJSONString(feature), "$.properties.name");
                Integer code = (Integer) JSONPath.read(JSON.toJSONString(feature), "$.properties.adcode");
                District district = districtService.getByCode(code * 1000L);
                if (district == null) {
                    try {
                        district = districtService.findOne(new District().setName(name));
                    } catch (Exception e) {
                        System.err.println(name);
                        e.printStackTrace();
                    }
                }
                JSONArray center = (JSONArray) JSONPath.read(JSON.toJSONString(feature), "$.properties.center");
                //string 转换成long
                longJSONArrayHashMap.put(district.getId(), center);
            });
            //bigScreenMap.setCityCenterLocation(longJSONArrayHashMap);
            bigScreenMapService.getBaseMapper().updateById(bigScreenMap);
        });
    }

    /**
     *
     * @return
     */
    public Object getJSONObject(Long code) {
        code = code / 1000;
        if (code.equals(830000)) {
            //高德的台湾省是710000
            code = 710000L;
        }
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建http GET请求
        HttpGet httpGet = new HttpGet("https://geo.datav.aliyun.com/areas_v2/bound/" + code + "_full.json");
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                return JSONObject.toJSON(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //相当于关闭浏览器
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}