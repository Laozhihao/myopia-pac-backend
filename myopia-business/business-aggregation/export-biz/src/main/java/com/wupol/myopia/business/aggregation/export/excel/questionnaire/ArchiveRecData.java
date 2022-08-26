package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 导出监测表数据
 *
 * @author hang.yuan 2022/8/26 10:08
 */
@Component
public class ArchiveRecData {

    private Map<String,List<RecData>> dataMap = Maps.newConcurrentMap();

    @Data
    public static class RecData{
        private Integer schoolType;
        private Integer schoolId;
        private List<String> qesFieldList;
        private List<List<String>> dataList;
    }

    public void setDataMap(String key,List<RecData> dataList){
        dataMap.put(key,dataList);
    }

    public List<RecData> getDataMap(String key){
        List<RecData> recDataList = dataMap.getOrDefault(key, Lists.newArrayList());
        dataMap.remove(key);
        return recDataList;
    }
}
