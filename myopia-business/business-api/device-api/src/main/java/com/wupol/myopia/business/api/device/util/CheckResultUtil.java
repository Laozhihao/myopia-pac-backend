package com.wupol.myopia.business.api.device.util;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.common.utils.util.ObjectUtil;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname TypeUtil
 * @Description 检查结果类型的判断
 * @Date 2021/7/20 5:38 下午
 * @Author Jacob
 * @Version
 */
@UtilityClass
public class CheckResultUtil {

    private final String MYOPIA = "近视";
    private final String FARSIGHTEDNESS = "远视";
    private final String ASTIGMATISM = "散光";
    private final String GAZE_RANGING = "凝视不等";
    private final String ANISOMETROPIA = "屈光参差";
    private final String UNEQUAL_PUPIL = "瞳孔大小不等";
    private final String STRABISM = "斜视";
    private final String REDREFLEX = "红光反射";

    /**
     * 中文分割符
     */
    private String sep = "、";

    /**
     * 获取检查结果:
     * 1.需要双眼计算的结果(如红外反射需要左右眼数据才能计算),拼接显示;
     * 2.单眼计算的结果,如果有严重程度区分,则双眼同时存在同一视力缺陷不同严重程度时,取最高严重程度,如: 左眼轻度近视, 右眼中度近视,则最后取中度近视.
     * 3.最后取出说有的数据用{@link CheckResultUtil#sep} 符号隔开:  近视、中度近视、中度远视
     *
     * @param deviceScreenDataDTO 数据
     * @return 当不符合判断时候, 返回""
     */
    public String getCheckResult(DeviceScreenDataDTO deviceScreenDataDTO) {
        //01.获取左右眼单眼计算结果
        List<String> leftResults = getSingleEyeResult(deviceScreenDataDTO.getLeftPa(), deviceScreenDataDTO.getLeftCyl(), deviceScreenDataDTO.getLeftAxsiH(), deviceScreenDataDTO.getLeftAxsiV(), deviceScreenDataDTO.getPatientAge());
        List<String> rightResults = getSingleEyeResult(deviceScreenDataDTO.getRightPa(), deviceScreenDataDTO.getRightCyl(), deviceScreenDataDTO.getRightAxsiH(), deviceScreenDataDTO.getRightAxsiV(), deviceScreenDataDTO.getPatientAge());
        Collection<String> singleEyeResult = CollectionUtils.union(leftResults, rightResults);
        //02.获取全眼计算结果
        List<String> allEyeResults = getAllEyeResult(deviceScreenDataDTO);
        //03.拼接结果
        Set<String> results = new HashSet<>();
        results.addAll(singleEyeResult);
        results.addAll(allEyeResults);
        return results.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(sep));
    }

    /**
     * 获取远视level
     *
     * @param patientAge 月龄
     * @param oneEyePa 某只眼等效球镜
     * @param anotherEyePa 另一只眼等效球镜
     * @return
     */
    public static int getHyperopiaLevel(Integer patientAge, Double oneEyePa, Double anotherEyePa) {
        int oneEyeHyperopiaLevel = getSingleEyeHyperopiaLevel(patientAge, oneEyePa);
        int anotherEyeHyperopiaLevel = getSingleEyeHyperopiaLevel(patientAge, anotherEyePa);
        return oneEyeHyperopiaLevel > anotherEyeHyperopiaLevel ? oneEyeHyperopiaLevel : anotherEyeHyperopiaLevel;
    }

    /**
     * 获取远视展示
     *
     * @param patientAge
     * @param oneEyePa 某只眼等效球镜
     * @param anotherEyePa 另一只眼等效球镜
     * @return
     */
    private static String getHyperopiaLevelForDisplay(Integer patientAge, Double oneEyePa, Double anotherEyePa) {
        return LevelEnum.getDisplayByLevel(getHyperopiaLevel(patientAge, oneEyePa, anotherEyePa),FARSIGHTEDNESS);
    }

    /**
     * 获取近视展示
     *
     * @param oneEyePa 某只眼等效球镜
     * @param anotherEyePa 另一只眼等效球镜
     * @return
     */
    private static String getMyopiaLevelForDisplay(Double oneEyePa, Double anotherEyePa) {
        return LevelEnum.getDisplayByLevel(getMyopiaLevel(oneEyePa, anotherEyePa),MYOPIA);
    }

    /**
     * 获取近视level
     * @param oneEyePa 某只眼等效球镜
     * @param anotherEyePa 另一只眼等效球镜
     * @return
     */
    private static int getMyopiaLevel(Double oneEyePa, Double anotherEyePa) {
        int oneEyeMyopiaLevel = getSingleEyeMyopiaLevel(oneEyePa);
        int anotherEyeMyopiaLevel = getSingleEyeMyopiaLevel(anotherEyePa);
        return oneEyeMyopiaLevel > anotherEyeMyopiaLevel ? oneEyeMyopiaLevel : anotherEyeMyopiaLevel;
    }

    /**
     * 获取全眼结果
     *
     * @param deviceScreenDataDTO 数据
     * @return
     */
    public List<String> getAllEyeResult(DeviceScreenDataDTO deviceScreenDataDTO) {
        //是否瞳孔大小不等
        String prForDisplay = isPRForDisplay(deviceScreenDataDTO.getLeftPR(), deviceScreenDataDTO.getRightPR());
        //是否屈光参差
        String anisometropiaForDisplay = isAnisometropiaForDisplay(deviceScreenDataDTO.getPatientAge(), Math.abs(deviceScreenDataDTO.getLeftSph() - deviceScreenDataDTO.getRightSph()), Math.abs(deviceScreenDataDTO.getLeftCyl() - deviceScreenDataDTO.getRightCyl()));
        //凝视不对等
        String unequalPupilForDisplay = isUnequalPupilForDisplay(deviceScreenDataDTO.getLeftAxsiH(), deviceScreenDataDTO.getRightAxsiH(), deviceScreenDataDTO.getLeftAxsiV(), deviceScreenDataDTO.getRightAxsiV());
        //红光反射
        String redReflectForDisplay = isRedReflectForDisplay(deviceScreenDataDTO.getRedReflectRight(), deviceScreenDataDTO.getRedReflectLeft());
        //获取远视严重程度情况
        String hyperopiaLevelForDisplay = getHyperopiaLevelForDisplay(deviceScreenDataDTO.getPatientAge(), deviceScreenDataDTO.getLeftPa(), deviceScreenDataDTO.getRightPa());
        //获取近视严重程度情况
        String myopiaLevelForDisplay = getMyopiaLevelForDisplay(deviceScreenDataDTO.getLeftPa(), deviceScreenDataDTO.getRightPa());
        return Arrays.asList(prForDisplay, anisometropiaForDisplay, unequalPupilForDisplay, redReflectForDisplay, hyperopiaLevelForDisplay, myopiaLevelForDisplay);
    }

    /**
     * 单眼结果计算:  只需要单眼就能计算的数据
     *
     * @param se 等效球镜
     * @param cyl 柱镜
     * @param axsiH 水平斜视度数
     * @param axsiV 垂直斜视度数
     * @param moonAge 月龄
     * @return
     */
    private List<String> getSingleEyeResult(Double se, Double cyl, Integer axsiH, Integer axsiV, Integer moonAge) {
        //是否近视
        String myopiaForDisplay = isMyopiaForDisplay(se);
        //是否远视
        String hyperopiaForDisplay = isHyperopiaForDisplay(moonAge, se);
        //是否散光
        String astigmiaForDisplay = isAstigmiaForDisplay(cyl);
        //是否斜视
        String strabismForDisplay = isStrabismForDisplay(axsiH, axsiV);
        return Arrays.asList(myopiaForDisplay, hyperopiaForDisplay, astigmiaForDisplay, strabismForDisplay);
    }

    /**
     * 展示是否远视
     *
     * @param moonAge 月龄
     * @param se 等效球镜
     * @return
     */
    public String isHyperopiaForDisplay(Integer moonAge, Double se) {
        return isHyperopia(moonAge, se) ? FARSIGHTEDNESS : StringUtils.EMPTY;
    }

    /**
     * 检查是否远视
     * 3岁前：等效球镜SE＞+3.50D
     * <p>
     * 4-5岁：等效球镜SE＞+2.50D
     * <p>
     * 6-7岁：等效球镜SE＞+2.00D
     * <p>
     * 8岁：等效球镜SE＞+1.50D
     * <p>
     * 9岁：等效球镜SE＞+1.25D
     * <p>
     * 10岁：等效球镜SE＞+1.00D
     * <p>
     * 11岁：等效球镜＞SE+0.75D
     * <p>
     * *12岁及以上：等效球镜SE＞+0.50D
     *
     * @param moonAge 月龄
     * @param se      等效球镜
     * @return
     */
    public boolean isHyperopia(Integer moonAge, Double se) {
        if (ObjectsUtil.hasNull(moonAge, se)) {
            return false;
        }
        int age = moonAge / 12;

        if (age < 0) {
            return false;
        }

        switch (age) {
            case 0:
            case 1:
            case 2:
            case 3:
                return se > 3.5;
            case 4:
            case 5:
                return se > 2.5;
            case 6:
            case 7:
                return se > 2.0;
            case 8:
                return se > 1.5;
            case 9:
                return se > 1.25;
            case 10:
                return se > 1.00;
            case 11:
                return se > 0.75;
            default:
                return se > 0.50;
        }
    }

    /**
     * 检查远视 视力级别 (只针对12岁)
     *
     * @param moonAge 月龄
     * @param se 等效球镜
     * @return
     */
    public int getSingleEyeHyperopiaLevel(Integer moonAge, Double se) {
        if (!isHyperopia(moonAge, se)) {
            return -1;
        }
        //moonAge < 12岁 才有分级
        if (moonAge < 12 * 12) {
            return 0;
        }

        if (se <= 3.00 && se > 0.5) {
            return 1;
        }

        if (se <= 6.00 && se > 3.00) {
            return 2;
        }

        if (se > 6.00) {
            return 3;
        }
        return 0;
    }


    /**
     * 展示是否散光
     *
     * @param cyl 柱镜
     * @return
     */
    public String isAstigmiaForDisplay(Double cyl) {
        return isAstigmia(cyl) ? ASTIGMATISM : StringUtils.EMPTY;
    }

    /**
     * 是否散光
     *
     * @param cyl 柱镜
     * @return
     */
    public boolean isAstigmia(Double cyl) {
        return cyl != null && Math.abs(cyl) > 0.5;
    }


    /**
     * 展示是否斜视
     * 水平、垂直斜视度数的绝对值大于8，则显示有斜视。
     *
     * @param greedH 水平斜视度数
     * @param greedV 垂直斜视度数
     * @return
     */
    public String isStrabismForDisplay(Integer greedH, Integer greedV) {
        return isStrabism(greedH, greedV) ? STRABISM : StringUtils.EMPTY;
    }


    /**
     * 检查是否斜视
     * 水平、垂直斜视度数的绝对值大于8，则显示有斜视。
     *
     * @param axsiH 水平斜视度数
     * @param axsiV 垂直斜视度数
     * @return
     */
    public boolean isStrabism(Integer axsiH, Integer axsiV) {
        return (axsiH != null && Math.abs(axsiV) > 8) || (axsiV != null && Math.abs(axsiV) > 8);
    }


    /**
     * 展示是否瞳孔大小不等
     * 标准: 左右瞳孔直径相差>=1
     *
     * @param oneEyePR 某只眼瞳孔大小
     * @param anotherEyePR 另一只眼瞳孔大小
     * @return
     */
    public String isPRForDisplay(Double oneEyePR, Double anotherEyePR) {
        return isPR(oneEyePR, anotherEyePR) ? UNEQUAL_PUPIL : StringUtils.EMPTY;
    }


    /**
     * 检查是否瞳孔大小不等
     * 标准: 左右瞳孔直径相差>=1
     *
     * @param oneEyePR 某只眼瞳孔大小
     * @param anotherEyePR 另一只眼瞳孔大小
     * @return
     */
    public boolean isPR(Double oneEyePR, Double anotherEyePR) {
        return ObjectsUtil.allNotNull(oneEyePR, anotherEyePR) && Math.abs(oneEyePR - anotherEyePR) >= 1;
    }


    /**
     * 展示是否 凝视不等
     * 标准: 左右眼水平或者垂直斜视度数差的绝对值大于8
     *
     * @param oneEyeAxsiH  某只眼垂直斜视度数
     * @param anotherEyeAxsiH  另一只眼垂直斜视度数
     * @param oneEyeAxsiV 某只眼水平斜视度数
     * @param anotherEyeAxsiV 另一只眼水平斜视度数
     * @return
     */
    public String isUnequalPupilForDisplay(Integer oneEyeAxsiH, Integer anotherEyeAxsiH, Integer oneEyeAxsiV, Integer anotherEyeAxsiV) {
        return isUnequalPupil(oneEyeAxsiH, anotherEyeAxsiH, oneEyeAxsiV, anotherEyeAxsiV) ? GAZE_RANGING : StringUtils.EMPTY;
    }


    /**
     * 是否 凝视不等
     * 标准: 左右眼水平或者垂直斜视度数差的绝对值大于8
     *
     * @param oneEyeAxsiH  某只眼垂直斜视度数
     * @param anotherEyeAxsiH  另一只眼垂直斜视度数
     * @param oneEyeAxsiV 某只眼水平斜视度数
     * @param anotherEyeAxsiV 另一只眼水平斜视度数
     * @return
     */
    public boolean isUnequalPupil(Integer oneEyeAxsiH, Integer anotherEyeAxsiH, Integer oneEyeAxsiV, Integer anotherEyeAxsiV) {
        boolean axsiH = ObjectsUtil.allNotNull(oneEyeAxsiH, anotherEyeAxsiH) && Math.abs(oneEyeAxsiH - anotherEyeAxsiH) > 8;
        boolean axsiV = ObjectsUtil.allNotNull(oneEyeAxsiV, anotherEyeAxsiV) && Math.abs(oneEyeAxsiV - anotherEyeAxsiV) > 8;
        return (axsiV || axsiH);
    }

    /**
     * 展示是否 屈光参差
     * 标准:
     * 1岁以下：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1.5D
     * 1岁以上：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1D
     *
     * @param moonAge 月龄
     * @param sphdest 球镜差值 
     * @param cyldest 柱镜差值
     * @return
     */
    public String isAnisometropiaForDisplay(Integer moonAge, Double sphdest, Double cyldest) {
        return isAnisometropia(moonAge, sphdest, cyldest) ? ANISOMETROPIA : StringUtils.EMPTY;
    }


    /**
     * 是否 屈光参差
     * 标准:
     * 1岁以下：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1.5D
     * 1岁以上：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1D
     *
     * @param moonAge 月龄
     * @param sphdest 球镜差值
     * @param cyldest 柱镜差值
     * @return
     */
    public boolean isAnisometropia(Integer moonAge, Double sphdest, Double cyldest) {
        if (moonAge == null) {
            return false;
        }

        if (moonAge > 12) {
            return (sphdest != null && sphdest > 1) || (cyldest != null && cyldest > 1);
        } else {
            return (sphdest != null && sphdest > 1.5) || (cyldest != null && cyldest > 1.5);
        }
    }

    /**
     * 展示是否近视
     *
     * @param se 等效球镜
     * @return
     */
    public String isMyopiaForDisplay(Double se) {
        return isMyopia(se) ? MYOPIA : StringUtils.EMPTY;
    }

    /**
     * 是否近视
     *
     * @param se 等效球镜
     * @return
     */
    public boolean isMyopia(Double se) {
        return se != null && se < -0.50;
    }

    /**
     * 检查视力是近视结果
     *
     * @param se 等效球镜
     * @return
     */
    public int getSingleEyeMyopiaLevel(Double se) {
        if (!isMyopia(se)) {
            return LevelEnum.NOT_LEVEL;
        }
        if (se >= -3.00 && se < -0.50) {
            return LevelEnum.LOW_LEVEL.getLevel();
        }

        if (se >= -6.00 && se < -3.00) {
            return LevelEnum.MEDIUM_LEVEL.getLevel();
        }

        if (se < -6.00) {
            return LevelEnum.HIGH_LEVEL.getLevel();
        }
        return LevelEnum.NORMAL_LEVEL;
    }

    /**
     * 是否红光反射
     * 标准: 两眼的红光反射值 相差＞15
     * @param oneRedReflect 某只眼红外反射值
     * @param anotherOneRedReflect 另一只眼红外反射值
     * @return
     */
    public boolean isRedReflect(Integer oneRedReflect, Integer anotherOneRedReflect) {
        if (ObjectUtil.hasSomeNull(oneRedReflect, anotherOneRedReflect)) {
            return false;
        }
        return Math.abs(oneRedReflect - anotherOneRedReflect) > 15;
    }

    /**
     * 展示是否红光反射
     * 标准: 两眼的红光反射值 相差＞15
     *
     * @param oneRedReflect 某只眼红外反射值
     * @param anotherOneRedReflect 另一只眼红外反射值
     * @return
     */
    public String isRedReflectForDisplay(Integer oneRedReflect, Integer anotherOneRedReflect) {
        return isRedReflect(oneRedReflect, anotherOneRedReflect) ? REDREFLEX : StringUtils.EMPTY;
    }


    /**
     * @Classname HyperopiaLevelEnum 用于check result 轻度中度中度的判断
     * @Description 远视/远视程度
     * @Date 2021/8/9 6:16 下午
     * @Author Jacob
     * @Version
     */
    @Getter
    @AllArgsConstructor
    public enum LevelEnum {
        LOW_LEVEL(1, "轻度"),
        MEDIUM_LEVEL(2, "中度"),
        HIGH_LEVEL(3, "高度");
        public static final int NORMAL_LEVEL = 0;
        public static final int NOT_LEVEL = 0;
        public static final Map<Integer, String> levelDisplayMap = new HashMap(3);

        static {
            for (LevelEnum levelEnum : EnumSet.allOf(LevelEnum.class)) {
                levelDisplayMap.put(levelEnum.getLevel(), levelEnum.getDisplay());
            }
        }

        /**
         * 等级
         */
        private int level;
        /**
         * 展示
         */
        private String display;

        /**
         * 根据等级获取展示
         *
         * @param level
         * @return
         */
        public static String getDisplayByLevel(int level,String suffix) {
            String levelDisplay = levelDisplayMap.get(level);
            if (StringUtils.isNotBlank(levelDisplay)) {
                return levelDisplay + suffix;
            }
            return StringUtils.EMPTY;
        }
    }






}
