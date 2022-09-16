<script>
  <#if !authenticated>
    agatejs.redirect('${postLogoutRedirectUri!".."}');
  <#elseif !confirm>
    agatejs.signout('${postLogoutRedirectUri!".."}');
  </#if>
</script>