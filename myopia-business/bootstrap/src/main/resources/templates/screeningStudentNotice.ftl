
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
            margin: 180px 50px 200px 50px;

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
<header id="header">
    <h1 class="report-title">
        山西省疾控处告家长书
    </h1>
    <h3 class="report-subtitle">
        视力筛查报告
    </h3>
</header>

<div class="wraper">
    <div class="report-content">
        <div class="report-images">
            <div class="images-desc flex">
                <div class="note-label">
                    <label>学校名称：</label>这是学校名称
                </div>
                <div class="note-label">
                    <label>年级班级：</label>6年级12班
                </div>
                <div class="note-label">
                    <label>学生姓名：</label>陈宇轩
                </div>
            </div>
            <div class="images-desc">
                <div class="images-wraper">
                    <img crossorigin="anonymous" src="./img/code.png" />
                </div>
            </div>
        </div>
        <div class="report-conclus">
            <p class="title">亲爱的家长朋友：</p>
            <p class="">您好！</p>
            <div class="report-layout">
                <div class="layout-row">
                    <div class="conclus-part">
                        <span contenteditable="true">
                            人生是一个不断做减法的过程，具体表现在：钱包越来越空了，头发越来越少了，喜欢的人也不见了。 你的脸上云淡风轻，谁也不知道你的牙咬得有多紧。你走路带着风，谁也不知道你膝盖上仍有曾摔伤的淤青。你笑得没心没肺，没人知道你哭起来只能无声落泪。去交会让你开心的朋友，去爱不会让你流泪的人，去向自己想去的方向，去完成不论大小的梦想；生活应该是美好而又温柔的，你也是。情怀是，过期的凤梨罐头，不过期的食欲，过期的底片，不过期的创作欲，过期的旧书，不过期的求知欲。总有一天，会有一个人进入你的世界，陪你过每一个节日，陪你听你爱听的歌，每天有对你说不完的情话，只是想弥补他迟到的时光。当你特别想做某件事时，就去做吧，不要总是和那些人生的美好擦肩而过。冲动也许会付出代价，但错过付出的代价更让人追悔莫及。人生是一个不断做减法的过程，具体表现在：钱包越来越空了，头发越来越少了，喜欢的人也不见了。你的脸上云淡风轻，谁也不知道你的牙咬得有多紧。你走路带着风，谁也不知道你膝盖上仍有曾摔伤的淤青。你笑得没心没肺，没人知道你哭起来只能无声落泪。去交会让你开心的朋友，去爱不会让你流泪的人，去向自己想去的方向，去完成不论大小的梦想；生活应该是美好而又温柔的，你也是。情怀是，过期的凤梨罐头，不过期的食欲，过期的底片，不过期的创作欲，过期的旧书，不过期的求知欲。总有一天，会有一个人进入你的世界，陪你过每一个节日，陪你听你爱听的歌，每天有对你说不完的情话，只是想弥补他迟到的时光。当你特别想做某件事时，就去做吧，不要总是和那些人生的美好擦肩而过。冲动也许会付出代价，但错过付出的代价更让人追悔莫及。歌，每天有对你说不完的情话，只是想弥补他迟到的时光。当你特别想做某件事时，就去做吧，不要总是和那些人生的美好擦肩而过。冲动也许会付出代价，但错过付出的代价更让人追悔莫及。人生是一个不断做减法的过程，具体表现在：钱包越来越空了，头发越来越少了，喜欢的人也不见了。你的脸上云淡风轻，谁也不知
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <footer id="footer" class="cf">
        <div class="text-w">
            关注公众号，绑定学生信息可随时查看学生的眼健康信息，查看最新的眼健康知识，随时与医生进行沟通互动。
        </div>
        <div class="images-desc">
            <div class="images-wraper">
                <img crossorigin="anonymous" src="./img/code.png" />
            </div>
        </div>
    </footer>

</body>

</html>

