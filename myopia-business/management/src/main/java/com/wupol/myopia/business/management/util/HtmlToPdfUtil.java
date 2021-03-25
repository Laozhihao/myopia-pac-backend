package com.wupol.myopia.business.management.util;

import lombok.extern.log4j.Log4j2;

import java.io.*;

/**
 * HTML转PDF工具
 *
 * @Author HaoHao
 * @Date 2021/3/17
 **/
@Log4j2
public class HtmlToPdfUtil {
    /**
     * 转换工具命令
     **/
    private static final String HTML_TO_PDF_TOOL_COMMAND = "wkhtmltopdf";

    /**
     * 转换
     *
     * @param htmlSrcPath html页面地址（可以是网页或者本地html文件绝对路径）
     * @param pdfFilePath 生成的PDF绝对路径
     * @return boolean
     **/
    public static boolean convert(String htmlSrcPath, String pdfFilePath) {
        File file = new File(pdfFilePath);
        File parent = file.getParentFile();
        // 如果pdf保存路径不存在，则创建路径
        if(!parent.exists()){
            parent.mkdirs();
        }
        // "--window-status 1" 允许js异步请求
        ProcessBuilder processBuilder = new ProcessBuilder(HTML_TO_PDF_TOOL_COMMAND, "--window-status", "1", htmlSrcPath, pdfFilePath);
        log.debug(processBuilder.command().toString());
        processBuilder.redirectErrorStream(true);
        BufferedReader br = null;
        InputStreamReader reader = null;
        try {
            Process process = processBuilder.start();
            reader = new InputStreamReader(process.getInputStream());
            br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                log.debug(line);
                if (line.contains("Error")) {
                    log.error("【HTML转PDF异常】：" + line);
                    process.destroy();
                    return false;
                }
            }
            int exitCode = process.waitFor();
            log.debug("exitCode = "+exitCode);
        } catch (IOException | InterruptedException e) {
            log.error("【HTML转PDF异常】", e);
            return false;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("【HTML转PDF】关闭数据流异常", e);
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        String htmlSrcPath = "http://8.135.35.154:7002?notificationId=199&districtId=31908&token=Bearer%20eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6eyJpZCI6Mywib3JnSWQiOjEsInVzZXJuYW1lIjoiaGFvaGFvIiwic3lzdGVtQ29kZSI6MSwicm9sZVR5cGVzIjpbMF0sInBsYXRmb3JtQWRtaW5Vc2VyIjp0cnVlfSwiZXhwIjoxNjE2MDQxMDA5LCJ1c2VyX25hbWUiOiJoYW9oYW8iLCJqdGkiOiI2YzhmYjcxYi0xNjc5LTRkNjEtODEzYS01OTVlYWQ0MTgyZDAiLCJjbGllbnRfaWQiOiIxIiwic2NvcGUiOlsiYWxsIl19.ZDPzV32THIU56szn9gGsS_5L5VdpPuNlwrLudR0QnyKo7ML8dPw-HkXLZ7ylsOguX2Y-elII9kmq5Q7sAAi1JoKzWP5Mu4ja8n5JQk476GTyvlv8fbQ3MWFhrSQ03Xp2bB5LSMJltc6hT2evGYlFe5gO6iUEi1XFWxe_TQF2jqNvJeuETnR2bWflOjknpvTZLMGLBYls4DgxnRKvmAsUry14rXjk87fRqBwuhWWcRVU9CrmoPGIGqhfMuaxT7w5HVxgzLGZk0BbRZcBlYv6T0zHc8PA8iCOcBReCkV5n3RGz6bPtF8USQjsQJjrVPR5PpJboRx6hMdNn7dokxsLarQ";
        String htmlSrcPath = "http://8.135.35.154:7002?notificationId=199&districtId=31908&token=Bearer%20eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6eyJpZCI6Mywib3JnSWQiOjEsInVzZXJuYW1lIjoiaGFvaGFvIiwic3lzdGVtQ29kZSI6MSwicm9sZVR5cGVzIjpbMF0sInBsYXRmb3JtQWRtaW5Vc2VyIjp0cnVlfSwiZXhwIjoxNjE2MDkwNzE2LCJ1c2VyX25hbWUiOiJoYW9oYW8iLCJqdGkiOiJkMGYzYTA0Ny1jZThmLTQ2MmUtOTIzNy01MDVkNzMzODkyZWEiLCJjbGllbnRfaWQiOiIxIiwic2NvcGUiOlsiYWxsIl19.StVnOFJ8nJS32ofqQCMTrBdYvr2U2JOoBf5mnb3S9JusUYEbTodHXwlhZGvwyqtQazOEkpSAE_g0tx3UCAkM2uDy_OqfTsrbwzAs_DpZIr-SC8fZHG7YUVzpZpnQMTEkpiuJeo7cXCkVtZ6UiBniwyYcZB1xMAguJT-L-mmzFA2NMz-KgI8aWEW1yW5LzzKAnDy-sU5KqlZim-FMbjtWxj2lSEdjgaGYEqjOGiZgApAszgrCA8IVRQ21651IGRlWb7iY9x3JD3w1pGzTwkNvs5apPt28HSLC0eUN9gA2S6jqbnfHsOIFm-3xS2Js5t3-WmRT8kftJAyScWF-n6_A1g";
//        String htmlSrcPath = "http://8.135.35.154:7002?notificationId=199&districtId=31908&token=Bearer%20";
        String pdfFilePath = "f:/pdf/report-7.pdf";
//        HtmlToPdfUtil.convert(htmlSrcPath, pdfFilePath);
//        pdfFilePath = "f:/report-8.pdf";
        HtmlToPdfUtil.convert(htmlSrcPath, pdfFilePath);
    }
}