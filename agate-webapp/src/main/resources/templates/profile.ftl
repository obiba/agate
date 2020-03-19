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
                  <dd class="col-sm-8">${user.name}</dd>
                  <dt class="col-sm-4"><@message "email"/></dt>
                  <dd class="col-sm-8">${user.email}</dd>
                  <dt class="col-sm-4"><@message "preferred-language"/></dt>
                  <dd class="col-sm-8">${user.preferredLanguage}</dd>
                  <dt class="col-sm-4"><@message "role"/></dt>
                  <dd class="col-sm-8">${user.role}</dd>
                  <#if user.groups??>
                    <dt class="col-sm-4"><@message "groups"/></dt>
                    <dd class="col-sm-8">
                      <#list user.groups as group>
                        <span class="badge badge-info">${group}</span>
                      </#list>
                    </dd>
                  </#if>
                  <#if user.applications??>
                    <dt class="col-sm-4"><@message "applications"/></dt>
                    <dd class="col-sm-8">
                      <#list user.applications as application>
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
                  <dt class="col-sm-4"><@message "last-login"/></dt>
                  <dd class="col-sm-8">
                    <span class="moment-datetime">${user.lastLogin.toString(datetimeFormat)}</span>
                  </dd>
                </dl>
              </div>
            </div>
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

</body>
</html>
