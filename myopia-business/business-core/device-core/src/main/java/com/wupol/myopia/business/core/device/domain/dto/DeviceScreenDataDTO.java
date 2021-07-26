package com.wupol.myopia.business.core.device.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.util.PatientAgeUtil;
import com.wupol.myopia.business.core.device.domain.model.Device;
import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname DeviceScreenDataDTO
 * @Description 用于接受上传的数据
 * @Date 2021/7/6 2:58 下午
 * @Author Jacob
 * @Version
 */
@Data
@Accessors(chain = true)
public class DeviceScreenDataDTO implements Serializable {

    /**分割符*/
    private static final String DELIMITER_CHAR = "-";
    /*** ID: * 个⼈筛查⽅式：年⽉⽇_000X，20180920_0001 * 批量筛查⽅式：excel表格中定义 */
    private String patientId;
    /*** 姓名 */
    private String patientName;
    /*** 性别： * 男：M * ⼥：FM */
    private String patientGender;
    /*** 年龄 ⽉份，⽐如3岁就是3*12 = 36 个⽉ */
    private Integer patientAge;
    /*** 筛查⽅式 * 个体筛查：0 * 批量筛查：1 */
    private Integer checkType;
    /*** 筛查模式 * 双眼模式：0 * 左眼模式：1 * 右眼模式：2 */
    private Integer checkMode;
    /*** 筛查结果 * 优：1 * 良：2 * 差：3 */
    private Integer checkResult;
    /*** 筛查时间： * 年⽉⽇时分秒：20180920233030 */
    @JsonFormat(pattern = "yyyyMMddHHmmss",timezone="GMT+8")
    private Date checkTime;
    /*** 是否筛查： * 是：1 * 否：0 */
    private Integer doCheck;
    /*** 患者机构  */
    private String patientOrg;
    /*** 患者信息： * 班级 * 公司 * 等 */
    private String patientInfo;
    /*** 患者⾝份证ID */
    private String patientCID;
    /*** 患者⼿机号 */
    private String patientPhoneNumber;
    /*** 是否需要上传 */
    private Integer forceUpload;
    /*** 是否已经上传 */
    private Integer doUploaded;
    /*** 左眼球径 */
    private Double leftSph;
    /*** 左眼柱径 */
    private Double leftCyl;
    /*** 左眼轴位 */
    private Double leftAxsi;
    /*** 左眼瞳孔半径 */
    private Double leftPR;
    /*** 右眼球径 */
    private Double rightSph;
    /*** 右眼球柱径 */
    private Double rightCyl;
    /*** 右眼球轴位 */
    private Double rightAxsi;
    /*** 右眼瞳孔半径 */
    private Double rightPR;
    /*** 瞳距 */
    @JsonProperty("PD")
    private Double pd;
    /*** 右眼等效球镜度 */
    private Double rightPa;
    /*** 左眼等效球镜度 */
    private Double leftPa;
    /*** 右垂直⽅向斜视度数 */
    private Integer rightAxsiV;
    /*** 右⽔平⽅向斜视度数 */
    private Integer rightAxsiH;
    /*** 左垂直⽅向斜视度数 */
    private Integer leftAxsiV;
    /*** 左⽔平⽅向斜视度数 */
    private Integer leftAxsiH;
    /*** 红光反射左眼 */
    private Integer redReflectLeft;
    /*** 红光反射右眼 */
    private Integer redReflectRight;
    /*** 机构id */
    private Integer screeningOrgId;
    /*** 设备编码*/
    private String deviceSn;

    /**
     * 获取新的对象
     * @param device
     * @return
     */
    public DeviceScreeningData newDeviceScreeningDataInstance(Device device) {
        DeviceScreeningData deviceScreeningData = BeanCopyUtil.copyBeanPropertise(this, DeviceScreeningData.class);
        deviceScreeningData.setDeviceId(device.getId());
        deviceScreeningData.setDeviceSn(device.getDeviceSn());
        deviceScreeningData.setCreateTime(new Date());
        deviceScreeningData.setPatientAge(patientAge);
        deviceScreeningData.setPatientCid(patientCID);
        deviceScreeningData.setPd(pd);
        deviceScreeningData.setLeftPr(leftPR);
        deviceScreeningData.setRightPr(rightPR);
        deviceScreeningData.setPatientPno(patientPhoneNumber);
        deviceScreeningData.setPatientGender(GenderEnum.getType(patientGender));
        deviceScreeningData.setScreeningOrgId(device.getBindingScreeningOrgId());
        deviceScreeningData.setPatientAgeGroup(PatientAgeUtil.getPatientAgeRange(patientAge));
        deviceScreeningData.setPatientDept(patientInfo);
        deviceScreeningData.setScreeningTime((checkTime));
        return deviceScreeningData;
    }

    /**
     * 获取唯一keyString
     * @return
     */
    public String getUnikeyString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(screeningOrgId)
                .append(DELIMITER_CHAR)
                .append(deviceSn)
                .append(DELIMITER_CHAR)
                .append(patientId)
                .append(DELIMITER_CHAR)
                .append(checkTime);
        return stringBuilder.toString();
    }
}