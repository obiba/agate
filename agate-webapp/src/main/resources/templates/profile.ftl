<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">
  <title>${config.name!"Agate"} | <@message "profile"/></title>
</head>
<body class="hold-transition layout-top-nav layout-navbar-fixed">
<div class="wrapper">

  <!-- Navbar -->
  <#include "libs/top-navbar.ftl">
  <!-- /.navbar -->

  <!-- Content Wrapper. Contains page content -->
  <div class="content-wrapper">

    <!-- Content Header (Page header) -->
    <div class="content-header bg-info mb-4">
      <div class="container">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">${user.firstName!""} ${user.lastName!""}</h1>
          </div><!-- /.col -->
          <div class="col-sm-6">

          </div><!-- /.col -->
        </div><!-- /.row -->
      </div><!-- /.container-fluid -->
    </div>
    <!-- /.content-header -->

    <!-- Main content -->
    <div class="content">
      <div class="container">
        <div class="row">
          <div class="col-6">
            <div class="card card-primary card-outline">
              <div class="card-header">
                <h3 class="card-title"><@message "attributes"/></h3>
              </div>
              <div class="card-body">
                <dl class="row">
                  <dt class="col-sm-4"><@message "firstname"/></dt>
                  <dd class="col-sm-8">${user.firstName!""}</dd>
                  <dt class="col-sm-4"><@message "lastname"/></dt>
                  <dd class="col-sm-8">${user.lastName!""}</dd>
                  <dt class="col-sm-4"><@message "username"/></dt>
                  <dd class="col-sm-8 text-monospace">${user.name}</dd>
                  <dt class="col-sm-4"><@message "email"/></dt>
                  <dd class="col-sm-8">${user.email}</dd>
                  <dt class="col-sm-4"><@message "preferred-language"/></dt>
                  <dd class="col-sm-8"><@message "language." + user.preferredLanguage/></dd>
                  <dt class="col-sm-4"><@message "role"/></dt>
                  <dd class="col-sm-8"><span class="badge badge-secondary">${user.role}</span></dd>
                  <#if user.groups??>
                    <dt class="col-sm-4"><@message "groups"/></dt>
                    <dd class="col-sm-8">
                      <#list user.groups as group>
                        <span class="badge badge-info">${group}</span>
                      </#list>
                    </dd>
                  </#if>
                  <#if applications??>
                    <dt class="col-sm-4"><@message "applications"/></dt>
                    <dd class="col-sm-8">
                      <#list applications as application>
                        <span class="badge badge-primary">${application}</span>
                      </#list>
                    </dd>
                  </#if>
                  <#if user.attributes??>
                    <#list user.attributes?keys as key>
                      <dt class="col-sm-4">
                        <@message key/>
                      </dt>
                      <dd class="col-sm-8">
                        <#if user.attributes[key] == "true">
                          <i class="fas fa-check"></i>
                        <#else>
                          ${user.attributes[key]}
                        </#if>
                      </dd>
                    </#list>
                  </#if>
                </dl>
              </div>
            </div>
          </div>
          <div class="col-6">
            <#if user.realm == "agate-user-realm">
              <div class="card card-primary card-outline">
                <div class="card-header">
                  <h3 class="card-title"><@message "update-password"/></h3>
                </div>
                <div class="card-body">
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
                    <small><@message "update-password-failed"/></small>
                  </div>

                  <div id="alertSuccess" class="alert alert-success d-none">
                    <small><@message "update-password-success"/></small>
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
                    <div class="float-right">
                      <button type="submit" class="btn btn-primary btn-block"><@message "update"/></button>
                    </div>
                  </form>
                </div>
              </div>
            </#if>
          </div>
        </div>
      </div><!-- /.container-fluid -->
    </div>
    <!-- /.content -->
  </div>
  <!-- /.content-wrapper -->

  <#include "libs/footer.ftl">
</div>
<!-- ./wrapper -->

<#include "libs/scripts.ftl">

<script>
  const errorMessages = {
    'server.error.password.not-changed': '<@message "server.error.password.not-changed"/>'
  };
  agatejs.updatePassword("#form", function() {
    $("#alertSuccess").removeClass("d-none");
    $("input[type=password]").val("");
    setTimeout(function() {
      $(alertId).addClass("d-none");
    }, 5000);
  }, function (errorKey, responseData) {
    var alertId = "#alert" + errorKey;
    if (errorKey === "Failure" && responseData && responseData.messageTemplate && errorMessages[responseData.messageTemplate]) {
      $(alertId + " > small").text(errorMessages[responseData.messageTemplate]);
    }
    $(alertId).removeClass("d-none");
    setTimeout(function() {
      $(alertId).addClass("d-none");
    }, 5000);
  });
</script>

</body>
</html>
