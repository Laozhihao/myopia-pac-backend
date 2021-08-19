
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
            margin: 170px 0 200px 0;

            @bottom-left {
                content: element(footer)
            }

            @top-left {
                content: element(header);
            }

        }

        #header {
            position: running(header);
        }

        .report-images {
            display: block;
            overflow: hidden;
            padding: 6px 0 2px;
            background: #F2F2F2;
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
            width: 794px;
            height: 752.5px;
            position: relative;
            display: block;
            margin: 0 auto;
        }


        .report-title {
            display: block;
            padding-top: 80px;
            padding-bottom: 10px;
            text-align: center;
            font-size: 26px;
        }

        .report-subtitle {
            display: block;
            font-size: 18px;
            color: #333;
            text-align: center;
            font-weight: initial;
            padding-bottom: 9px;
        }

        .images-desc {
            width: 356px;
            float: left;
            padding-left: 15px;
            border: 1px solid transparent;
            text-align: left;
        }

        .images-desc .title {
            font-weight: 500;
        }

        .report-images .flex {
            padding-left: 30px;
        }

        .report-images .flex .note-label {
            padding-top: 10px;
            color: #595959;
            font-size: 14px;
        }

        .images-wraper {
            position: relative;
            width: 100%;
            height: 128px;
            text-align: right;
            box-sizing: border-box;
            overflow: hidden;
        }

        .images-wraper .title {
            position: absolute;
            top: 0;
            right: 128px;
        }

        .images-wraper img {
            width: 128px;
            display: inline-block;
            vertical-align: bottom;
        }

        .report-conclus {
            margin: 0 32px;
        }

        .report-conclus .report-layout {
            width: 100%;
        }

        .report-conclus .layout-row {
            width: 100%;
            color: #333;
            padding-top: 7px;
        }

        .report-conclus .conclus-part {
            word-wrap: break-word;
            white-space: normal;
            white-space:-moz-pre-wrap;
            cursor: text;
            line-height: 16px;
            border: 1px solid transparent;
            overflow: hidden;
            text-indent: 2em;
        }

        .report-conclus .conclus-part span[contenteditable] {
            margin-left: 1px;
            outline: none;
        }

        .images-desc .note-label {
            display: block;
            color: #121212;
            margin: 3px 0;
            font-weight: normal;
        }

        .images-desc .note-label label {
            display: inline-block;
            width: 70px;
            text-align: right;
        }

        .report-conclus .title {
            margin: 60px 0 10px 0;
        }

        #footer {
            position: running(footer);
            width: 794px;
            margin-left: 52px;
            margin-top: -100px;
        }

        #footer .text-w {
            display: inline-block;
            float: left;
            width: 380px;
            margin-top: -70px;
            color: #595959;
            font-size: 12px;
        }

        .text-w p {
            color: #262626;
            font-size: 14px;
            margin-left: -12px;
            margin-bottom: 12px;
        }

        .text-w li {
            margin-bottom: 6px;
        }

        #footer .images-desc {
            width: 320px;
        }
    </style>
</head>
<body>
<#list students as student>
    <div class="wraper">
        <div id="header">
            <h1 class="report-title">
                ${screeningOrgConfigs.title?if_exists}
            </h1>
            <h3 class="report-subtitle">
                ${screeningOrgConfigs.subTitle?if_exists}
            </h3>
        </div>
        <div id="footer" class="cf">
            <div class="text-w">
                <p>关注公众号，及时查看孩子视力筛查结果报告！</p>
                <ul>
                    <li>查看孩子的眼健康档案</li>
                    <li>了解孩子的视力变化趋势</li>
                    <li>学习相关的眼健康知识与科普</li>
                    <li>有问题可在线咨询医生进行解答</li>
                </ul>
            </div>
            <div class="images-desc">
                <div class="images-wraper">
                    <img crossorigin="anonymous" src="${qrCodeFile?if_exists}"/>
                </div>
            </div>
        </div>
        <div class="report-content">
            <div class="report-images">
                <div class="images-desc flex">
                    <p class="title">学生信息：</p>
                    <div class="note-label">
                        <label>学校名称：</label>${schoolName?if_exists}
                    </div>
                    <div class="note-label">
                        <label>班级年级：</label>${classDisplay?if_exists}
                    </div>
                    <div class="note-label">
                        <label>性别：</label>${student.genderDesc?if_exists}
                    </div>
                    <div class="note-label">
                        <label>姓名：</label>${student.name?if_exists}
                    </div>
                </div>
                <div class="images-desc">
                    <div class="images-wraper">
                        <span class="title">筛查二维码：</span>
                        <img crossorigin="anonymous" src="${student.qrCodeUrl?if_exists}"/>
                    </div>
                </div>
            </div>
            <div class="report-conclus">
                <p class="title">${screeningOrgConfigs.call?if_exists}</p>
                <p class="">${screeningOrgConfigs.greetings?if_exists}</p>
                <div class="report-layout">
                    <div class="layout-row">
                        <div class="conclus-part">
                        <span contenteditable="true">
                        ${screeningOrgConfigs.content?if_exists}
                        </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</#list>
</body>

</html>

