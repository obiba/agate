<!-- Main Footer -->
<footer class="app-footer d-print-none">
  <!-- Default to the left -->
  <small><@message "powered-by"/> <a href="https://www.obiba.org">OBiBa Agate</a></small>
  <!-- To the right -->
  <div class="float-end d-none d-sm-inline">
    <#if !user?? || user.role == "agate-administrator">
      <a href="${contextPath}/admin" title="<@message "administration"/>" class="float-right border-right pr-2 mr-2"><i class="fa-solid fa-lock"></i></a>
    </#if>
    <#if config??>
      <strong><@message "copyright"/> &copy; 2025 <a href="${config.portalUrl!"#"}">${config.name!""}</a>.</strong> <@message "all-rights-reserved"/>
    </#if>
  </div>
</footer>
