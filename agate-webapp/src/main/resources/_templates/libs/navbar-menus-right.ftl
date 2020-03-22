<#macro rightmenus>
  <#if username??>
    <#if config?? && config.locales?size != 1>
      <li class="nav-item dropdown">
        <a id="userMenu" href="#" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" class="nav-link dropdown-toggle"> ${.lang?upper_case}</a>
        <ul aria-labelledby="dropdownSubMenu1" class="dropdown-menu border-0 shadow">
          <#list config.locales as locale>
            <li><a href="#" onclick="agatejs.changeLanguage('${locale.language}')" class="dropdown-item">${locale.language?upper_case}</a></li>
          </#list>
        </ul>
      </li>
    </#if>
    <li class="nav-item dropdown">
      <a id="userMenu" href="#" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" class="nav-link dropdown-toggle">
        <i class="fas fa-user"></i> <#if user??>${user.firstName!""} ${user.lastName!""}<#else>${username}</#if>
      </a>
      <ul aria-labelledby="dropdownSubMenu1" class="dropdown-menu border-0 shadow">
        <#if user??>
          <li><a href="${pathPrefix!".."}/profile" class="dropdown-item"><@message "profile"/></a></li>
        </#if>
        <li><a href="#" onclick="agatejs.signout('${pathPrefix!".."}');" class="dropdown-item"><@message "sign-out"/></a></li>
      </ul>
    </li>
  <#elseif config??>
    <#if config.locales?size != 1>
      <li class="nav-item dropdown">
        <a id="userMenu" href="#" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" class="nav-link dropdown-toggle"> ${.lang?upper_case}</a>
        <ul aria-labelledby="dropdownSubMenu1" class="dropdown-menu border-0 shadow">
          <#list config.locales as locale>
            <li><a id="lang-${locale.language}" href="#" onclick="agatejs.changeLanguage('${locale.language}')" class="dropdown-item">${locale.language?upper_case}</a></li>
          </#list>
        </ul>
      </li>
    </#if>
    <li class="nav-item">
      <a class="nav-link" href="${pathPrefix!".."}/signin<#if rc.requestUri != "/" && !rc.requestUri?starts_with("/reset-password") && !rc.requestUri?starts_with("/just-registered") && !rc.requestUri?starts_with("/error") && !rc.requestUri?starts_with("/signin")>?redirect=${rc.requestUri}</#if>"><@message "sign-in"/></a>
    </li>
    <#if config.joinPageEnabled>
      <li class="nav-item">
        <a class="btn btn-outline-primary" href="${pathPrefix!".."}/signup"><@message "sign-up"/></a>
      </li>
    </#if>
  </#if>
</#macro>
