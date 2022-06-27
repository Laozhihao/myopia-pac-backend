package com.wupol.myopia.business.core.screening.flow.util;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.BigDecimalUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.List;

/**
 * 判断标准表数据
 *
 * @author hang.yuan 2022/4/12 16:58
 */
@UtilityClass
public class StandardTableData {

    private static final List<OverweightAndObesityData> OVERWEIGHT_AND_OBESITY_DATA_LIST;
    private static final List<StuntingData> STUNTING_DATA_LIST;
    private static final List<WastingData> WASTING_DATA_LIST;
    private static final List<BloodPressureData> BLOOD_PRESSURE_DATA_LIST;

    static {
        OVERWEIGHT_AND_OBESITY_DATA_LIST = initOverweightAndObesityData();
        STUNTING_DATA_LIST = initStuntingData();
        WASTING_DATA_LIST = initWastingData();
        BLOOD_PRESSURE_DATA_LIST = initBloodPressureData();
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

    /**
     * 初始化超重和肥胖数据
     */
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

    /**
     * 获取超重和肥胖数据
     * @param age 年龄
     * @param gender 性别
     */
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

    /**
     * 初始化生长迟缓数据
     */
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

    /**
     * 获取生长迟缓数据
     * @param age 年龄
     * @param gender 性别
     */
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

    /**
     * 消瘦数据
     */
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

    /**
     * 获取消瘦数据
     * @param age 年龄
     * @param gender 性别
     */
    public static WastingData getWastingData(String age,Integer gender){
        String newAge = BigDecimalUtil.lessThan(new BigDecimal(age),"6.0")?"6.0":BigDecimalUtil.moreThan(new BigDecimal(age),"17.5")?"17.5":age;
        return  WASTING_DATA_LIST.stream()
                .filter(data -> data.getAge().equals(newAge) && data.getGender().equals(gender))
                .findFirst()
                .orElse(null);

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
        private Integer sbp;
        private Integer dbp;
    }

    /**
     * 初始化血压偏高数据
     */
    private static List<BloodPressureData> initBloodPressureData(){
        List<BloodPressureData> bloodPressureDataList =Lists.newLinkedList();
        bloodPressureDataList.add(new BloodPressureData().setAge(7).setGender(0).setSbp(121).setDbp(79));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setGender(0).setSbp(124).setDbp(80));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setGender(0).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setGender(0).setSbp(129).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setGender(0).setSbp(131).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setGender(0).setSbp(133).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setGender(0).setSbp(134).setDbp(83));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setGender(0).setSbp(135).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setGender(0).setSbp(136).setDbp(84));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setGender(0).setSbp(136).setDbp(85));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setGender(0).setSbp(136).setDbp(86));

        bloodPressureDataList.add(new BloodPressureData().setAge(7).setGender(1).setSbp(121).setDbp(77));
        bloodPressureDataList.add(new BloodPressureData().setAge(8).setGender(1).setSbp(123).setDbp(78));
        bloodPressureDataList.add(new BloodPressureData().setAge(9).setGender(1).setSbp(124).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(10).setGender(1).setSbp(125).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(11).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(12).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(13).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(14).setGender(1).setSbp(126).setDbp(81));
        bloodPressureDataList.add(new BloodPressureData().setAge(15).setGender(1).setSbp(126).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(16).setGender(1).setSbp(126).setDbp(82));
        bloodPressureDataList.add(new BloodPressureData().setAge(17).setGender(1).setSbp(126).setDbp(82));

        return bloodPressureDataList;
    }
    /**
     * 获取血压偏高数据
     * @param age 年龄
     * @param gender 性别
     */
    public static BloodPressureData getBloodPressureData(Integer age,Integer gender){
        return  BLOOD_PRESSURE_DATA_LIST.stream()
                .filter(data -> data.getAge().equals(age) && data.getGender().equals(gender))
                .findFirst()
                .orElse(null);

    }
}
