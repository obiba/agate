<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>${config.name!""} | <@message "update-password"/></title>
  <!-- Tell the browser to be responsive to screen width -->
  <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body id="reset-password-page" class="hold-transition login-page">
<div class="login-box">
  <div class="login-logo">
    <a href=".."><b>${config.name!""}</b></a>
  </div>
  <!-- /.login-logo -->
  <div class="card">
    <div class="card-body login-card-body">

      <#if key??>
        <p class="login-box-msg"><@message "update-password"/></p>

        <div id="alertPasswordMissing" class="alert alert-danger d-none">
          <small><@message "password-missing"/></small>
        </div>

        <div id="alertPasswordTooShort" class="alert alert-danger d-none">
          <small><@message "password-too-short"/></small>
        </div>

        <div id="alertPasswordNoMatch" class="alert alert-danger d-none">
          <small><@message "password-no-match"/></small>
        </div>

        <div id="alertFailure" class="alert alert-danger d-none">
          <small><@message "reset-password-failed"/></small>
        </div>

        <form id="form" method="post">
          <div class="input-group mb-3">
            <input name="password" type="password" class="form-control" placeholder="<@message "new-password"/>">
            <div class="input-group-append">
              <div class="input-group-text">
                <span class="fas fa-lock"></span>
              </div>
            </div>
          </div>
          <div class="input-group mb-3">
            <input name="password2" type="password" class="form-control" placeholder="<@message "repeat-new-password"/>">
            <div class="input-group-append">
              <div class="input-group-text">
                <span class="fas fa-lock"></span>
              </div>
            </div>
          </div>
          <input name="key" type="hidden" value="${key}">
          <div class="row">
            <div class="col-8">

            </div>
            <!-- /.col -->
            <div class="col-4">
              <button type="submit" class="btn btn-primary btn-block"><@message "update"/></button>
            </div>
            <!-- /.col -->
          </div>
        </form>
      <#else>
        <div id="alertFailure" class="alert alert-danger">
          <small><@message "reset-key-missing"/></small>
        </div>
      </#if>

    </div>
    <!-- /.login-card-body -->
  </div>
</div>
<!-- /.login-box -->

<#include "libs/scripts.ftl">

<script>
  agatejs.resetPassword("#form", function (errorKey) {
    var alertId = "#alert" + errorKey;
    $(alertId).removeClass("d-none");
    setTimeout(function() {
      $(alertId).addClass("d-none");
    }, 5000);
  });
</script>

</body>
</html>
