package com.wupol.myopia.business.api.screening.app.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname DeviceScreenDataDTO
 * @Description
 * @Date 2021/7/6 2:58 下午
 * @Author Jacob
 * @Version
 */

@Data
public class DeviceScreenDataDTO implements Serializable {
    /*** ID: * 个⼈筛查⽅式：年⽉⽇_000X，20180920_0001 * 批量筛查⽅式：excel表格中定义 */
    public String patientId;
    /*** 姓名 */
    public String patientName;
    /*** 性别： * 男：M * ⼥：FM */
    public String patientGender;
    /*** 年龄 ⽉份，⽐如3岁就是3*12 = 36 个⽉ */
    public Integer patientAge;
    /*** 筛查⽅式 * 个体筛查：0 * 批量筛查：1 */
    public Integer checkType;
    /*** 筛查模式 * 双眼模式：0 * 左眼模式：1 * 右眼模式：2 */
    public Integer checkMode;
    /*** 筛查结果 * 优：1 * 良：2 * 差：3 */
    public Integer checkResult;
    /*** 筛查时间： * 年⽉⽇时分秒：20180920233030 */
    public String checkTime;
    /*** 是否筛查： * 是：1 * 否：0 */
    public Integer doCheck;

    public String patientOrg;
    /*** 患者信息： * 班级 * 公司 * 等 */
    public String patientInfo;
    /*** 患者⾝份证ID */
    public String patientCID;
    /*** 患者⼿机号 */
    public String patientPhoneNumber;
    /*** 是否需要上传 */
    public Integer forceUpload;
    /*** 是否已经上传 */
    public Integer doUploaded;
    /*** 左眼球径 */
    public Float leftSph;
    /*** 左眼柱径 */
    public Float leftCyl;
    /*** 左眼轴位 */
    public Float leftAxsi;
    /*** 左眼瞳孔半径 */
    public Float leftPR;
    /*** 右眼球径 */
    public Float rightSph;
    /*** 右眼球柱径 */
    public Float rightCyl;
    /*** 右眼球轴位 */
    public Float rightAxsi;
    /*** 右眼瞳孔半径 */
    public Float rightPR;
    /*** 瞳距 */
    public Float PD;
    /*** 右眼等效球镜度 */
    public Float rightPa;
    /*** 左眼等效球镜度 */
    public Float leftPa;
    /*** 右垂直⽅向斜视度数 */
    public Integer rightAxsiV;
    /*** 右⽔平⽅向斜视度数 */
    public Integer rightAxsiH;
    /*** 左垂直⽅向斜视度数 */
    public Integer leftAxsiV;
    /*** 左⽔平⽅向斜视度数 */
    public Integer leftAxsiH;
    /*** 红光反射左眼 */
    public Integer redReflectLeft;
    /*** 红光反射右眼 */
    public Integer redReflectRight;
}