<!DOCTYPE html>
<html lang="${.lang}">
<head>
  <#include "libs/head.ftl">
  <title>${config.name!"Agate"} | <@message "profile"/></title>
</head>
<body id="profile-page" class="hold-transition layout-top-nav layout-navbar-fixed">
<div class="wrapper">

  <!-- Navbar -->
  <#include "libs/top-navbar.ftl">
  <!-- /.navbar -->

  <!-- Content Wrapper. Contains page content -->
  <div class="content-wrapper">

    <!-- Content Header (Page header) -->
    <div class="content-header bg-info mb-4">
      <div class="container">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">${user.firstName!""} ${user.lastName!""}</h1>
          </div><!-- /.col -->
          <div class="col-sm-6">

          </div><!-- /.col -->
        </div><!-- /.row -->
      </div><!-- /.container-fluid -->
    </div>
    <!-- /.content-header -->

    <!-- Main content -->
    <div class="content">
      <div class="container">
        <div class="row">
          <div class="col-sm-12 col-lg-6">
            <div class="card card-primary card-outline">
              <div class="card-header">
                <h3 class="card-title"><@message "attributes"/></h3>
              </div>
              <div class="card-body">
                <dl class="row">
                  <dt class="col-sm-4"><@message "firstname"/></dt>
                  <dd class="col-sm-8">${user.firstName!""}</dd>
                  <dt class="col-sm-4"><@message "lastname"/></dt>
                  <dd class="col-sm-8">${user.lastName!""}</dd>
                  <dt class="col-sm-4"><@message "username"/></dt>
                  <dd class="col-sm-8 text-monospace">${user.name}</dd>
                  <dt class="col-sm-4"><@message "email"/></dt>
                  <dd class="col-sm-8">${user.email}</dd>
                  <dt class="col-sm-4"><@message "preferred-language"/></dt>
                  <dd class="col-sm-8"><@message "language." + user.preferredLanguage/></dd>
                  <dt class="col-sm-4"><@message "role"/></dt>
                  <dd class="col-sm-8"><span class="badge badge-secondary">${user.role}</span></dd>
                  <#if user.groups??>
                    <dt class="col-sm-4"><@message "groups"/></dt>
                    <dd class="col-sm-8">
                      <#list user.groups as group>
                        <span class="badge badge-info">${group}</span>
                      </#list>
                    </dd>
                  </#if>
                  <#if applications??>
                    <dt class="col-sm-4"><@message "applications"/></dt>
                    <dd class="col-sm-8">
                      <#list applications as application>
                        <span class="badge badge-primary">${application}</span>
                      </#list>
                    </dd>
                  </#if>
                  <#if authConfig.userAttributes??>
                    <#list authConfig.userAttributes as attribute>
                      <dt class="col-sm-4">
                        <@message attribute.name/>
                      </dt>
                      <dd class="col-sm-8">
                        <#if user.attributes?? && user.attributes[attribute.name]??>
                          <#if user.attributes[attribute.name] == "true">
                            <i class="fas fa-check"></i>
                          <#else>
                            ${user.attributes[attribute.name]}
                          </#if>
                        <#elseif attribute.inputType == "checkbox">
                          <i class="fas fa-times"></i>
                        </#if>
                      </dd>
                    </#list>
                  </#if>
                </dl>
              </div>
              <div class="card-footer">
                <div class="float-right">
                  <button type="button" class="btn btn-primary" data-toggle="modal"
                          data-target="#modal-profile"><i class="fas fa-pen"></i> <@message "personal-information"/></button>
                </div>
              </div>
            </div>
          </div>
          <div class="col-sm-12 col-lg-6">
              <div class="card card-primary card-outline">
                <div class="card-header">
                  <h3 class="card-title"><@message "credentials"/></h3>
                </div>
                <div class="card-body">
                  <#if user.realm == "agate-user-realm">
                    <div id="alertPasswordMissing" class="alert alert-danger d-none">
                      <small><@message "password-missing"/></small>
                    </div>

                    <div id="alertPasswordTooShort" class="alert alert-danger d-none">
                      <small><@message "password-too-short"/></small>
                    </div>

                    <div id="alertPasswordNoMatch" class="alert alert-danger d-none">
                      <small><@message "password-no-match"/></small>
                    </div>

                    <div id="alertFailure" class="alert alert-danger d-none">
                      <small><@message "update-password-failed"/></small>
                    </div>

                    <div id="alertSuccess" class="alert alert-success d-none">
                      <small><@message "update-password-success"/></small>
                    </div>

                    <form id="password-form" method="post">
                      <div class="input-group mb-3">
                        <input name="password" type="password" class="form-control" placeholder="<@message "new-password"/>">
                        <div class="input-group-append">
                          <div class="input-group-text">
                            <span class="fas fa-lock"></span>
                          </div>
                        </div>
                      </div>
                      <div class="input-group mb-3">
                        <input name="password2" type="password" class="form-control" placeholder="<@message "repeat-new-password"/>">
                        <div class="input-group-append">
                          <div class="input-group-text">
                            <span class="fas fa-lock"></span>
                          </div>
                        </div>
                      </div>
                      <button type="submit" id="submit-password" class="d-none"></button>
                    </form>
                  <#elseif realmConfig??>
                    <#if providerUrl??>
                      <@message "user-account-at"/>
                      <a href="${providerUrl}" class="btn btn-primary" target="_blank"><i class="fas fa-user"></i> ${realmConfig.title[.lang]}</a>
                    <#else>
                      <@message "contact-system-administrator-to-change-password"/>
                    </#if>
                  <#else>
                    <@message "contact-system-administrator-to-change-password"/>
                  </#if>
                </div>
                <div class="card-footer">
                  <div class="float-right">
                    <label for="submit-password" class="btn btn-primary mb-0" style="cursor: pointer; font-weight: normal;"><@message "update"/></label>
                  </div>
                </div>
              </div>
          </div>
        </div>
      </div><!-- /.container-fluid -->
    </div>
    <!-- /.content -->
  </div>
  <!-- /.content-wrapper -->

  <div class="modal fade" id="modal-profile">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h4 class="modal-title"><@message "personal-information"/></h4>
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
        </div>
        <div class="modal-body">
          <div id="alertProfileFailure" class="alert alert-danger d-none">

          </div>

          <form id="profile-form" method="post">
            <div class="form-group mb-3">
              <label><@message "firstname"/></label>
              <input name="firstname" type="text" class="form-control" value="${user.firstName!""}">
            </div>
            <div class="form-group mb-3">
              <label><@message "lastname"/></label>
              <input name="lastname" type="text" class="form-control" value="${user.lastName!""}">
            </div>

            <#if authConfig.languages?size gt 1>
              <div class="form-group mb-3">
                <label><@message "preferred-language"/></label>
                <select class="form-control" name="locale">
                  <#list authConfig.languages as language>
                    <option value="${language}" <#if user.preferredLanguage == language>selected</#if>><@message "language." + language/></option>
                  </#list>
                </select>
              </div>
            <#else>
              <input type="hidden" name="locale" value="${authConfig.languages[0]!"en"}"/>
            </#if>

            <#list authConfig.userAttributes as attribute>
              <div class="form-group mb-3">
                <#if attribute.inputType == "checkbox">
                  <div class="form-check">
                    <input name="${attribute.name}" type="checkbox" value="true" class="form-check-input"
                           <#if user.attributes[attribute.name]??>checked</#if> id="${attribute.name}">
                    <label class="form-check-label" for="${attribute.name}"><@message attribute.name/></label>
                  </div>
                <#elseif attribute.values?size != 0>
                  <label><@message attribute.name/></label>
                  <select class="form-control" name="${attribute.name}">
                    <#list attribute.values as value>
                      <option value="${value}" <#if user.attributes[attribute.name]?? && user.attributes[attribute.name] == value>selected</#if>>
                        <@message value/>
                      </option>
                    </#list>
                  </select>
                <#else>
                  <label><@message attribute.name/></label>
                  <input name="${attribute.name}" type="${attribute.inputType}" class="form-control" value="${user.attributes[attribute.name]!""}">
                </#if>
              </div>
            </#list>
            <button type="submit" id="submit-profile" class="d-none"></button>
          </form>
        </div>
        <div class="modal-footer justify-content-between">
          <button type="button" class="btn btn-default" data-dismiss="modal"><@message "cancel"/></button>
          <label for="submit-profile" class="btn btn-primary mb-0" style="cursor: pointer; font-weight: normal;"><@message "update"/></label>
        </div>
      </div>
      <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
  </div>
  <!-- /.modal -->

  <#include "libs/footer.ftl">
</div>
<!-- ./wrapper -->

<#include "libs/scripts.ftl">
<#include "libs/profile-scripts.ftl">

</body>
</html>
