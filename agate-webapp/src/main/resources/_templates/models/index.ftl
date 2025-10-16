<#macro homeModel>

  <#if user?? && otpSupport && !user.otpEnabled>
    <div class="alert alert-info">
      <h5><i class="icon fa-solid fa-lock"></i> <@message "security-info"/></h5>
      <@message "2fa-info"/>
      <a href="${contextPath}/profile">
        <@message "2fa-enable"/>
      </a>
    </div>
  </#if>

  <#if realm?? && realm == "agate-ini-realm" && !otpEnabled>
    <div class="alert alert-info">
      <h5><i class="icon fa-solid fa-lock"></i> <@message "security-info"/></h5>
      <@message "2fa-info"/>
      <a href="${contextPath}/profile">
        <@message "2fa-enable"/>
      </a>
    </div>
  </#if>

  <div class="row">

    <#if !user?? || user.role == "agate-administrator">
      <div class="col-sm-12 col-lg-6">
        <div class="small-box bg-info text-white position-relative">
          <div class="inner z-1">
            <h3><@message "administration"/></h3>
            <p><@message "admin-users-apps"/></p>
          </div>
          <div class="icon position-absolute top-0 end-0 pt-2 pe-3 opacity-25">
            <i class="fa-solid fa-gears fa-5x"></i>
          </div>
          <a href="${contextPath}/admin" class="small-box-footer z-1">
            <@message "more-info"/> <i class="fa-solid fa-circle-arrow-right"></i>
          </a>
        </div>
      </div>
    </#if>

    <#if !username?? || user??>
      <div class="col-sm-12 col-lg-6">
        <div class="small-box bg-warning position-relative">
          <div class="inner z-1">
            <h3><@message "profile"/></h3>
            <p>
              <#if user??>
                <@message "manage-my-profile"/>
              <#else>
                <@message "manage-your-profile"/>
              </#if>
            </p>
          </div>
          <div class="icon position-absolute top-0 end-0 pt-2 pe-3 opacity-25">
            <i class="fa-solid fa-user fa-5x"></i>
          </div>
          <a href="${contextPath}/profile" class="small-box-footer z-1">
            <@message "more-info"/> <i class="fa-solid fa-circle-arrow-right"></i>
          </a>
        </div>
      </div>
    </#if>
  </div>

</#macro>