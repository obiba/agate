'use strict';

/* App Module */

var agate = angular.module('agate', [
  'angular-loading-bar',
  'http-auth-interceptor',
  'localytics.directives',
  'agate.config',
  'ngObiba',
  'agate.application',
  'agate.user',
  'agate.group',
  'ngAnimate',
  'ngCookies',
  'ngResource',
  'ngRoute',
  'pascalprecht.translate',
  'tmh.dynamicLocale',
  'ui.bootstrap'
]);

agate
  .config(['$routeProvider', '$httpProvider', '$translateProvider', 'tmhDynamicLocaleProvider', 'USER_ROLES',
    function ($routeProvider, $httpProvider, $translateProvider, tmhDynamicLocaleProvider, USER_ROLES) {
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
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/profile', {
          templateUrl: 'app/views/profile.html',
          controller: 'ProfileController',
          access: {
            authorizedRoles: [USER_ROLES.all]
          }
        })
        .when('/password', {
          templateUrl: 'app/views/password.html',
          controller: 'PasswordController',
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
        .useCookieStorage();

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

  .run(['$rootScope', '$location', '$http', 'AuthenticationSharedService', 'Session', 'USER_ROLES',
    function ($rootScope, $location, $http, AuthenticationSharedService, Session, USER_ROLES) {
      $rootScope.$on('$routeChangeStart', function (event, next) {
        $rootScope.authenticated = AuthenticationSharedService.isAuthenticated();
        $rootScope.hasRole = AuthenticationSharedService.isAuthorized;
        $rootScope.userRoles = USER_ROLES;
        $rootScope.subject = Session;

        var authorizedRoles = next.access.authorizedRoles;
        if (!AuthenticationSharedService.isAuthorized(authorizedRoles)) {
          event.preventDefault();
          if (AuthenticationSharedService.isAuthenticated()) {
            // user is not allowed
            $rootScope.$broadcast('event:auth-notAuthorized');
          } else {
            // user is not logged in
            $rootScope.$broadcast('event:auth-loginRequired');
          }
        } else {
          // Check if the customer is still authenticated on the server
          // Try to load a protected 1 pixel image.
//          $http({method: 'GET', url: '/protected/transparent.gif'}).
//            error(function (response) {
//              // Not authorized
//              if (response.status === 401) {
//                $rootScope.$broadcast("event:auth-notAuthorized");
//              }
//            })
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
        $rootScope.errorMessage = 'errors.403';
        $location.path('/error').replace();
      });

      // Call when the user logs out
      $rootScope.$on('event:auth-loginCancelled', function () {
        $rootScope.authenticated = undefined;
        $location.path('/login');
      });
    }]);
