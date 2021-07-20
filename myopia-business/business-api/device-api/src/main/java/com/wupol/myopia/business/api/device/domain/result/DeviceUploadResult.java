package com.wupol.myopia.business.api.device.domain.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Classname DeviceUploadResult
 * @Description 设备上传通用result
 * @Date 2021/7/15 4:58 下午
 * @Author Jacob
 * @Version
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeviceUploadResult {
    /**
     * 成功
     */
    public static final  DeviceUploadResult SUCCESS = new DeviceUploadResult(true,"请求成功",null);
    /**
     * 失败
     */
    public static final  DeviceUploadResult FAILURE = new DeviceUploadResult(false,"请求错误",null);


    /**
     * 状态
     */
    private Boolean status;
    /**
     * 消息
     */
    private String message;
    /**
     * 数据 (todo 类型未知,目前用不上)
     */
    private Object data;

    /**
     * 失败
     * @param message 失败消息
     * @return
     */
    public static DeviceUploadResult FAILURE(String message) {
        return new DeviceUploadResult(true,message,null);
    }

}

