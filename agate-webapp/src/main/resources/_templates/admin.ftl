<!DOCTYPE html>
<html lang="${.lang}">
<head>
    <!-- Context path setting -->
    <#assign contextPath = "${config.contextPath}"/>
    <title>${config.name!"Agate"}</title>
    <meta charset=utf-8>
    <meta name=description content="OBiBa identity provider">
    <meta name=format-detection content="telephone=no">
    <meta name=msapplication-tap-highlight content=no>
    <meta name=viewport content="user-scalable=no,initial-scale=1,maximum-scale=1,minimum-scale=1,width=device-width">
    <link rel=icon type=image/png sizes=128x128 href="${contextPath}/admin/icons/favicon-128x128.png">
    <link rel=icon type=image/png sizes=96x96 href="${contextPath}/admin/icons/favicon-96x96.png">
    <link rel=icon type=image/png sizes=32x32 href="${contextPath}/admin/icons/favicon-32x32.png">
    <link rel=icon type=image/png sizes=16x16 href="${contextPath}/admin/icons/favicon-16x16.png">
    <link rel=icon type=image/ico href="favicon.ico">
    <script type="module" crossorigin src="${contextPath}/admin/assets/${entryPointJS}"></script>
    <link rel="stylesheet" href="${contextPath}/admin/assets/${entryPointCSS}">
</head>
<body>
<div id=q-app></div>
</body>
</html>