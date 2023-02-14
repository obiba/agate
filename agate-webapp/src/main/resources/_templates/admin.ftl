<!doctype html>
<!--
  ~ Copyright (c) 2019 OBiBa. All rights reserved.
  ~
  ~ This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v3.0.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -->

<!--[if lt IE 7]>
<html class="no-js lt-ie9 lt-ie8 lt-ie7">
<![endif]-->
<!--[if IE 7]>
<html class="no-js lt-ie9 lt-ie8">
<![endif]-->
<!--[if IE 8]>
<html class="no-js lt-ie9">
<![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js">
<!--<![endif]-->
<head>
  <!-- Context path setting -->
  <#assign contextPath = "${config.contextPath}"/>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title></title>
  <meta name="description" content="">
  <meta name="viewport" content="width=device-width">
  <!-- Place favicon.ico and apple-touch-icon.png in the root directory -->
  <!-- build:css styles/main.css -->
  <link rel="stylesheet" href="${contextPath}/styles/agate.css">
  <link rel="stylesheet" href="${contextPath}/styles/famfamfam-flags.css">
  <link rel="stylesheet" href="${contextPath}/bower_components/font-awesome/css/font-awesome.css" />
  <!-- bower:css -->
  <link rel="stylesheet" href="${contextPath}/bower_components/angular-loading-bar/build/loading-bar.css" />
  <link rel="stylesheet" href="${contextPath}/bower_components/angular-ui-select/dist/select.css" />
  <link rel="stylesheet" href="${contextPath}/bower_components/nvd3/build/nv.d3.css" />
  <link rel="stylesheet" href="${contextPath}/bower_components/ng-obiba/dist/css/ng-obiba.css" />
  <!-- endbower -->
  <!-- endbuild -->
  <link rel="stylesheet" href="${contextPath}/ws/config/style.css">
</head>
<body id="admin-page" ng-app="agate" ng-controller="MainController">
<!--[if lt IE 10]>
<p class="browsehappy">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade
  your browser</a> to improve your experience.</p>
<![endif]-->

<div class="navbar navbar-default navbar-fixed-top"  role="navigation">
  <div class="container">
    <div class="navbar-header">
      <a href="${contextPath}" class="navbar-brand">{{agateConfig.name}}</a>
      <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
        <span class="sr-only">Toggle navigation</span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
        <span class="icon-bar"></span>
      </button>
    </div>
    <div class="navbar-collapse collapse" id="navbar-main" ng-switch="authenticated">
      <ul class="nav navbar-nav"  ng-if="hasRole('agate-administrator')">
        <li>
          <a href="#/users">
            <span translate>global.users</span>
          </a>
        </li>
        <li>
          <a href="#/groups">
            <span translate>global.groups</span>
          </a>
        </li>
        <li>
          <a href="#/applications">
            <span translate>global.applications</span>
          </a>
        </li>
        <li>
          <a href="#/tickets">
            <span translate>global.tickets</span>
          </a>
        </li>
      </ul>

      <ul class="nav navbar-nav navbar-right" ng-if="authenticated">
        <li ng-if="hasRole('agate-administrator')">
          <a href="#/admin">
            <span translate>global.menu.admin</span>
          </a>
        </li>
        <li>
          <a href="http://agatedoc.obiba.org" target="_blank">
            <span translate>help</span>
          </a>
        </li>
        <li class="dropdown">
          <a href="" class="dropdown-toggle" data-toggle="dropdown">
            <i class="fa fa-user"></i>
            {{subject.login}}
            <i class="fa fa-caret-down"></i></a>
          <ul class="dropdown-menu pull-right" ng-controller="LanguageController">
            <li ng-if="hasProfile"><a href="#/profile"><i class="fa fa-cog"></i> <span translate>global.menu.myProfile</span></a></li>
            <li class="divider" ng-if="hasProfile"></li>
            <li ng-repeat="lang in languages">
              <a href ng-click="changeLanguage(lang)">
                <span>{{'language.' + lang | translate}}</span> <i class="fa fa-check" aria-hidden="true" ng-show="getCurrentLanguage() === lang"></i></a>
            </li>
            <li class="divider"></li>
            <li><a href="#/logout"><i class="fa fa-sign-out"></i> <span translate>global.menu.logout</span></a></li>
          </ul>
        </li>
      </ul>

    </div>
  </div>
</div>

<div class="container">
  <div class="page-header" ng-switch="authenticated">
  </div>

  <obiba-alert id="Application"></obiba-alert>

  <div class="alert-growl-container">
    <obiba-alert id="ApplicationGrowl"></obiba-alert>
  </div>

  <div ng-controller="NotificationController"></div>

  <div ng-view=""></div>

  <footer ng-if="authenticated">
    <div class="row">
      <div class="col-lg-12">
        <ul class="list-unstyled list-inline">
          <li class="pull-right">{{agateConfig.version}}</li>
          <li>© 2023</li>
          <li><a href="http://obiba.org" target="_blank"> OBiBa </a></li>
          <li><a href="http://agatedoc.obiba.org" target="_blank">
              Documentation </a></li>
          <li><a href="https://github.com/obiba/agate" target="_blank"> Sources
            </a></li>
        </ul>
      </div>
    </div>
  </footer>
</div>

<div id="recaptcha"></div>

<!-- Global js variables -->
<script>
  const contextPath = '${contextPath}';
</script>


<!-- build:js scripts/scripts.js -->
<!-- bower:js -->
<script src="${contextPath}/bower_components/modernizr/modernizr.js"></script>
<script src="${contextPath}/bower_components/jquery/dist/jquery.js"></script>
<script src="${contextPath}/bower_components/angular/angular.js"></script>
<script src="${contextPath}/bower_components/angular-animate/angular-animate.js"></script>
<script src="${contextPath}/bower_components/angular-bootstrap/ui-bootstrap-tpls.js"></script>
<script src="${contextPath}/bower_components/chosen/chosen.jquery.js"></script>
<script src="${contextPath}/bower_components/angular-chosen-localytics/dist/angular-chosen.js"></script>
<script src="${contextPath}/bower_components/angular-cookies/angular-cookies.js"></script>
<script src="${contextPath}/bower_components/angular-dynamic-locale/src/tmhDynamicLocale.js"></script>
<script src="${contextPath}/bower_components/angular-loading-bar/build/loading-bar.js"></script>
<script src="${contextPath}/bower_components/angular-resource/angular-resource.js"></script>
<script src="${contextPath}/bower_components/angular-route/angular-route.js"></script>
<script src="${contextPath}/bower_components/angular-sanitize/angular-sanitize.js"></script>
<script src="${contextPath}/bower_components/angular-translate/angular-translate.js"></script>
<script src="${contextPath}/bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.js"></script>
<script src="${contextPath}/bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.js"></script>
<script src="${contextPath}/bower_components/angular-ui-select/dist/select.js"></script>
<script src="${contextPath}/bower_components/angular-ui/build/angular-ui.min.js"></script>
<script src="${contextPath}/bower_components/bootstrap/dist/js/bootstrap.min.js"></script>
<script src="${contextPath}/bower_components/jquery-ui/jquery-ui.js"></script>
<script src="${contextPath}/bower_components/json3/lib/json3.js"></script>
<script src="${contextPath}/bower_components/moment/moment.js"></script>
<script src="${contextPath}/bower_components/moment/min/locales.js"></script>
<script src="${contextPath}/bower_components/angular-moment/angular-moment.js"></script>
<script src="${contextPath}/bower_components/marked/lib/marked.js"></script>
<script src="${contextPath}/bower_components/angular-marked/dist/angular-marked.js"></script>
<script src="${contextPath}/bower_components/d3/d3.js"></script>
<script src="${contextPath}/bower_components/nvd3/build/nv.d3.js"></script>
<script src="${contextPath}/bower_components/angular-nvd3/dist/angular-nvd3.js"></script>
<script src="${contextPath}/bower_components/ng-obiba/dist/ng-obiba.js"></script>
<script src="${contextPath}/bower_components/angular-utils-pagination/dirPagination.js"></script>
<script src="${contextPath}/bower_components/tv4/tv4.js"></script>
<script src="${contextPath}/bower_components/objectpath/lib/ObjectPath.js"></script>
<script src="${contextPath}/bower_components/angular-schema-form/dist/schema-form.js"></script>
<script src="${contextPath}/bower_components/angular-schema-form/dist/bootstrap-decorator.js"></script>
<script src="${contextPath}/bower_components/angular-schema-form-bootstrap/bootstrap-decorator.min.js"></script>
<script src="${contextPath}/bower_components/ace-builds/src-noconflict/ace.js"></script>
<script src="${contextPath}/bower_components/angular-ui-ace/ui-ace.js"></script>
<script src="${contextPath}/bower_components/angular-recaptcha/release/angular-recaptcha.js"></script>
<script src="${contextPath}/bower_components/angular-media-queries/match-media.js"></script>
<script src="${contextPath}/bower_components/es6-shim/es6-shim.js"></script>
<script src="${contextPath}/bower_components/obiba-shims/dist/obiba-shims.min.js"></script>
<script src="${contextPath}/bower_components/sf-obiba-ui-select/dist/sf-obiba-ui-select.js"></script>
<script src="${contextPath}/bower_components/sf-localized-string/dist/sf-localized-string.js"></script>
<!-- endbower -->
<script src="${contextPath}/app/http-auth-interceptor.js"></script>
<script src="${contextPath}/app/app.js"></script>
<script src="${contextPath}/app/controllers.js"></script>
<script src="${contextPath}/app/services.js"></script>
<script src="${contextPath}/app/services.js"></script>
<script src="${contextPath}/app/directives.js"></script>
<script src="${contextPath}/app/admin/admin.js"></script>
<script src="${contextPath}/app/admin/admin-router.js"></script>
<script src="${contextPath}/app/admin/admin-controller.js"></script>
<script src="${contextPath}/app/config/config.js"></script>
<script src="${contextPath}/app/config/config-router.js"></script>
<script src="${contextPath}/app/config/config-controller.js"></script>
<script src="${contextPath}/app/config/config-service.js"></script>
<script src="${contextPath}/app/commons/localized/localized.js"></script>
<script src="${contextPath}/app/commons/localized/localized-service.js"></script>
<script src="${contextPath}/app/commons/localized/localized-directives.js"></script>
<script src="${contextPath}/app/commons/password-modal/password-modal.js"></script>
<script src="${contextPath}/app/commons/password-modal/password-modal-directive.js"></script>
<script src="${contextPath}/app/commons/password-modal/password-modal-controller.js"></script>
<script src="${contextPath}/app/commons/users-summaries/component.js"></script>

<script src="${contextPath}/app/realm/realm.js"></script>
<script src="${contextPath}/app/realm/services/realm-service.js"></script>
<script src="${contextPath}/app/realm/services/realm-transformer.js"></script>
<script src="${contextPath}/app/realm/rest/realm-config-form-resource.js"></script>
<script src="${contextPath}/app/realm/rest/realms-config-resource.js"></script>
<script src="${contextPath}/app/realm/rest/realm-config-resource.js"></script>
<script src="${contextPath}/app/realm/rest/realms-resource.js"></script>
<script src="${contextPath}/app/realm/components/realms-list/component.js"></script>
<script src="${contextPath}/app/realm/components/realm-view/component.js"></script>
<script src="${contextPath}/app/realm/components/realm-form/component.js"></script>
<script src="${contextPath}/app/realm/realm-router.js"></script>
<script src="${contextPath}/app/realm/realm-controller.js"></script>

<script src="${contextPath}/app/application/application.js"></script>
<script src="${contextPath}/app/application/application-router.js"></script>
<script src="${contextPath}/app/application/application-controller.js"></script>
<script src="${contextPath}/app/application/application-service.js"></script>

<script src="${contextPath}/app/user/user.js"></script>
<script src="${contextPath}/app/user/user-router.js"></script>
<script src="${contextPath}/app/user/user-controller.js"></script>
<script src="${contextPath}/app/user/user-service.js"></script>
<script src="${contextPath}/app/user/attributes/attributes-directive.js"></script>
<script src="${contextPath}/app/user/attributes/attributes-filter.js"></script>
<script src="${contextPath}/app/user/attributes/attributes-controller.js"></script>
<script src="${contextPath}/app/user/attributes/attributes-service.js"></script>

<script src="${contextPath}/app/group/group.js"></script>
<script src="${contextPath}/app/group/group-router.js"></script>
<script src="${contextPath}/app/group/group-controller.js"></script>
<script src="${contextPath}/app/group/group-service.js"></script>

<script src="${contextPath}/app/ticket/ticket.js"></script>
<script src="${contextPath}/app/ticket/ticket-router.js"></script>
<script src="${contextPath}/app/ticket/ticket-controller.js"></script>
<script src="${contextPath}/app/ticket/ticket-service.js"></script>
<!-- endbuild -->
</body>
</html>
