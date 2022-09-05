<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>${config.name!""} | <@message "sign-out"/></title>
  <!-- Tell the browser to be responsive to screen width -->
  <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body id="confirm-page" class="hold-transition login-page">
<div class="login-box">
  <div class="login-logo">
    <a href=".."><b>${config.name!""}</b></a>
  </div>
  <!-- /.login-logo -->
  <div class="card">
    <div class="card-body login-card-body">
      <p class="login-box-msg"><@message "confirm-sign-out"/></p>
      <div>
        <a class="btn btn-outline-danger" href="#" onclick="agatejs.signout('${postLogoutRedirectUri!""}');"><@message "sign-out"/></a>
        <a class="btn btn-outline-info ml-2" href="${postLogoutRedirectUri!"/"}"><@message "keep-signed-in"/></a>
      </div>
    </div>
    <!-- /.login-card-body -->
  </div>
</div>
<!-- /.login-box -->

<#include "libs/scripts.ftl">

<script>
  agatejs.confirmAndSetPassword("#form", function (errorKey) {
    var alertId = "#alert" + errorKey;
    $(alertId).removeClass("d-none");
    setTimeout(function() {
      $(alertId).addClass("d-none");
    }, 5000);
  });
</script>

</body>
</html>
