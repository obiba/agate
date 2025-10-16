<!-- Macros -->
<#include "models/index.ftl">

<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">
  <title>${config.name!"Agate"}</title>
</head>
<body id="index-page" class="hold-transition layout-top-nav layout-navbar-fixed">

<div class="app-wrapper"><!-- was .wrapper -->

  <!-- Navbar -->
  <#include "libs/top-navbar.ftl">
  <!-- /.navbar -->

  <!-- Main -->
  <main class="app-main"><!-- was .content-wrapper -->

    <section class="py-5 mb-4 bg-body-tertiary">
      <div class="container py-4">
        <h1 class="display-4"><@message "auth-portal-title"/></h1>
        <p class="lead mb-0"><@message "auth-portal-text"/></p>
      </div>
    </section>

    <!-- Content -->
    <section class="app-content"><!-- was .content -->
      <div class="container">

        <@homeModel/>

      </div><!-- /.container -->
    </section>
    <!-- /.app-content -->

  </main>
  <!-- /.app-main -->

  <#include "libs/footer.ftl">

</div>
<!-- /.app-wrapper -->

<#include "libs/scripts.ftl">

</body>
</html>
