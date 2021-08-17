
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
            margin: 170px 50px 200px 50px;

            @bottom-left {content: element(footer)  }

            @top-left {
                content: element(header);
            }

        }
        #header {
            position: running(header);
        }

        .report-images {
            display: block;
            margin: 0 -15px;
            overflow: hidden;
            background: #e4e3e3;
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
            width: 694px;
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
            width: 330px;
            float: left;
            padding-left: 15px;
            border: 1px solid transparent;
        }

        .report-images .flex {
            padding-left: 30px;
        }

        .report-images .flex .note-label {
            padding-top: 20px;
        }

        .images-wraper {
            position: relative;
            width: 100%;
            height: 128px;
            text-align: right;
            box-sizing: border-box;
            overflow: hidden;
        }

        .images-wraper img{
            width: 128px;
            display: inline-block;
            vertical-align: bottom;
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
            width: 694px;
            margin: 0 auto;
            margin-top: -100px;
        }

        #footer .text-w {
            display: inline-block;
            float: left;
            width: 380px;
            margin-top: -30px;
        }

        #footer .images-desc {
            width: 290px;
            padding-right: 0;
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
            关注公众号，绑定学生信息可随时查看学生的眼健康信息，查看最新的眼健康知识，随时与医生进行沟通互动。
        </div>
        <div class="images-desc">
            <div class="images-wraper">
                <img crossorigin="anonymous" src="${qrCodeFile?if_exists}" />
            </div>
        </div>
    </div>
    <div class="report-content">
        <div class="report-images">
            <div class="images-desc flex">
                <div class="note-label">
                    <label>学校名称：</label>${schoolName?if_exists}
                </div>
                <div class="note-label">
                    <label>年级班级：</label>${classDisplay?if_exists}
                </div>
                <div class="note-label">
                    <label>学生姓名：</label>${student.name?if_exists}
                </div>
            </div>
            <div class="images-desc">
                <div class="images-wraper">
                    <img crossorigin="anonymous" src="${student.qrCodeUrl?if_exists}" />
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

