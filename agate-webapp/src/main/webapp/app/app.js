'use strict';

/* App Module */

var agate = angular.module('agate', [
  'angular-loading-bar',
  'http-auth-interceptor',
  'localytics.directives',
  'agate.config',
  'ngObiba',
  'agate.application',
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
  'obiba.utils',
  'vcRecaptcha'
]);

agate
  .config(['$routeProvider', '$httpProvider', '$translateProvider', 'tmhDynamicLocaleProvider', 'USER_ROLES', 'paginationTemplateProvider',
    function ($routeProvider, $httpProvider, $translateProvider, tmhDynamicLocaleProvider, USER_ROLES, paginationTemplateProvider) {
      $routeProvider
        .when('/login', {
          templateUrl: 'app/views/login.html',
          controller: 'LoginController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/join', {
          templateUrl: 'app/views/join.html',
          controller: 'JoinController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/error', {
          templateUrl: 'app/views/error.html',
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
        .when('/profile/edit', {
          templateUrl: 'app/views/profile/profile-form-modal.html',
          controller: 'ProfileController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/confirm', {
          templateUrl: 'app/views/public_password.html',
          controller: 'ResetPasswordController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/reset_password', {
          templateUrl: 'app/views/public_password.html',
          controller: 'ResetPasswordController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/forgotten', {
          templateUrl: 'app/views/forgotten.html',
          controller: 'ForgotLoginDetailsController',
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
          prefix: 'i18n/',
          suffix: '.json'
        })
        .preferredLanguage('en')
        .fallbackLanguage('en')
        .useCookieStorage()
        .useSanitizeValueStrategy('escaped');

      paginationTemplateProvider.setPath('app/views/pagination-template.html');
      tmhDynamicLocaleProvider.localeLocationPattern('bower_components/angular-i18n/angular-locale_{{locale}}.js');
      tmhDynamicLocaleProvider.useCookieStorage('NG_TRANSLATE_LANG_KEY');
    }])

  // Workaround for bug #1404
  // https://github.com/angular/angular.js/issues/1404
  // Source: http://plnkr.co/edit/hSMzWC?p=preview
  .config(['$provide', function ($provide) {
    $provide.decorator('ngModelDirective', ['$delegate', function ($delegate) {
      var ngModel = $delegate[0], controller = ngModel.controller;
      ngModel.controller = ['$scope', '$element', '$attrs', '$injector', function (scope, element, attrs, $injector) {
        var $interpolate = $injector.get('$interpolate');
        attrs.$set('name', $interpolate(attrs.name || '')(scope));
        $injector.invoke(controller, this, {
          '$scope': scope,
          '$element': element,
          '$attrs': attrs
        });
      }];
      return $delegate;
    }]);
    $provide.decorator('formDirective', ['$delegate', function ($delegate) {
      var form = $delegate[0], controller = form.controller;
      form.controller = ['$scope', '$element', '$attrs', '$injector', function (scope, element, attrs, $injector) {
        var $interpolate = $injector.get('$interpolate');
        attrs.$set('name', $interpolate(attrs.name || attrs.ngForm || '')(scope));
        $injector.invoke(controller, this, {
          '$scope': scope,
          '$element': element,
          '$attrs': attrs
        });
      }];
      return $delegate;
    }]);
  }])

  .run(['$rootScope', '$location', '$http', 'AuthenticationSharedService', 'Session', 'USER_ROLES', 'ServerErrorUtils',
    function ($rootScope, $location, $http, AuthenticationSharedService, Session, USER_ROLES, ServerErrorUtils) {
      $rootScope.$on('$routeChangeStart', function (event, next) {
        $rootScope.authenticated = AuthenticationSharedService.isAuthenticated();
        $rootScope.hasRole = AuthenticationSharedService.isAuthorized;
        $rootScope.hasProfile = AuthenticationSharedService.hasProfile();
        $rootScope.canChangePassword = AuthenticationSharedService.canChangePassword();
        $rootScope.userRoles = USER_ROLES;
        $rootScope.subject = Session;

        if (!$rootScope.authenticated) {
          $rootScope.$broadcast('event:auth-loginRequired');
        } else if (!AuthenticationSharedService.isAuthorized(next.access ? next.access.authorizedRoles : '*')) {
          $rootScope.$broadcast('event:auth-notAuthorized');
        }
      });

      // Call when the the client is confirmed
      $rootScope.$on('event:auth-loginConfirmed', function () {
        if ($location.path() === '/login') {
          $location.path('/').replace();
        }
      });

      // Call when the 401 response is returned by the server
      $rootScope.$on('event:auth-loginRequired', function () {
        Session.destroy();
        if ($location.path() !== '/' && $location.path() !== '') {
          $location.path('/login').replace();
        }
      });

      // Call when the 403 response is returned by the server
      $rootScope.$on('event:auth-notAuthorized', function () {
        if (!$rootScope.authenticated) {
          $location.path('/login').replace();
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
        $location.path('/login');
      });
    }]);
