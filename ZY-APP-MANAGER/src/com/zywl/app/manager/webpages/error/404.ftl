<!doctype html>
<html>
<head>
    <title>404 - ${company}</title>
    <meta charset="utf-8">
    <meta charset="utf-8">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <link rel="icon" href="${staticfile}/files/assets/images/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/extra-pages/404/2/css/style.css">
</head>

<body>
    <div id="container" class="container">
        <ul id="scene" class="scene">
            <li class="layer" data-depth="1.00"><img src="${staticfile}/files/extra-pages/404/2/images/404-01.png"></li>
            <li class="layer" data-depth="0.60"><img src="${staticfile}/files/extra-pages/404/2/images/shadows-01.png"></li>
            <li class="layer" data-depth="0.20"><img src="${staticfile}/files/extra-pages/404/2/images/monster-01.png"></li>
            <li class="layer" data-depth="0.40"><img src="${staticfile}/files/extra-pages/404/2/images/text-01.png"></li>
            <li class="layer" data-depth="0.10"><img src="${staticfile}/files/extra-pages/404/2/images/monster-eyes-01.png"></li>
        </ul>
        <h1>您请求的链接已删除或移动</h1>
        <a href="/" class="btn">返回首页</a>
    </div>
    <!-- Scripts -->
    <script src="${staticfile}/files/extra-pages/404/2/js/parallax.js"></script>
    <script>
        var scene = document.getElementById('scene');
        var parallax = new Parallax(scene);
    </script>
</body>
</html>