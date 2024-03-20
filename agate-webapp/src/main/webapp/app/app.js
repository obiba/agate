/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

/* App Module */

var agate = angular.module('agate', [
  'obibaShims',
  'angular-loading-bar',
  'http-auth-interceptor',
  'localytics.directives',
  'agate.config',
  'ngObiba',
  'agate.localized',
  'agate.admin',
  'agate.application',
  'agate.realm',
  'agate.ticket',
  'agate.user',
  'agate.group',
  'agate.passwordModal',
  'ngAnimate',
  'ngCookies',
  'ngResource',
  'ngRoute',
  'pascalprecht.translate',
  'tmh.dynamicLocale',
  'ui.bootstrap',
  'angularUtils.directives.dirPagination',
  'schemaForm',
  'sfObibaUiSelect',
  'sfLocalizedString',
  'obiba.utils',
  'vcRecaptcha',
  'matchMedia'
]);

agate
  .config(['$routeProvider',
    '$httpProvider',
    '$translateProvider',
    '$locationProvider',
    'tmhDynamicLocaleProvider',
    'USER_ROLES',
    'paginationTemplateProvider',
    'AlertBuilderProvider',
    function ($routeProvider,
              $httpProvider,
              $translateProvider,
              $locationProvider,
              tmhDynamicLocaleProvider,
              USER_ROLES,
              paginationTemplateProvider,
              AlertBuilderProvider) {

      $locationProvider.hashPrefix('');

      AlertBuilderProvider.setMsgKey('global.server-error');
      AlertBuilderProvider.setAlertId('Application');
      AlertBuilderProvider.setGrowlId('MainControllerGrowl');
      AlertBuilderProvider.setModeAlert();

      $routeProvider
        .when('/login', {
          templateUrl: 'app/views/login.html',
          controller: 'LoginController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/error', {
          templateUrl: 'app/views/error.html',
          controller: 'ErrorController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/authorize', {
          templateUrl: 'app/views/oauth-authorize.html',
          controller: 'OAuthController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/profile', {
          templateUrl: 'app/views/profile/profile-view.html',
          controller: 'ProfileController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/logout', {
          templateUrl: 'app/views/main.html',
          controller: 'LogoutController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .otherwise({
          templateUrl: 'app/views/main.html',
          controller: 'MainController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        });

      // Initialize angular-translate
      $translateProvider
        .useStaticFilesLoader({
          prefix: contextPath + '/ws/config/i18n/',
          suffix: '.json'
        })
        .registerAvailableLanguageKeys(['en', 'fr'], {
          'en_*': 'en',
          'fr_*': 'fr',
          '*': 'en'
        })
        .determinePreferredLanguage()
        .fallbackLanguage('en')
        .useCookieStorage()
        .useSanitizeValueStrategy('escaped');

      paginationTemplateProvider.setPath('app/views/pagination-template.html');
      tmhDynamicLocaleProvider.localeLocationPattern('bower_components/angular-i18n/angular-locale_{{locale}}.js');
      tmhDynamicLocaleProvider.useCookieStorage('NG_TRANSLATE_LANG_KEY');
    }])

  .run(['$rootScope',
    '$location',
    '$http',
    '$route',
    'AuthenticationSharedService',
    'Session',
    'USER_ROLES',
    'ServerErrorUtils',
    'amMoment',
    '$cookies',
    function ($rootScope,
      $location,
      $http,
      $route,
      AuthenticationSharedService,
      Session,
      USER_ROLES,
      ServerErrorUtils,
      amMoment,
      $cookies) {

      var langKey = $cookies.get('NG_TRANSLATE_LANG_KEY');
      amMoment.changeLocale(langKey ? langKey.replace(/"/g, '') : 'en');

      var isSessionInitialized = false;

      $rootScope.$on('$routeChangeStart', function (event, next) {
        if(!isSessionInitialized) {
          event.preventDefault();
          AuthenticationSharedService.isSessionInitialized().then(function() {
            $route.reload();
          });
        } else {
          $rootScope.authenticated = AuthenticationSharedService.isAuthenticated();
          $rootScope.hasRole = AuthenticationSharedService.isAuthorized;
          $rootScope.hasProfile = AuthenticationSharedService.hasProfile();
          $rootScope.canChangePassword = AuthenticationSharedService.canChangePassword();
          $rootScope.userRoles = USER_ROLES;
          $rootScope.subject = Session;

          if (!$rootScope.authenticated) {
            Session.destroy();
            var path = $location.path();
            var invalidRedirectPaths = ['', '/error', '/logout', '/login', '/confirm', '/forgotten', '/join', '/reset_password'];
            if (invalidRedirectPaths.indexOf(path) === -1) {
              // save path to navigate to after login
              var search = $location.search();
              search.redirect = path;
              $location.search(search);
            }
            if (['/confirm', '/forgotten', '/join', '/reset_password', '/error'].indexOf(path) === -1) {
              $rootScope.$broadcast('event:auth-loginRequired');
            }
          } else if (!AuthenticationSharedService.isAuthorized(next.access ? next.access.authorizedRoles : '*')) {
            $rootScope.$broadcast('event:auth-notAuthorized');
          }
        }
      });

      // Call when the the client is confirmed
      $rootScope.$on('event:auth-loginConfirmed', function () {
        if ($location.path() === '/login') {
          var path = '/';
          var search = $location.search();
          if (search.hasOwnProperty('redirect')) {
            path = search.redirect;
            delete search.redirect;
          }
          $location.path(path).search(search).replace();
        }
      });

      // Call when the 401 response is returned by the server
      $rootScope.$on('event:auth-loginRequired', function () {
        Session.destroy();
        $window.location.href = '../signin'
      });

      // Call when the 403 response is returned by the server
      $rootScope.$on('event:auth-notAuthorized', function () {
        if (!$rootScope.authenticated) {
          $window.location.href = '../signin'
        } else {
          $rootScope.errorMessage = 'errors.403';
          $location.path('/error').replace();
        }
      });

      $rootScope.$on('event:unhandled-server-error', function (event, response) {
        $rootScope.errorMessage = ServerErrorUtils.buildMessage(response);
        $location.path('/error').replace();
      });

      // Call when the user logs out
      $rootScope.$on('event:auth-loginCancelled', function () {
        $rootScope.authenticated = undefined;
        $window.location.href = '../signin'
      });

      AuthenticationSharedService.initSession().finally(function() {
        isSessionInitialized = true;
      });
    }]);
