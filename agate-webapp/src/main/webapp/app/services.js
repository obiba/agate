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

agate.constant('USER_ROLES', {
  all: '*',
  admin: 'agate-administrator',
  user: 'agate-user'
});

/* Services */

agate.factory('CurrentSession', ['$resource',
  function ($resource) {
    return $resource(contextPath + '/ws/auth/session/_current');
  }]);

agate.factory('Account', ['$resource',
  function ($resource) {
    return $resource(contextPath + '/ws/user/_current', {}, {
      'save': { method: 'PUT', params: { id: '@id' }, errorHandler: true }
    });
  }]);

agate.factory('AccountAuthorizations', ['$resource',
  function ($resource) {
    return $resource(contextPath + '/ws/user/_current/authorizations', {}, {
      'get': { method: 'GET' }
    });
  }]);

agate.factory('OidcProvidersResource', ['$resource',
  function ($resource) {
    return $resource(contextPath + '/ws/auth/providers', {locale: '@locale'}, {
      'get': { method: 'GET', errorHandler: true, isArray: true }
    });
  }]);

agate.factory('AccountAuthorization', ['$resource',
  function ($resource) {
    return $resource(contextPath + '/ws/user/_current/authorization/:authz', {}, {
      'get': { method: 'GET', params: { authz: '@authz' } },
      'delete': { method: 'DELETE', params: { authz: '@authz' }, errorHandler: true }
    });
  }]);

agate.factory('OAuthAuthorize', ['$http',
  function ($http) {
    return {
      post: function (data) {
        return $http.post(contextPath + '/ws/oauth2/authz', $.param(data), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
      }
    };
  }]);

agate.factory('Password', ['$resource', '$http',
  function ($resource, $http) {
    return {
      put: function (userId, data) {
        return $http.put(contextPath + '/ws/user/' + userId + '/password', $.param(data), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          errorHandler: true
        });
      }
    };
  }]);

agate.factory('ConfirmResource', ['$http',
  function ($http) {
    return {
      post: function (data) {
        return $http.post(contextPath + '/ws/users/_confirm', $.param(data), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
      }
    };
  }]);

agate.factory('PasswordResetResource', ['$http',
  function ($http) {
    return {
      post: function (data) {
        return $http.post(contextPath + '/ws/users/_reset_password', $.param(data), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
      }
    };
  }]);

agate.factory('JoinResource', ['$http',
  function ($http) {
    return {
      post: function (data) {
        return $http.post(contextPath + '/ws/users/_join', $.param(data), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
      }
    };
  }]);

agate.factory('ForgotUsernameResource', ['$http',
  function ($http) {
    return {
      post: function (data) {
        return $http.post(contextPath + '/ws/users/_forgot_username', $.param(data), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
      }
    };
  }]);

agate.factory('ForgotPasswordResource', ['$http',
  function ($http) {
    return {
      post: function (data) {
        return $http.post(contextPath + '/ws/users/_forgot_password', $.param(data), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          errorHandler: true
        });
      }
    };
  }]);

agate.factory('JoinConfigResource', ['$resource', '$translate',
  function ($resource, $translate) {
    return $resource(contextPath + '/ws/config/join', { locale: $translate.use });
  }]);

agate.factory('ClientConfig', ['$resource',
  function ($resource) {
    return $resource(contextPath + '/ws/config/client').get();
  }]);

agate.factory('Session', ['$cookieStore',
  function ($cookieStore) {
    this.create = function (login, role, realm) {
      this.login = login;
      this.role = role;
      this.realm = realm;
    };
    this.destroy = function () {
      this.login = null;
      this.role = null;
      this.realm = null;
      $cookieStore.remove('agate_subject');
      $cookieStore.remove('agatesid');
      $cookieStore.remove('obibaid');
    };
    return this;
  }]);

agate.factory('AuthenticationSharedService', ['$rootScope', '$http', '$log', '$cookies', 'authService', 'Session', 'CurrentSession',
  function ($rootScope, $http, $log, $cookies, authService, Session, CurrentSession) {
    return {
      login: function (param) {
        $rootScope.authenticationError = false;
        $rootScope.userBannedError = false;
        var data = 'username=' + param.username + '&password=' + param.password;
        $http.post(contextPath + '/ws/auth/sessions', data, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          errorHandler: true,
          ignoreAuthModule: 'ignoreAuthModule'
        }).then(function () {
          CurrentSession.get(function (data) {
            Session.create(data.username, data.role, data.realm);
            authService.loginConfirmed(data);
          });
        }, function (response) {
          var resp = response.data;
          if (resp.messageTemplate && resp.messageTemplate === 'error.userBanned') {
            $rootScope.userBannedError = true;
          }
          $rootScope.authenticationError = true;
          Session.destroy();
        });
      },
      isAuthenticated: function () {
        return Session.login !== null && Session.login !== undefined;
      },
      isAuthorized: function (authorizedRoles) {
        if (!angular.isArray(authorizedRoles)) {
          if (authorizedRoles === '*') {
            return true;
          }

          authorizedRoles = [authorizedRoles];
        }

        var isAuthorized = false;

        angular.forEach(authorizedRoles, function (authorizedRole) {
          var authorized = (!!Session.login &&
            Session.role === authorizedRole);

          if (authorized || authorizedRole === '*') {
            isAuthorized = true;
          }
        });

        return isAuthorized;
      },
      hasProfile: function () {
        return Session.realm !== 'agate-ini-realm';
      },
      canChangePassword: function () {
        return Session.realm !== 'agate-user-realm';
      },
      logout: function () {
        $rootScope.authenticationError = false;
        $rootScope.userBannedError = false;
        $http({ method: 'DELETE', url: contextPath + '/ws/auth/session/_current', errorHandler: true })
          .then(function () {
            Session.destroy();
            authService.loginCancelled(null, 'logout');
          }, function () {
            Session.destroy();
            authService.loginCancelled(null, 'logout failure');
          });
      }
    };
  }]);
