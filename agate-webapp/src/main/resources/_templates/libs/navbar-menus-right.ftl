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
        <a href="${contextPath}/profile" class="nav-link">
          <i class="fas fa-user"></i> ${user.displayName}
        </a>
      <#else>
        <a href="${contextPath}/profile" class="nav-link">
          <i class="fas fa-user"></i> ${username}
        </a>
      </#if>
    </li>
    <li class="nav-item">
      <a class="btn btn-outline-danger" href="${contextPath}/signout"><@message "sign-out"/></a>
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
      <a class="nav-link" href="${contextPath}/signin"><@message "sign-in"/></a>
    </li>
    <#if config.joinPageEnabled>
      <li class="nav-item">
        <a class="btn btn-outline-primary" href="${contextPath}/signup"><@message "sign-up"/></a>
      </li>
    </#if>
  </#if>
</#macro>
