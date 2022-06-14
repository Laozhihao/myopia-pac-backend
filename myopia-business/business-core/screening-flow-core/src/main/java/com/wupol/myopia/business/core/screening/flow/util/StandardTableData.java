package com.wupol.myopia.business.core.screening.flow.util;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.BigDecimalUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 判断标准表数据
 *
 * @author hang.yuan 2022/4/12 16:58
 */
public class StandardTableData {

    private static final List<OverweightAndObesityData> OVERWEIGHT_AND_OBESITY_DATA_LIST;
    private static final List<StuntingData> STUNTING_DATA_LIST;
    private static final List<WastingData> WASTING_DATA_LIST;
    private static final List<BloodPressureData> BLOOD_PRESSURE_DATA_LIST;
    private static final List<StandardHeight> STANDARD_HEIGHT;

    static {
        OVERWEIGHT_AND_OBESITY_DATA_LIST = initOverweightAndObesityData();
        STUNTING_DATA_LIST = initStuntingData();
        WASTING_DATA_LIST = initWastingData();
        BLOOD_PRESSURE_DATA_LIST = initBloodPressureData();
        STANDARD_HEIGHT= initStandardHeight();
    }



    /**
     * 超重和肥胖数据
     * @author hang.yuan
     * @date 2022/4/12
     */
    @Data
    @Accessors(chain = true)
    static class OverweightAndObesityData{
        private String age;
        private Integer gender;
        private String overweight;
        private String obesity;
    }

    private static List<OverweightAndObesityData> initOverweightAndObesityData(){
        List<OverweightAndObesityData> overweightAndObesityDataList= Lists.newArrayList();
        //男
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("6.0").setGender(0).setOverweight("16.4").setObesity("17.7"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("6.5").setGender(0).setOverweight("16.7").setObesity("18.1"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("7.0").setGender(0).setOverweight("17.0").setObesity("18.7"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("7.5").setGender(0).setOverweight("17.4").setObesity("19.2"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("8.0").setGender(0).setOverweight("17.8").setObesity("19.7"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("8.5").setGender(0).setOverweight("18.1").setObesity("20.3"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("9.0").setGender(0).setOverweight("18.5").setObesity("20.8"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("9.5").setGender(0).setOverweight("18.9").setObesity("21.4"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("10.0").setGender(0).setOverweight("19.2").setObesity("21.9"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("10.5").setGender(0).setOverweight("19.6").setObesity("22.5"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("11.0").setGender(0).setOverweight("19.9").setObesity("23.0"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("11.5").setGender(0).setOverweight("20.3").setObesity("23.6"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("12.0").setGender(0).setOverweight("20.7").setObesity("24.1"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("12.5").setGender(0).setOverweight("21.0").setObesity("24.7"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("13.0").setGender(0).setOverweight("21.4").setObesity("25.2"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("13.5").setGender(0).setOverweight("21.9").setObesity("25.7"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("14.0").setGender(0).setOverweight("22.3").setObesity("26.1"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("14.5").setGender(0).setOverweight("22.6").setObesity("26.4"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("15.0").setGender(0).setOverweight("22.9").setObesity("26.6"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("15.5").setGender(0).setOverweight("23.1").setObesity("26.9"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("16.0").setGender(0).setOverweight("23.3").setObesity("27.1"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("16.5").setGender(0).setOverweight("23.5").setObesity("27.4"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("17.0").setGender(0).setOverweight("23.7").setObesity("27.6"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("17.5").setGender(0).setOverweight("23.8").setObesity("27.8"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("18.0").setGender(0).setOverweight("24.0").setObesity("28.0"));
        //女
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("6.0").setGender(1).setOverweight("16.2").setObesity("17.5"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("6.5").setGender(1).setOverweight("16.5").setObesity("18.0"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("7.0").setGender(1).setOverweight("16.8").setObesity("18.5"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("7.5").setGender(1).setOverweight("17.2").setObesity("19.0"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("8.0").setGender(1).setOverweight("17.6").setObesity("19.4"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("8.5").setGender(1).setOverweight("18.1").setObesity("19.9"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("9.0").setGender(1).setOverweight("18.5").setObesity("20.4"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("9.5").setGender(1).setOverweight("19.0").setObesity("21.0"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("10.0").setGender(1).setOverweight("19.5").setObesity("21.5"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("10.5").setGender(1).setOverweight("20.0").setObesity("22.1"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("11.0").setGender(1).setOverweight("20.5").setObesity("22.7"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("11.5").setGender(1).setOverweight("21.1").setObesity("23.3"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("12.0").setGender(1).setOverweight("21.5").setObesity("23.9"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("12.5").setGender(1).setOverweight("21.9").setObesity("24.5"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("13.0").setGender(1).setOverweight("22.2").setObesity("25.0"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("13.5").setGender(1).setOverweight("22.6").setObesity("25.6"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("14.0").setGender(1).setOverweight("22.8").setObesity("25.9"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("14.5").setGender(1).setOverweight("23.0").setObesity("26.3"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("15.0").setGender(1).setOverweight("23.2").setObesity("26.6"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("15.5").setGender(1).setOverweight("23.4").setObesity("26.9"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("16.0").setGender(1).setOverweight("23.6").setObesity("27.1"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("16.5").setGender(1).setOverweight("23.7").setObesity("27.4"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("17.0").setGender(1).setOverweight("23.8").setObesity("27.6"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("17.5").setGender(1).setOverweight("23.9").setObesity("27.8"));
        overweightAndObesityDataList.add(new OverweightAndObesityData().setAge("18.0").setGender(1).setOverweight("24.0").setObesity("28.0"));

        return overweightAndObesityDataList;
    }


    public static OverweightAndObesityData getOverweightAndObesityData(String age,Integer gender){
         String newAge = BigDecimalUtil.lessThan(new BigDecimal(age),"6.0")?"6.0":BigDecimalUtil.moreThan(new BigDecimal(age),"18.0")?"18.0":age;
         return OVERWEIGHT_AND_OBESITY_DATA_LIST.stream()
                .filter(data -> data.getAge().equals(newAge) && data.getGender().equals(gender))
                .findFirst()
                .orElse(null);
    }

    /**
     * 营养不良 （生长迟缓数据）
     * @author hang.yuan
     * @date 2022/4/13
     */
    @Data
    @Accessors(chain = true)
    static class StuntingData {
        private String age;
        private Integer gender;
        private String height;
    }

    private static List<StuntingData> initStuntingData() {
        List<StuntingData> stuntingDataList= Lists.newArrayList();
        //男
        stuntingDataList.add(new StuntingData().setAge("6.0").setGender(0).setHeight("106.3"));
        stuntingDataList.add(new StuntingData().setAge("6.5").setGender(0).setHeight("109.5"));
        stuntingDataList.add(new StuntingData().setAge("7.0").setGender(0).setHeight("111.3"));
        stuntingDataList.add(new StuntingData().setAge("7.5").setGender(0).setHeight("112.8"));
        stuntingDataList.add(new StuntingData().setAge("8.0").setGender(0).setHeight("115.4"));
        stuntingDataList.add(new StuntingData().setAge("8.5").setGender(0).setHeight("117.6"));
        stuntingDataList.add(new StuntingData().setAge("9.0").setGender(0).setHeight("120.6"));
        stuntingDataList.add(new StuntingData().setAge("9.5").setGender(0).setHeight("123.0"));
        stuntingDataList.add(new StuntingData().setAge("10.0").setGender(0).setHeight("125.2"));
        stuntingDataList.add(new StuntingData().setAge("10.5").setGender(0).setHeight("127.0"));
        stuntingDataList.add(new StuntingData().setAge("11.0").setGender(0).setHeight("129.1"));
        stuntingDataList.add(new StuntingData().setAge("11.5").setGender(0).setHeight("130.8"));
        stuntingDataList.add(new StuntingData().setAge("12.0").setGender(0).setHeight("133.1"));
        stuntingDataList.add(new StuntingData().setAge("12.5").setGender(0).setHeight("134.9"));
        stuntingDataList.add(new StuntingData().setAge("13.0").setGender(0).setHeight("136.9"));
        stuntingDataList.add(new StuntingData().setAge("13.5").setGender(0).setHeight("138.6"));
        stuntingDataList.add(new StuntingData().setAge("14.0").setGender(0).setHeight("141.9"));
        stuntingDataList.add(new StuntingData().setAge("14.5").setGender(0).setHeight("144.7"));
        stuntingDataList.add(new StuntingData().setAge("15.0").setGender(0).setHeight("149.6"));
        stuntingDataList.add(new StuntingData().setAge("15.5").setGender(0).setHeight("153.6"));
        stuntingDataList.add(new StuntingData().setAge("16.0").setGender(0).setHeight("155.1"));
        stuntingDataList.add(new StuntingData().setAge("16.5").setGender(0).setHeight("156.4"));
        stuntingDataList.add(new StuntingData().setAge("17.0").setGender(0).setHeight("156.8"));
        stuntingDataList.add(new StuntingData().setAge("17.5").setGender(0).setHeight("157.1"));
        //女
        stuntingDataList.add(new StuntingData().setAge("6.0").setGender(1).setHeight("105.7"));
        stuntingDataList.add(new StuntingData().setAge("6.5").setGender(1).setHeight("108.0"));
        stuntingDataList.add(new StuntingData().setAge("7.0").setGender(1).setHeight("110.2"));
        stuntingDataList.add(new StuntingData().setAge("7.5").setGender(1).setHeight("111.8"));
        stuntingDataList.add(new StuntingData().setAge("8.0").setGender(1).setHeight("114.5"));
        stuntingDataList.add(new StuntingData().setAge("8.5").setGender(1).setHeight("116.8"));
        stuntingDataList.add(new StuntingData().setAge("9.0").setGender(1).setHeight("119.5"));
        stuntingDataList.add(new StuntingData().setAge("9.5").setGender(1).setHeight("121.7"));
        stuntingDataList.add(new StuntingData().setAge("10.0").setGender(1).setHeight("123.9"));
        stuntingDataList.add(new StuntingData().setAge("10.5").setGender(1).setHeight("125.7"));
        stuntingDataList.add(new StuntingData().setAge("11.0").setGender(1).setHeight("128.6"));
        stuntingDataList.add(new StuntingData().setAge("11.5").setGender(1).setHeight("131.0"));
        stuntingDataList.add(new StuntingData().setAge("12.0").setGender(1).setHeight("133.6"));
        stuntingDataList.add(new StuntingData().setAge("12.5").setGender(1).setHeight("135.7"));
        stuntingDataList.add(new StuntingData().setAge("13.0").setGender(1).setHeight("138.8"));
        stuntingDataList.add(new StuntingData().setAge("13.5").setGender(1).setHeight("141.4"));
        stuntingDataList.add(new StuntingData().setAge("14.0").setGender(1).setHeight("142.9"));
        stuntingDataList.add(new StuntingData().setAge("14.5").setGender(1).setHeight("144.1"));
        stuntingDataList.add(new StuntingData().setAge("15.0").setGender(1).setHeight("145.4"));
        stuntingDataList.add(new StuntingData().setAge("15.5").setGender(1).setHeight("146.5"));
        stuntingDataList.add(new StuntingData().setAge("16.0").setGender(1).setHeight("146.8"));
        stuntingDataList.add(new StuntingData().setAge("16.5").setGender(1).setHeight("147.0"));
        stuntingDataList.add(new StuntingData().setAge("17.0").setGender(1).setHeight("147.3"));
        stuntingDataList.add(new StuntingData().setAge("17.5").setGender(1).setHeight("147.5"));

        return stuntingDataList;
    }

    public static StuntingData getStuntingData(String age,Integer gender){
        String newAge = BigDecimalUtil.lessThan(new BigDecimal(age),"6.0")?"6.0":BigDecimalUtil.moreThan(new BigDecimal(age),"17.5")?"17.5":age;
        return  STUNTING_DATA_LIST.stream()
                .filter(data -> data.getAge().equals(newAge) && data.getGender().equals(gender))
                .findFirst()
                .orElse(null);

    }

    /**
     * 营养不良 （消瘦数据）
     * @author hang.yuan
     * @date 2022/4/13
     */
    @Data
    @Accessors(chain = true)
    static class WastingData{
        private String age;
        private Integer gender;
        private String[] mild;
        private String moderateAndHigh;
    }

    private static List<WastingData> initWastingData(){
        List<WastingData> wastingDataList= Lists.newArrayList();
        wastingDataList.add(new WastingData().setAge("6.0").setGender(0).setModerateAndHigh("13.2").setMild(new String[]{"13.3","13.4"}));
        wastingDataList.add(new WastingData().setAge("6.5").setGender(0).setModerateAndHigh("13.4").setMild(new String[]{"13.5","13.8"}));
        wastingDataList.add(new WastingData().setAge("7.0").setGender(0).setModerateAndHigh("13.5").setMild(new String[]{"13.6","13.9"}));
        wastingDataList.add(new WastingData().setAge("7.5").setGender(0).setModerateAndHigh("13.5").setMild(new String[]{"13.6","13.9"}));
        wastingDataList.add(new WastingData().setAge("8.0").setGender(0).setModerateAndHigh("13.6").setMild(new String[]{"13.7","14.0"}));
        wastingDataList.add(new WastingData().setAge("8.5").setGender(0).setModerateAndHigh("13.6").setMild(new String[]{"13.7","14.0"}));
        wastingDataList.add(new WastingData().setAge("9.0").setGender(0).setModerateAndHigh("13.7").setMild(new String[]{"13.8","14.1"}));
        wastingDataList.add(new WastingData().setAge("9.5").setGender(0).setModerateAndHigh("13.8").setMild(new String[]{"13.9","14.2"}));
        wastingDataList.add(new WastingData().setAge("10.0").setGender(0).setModerateAndHigh("13.9").setMild(new String[]{"14.0","14.4"}));
        wastingDataList.add(new WastingData().setAge("10.5").setGender(0).setModerateAndHigh("14.0").setMild(new String[]{"14.1","14.6"}));
        wastingDataList.add(new WastingData().setAge("11.0").setGender(0).setModerateAndHigh("14.2").setMild(new String[]{"14.3","14.9"}));
        wastingDataList.add(new WastingData().setAge("11.5").setGender(0).setModerateAndHigh("14.3").setMild(new String[]{"14.4","15.1"}));
        wastingDataList.add(new WastingData().setAge("12.0").setGender(0).setModerateAndHigh("14.4").setMild(new String[]{"14.5","15.4"}));
        wastingDataList.add(new WastingData().setAge("12.5").setGender(0).setModerateAndHigh("14.5").setMild(new String[]{"14.6","15.6"}));
        wastingDataList.add(new WastingData().setAge("13.0").setGender(0).setModerateAndHigh("14.8").setMild(new String[]{"14.9","15.9"}));
        wastingDataList.add(new WastingData().setAge("13.5").setGender(0).setModerateAndHigh("15.0").setMild(new String[]{"15.1","16.1"}));
        wastingDataList.add(new WastingData().setAge("14.0").setGender(0).setModerateAndHigh("15.3").setMild(new String[]{"15.4","16.4"}));
        wastingDataList.add(new WastingData().setAge("14.5").setGender(0).setModerateAndHigh("15.5").setMild(new String[]{"15.6","16.7"}));
        wastingDataList.add(new WastingData().setAge("15.0").setGender(0).setModerateAndHigh("15.8").setMild(new String[]{"15.9","16.9"}));
        wastingDataList.add(new WastingData().setAge("15.5").setGender(0).setModerateAndHigh("16.0").setMild(new String[]{"16.1","17.0"}));
        wastingDataList.add(new WastingData().setAge("16.0").setGender(0).setModerateAndHigh("16.2").setMild(new String[]{"16.3","17.3"}));
        wastingDataList.add(new WastingData().setAge("16.5").setGender(0).setModerateAndHigh("16.4").setMild(new String[]{"16.5","17.5"}));
        wastingDataList.add(new WastingData().setAge("17.0").setGender(0).setModerateAndHigh("16.6").setMild(new String[]{"16.7","17.7"}));
        wastingDataList.add(new WastingData().setAge("17.5").setGender(0).setModerateAndHigh("16.8").setMild(new String[]{"16.9","17.9"}));

        wastingDataList.add(new WastingData().setAge("6.0").setGender(1).setModerateAndHigh("12.8").setMild(new String[]{"12.9","13.1"}));
        wastingDataList.add(new WastingData().setAge("6.5").setGender(1).setModerateAndHigh("12.9").setMild(new String[]{"13.0","13.3"}));
        wastingDataList.add(new WastingData().setAge("7.0").setGender(1).setModerateAndHigh("13.0").setMild(new String[]{"13.1","13.4"}));
        wastingDataList.add(new WastingData().setAge("7.5").setGender(1).setModerateAndHigh("13.0").setMild(new String[]{"13.1","13.5"}));
        wastingDataList.add(new WastingData().setAge("8.0").setGender(1).setModerateAndHigh("13.1").setMild(new String[]{"13.2","13.6"}));
        wastingDataList.add(new WastingData().setAge("8.5").setGender(1).setModerateAndHigh("13.1").setMild(new String[]{"13.2","13.7"}));
        wastingDataList.add(new WastingData().setAge("9.0").setGender(1).setModerateAndHigh("13.2").setMild(new String[]{"13.3","13.8"}));
        wastingDataList.add(new WastingData().setAge("9.5").setGender(1).setModerateAndHigh("13.2").setMild(new String[]{"13.3","13.9"}));
        wastingDataList.add(new WastingData().setAge("10.0").setGender(1).setModerateAndHigh("13.3").setMild(new String[]{"13.4","14.0"}));
        wastingDataList.add(new WastingData().setAge("10.5").setGender(1).setModerateAndHigh("13.4").setMild(new String[]{"13.5","14.1"}));
        wastingDataList.add(new WastingData().setAge("11.0").setGender(1).setModerateAndHigh("13.7").setMild(new String[]{"13.8","14.3"}));
        wastingDataList.add(new WastingData().setAge("11.5").setGender(1).setModerateAndHigh("13.9").setMild(new String[]{"14.0","14.5"}));
        wastingDataList.add(new WastingData().setAge("12.0").setGender(1).setModerateAndHigh("14.1").setMild(new String[]{"14.2","14.7"}));
        wastingDataList.add(new WastingData().setAge("12.5").setGender(1).setModerateAndHigh("14.3").setMild(new String[]{"14.4","14.9"}));
        wastingDataList.add(new WastingData().setAge("13.0").setGender(1).setModerateAndHigh("14.6").setMild(new String[]{"14.7","15.3"}));
        wastingDataList.add(new WastingData().setAge("13.5").setGender(1).setModerateAndHigh("14.9").setMild(new String[]{"15.0","15.3"}));
        wastingDataList.add(new WastingData().setAge("14.0").setGender(1).setModerateAndHigh("15.3").setMild(new String[]{"15.4","16.0"}));
        wastingDataList.add(new WastingData().setAge("14.5").setGender(1).setModerateAndHigh("15.7").setMild(new String[]{"15.8","16.3"}));
        wastingDataList.add(new WastingData().setAge("15.0").setGender(1).setModerateAndHigh("16.0").setMild(new String[]{"16.1","16.6"}));
        wastingDataList.add(new WastingData().setAge("15.5").setGender(1).setModerateAndHigh("16.2").setMild(new String[]{"16.3","16.8"}));
        wastingDataList.add(new WastingData().setAge("16.0").setGender(1).setModerateAndHigh("16.4").setMild(new String[]{"16.5","17.0"}));
        wastingDataList.add(new WastingData().setAge("16.5").setGender(1).setModerateAndHigh("16.5").setMild(new String[]{"16.6","17.1"}));
        wastingDataList.add(new WastingData().setAge("17.0").setGender(1).setModerateAndHigh("16.6").setMild(new String[]{"16.7","17.2"}));
        wastingDataList.add(new WastingData().setAge("17.5").setGender(1).setModerateAndHigh("16.7").setMild(new String[]{"16.8","17.3"}));

        return wastingDataList;
    }

    public static WastingData getWastingData(String age,Integer gender){
        String newAge = BigDecimalUtil.lessThan(new BigDecimal(age),"6.0")?"6.0":BigDecimalUtil.moreThan(new BigDecimal(age),"17.5")?"17.5":age;
        return  WASTING_DATA_LIST.stream()
                .filter(data -> data.getAge().equals(newAge) && data.getGender().equals(gender))
                .findFirst()
                .orElse(null);

    }

    /**
     * 身高
     */
    @Data
    @Accessors(chain = true)
    static class StandardHeight implements Serializable {
        private Integer age;
        private Integer gender;
        private HeightPercentileEnum heightPercentile;
        private String heightValue;
    }

    private static List<StandardHeight> initStandardHeight(){
        List<StandardHeight> standardHeightList =Lists.newArrayList();
        standardHeightList.add(new StandardHeight().setAge(7).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("115.7"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("117.9"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("121.5"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("125.5"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("129.5"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("133.3"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("135.4"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("120.6"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("122.9"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("126.5"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("130.7"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("134.9"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("138.7"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("141.0"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("125.0"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("127.4"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("131.4"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("135.8"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("140.3"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("144.2"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("146.6"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("130.0"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("132.1"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("136.1"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("140.8"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("145.4"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("149.8"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("152.4"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("133.7"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("136.4"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("141.0"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("146.0"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("151.3"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("156.5"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("159.7"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("138.4"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("141.2"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("146.0"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("152.0"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("158.6"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("164.1"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("167.3"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("145.1"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("148.2"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("154.0"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("160.2"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("166.2"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("170.7"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("173.4"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("151.6"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("155.0"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("160.4"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("165.7"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("170.5"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("175.0"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("177.5"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("157.2"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("160.0"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("164.4"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("169.0"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("173.4"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("177.4"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("180.0"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("160.0"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("162.4"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("166.3"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("170.5"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("174.9"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("178.8"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("181.0"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(0).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("161.2"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("163.3"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("167.1"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("171.4"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("175.6"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("179.5"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(0).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("181.9"));

        standardHeightList.add(new StandardHeight().setAge(7).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("114.3"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("116.5"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("120.1"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("124.1"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("128.1"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("131.9"));
        standardHeightList.add(new StandardHeight().setAge(7).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("134.0"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("119.2"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("121.5"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("125.2"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("129.3"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("133.6"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("137.2"));
        standardHeightList.add(new StandardHeight().setAge(8).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("139.6"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("124.0"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("126.4"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("130.3"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("135.0"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("139.6"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("143.9"));
        standardHeightList.add(new StandardHeight().setAge(9).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("146.5"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("129.1"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("131.8"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("136.0"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("141.2"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("146.3"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("150.7"));
        standardHeightList.add(new StandardHeight().setAge(10).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("153.3"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("134.1"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("137.1"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("142.0"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("147.3"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("152.7"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("157.1"));
        standardHeightList.add(new StandardHeight().setAge(11).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("159.6"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("139.7"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("142.6"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("147.5"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("152.5"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("157.1"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("160.9"));
        standardHeightList.add(new StandardHeight().setAge(12).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("163.5"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("145.6"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("148.0"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("152.0"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("156.1"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("160.0"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("163.8"));
        standardHeightList.add(new StandardHeight().setAge(13).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("166.0"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("148.2"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("150.4"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("154.0"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("157.8"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("161.6"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("165.1"));
        standardHeightList.add(new StandardHeight().setAge(14).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("167.2"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("149.2"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("151.4"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("154.8"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("158.4"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("162.2"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("166.0"));
        standardHeightList.add(new StandardHeight().setAge(15).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("168.0"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("150.0"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("151.7"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("155.2"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("159.0"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("162.8"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("166.3"));
        standardHeightList.add(new StandardHeight().setAge(16).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("168.3"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(1).setHeightPercentile(HeightPercentileEnum.equalP5).setHeightValue("150.1"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setHeightValue("152.0"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setHeightValue("155.3"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setHeightValue("159.2"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setHeightValue("163.1"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setHeightValue("166.6"));
        standardHeightList.add(new StandardHeight().setAge(17).setGender(1).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setHeightValue("168.8"));

        return standardHeightList;
    }

    private static StandardHeight getStandardHeight(Integer age,Integer gender,BigDecimal height){
        List<StandardHeight> heightList = STANDARD_HEIGHT.stream()
                .filter(data -> data.getAge().equals(age) && data.getGender().equals(gender))
                .collect(Collectors.toList());

        Map<HeightPercentileEnum, StandardHeight> standardHeightMap = heightList.stream().collect(Collectors.toMap(StandardHeight::getHeightPercentile, Function.identity()));

        StandardHeight result = null;

        for (StandardHeight standardHeight : heightList) {

            if (Objects.nonNull(result)){
                break;
            }

            switch (standardHeight.heightPercentile){
                case equalP5:
                    if (BigDecimalUtil.lessThan(height,standardHeight.getHeightValue())){
                        result = ObjectUtil.clone(standardHeight);
                        result.setHeightPercentile(HeightPercentileEnum.lessThanP5);
                    }
                    StandardHeight moreThanEqualP10 = standardHeightMap.get(HeightPercentileEnum.moreThanEqualP10);
                    if (BigDecimalUtil.isBetweenLeft(height,standardHeight.getHeightValue(),moreThanEqualP10.getHeightValue()) ){
                        result = ObjectUtil.clone(standardHeight);
                        result.setHeightPercentile(HeightPercentileEnum.moreThanEqualP5);
                    }
                    break;
                case moreThanEqualP10:
                    StandardHeight moreThanEqualP25 = standardHeightMap.get(HeightPercentileEnum.moreThanEqualP25);
                    if (BigDecimalUtil.isBetweenLeft(height,standardHeight.getHeightValue(),moreThanEqualP25.getHeightValue()) ){
                        result = standardHeight;
                    }
                    break;
                case moreThanEqualP25:
                    StandardHeight moreThanEqualP50 = standardHeightMap.get(HeightPercentileEnum.moreThanEqualP50);
                    if (BigDecimalUtil.isBetweenLeft(height,standardHeight.getHeightValue(),moreThanEqualP50.getHeightValue()) ){
                        result = standardHeight;
                    }
                    break;
                case moreThanEqualP50:
                    StandardHeight moreThanEqualP75 = standardHeightMap.get(HeightPercentileEnum.moreThanEqualP75);
                    if (BigDecimalUtil.isBetweenLeft(height,standardHeight.getHeightValue(),moreThanEqualP75.getHeightValue()) ){
                        result = standardHeight;
                    }
                    break;
                case moreThanEqualP75:
                    StandardHeight moreThanEqualP90 = standardHeightMap.get(HeightPercentileEnum.moreThanEqualP90);
                    if (BigDecimalUtil.isBetweenLeft(height,standardHeight.getHeightValue(),moreThanEqualP90.getHeightValue()) ){
                        result = standardHeight;
                    }
                    break;
                case moreThanEqualP90:
                    StandardHeight moreThanEqualP95 = standardHeightMap.get(HeightPercentileEnum.moreThanEqualP95);
                    if (BigDecimalUtil.isBetweenLeft(height,standardHeight.getHeightValue(),moreThanEqualP95.getHeightValue()) ){
                        result = standardHeight;
                    }
                    continue;
                case moreThanEqualP95:
                    if (BigDecimalUtil.moreThanAndEqual(height,standardHeight.getHeightValue()) ){
                        result = standardHeight;
                    }
                    break;
                default:
                    break;
            }
        }
        return  result;

    }

    /**
     * 血压偏高数据
     * @author hang.yuan
     * @date 2022/4/13
     */
    @Data
    @Accessors(chain = true)
    static class BloodPressureData {
        private Integer age;
        private Integer gender;
        private HeightPercentileEnum heightPercentile;
        private Integer sbp;
        private Integer dbp;
    }

    enum HeightPercentileEnum{
        /** 小于P5**/
        lessThanP5,
        /** 等于P5**/
        equalP5,
        /** 大于等于P5**/
        moreThanEqualP5,
        /** 大于等于P10**/
        moreThanEqualP10,
        /** 大于等于P25**/
        moreThanEqualP25,
        /** 大于等于P50**/
        moreThanEqualP50,
        /** 大于等于P75**/
        moreThanEqualP75,
        /** 大于等于P90**/
        moreThanEqualP90,
        /** 大于等于P95**/
        moreThanEqualP95
    }

    private static List<BloodPressureData> initBloodPressureData(){
        List<BloodPressureData> bloodPressureDataList =Lists.newLinkedList();
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(108).setDbp(72));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(110).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(112).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(113).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(115).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(117).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(118).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(121).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(110).setDbp(73));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(112).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(113).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(115).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(117).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(119).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(120).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(124).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(112).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(114).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(114).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(117).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(119).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(121).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(122).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(113).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(115).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(116).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(119).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(121).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(129).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(115).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(116).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(117).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(120).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(123).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(128).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(131).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(116).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(117).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(119).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(122).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(125).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(128).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(130).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(133).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(117).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(119).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(122).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(125).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(127).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(130).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(132).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(134).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(120).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(122).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(125).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(128).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(129).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(131).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(133).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(135).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(126).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(128).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(130).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(131).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(132).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(133).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(136).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(127).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(129).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(130).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(131).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(132).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(133).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(134).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(136).setDbp(85));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(0).setSbp(129).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(0).setSbp(131).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(0).setSbp(131).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(0).setSbp(132).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(0).setSbp(133).setDbp(85));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(0).setSbp(134).setDbp(85));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(0).setSbp(135).setDbp(85));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(0).setSbp(136).setDbp(86));

        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(109).setDbp(73));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(109).setDbp(73));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(111).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(111).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(113).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(115).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(117).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(121).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(110).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(110).setDbp(74));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(113).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(113).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(115).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(117).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(119).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(123).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(112).setDbp(75));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(112).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(114).setDbp(76));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(115).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(117).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(119).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(121).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(124).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(113).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(114).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(116).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(117).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(119).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(121).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(115).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(116).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(117).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(119).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(121).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(124).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(116).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(117).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(119).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(121).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(124).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(118).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(119).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(120).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(124).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(120).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(120).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(121).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(124).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(124).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(126).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(126).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(123).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(123).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(126).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(126).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.lessThanP5).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP5).setGender(1).setSbp(122).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP10).setGender(1).setSbp(123).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP25).setGender(1).setSbp(124).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP50).setGender(1).setSbp(124).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP75).setGender(1).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP90).setGender(1).setSbp(126).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setHeightPercentile(HeightPercentileEnum.moreThanEqualP95).setGender(1).setSbp(126).setDbp(82));

        return bloodPressureDataList;
    }

    public static BloodPressureData getBloodPressureData(Integer age,Integer gender,BigDecimal height){
        StandardHeight standardHeight = getStandardHeight(age, gender, height);

        return BLOOD_PRESSURE_DATA_LIST.stream()
                .filter(data -> Objects.equals(data.getAge(),age) && Objects.equals(data.getGender(),gender) && Objects.equals(data.getHeightPercentile(),standardHeight.getHeightPercentile()))
                .findFirst()
                .orElse(null);

    }
}
