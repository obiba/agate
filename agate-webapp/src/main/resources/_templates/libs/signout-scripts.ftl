<script>
  <#if !authenticated>
    agatejs.redirect('${(postLogoutRedirectUri!contextPath)?js_string?no_esc}');
  <#elseif !confirm>
    agatejs.signout('${(postLogoutRedirectUri!contextPath)?js_string?no_esc}');
  </#if>
</script>