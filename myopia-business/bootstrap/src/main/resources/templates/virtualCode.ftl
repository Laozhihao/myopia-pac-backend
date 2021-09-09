
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <link rel="icon" href="../../static/img/favicon.ico" mce_href="../../static/img/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../../static/img/favicon.ico" mce_href="../../static/img/favicon.ico"
          type="image/x-icon"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta name="renderer" content="webkit"/>
    <title>报告中心</title>
    <style>
        .cf:after {
            display: block;
            clear: both;
            content: "";
            visibility: hidden;
            height: 0
        }

        .cf {
            zoom: 1
        }

        * {
            margin: 0;
            padding: 0;
        }

        input[type=hidden] {
            display: none;
        }

        body {
            font-family: SimHei;
            font-size: 14px;
            position: relative;
        }

        @page {
            size: A4 portrait;
            margin: 36px 0 12px 12px;

            @bottom-left {content: element(footer)  }

            @top-left {
                content: element(header);
            }

        }
        #header {
            position: running(header);
            height: 36px;
        }

        #footer {
            position: running(footer);
            height: 12px;
        }

        .h-title {
            text-align: center;
            line-height: 48px;
            color: #111;
            font-size: 14px;
        }

        .grade {
            margin-left: 20px;
        }

        .report-images {
            display: block;
            margin: 0;
            overflow: hidden;
        }

        .wraper {
            position: relative;
            color: #000;
            display: block;
            width: 100vw;
            height: 100vh;
            overflow: hidden;
            overflow-y: auto;
        }

        /*生成pdf 96PPI*/
        .wraper .report-content {
            width: 800px;
            position: relative;
            display: block;
            margin: 0 auto;
        }

        .report-bar .bar-item span{
            color: #000;
        }

        .images-desc {
            float: left;
            width: 247px;
            height: 165px;
            margin-right: 12px;
            margin-top: 12px;
            border: 1px solid #AAAAAA;
        }

        .images-wraper {
            width: 104px;
            height: 140px;
            float: left;
            position: relative;
            margin: 8px;
            text-align: center;
            border: 1px dashed #AAAAAA;
            box-sizing: border-box;
        }

        .images-wraper img{
            height: 88px;
            margin: 10px 8px;
            display: inline-block;
        }

        .images-wraper p {
            margin: 0 10px;
            font-size: 13px;
        }

        .info-wraper p {
            position: relative;
            font-size: 13px;
            margin-top: 24px;
            color: #111;
        }

        .info-wraper .code {
            margin-top: 14px;
        }

        .info-wraper .info-text {
            display:inline-block;
            border-bottom: 1px solid #888888;
        }

        .info-wraper p .name {
            width: 86px;
        }

        .info-wraper p .age {
            width: 70px;
        }

        .info-wraper .checkbox {
            display: inline-block;
            vertical-align: top;
            width: 12px;
            height: 12px;
            border: 1px solid #888;
        }

        .info-wraper .checkbox.female {
            margin-left: 10px;
        }
</style>
</head>
<body>
<header id="header">
    <p class="h-title">学校：${schoolName?if_exists}<span class="grade">年级班级：${classDisplay?if_exists}</span></p>
</header>
<footer id="footer">
</footer>
<div id="report" class="wraper">
    <div class="report-content">
        <div class="report-images">
            <#list students as student>
            <div class="images-desc">
                <div class="images-wraper">
                    <img crossorigin="anonymous" src="${student.qrCodeUrl?if_exists}" />
                    <p>${student.screeningCode?string.computer}</p>
                </div>
                <div class="info-wraper">
                    <p class="code">编码:${student.screeningCode?string.computer}</p>
                    <p>姓名:<span class="info-text name"></span></p>
                    <p>性别:<span class="checkbox"></span> 男 <span class="checkbox female"></span> 女</p>
                    <p>年龄:<span class="info-text age"></span> 岁</p>
                </div>
            </div>
            </#list>
        </div>
    </div>
</div>

</body>

</html>

