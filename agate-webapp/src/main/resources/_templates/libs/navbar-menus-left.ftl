<#macro leftmenus>
  <li class="nav-item">
    <a href="${pathPrefix!".."}" class="nav-link"><@message "home"/></a>
  </li>
  <#include "../models/navbar-menus-left.ftl"/>
</#macro>
