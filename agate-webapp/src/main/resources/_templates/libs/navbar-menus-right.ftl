<#macro rightmenus>
  <#include "../models/navbar-menus-right.ftl"/>
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
    <li class="nav-item">
      <#if user??>
        <a href="/profile" class="nav-link">
          <i class="fas fa-user"></i> ${user.displayName}
        </a>
      <#else>
        <span class="nav-link">
          <i class="fas fa-user"></i> ${username}
        </span>
      </#if>
    </li>
    <li class="nav-item">
      <a class="btn btn-outline-danger" href="#" onclick="agatejs.signout();"><@message "sign-out"/></a>
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
      <a class="nav-link" href="/signin<#if rc.requestUri != "/" && !rc.requestUri?starts_with("/reset-password") && !rc.requestUri?starts_with("/just-registered") && !rc.requestUri?starts_with("/error") && !rc.requestUri?starts_with("/signin")>?redirect=${rc.requestUri}</#if>"><@message "sign-in"/></a>
    </li>
    <#if config.joinPageEnabled>
      <li class="nav-item">
        <a class="btn btn-outline-primary" href="/signup"><@message "sign-up"/></a>
      </li>
    </#if>
  </#if>
</#macro>
