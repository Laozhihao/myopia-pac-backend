
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
            margin: 10px 0 34px 24px;;

            @bottom-left {
                content: element(footer)
            }

            @top-left {
                content: element(header);
            }

        }

        #header {
            position: running(header);
            height: 10px;
        }

        #footer {
            position: running(footer);
            height: 34px;
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
            padding-right: 22px;
            border: 1px solid transparent;
            margin-top: 30px;
        }

        .images-wraper {
            position: relative;
            width: 130px;
            height: 130px;
            margin-bottom: 5px;
            box-sizing: border-box;
            overflow: hidden;
        }

        .images-wraper img{
            height: 130px;
            display: inline-block;
        }

        .images-desc .note-label {
            display: block;
            color: #111111;
            margin: 7px 0 0;
            font-weight: normal;
            font-size: 16px;
        }

        .images-desc .note-label.name {
            display: block;
            font-weight: bold;
            font-size: 20px;
        }
    </style>
</head>

<body>
<header id="header">
</header>
<footer id="footer">
</footer>
<div id="report" class="wraper">
    <div class="report-content">
        <div class="report-images">
            <#list students as student>
            <div class="images-desc">
                <div class="images-wraper">
                    <img crossorigin="anonymous" src="${student.qrCodeUrl?if_exists}" alt=""/>
                </div>
                <div class="note-label name">
                    ${student.name?if_exists}
                </div>
                <div class="note-label">
                    ${student.genderDesc?if_exists}<span>${classDisplay?if_exists}</span>
                </div>
            </div>
            </#list>
        </div>
    </div>
</div>

</body>

</html>

