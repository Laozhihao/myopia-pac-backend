package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.util.MathUtil;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description
 * @Date 2021/3/18 19:28
 * @Author by Jacob todo 这些字段的定义是早期给到前端，为节省前端的更改成本，字段名暂时不修改。
 */
@Data
@NoArgsConstructor
public class DistributionDTO implements Serializable {

    /**
     * 年龄占比分布
     */
    private Map<String, Double> age;
    /**
     * 数量相关分布
     */
    private NumDTO num;
    /**
     * 性别分布情况
     */
    private GenderDTO gender;
    /**
     * 学龄分布情况
     */
    private SchoolAgeDTO schoolAge;
    /**
     * 城市的统计情况
     */
    private List<StatisticDistrictDTO> statisticDistrict;

    /**
     * 私有构造方法
     */
    private DistributionDTO(Builder builder) {
        this.num = builder.num;
        this.gender = builder.gender;
        this.age = builder.age;
        this.schoolAge = builder.schoolAge;
        this.statisticDistrict = builder.statisticDistrict;
    }

    /**
     * 构建类
     */
    public static class Builder {
        private List<BigScreenStatDataDTO> bigScreenStatDataDTOList;
        private Map<String, Double> age;
        private NumDTO num;
        private GenderDTO gender;
        private SchoolAgeDTO schoolAge;
        private List<StatisticDistrictDTO> statisticDistrict;
        /**
         * 实际筛查学生总数
         */
        private Long screeningStudentNum;
        /**
         * 有效数据
         */
        private Long realScreeningNum;

        /**
         * 城市地区的下属地区映射关系
         */
        private Map<District, List<District>> cityDistrictMap;

        public static Builder getBuilder() {
            return new Builder();
        }


        public Builder setBigScreenStatDataDTOList(List<BigScreenStatDataDTO> bigScreenStatDataDTOList) {
            this.bigScreenStatDataDTOList = bigScreenStatDataDTOList;
            return this;
        }


        public Builder setScreeningStudentNum(Long screeningStudentNum) {
            this.screeningStudentNum = screeningStudentNum;
            return this;
        }

        public Builder setRealScreeningNum(Long realScreeningNum) {
            this.realScreeningNum = realScreeningNum;
            return this;
        }

        public Builder setCityDistrictMap(Map<District, List<District>> cityDistrictMap) {
            this.cityDistrictMap = cityDistrictMap;
            return this;
        }


        /**
         * 构建
         *
         * @return
         */
        public DistributionDTO build() {
            if (CollectionUtils.isEmpty(bigScreenStatDataDTOList) || screeningStudentNum == null || screeningStudentNum < 0) {
                throw new BusinessException("构建对象DistributionDTO失败，部分构建参数为空");
            }
            //将bigScreenStatDataDTOList的数据完善下
            this.getCompletedBigScreenStatDataDTOList();
            //设置总数的比例
            this.setNum();
            //设置性别参数
            this.setGenderData();
            //设置年龄分段
            this.setAgeData();
            //设置学龄分段
            this.setSchoolAgeData();
            //设置城市数据
            this.setCityData();
            return new DistributionDTO(this);
        }

        /**
         * 将城市的数据填满
         */
        private void getCompletedBigScreenStatDataDTOList() {
        }

        /**
         * 设置总的占比分布
         */
        private void setNum() {
            long matchStudentNum = bigScreenStatDataDTOList.stream().filter(Objects::nonNull).count();
            double ratio = MathUtil.getFormatNumWith2Scale(matchStudentNum / (double) realScreeningNum * 100);
            NumDTO numDTO = new NumDTO();
            numDTO.studentNum = screeningStudentNum;
            numDTO.studentDistribution = ratio;
            this.num = numDTO;
        }

        /**
         * 设置城市数据
         */
        private void setCityData() {
            List<StatisticDistrictDTO> statisticDistrictList = new ArrayList<>();
            Map<Integer, String> cityDistrictIdNameMap = bigScreenStatDataDTOList.stream().collect(Collectors.toMap(e -> e.getCityDistrictId(), e -> e.getCityDistrictName(),(v1,v2)->v1));
            bigScreenStatDataDTOList.stream().collect(Collectors.groupingBy(BigScreenStatDataDTO::getCityDistrictId, Collectors.counting())).forEach((cityDistrictId, num) -> {
                StatisticDistrictDTO statisticDistrictDTO = new StatisticDistrictDTO();
                statisticDistrictDTO.cityName =cityDistrictIdNameMap.get(cityDistrictId);
                statisticDistrictDTO.num = num;
                statisticDistrictDTO.cityDistrictId = cityDistrictId;
                statisticDistrictDTO.ratio = MathUtil.getFormatNumWith2Scale(num / (double) screeningStudentNum * 100);
                statisticDistrictList.add(statisticDistrictDTO);
            });
            this.statisticDistrict = statisticDistrictList;
        }


        /**
         * 设置学龄
         */
        private void setSchoolAgeData() {
            SchoolAgeDTO schoolAgeDTO = new SchoolAgeDTO();
            bigScreenStatDataDTOList.stream().collect(Collectors.groupingBy(BigScreenStatDataDTO::getSchoolAge, Collectors.collectingAndThen(Collectors.counting(), e ->
                    MathUtil.getFormatNumWith2Scale(e / (double) screeningStudentNum * 100)
            ))).forEach((schoolAgeType, ratio) -> {
                if (SchoolAge.KINDERGARTEN.code == schoolAgeType) {
                    schoolAgeDTO.kindergarten = ratio;
                } else if (SchoolAge.PRIMARY.code == schoolAgeType) {
                    schoolAgeDTO.primary = ratio;
                } else if (SchoolAge.JUNIOR.code == schoolAgeType) {
                    schoolAgeDTO.junior = ratio;
                } else if (SchoolAge.HIGH.code == schoolAgeType) {
                    schoolAgeDTO.high = ratio;
                } else if (SchoolAge.VOCATIONAL_HIGH.code == schoolAgeType) {
                    schoolAgeDTO.vocationalHigh = ratio;
                } else {
                    throw new BusinessException("数据异常，schoolAgeType = " + schoolAgeType);
                }
            });
            this.schoolAge = schoolAgeDTO;
        }

        /**
         * 设置年龄数据
         */
        public void setAgeData() {
            Map<String, Double> ageDemoRatioMap = new HashMap<>();
            ageDemoRatioMap.put("0-3",null);
            ageDemoRatioMap.put("4-6",null);
            ageDemoRatioMap.put("7-9",null);
            ageDemoRatioMap.put("10-12",null);
            ageDemoRatioMap.put("13-15",null);
            ageDemoRatioMap.put("16-18",null);
            ageDemoRatioMap.put("19 以上",null);
            Map<String, Double> ageResultRatioMap = bigScreenStatDataDTOList.stream().collect(Collectors.groupingBy(item -> {
                if (0 <= item.getAge() && item.getAge() <= 3) {
                    return "0-3";
                }

                if (4 <= item.getAge() && item.getAge() <= 6) {
                    return "4-6";
                }

                if (7 <= item.getAge() && item.getAge() <= 9) {
                    return "7-9";
                }

                if (10 <= item.getAge() && item.getAge() <= 12) {
                    return "10-12";
                }

                if (13 <= item.getAge() && item.getAge() <= 15) {
                    return "13-15";
                }

                if (16 <= item.getAge() && item.getAge() <= 18) {
                    return "16-18";
                }

                if (19 <= item.getAge() && item.getAge() <= 145) {
                    return "19 以上";
                }
                return null;
            }, Collectors.collectingAndThen(Collectors.counting(), e ->
                    MathUtil.getFormatNumWith2Scale(e / (double) screeningStudentNum * 100)
            )));
            ageDemoRatioMap.putAll(ageResultRatioMap);
            this.age = ageDemoRatioMap;
        }

        /**
         * 设置性别参数
         */
        private void setGenderData() {
            Map<Integer, Double> genderDataMap = bigScreenStatDataDTOList.stream().collect(Collectors.groupingBy(BigScreenStatDataDTO::getGender, Collectors.collectingAndThen(Collectors.counting(), e ->
                    MathUtil.getFormatNumWith2Scale(e / (double) screeningStudentNum * 100)
            )));
            GenderDTO genderDTO = new GenderDTO();
            genderDTO.male = MathUtil.getFormatNumWith2Scale(genderDataMap.get(GenderEnum.MALE.type));
            genderDTO.female = MathUtil.getFormatNumWith2Scale(genderDataMap.get(GenderEnum.FEMALE.type));
            this.gender = genderDTO;
        }
    }


    /**
     * 占比分布
     */
    @Getter
    @Setter
    public static class NumDTO implements Serializable {

        /**
         * studentNum : 24332
         * studentDistribution : 15.3
         */
        private Long studentNum;
        private double studentDistribution;
    }

    @Getter
    @Setter
    public static class GenderDTO implements Serializable {
        /**
         * 男性比例  todo（ 由于早期已经给了前端定义，字段暂时不修改，实际应该是maleRatio）
         */
        private double male;
        /**
         * 女性比例  todo（ 由于早期已经给了前端定义，字段暂时不修改，实际应该是femaleRatio）
         */
        private double female;
    }

    @Getter
    @Setter
    public static class SchoolAgeDTO implements Serializable {


        /**
         * high : 15.5
         * junior : 20.3
         * primary : 25.3
         * university : 4.5
         * kindergarten : 15.3
         * vocationalHigh : 17.3
         */
        private Double high;
        private Double junior;
        private Double primary;
        private Double university;
        private Double kindergarten;
        private Double vocationalHigh;

    }

    @Getter
    @Setter
    public static class StatisticDistrictDTO implements Serializable {
        public Integer cityDistrictId;
        /**
         * ratio : 23
         * cityName : 广州市
         */
        private double ratio;
        private long num;
        private String cityName;
    }


}
