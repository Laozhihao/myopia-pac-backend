package com.wupol.myopia.base.minio;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MinioUtil {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioConfig minioConfig;


    /**
     * description: 上传文件
     *
     * @param multipartFile
     * @return: java.lang.String

     */
    public List<String> upload(MultipartFile[] multipartFile) {
        List<String> names = new ArrayList<>(multipartFile.length);
        for (MultipartFile file : multipartFile) {
            String fileName = file.getOriginalFilename();
            String[] split = fileName.split("\\.");
            if (split.length > 1) {
                fileName = split[0] + "_" + System.currentTimeMillis() + "." + split[1];
            } else {
                fileName = fileName + System.currentTimeMillis();
            }
            InputStream in = null;
            try {
                in = file.getInputStream();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(fileName)
                        .stream(in, in.available(), -1)
                        .contentType(file.getContentType())
                        .build()
                );
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            names.add(fileName);
        }
        return names;
    }

    /**
     * description: 下载文件
     *
     * @param fileName
     * @return: org.springframework.http.ResponseEntity<byte [ ]>
     */
    public ResponseEntity<byte[]> download(String fileName) {
        ResponseEntity<byte[]> responseEntity = null;
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = minioClient.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName).build());
            out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            //封装返回值
            byte[] bytes = out.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            try {
                headers.add("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            headers.setContentLength(bytes.length);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setAccessControlExposeHeaders(Collections.singletonList("*"));
            responseEntity = new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseEntity;
    }

    /**
     * 获取外链
     *
     * @param bucketName 存储bucket名称
     * @return 存储bucket内文件对象信息
     */
    public String listObjects(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 时间单位秒
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object("").expiry(3600).method(Method.GET).region("").build());
    }

    /**
     * 批量删除文件对象
     * @param bucketName 存储bucket名称
     * @param objects 对象名称集合
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objects) {
        List<DeleteObject> dos = objects.stream().map(DeleteObject::new).collect(Collectors.toList());
        return minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
    }


}
