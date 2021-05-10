<!-- Spring utils for translations -->
<#import "/spring.ftl" as spring/>
<#macro message code>
  <@spring.messageText code code/>
</#macro>

<!-- Context path setting -->
<#assign contextPath = "${config.contextPath}"/>

<!-- App settings -->
<#include "settings.ftl"/>
<#include "../models/settings.ftl"/>

<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="x-ua-compatible" content="ie=edge">

<!-- Favicon -->
<link rel="shortcut icon" href="${faviconPath}" />

<!-- Font Awesome Icons -->
<link rel="stylesheet" href="${adminLTEPath}/plugins/fontawesome-free/css/all.min.css">
<!-- Theme style -->
<link rel="stylesheet" href="${adminLTEPath}/dist/css/adminlte.min.css">
<!-- Font: Source Sans Pro -->
<style type="text/css">
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: normal;
    font-weight: 300;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Light.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-light-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-light-webfont.woff") format("woff");
  }
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: normal;
    font-weight: 400;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Regular.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-regular-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-regular-webfont.woff") format("woff");
  }
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: normal;
    font-weight: 700;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Bold.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-bold-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-bold-webfont.woff") format("woff");
  }
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: italic;
    font-weight: 400;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Italic.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-italic-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-italic-webfont.woff") format("woff");
  }
</style>
<!-- DataTables -->
<link rel="stylesheet" href="${adminLTEPath}/plugins/datatables-bs4/css/dataTables.bootstrap4.css">
<!-- Toastr -->
<link rel="stylesheet" href="${adminLTEPath}/plugins/toastr/toastr.min.css">

<!-- Custom head -->
<#include "../models/head.ftl"/>
