<script>
  <#if !authenticated>
    agatejs.redirect('${postLogoutRedirectUri!"${contextPath}"}');
  <#elseif !confirm>
    agatejs.signout('${postLogoutRedirectUri!"${contextPath}"}');
  </#if>
</script>