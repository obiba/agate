<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">
  <title>${config.name!"Agate"}</title>
</head>
<body class="hold-transition layout-top-nav layout-navbar-fixed">
<div class="wrapper">

  <!-- Navbar -->
  <#include "libs/top-navbar.ftl">
  <!-- /.navbar -->


  <!-- Content Wrapper. Contains page content -->
  <div class="content-wrapper">

    <div class="jumbotron jumbotron-fluid">
      <div class="container">
        <h1 class="display-4"><@message "auth-portal-title"/></h1>
        <p class="lead"><@message "auth-portal-text"/></p>
      </div>
    </div>

    <!-- Main content -->
    <div class="content">
      <div class="container">

        <div class="row">

          <#if !user?? || user.role == "agate-administrator">
            <div class="col-sm-12 col-lg-6">
              <div class="small-box bg-info">
                <div class="inner">
                  <h3><@message "admin"/></h3>
                  <p><@message "admin-users-apps"/></p>
                </div>
                <div class="icon">
                  <i class="fas fa-cogs"></i>
                </div>
                <a href="admin" class="small-box-footer">
                  <@message "more-info"/> <i class="fas fa-arrow-circle-right"></i>
                </a>
              </div>
            </div>
          </#if>

          <#if !username?? || user??>
            <div class="col-sm-12 col-lg-6">
              <div class="small-box bg-warning">
                <div class="inner">
                  <h3><@message "profile"/></h3>
                  <p>
                    <#if user??>
                      <@message "manage-my-profile"/>
                    <#else>
                      <@message "manage-your-profile"/>
                    </#if>
                  </p>
                </div>
                <div class="icon">
                  <i class="fas fa-user"></i>
                </div>
                <a href="profile" class="small-box-footer">
                  <@message "more-info"/> <i class="fas fa-arrow-circle-right"></i>
                </a>
              </div>
            </div>
          </#if>
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
