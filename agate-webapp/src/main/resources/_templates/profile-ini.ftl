<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">
  <title>${config.name!"Agate"} | <@message "profile"/></title>
</head>
<body id="profile-page" class="hold-transition layout-top-nav layout-navbar-fixed">
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
            <h1 class="m-0">${username!""}</h1>
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
          <div class="col-sm-12 col-lg-6">
            <div class="card card-primary card-outline">
              <div class="card-header">
                <h3 class="card-title"><@message "two-factor-authentication"/></h3>
              </div>
              <div class="card-body">
                <div class="mb-3">
                  <@message "2fa-instructions"/>
                </div>
                <div>
                  <button id="disable-otp" type="button" class="btn btn-primary" onclick="disableOtp();" <#if !otpEnabled>style="display: none;"</#if>><i class="fas fa-unlock"></i> <@message "2fa-disable"/></button>
                  <button id="enable-otp" type="button" class="btn btn-primary" onclick="enableOtp();" <#if otpEnabled>style="display: none;"</#if>><i class="fas fa-lock"></i> <@message "2fa-enable"/></button>
                </div>
              </div>
              <div id="qr-panel" class="card-footer" style="display:none;">
                <div>
                  <@message "2fa-qrcode-instructions"/>
                </div>
                <div class="text-center">
                  <img id="qr-img" src=""/>
                </div>
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
<#include "libs/profile-ini-scripts.ftl">

</body>
</html>
