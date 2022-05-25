<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">
  <title>${config.name!"Agate"} | <@message "authorization"/></title>
</head>
<body id="authorize-page" class="hold-transition layout-top-nav layout-navbar-fixed">
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
            <h1 class="m-0"><@message "authorization"/></h1>
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
          <div class="col-3">

          </div>
          <div class="col-6">

            <#if application??>
              <#if applicationAuthorized && applicationScopes?? && applicationScopes?keys?size gt 0>
                <div class="card card-primary card-outline">
                  <div class="card-header">
                    <h3 class="card-title"><@message "oauth.review-permissions"/></h3>
                  </div>
                  <div class="card-body">
                    <#list applicationScopes?keys as key>
                      <#assign bundle = applicationScopes[key]/>
                      <h4><#if bundle.name == "_agate">${config.name!"Agate"}<#else>${bundle.name}</#if></h4>
                      <p class="help-block">
                        <@message bundle.description/>
                      </p>
                      <ul>
                        <#if bundle.scopes?size gt 0>
                          <#list bundle.scopes as scope>
                            <li>
                              <code>${scope.name}</code>
                              <div><#if scope.description??><@message scope.description/></#if></div>
                            </li>
                          </#list>
                        <#else>
                          <li>
                            <@message "oauth.all-permissions"/>
                          </li>
                        </#if>
                      </ul>
                    </#list>
                  </div>
                  <div class="card-footer">

                    <form id="oauthForm" role="form" action="ws/oauth2/authz" method="post">
                      <input type="hidden" name="client_id" value="${clientId}">
                      <input type="hidden" name="redirect_uri" value="${redirectUri}">
                      <input type="hidden" name="response_type" value="${responseType}">
                      <input type="hidden" name="state" value="${state}">
                      <input type="hidden" name="scope" value="${scope}">
                      <button type="submit" name="grant" class="btn btn-default" value="false">
                        <span><@message "oauth.decline"/></span>
                      </button>
                      <button type="submit" name="grant" class="btn btn-primary" value="true">
                        <span><@message "oauth.accept"/></span>
                      </button>
                    </form>

                  </div>
                </div>
              <#else>
                <div class="alert alert-danger">
                  <h5><i class="icon fas fa-ban"></i> <@message "alert"/></h5>
                  <@message "oauth.application-access-denied"/>
                </div>
              </#if>
            <#else>
              <div class="alert alert-danger">
                <h5><i class="icon fas fa-ban"></i> <@message "alert"/></h5>
                <@message "oauth.unknown-client-application"/>
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

</body>
</html>
