package com.wupol.myopia.oauth;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;

import java.util.Base64;
import java.util.Objects;

/**
 * 图片获取
 *
 * @author hang.yuan 2022/5/5 18:32
 */
public class ImageTest {

    //@Test
    public void getImage() {
        Integer imageId=1;
        String url ="https://picsum.photos/300/150/?image=%s";
        HttpRequest httpRequest = HttpUtil.createGet(String.format(url, imageId));
        httpRequest.setFollowRedirects(true);
        HttpResponse execute = httpRequest.execute();
        if (Objects.equals(execute.getStatus(), HttpStatus.HTTP_OK)){
            byte[] bytes = execute.bodyBytes();
            System.out.println(Base64.getEncoder().encodeToString(bytes));
        }
    }
}
