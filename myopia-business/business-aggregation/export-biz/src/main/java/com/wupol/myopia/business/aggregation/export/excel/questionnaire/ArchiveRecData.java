package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 导出监测表数据
 *
 * @author hang.yuan 2022/8/26 10:08
 */
@Component
public class ArchiveRecData {

    /**
     * 监测表数据中转集合点
     */
    private Map<String,List<RecData>> dataMap = Maps.newConcurrentMap();
    /**
     * 是否将数据存储到监测表中转集合点
     */
    private Map<String,Boolean> dataStatusMap = Maps.newConcurrentMap();

    @Data
    public static class RecData{
        private Integer schoolType;
        private Integer schoolId;
        private List<String> qesFieldList;
        private List<List<String>> dataList;
    }

    /**
     * 设置监测表数据
     * @param key 唯一Key
     * @param dataList 监测表数据
     */
    public void setDataMap(String key,List<RecData> dataList){
        Boolean status = dataStatusMap.get(key);
        if (Objects.equals(status,Boolean.TRUE)){
            dataMap.put(key,dataList);
        }
    }


    public List<RecData> getDataMap(String key){
        List<RecData> recDataList = dataMap.getOrDefault(key, Lists.newArrayList());
        dataMap.remove(key);
        dataStatusMap.remove(key);
        return recDataList;
    }
}
