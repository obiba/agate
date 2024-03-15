<#macro homeModel>

  <#if user?? && user.realm == "agate-user-realm" && !user.otpEnabled>
    <div class="alert alert-info">
      <h5><i class="icon fas fa-lock"></i> <@message "security-info"/></h5>
      <@message "2fa-info"/>
      <a href="${contextPath}/profile">
        <@message "2fa-enable"/>
      </a>
    </div>
  </#if>

  <#if realm?? && realm == "agate-ini-realm" && !otpEnabled>
    <div class="alert alert-info">
      <h5><i class="icon fas fa-lock"></i> <@message "security-info"/></h5>
      <@message "2fa-info"/>
      <a href="${contextPath}/profile">
        <@message "2fa-enable"/>
      </a>
    </div>
  </#if>

  <div class="row">

    <#if !user?? || user.role == "agate-administrator">
      <div class="col-sm-12 col-lg-6">
        <div class="small-box bg-info">
          <div class="inner">
            <h3><@message "administration"/></h3>
            <p><@message "admin-users-apps"/></p>
          </div>
          <div class="icon">
            <i class="fas fa-cogs"></i>
          </div>
          <a href="${contextPath}/admin" class="small-box-footer">
            <@message "more-info"/> <i class="fas fa-arrow-circle-right"></i>
          </a>
        </div>
      </div>
    </#if>

    <#if !username?? || user??>
      <div class="col-sm-12 col-lg-6">
        <div class="small-box bg-warning">
          <div class="inner">
            <h3><@message "profile"/></h3>
            <p>
              <#if user??>
                <@message "manage-my-profile"/>
              <#else>
                <@message "manage-your-profile"/>
              </#if>
            </p>
          </div>
          <div class="icon">
            <i class="fas fa-user"></i>
          </div>
          <a href="${contextPath}/profile" class="small-box-footer">
            <@message "more-info"/> <i class="fas fa-arrow-circle-right"></i>
          </a>
        </div>
      </div>
    </#if>
  </div>

</#macro>