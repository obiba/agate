<!-- Date and Datetime formats -->
<#assign datetimeFormat = "yyyy-MM-dd hh:mm"/>
<#assign dateFormat = "yyyy-MM-dd"/>

<!-- Favicon -->
<#assign faviconPath = "${contextPath}/favicon.ico"/>

<!-- Assets location -->
<#assign assetsPath = "${contextPath}/assets"/>

<!-- Branding -->
<#assign brandImageSrc = "${assetsPath}/images/agate-logo.png"/>
<#assign brandImageClass = "img-circle elevation-3"/>
<#assign brandTextEnabled = true/>
<#assign brandTextClass = "font-weight-light"/>

<!-- Theme -->
<#assign adminLTEPath = "${assetsPath}/libs/node_modules/admin-lte"/>
<#assign bootstrapPath = "${assetsPath}/libs/node_modules/bootstrap/dist"/>
<#assign jqueryPath = "${assetsPath}/libs/node_modules/jquery/dist"/>
<#assign toastrPath = "${assetsPath}/libs/node_modules/toastr/build"/>

<!-- Home page settings -->
<#assign portalLink = "${config.portalUrl!contextPath}" + "/"/>

<!-- Profile -->
<#assign showProfileRole = false/>
<#assign showProfileGroups = false/>
<#assign showProfileApplications = false/>
