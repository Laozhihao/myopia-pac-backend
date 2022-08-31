package com.wupol.myopia.rec.feign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.wupol.myopia.rec.domain.ApiResult;
import com.wupol.myopia.rec.util.TypeUtils;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author jacob
 * @version 1.0.0
 * Detail: ApiGateway 自定义解码
 * @date 2020/9/10 11:08
 */
@Slf4j
public class BusinessServiceCustomDecoder extends Decoder.Default {

    /**
     * 自定义结果处理器
     *
     * @param response 响应上下文
     * @param type 当前请求方法返回数据类型
     * @return Object
     * @throws IOException
     */
    @Override
    public Object decode(Response response, Type type) throws IOException {
        //以下解析http情况
        if (response.status() == HttpStatus.NOT_FOUND.value()) {
            throw new DecodeException(response.status(), "请求异常,未找到该api,响应状态: response = " + response.status(), response.request());
        } else if (response.body() == null) {
            throw new DecodeException(response.status(), "没有返回有效的数据,响应状态: response = " + response.status(), response.request());
        }
        String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
        ApiResult result = JSON.parseObject(bodyStr, ApiResult.class);
        // 实际数据
        String data = this.parseResult(result);
        // 类型转换
        return TypeUtils.getObject(type, data);
    }

    /**
     * 解析结果
     *
     * @param result 请求响应结果
     * @return String
     */
    private String parseResult(final ApiResult result) {
        int code = result.getCode();
        // code == 200 的情况:
        if (Objects.equals(200,code)) {
            return JSON.toJSONString(result.getData(), SerializerFeature.WriteMapNullValue);
        } else {
            throw new RuntimeException(result.getMessage());
        }
    }
}
