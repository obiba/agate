<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>${config.name!""} | <@message "set-password"/></title>
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

      <#if key??>
        <p class="login-box-msg"><@message "set-password"/></p>

        <div id="alertPasswordMissing" class="alert alert-danger d-none">
          <small><@message "password-missing"/></small>
        </div>

        <div id="alertPasswordTooShort" class="alert alert-danger d-none">
          <small><@message "password-too-short"/></small>
        </div>

        <div id="alertPasswordTooWeak" class="alert alert-danger d-none">
          <small><@message "server.error.password.too-weak"/></small>
        </div>

        <div id="alertPasswordNoMatch" class="alert alert-danger d-none">
          <small><@message "password-no-match"/></small>
        </div>

        <div id="alertFailure" class="alert alert-danger d-none">
          <small><@message "set-password-failed"/></small>
        </div>

        <form id="form" method="post">
          <div class="input-group mb-3">
            <input name="password" type="password" class="form-control" placeholder="<@message "new-password"/>">
            <span class="input-group-text"><i class="fa-solid fa-lock"></i></span>
          </div>
          <div class="input-group mb-3">
            <input name="password2" type="password" class="form-control" placeholder="<@message "repeat-new-password"/>">
            <span class="input-group-text"><i class="fa-solid fa-lock"></i></span>
          </div>
          <input name="key" type="hidden" value="${key}">
          <div class="row">
            <div class="col-8">

            </div>
            <!-- /.col -->
            <div class="col-6">
              <button type="submit" class="btn btn-primary btn-block"><@message "submit"/></button>
            </div>
            <!-- /.col -->
          </div>
        </form>
      <#else>
        <div id="alertFailure" class="alert alert-danger">
          <small><@message "confirm-key-missing"/></small>
        </div>
      </#if>

    </div>
    <!-- /.login-card-body -->
  </div>
</div>
<!-- /.login-box -->

<#include "libs/scripts.ftl">
<#include "libs/confirm-scripts.ftl">

</body>
</html>
