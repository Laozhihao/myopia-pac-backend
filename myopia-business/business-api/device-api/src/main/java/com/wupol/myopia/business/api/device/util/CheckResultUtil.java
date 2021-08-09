package com.wupol.myopia.business.api.device.util;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.api.device.domain.enums.HyperopiaLevelEnum;
import com.wupol.myopia.business.api.device.domain.enums.MyopiaLevelEnum;
import com.wupol.myopia.business.common.utils.util.ObjectUtil;
import com.wupol.myopia.business.core.device.domain.dto.DeviceScreenDataDTO;
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

    private final String MSG_RESULT_TEXT_MYOPIA = "近视";
    private final String MSG_RESULT_TEXT_FARSIGHTEDNESS = "远视";
    private final String MSG_RESULT_TEXT_ASTIGMATISM = "散光";
    private final String MSG_RESULT_TEXT_GAZE_RANGING = "凝视不等";
    private final String MSG_RESULT_TEXT_ANISOMETROPIA = "屈光参差";
    private final String MSG_RESULT_TEXT_UNEQUAL_PUPIL = "瞳孔大小不等";
    private final String MSG_RESULT_TEXT_STRABISM = "斜视";

    /**
     * 中文分割符
     */
    private String sep = "、";

    /**
     * 获取检查结果
     *
     * @param deviceScreenDataDTO
     * @return
     */
    public String getCheckResult(DeviceScreenDataDTO deviceScreenDataDTO) {
        List<String> leftResults = getSingleEyeResult(deviceScreenDataDTO.getLeftPa(), deviceScreenDataDTO.getLeftCyl(), deviceScreenDataDTO.getLeftAxsiH(), deviceScreenDataDTO.getLeftAxsiV(), deviceScreenDataDTO.getPatientAge());
        List<String> rightResults = getSingleEyeResult(deviceScreenDataDTO.getRightPa(), deviceScreenDataDTO.getRightCyl(), deviceScreenDataDTO.getRightAxsiH(), deviceScreenDataDTO.getRightAxsiV(), deviceScreenDataDTO.getPatientAge());
        Collection<String> singleEyeResult = CollectionUtils.union(leftResults, rightResults);
        List<String> allEyeResults = getAllEyeResult(deviceScreenDataDTO);
        //获取近视,远视情况
        String hyperopiaLevelForDisplay = getHyperopiaLevelForDisplay(deviceScreenDataDTO.getPatientAge(), deviceScreenDataDTO.getLeftPa(), deviceScreenDataDTO.getRightPa());
        String myopiaLevelForDisplay = getMyopiaLevelForDisplay(deviceScreenDataDTO.getPatientAge(), deviceScreenDataDTO.getLeftPa(), deviceScreenDataDTO.getRightPa());
        Set<String> results = new HashSet<>();
        results.addAll(singleEyeResult);
        results.addAll(allEyeResults);
        results.add(hyperopiaLevelForDisplay);
        results.add(myopiaLevelForDisplay);
        return results.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(sep));
    }

    /**
     * 获取远视level
     *
     * @param patientAge
     * @param oneEyePa
     * @param anotherEyePa
     * @return
     */
    public static Integer getHyperopiaLevel(Integer patientAge, Double oneEyePa, Double anotherEyePa) {
        Integer leftHyperopiaLevel = getSingleEyeHyperopiaLevel(patientAge, oneEyePa);
        Integer rightHyperopiaLevel = getSingleEyeHyperopiaLevel(patientAge, anotherEyePa);
        return leftHyperopiaLevel > rightHyperopiaLevel ? leftHyperopiaLevel : rightHyperopiaLevel;
    }

    /**
     * 获取远视展示
     *
     * @param patientAge
     * @param oneEyePa
     * @param anotherEyePa
     * @return
     */
    private static String getHyperopiaLevelForDisplay(Integer patientAge, Double oneEyePa, Double anotherEyePa) {
        Integer hyperopiaLevel = getHyperopiaLevel(patientAge, oneEyePa, anotherEyePa);
        return HyperopiaLevelEnum.getDisplayByLevel(hyperopiaLevel);
    }

    /**
     * 获取近视展示
     *
     * @param oneEyePa
     * @param anotherEyePa
     * @return
     */
    private static String getMyopiaLevelForDisplay(Integer age, Double oneEyePa, Double anotherEyePa) {
        return MyopiaLevelEnum.getDisplayByLevel(getMyopiaLevel(age, oneEyePa, anotherEyePa));
    }

    /**
     * 获取近视
     *
     * @param oneEyePa
     * @param anotherEyePa
     * @return
     */
    private static int getMyopiaLevel(Integer age, Double oneEyePa, Double anotherEyePa) {
        Integer oneEyeHyperopiaLevel = getSingleEyeHyperopiaLevel(age, oneEyePa);
        Integer anotherHyperopiaLevel = getSingleEyeHyperopiaLevel(age, anotherEyePa);
        return oneEyeHyperopiaLevel > anotherHyperopiaLevel ? oneEyeHyperopiaLevel : anotherHyperopiaLevel;
    }

    /**
     * 获取全眼结果
     *
     * @param deviceScreenDataDTO
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
        return Arrays.asList(prForDisplay, anisometropiaForDisplay, unequalPupilForDisplay, redReflectForDisplay);
    }

    /**
     * 单眼结果计算:  只需要单眼就能计算的数据
     *
     * @param se
     * @param cyl
     * @param axsiH
     * @param axsiV
     * @param moonAge
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
     * @param moonAge
     * @param se
     * @return
     */
    public String isHyperopiaForDisplay(Integer moonAge, Double se) {
        return isHyperopia(moonAge, se) ? MSG_RESULT_TEXT_FARSIGHTEDNESS : StringUtils.EMPTY;
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
        if (ObjectsUtil.allNull(moonAge, se)) {
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
     * @param age
     * @return
     */
    public Integer getSingleEyeHyperopiaLevel(Integer age, Double se) {
        if (!isHyperopia(age, se)) {
            return -1;
        }
        //age < 12岁
        if (age < 12 * 12) {
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
        return isAstigmia(cyl) ? MSG_RESULT_TEXT_ASTIGMATISM : StringUtils.EMPTY;
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
     * @param greedH 垂直斜视度数
     * @param greedV 水平斜视度数
     * @return
     */
    public String isStrabismForDisplay(Integer greedH, Integer greedV) {
        return isStrabism(greedH, greedV) ? MSG_RESULT_TEXT_STRABISM : StringUtils.EMPTY;
    }


    /**
     * 检查是否斜视
     * 水平、垂直斜视度数的绝对值大于8，则显示有斜视。
     *
     * @param greedH
     * @param greedV
     * @return
     */
    public boolean isStrabism(Integer greedH, Integer greedV) {
        return ObjectsUtil.allNotNull(greedH, greedV) && Math.abs(greedH) > 8 || Math.abs(greedV) > 8;
    }


    /**
     * 展示是否瞳孔大小不等
     * 标准: 左右瞳孔直径相差>=1
     *
     * @param oneEyePR
     * @param anotherEyePR
     * @return
     */
    public String isPRForDisplay(Double oneEyePR, Double anotherEyePR) {
        return isPR(oneEyePR, anotherEyePR) ? MSG_RESULT_TEXT_UNEQUAL_PUPIL : StringUtils.EMPTY;
    }


    /**
     * 检查是否瞳孔大小不等
     * 标准: 左右瞳孔直径相差>=1
     *
     * @param oneEyePR
     * @param anotherEyePR
     * @return
     */
    public boolean isPR(Double oneEyePR, Double anotherEyePR) {
        return ObjectsUtil.allNotNull(oneEyePR, anotherEyePR) && Math.abs(oneEyePR - anotherEyePR) >= 1;
    }


    /**
     * 展示是否 凝视不等
     * 标准: 左右眼水平或者垂直斜视度数差的绝对值大于8
     *
     * @param oneEyeAxsiH
     * @param anotherEyeAxsiH
     * @param oneEyeAxsiV
     * @param anotherEyeAxsiV
     * @return
     */
    public String isUnequalPupilForDisplay(Integer oneEyeAxsiH, Integer anotherEyeAxsiH, Integer oneEyeAxsiV, Integer anotherEyeAxsiV) {
        return isUnequalPupil(oneEyeAxsiH, anotherEyeAxsiH, oneEyeAxsiV, anotherEyeAxsiV) ? MSG_RESULT_TEXT_GAZE_RANGING : StringUtils.EMPTY;
    }


    /**
     * 是否 凝视不等
     * 标准: 左右眼水平或者垂直斜视度数差的绝对值大于8
     *
     * @param oneEyeAxsiH
     * @param anotherEyeAxsiH
     * @param oneEyeAxsiV
     * @param anotherEyeAxsiV
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
     * @param age
     * @param shpdest
     * @param cyldest
     * @return
     */
    public String isAnisometropiaForDisplay(Integer age, Double shpdest, Double cyldest) {
        return isAnisometropia(age, shpdest, cyldest) ? MSG_RESULT_TEXT_ANISOMETROPIA : StringUtils.EMPTY;
    }


    /**
     * 是否 屈光参差
     * 标准:
     * 1岁以下：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1.5D
     * 1岁以上：左右眼的sph(球镜度) 或者cyl(柱镜度) 相差超过1D
     *
     * @param age
     * @param shpdest 球镜
     * @param cyldest 柱镜
     * @return
     */
    public boolean isAnisometropia(Integer age, Double shpdest, Double cyldest) {
        if (age == null) {
            return false;
        }

        if (age > 12) {
            return (shpdest != null && shpdest > 1) || (cyldest != null && cyldest > 1);
        } else {
            return (shpdest != null && shpdest > 1.5) || (cyldest != null && cyldest > 1.5);
        }
    }

    /**
     * 展示是否近视
     *
     * @param se
     * @return
     */
    public String isMyopiaForDisplay(Double se) {
        return isMyopia(se) ? MSG_RESULT_TEXT_MYOPIA : StringUtils.EMPTY;
    }

    /**
     * 是否近视
     *
     * @param se
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
            return MyopiaLevelEnum.NOT_LEVEL.getLevel();
        }
        if (se >= -3.00 && se < -0.50) {
            return MyopiaLevelEnum.LOW_LEVEL.getLevel();
        }

        if (se >= -6.00 && se < -3.00) {
            return MyopiaLevelEnum.MEDIUM_LEVEL.getLevel();
        }

        if (se < -6.00) {
            return MyopiaLevelEnum.HIGH_LEVEL.getLevel();
        }
        return MyopiaLevelEnum.NORMAL_LEVEL.getLevel();
    }

    /**
     * 是否红光反射
     * 标准: 两眼的红光反射值 相差＞15%
     *
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
     * 标准: 两眼的红光反射值 相差＞15%
     *
     * @return
     */
    public String isRedReflectForDisplay(Integer oneRedReflect, Integer anotherOneRedReflect) {
        return isRedReflect(oneRedReflect, anotherOneRedReflect) ? MSG_RESULT_TEXT_GAZE_RANGING : StringUtils.EMPTY;
    }
}
