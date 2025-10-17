<!-- Spring utils for translations -->
<#import "/spring.ftl" as spring/>
<#macro message code>
  <@spring.messageText code code/>
</#macro>

<#function messageWithFallback prefix attributeName>
  <#assign fullKey = prefix + attributeName>
  <#assign message1><@message fullKey /></#assign>
  <#assign message2><@message attributeName /></#assign>
  <#if message1?has_content>
    <#return message1>
  <#else>
    <#return message2>
  </#if>
</#function>

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
<link rel="stylesheet" href="${bootstrapPath}/css/bootstrap.min.css">

<link rel="stylesheet" href="${assetsPath}/libs/node_modules/@fortawesome/fontawesome-free/css/all.min.css">
<!-- Theme style -->
<link rel="stylesheet" href="${adminLTEPath}/dist/css/adminlte.min.css">
<link rel="stylesheet" href="${assetsPath}/css/agate.min.css">
<!-- Font: Source Sans Pro -->
<style type="text/css">
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: normal;
    font-weight: 300;
    font-display: swap;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Light.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-light-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-light-webfont.woff") format("woff");
  }
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: normal;
    font-weight: 400;
    font-display: swap;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Regular.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-regular-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-regular-webfont.woff") format("woff");
  }
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: normal;
    font-weight: 700;
    font-display: swap;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Bold.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-bold-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-bold-webfont.woff") format("woff");
  }
  @font-face {
    font-family: 'Source Sans Pro';
    font-style: italic;
    font-weight: 400;
    font-display: swap;
    src: url("${assetsPath}/fonts/Source_Sans_Pro/SourceSansPro-Italic.ttf") format("truetype"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-italic-webfont.woff2") format("woff2"),
    url("${assetsPath}/fonts/Source_Sans_Pro/sourcesanspro-italic-webfont.woff") format("woff");
  }

  /* Use the AdminLTE 3 default font */
  :root{
    --bs-font-sans-serif: "Source Sans Pro", system-ui, -apple-system, "Segoe UI",
    Roboto, "Helvetica Neue", Arial, "Noto Sans", "Liberation Sans",
    "Apple Color Emoji","Segoe UI Emoji","Segoe UI Symbol","Noto Color Emoji";
  }

  body{ font-family: var(--bs-font-sans-serif) !important; }
</style>
<!-- DataTables -->
<#--<link rel="stylesheet" href="${adminLTEPath}/plugins/datatables-bs4/css/dataTables.bootstrap4.css">-->
<!-- Toastr -->
<link rel="stylesheet" href="${toastrPath}/toastr.min.css">

<!-- Custom head -->
<#include "../models/head.ftl"/>
