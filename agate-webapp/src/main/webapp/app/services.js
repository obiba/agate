'use strict';

agate.constant('USER_ROLES', {
  all: '*',
  admin: 'agate-administrator',
  user: 'agate-user'
});

/* Services */

agate.factory('CurrentSession', ['$resource',
  function ($resource) {
    return $resource('ws/auth/session/_current');
  }]);

agate.factory('Account', ['$resource',
  function ($resource) {
    return $resource('ws/user/_current', {}, {
      'save': {method: 'PUT', params: {id: '@id'}, errorHandler: true}
    });
  }]);

agate.factory('Password', ['$resource', '$log', '$http',
  function ($resource, $log, $http) {
    return {
      put: function(data) {
        return $http.put('ws/user/_current/password', $.param(data), {
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
      }
    };
  }]);

agate.factory('ConfirmResource', ['$http',
  function ($http) {
    return {
      post: function(data) {
        return $http.post('ws/users/_confirm', $.param(data), {
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
      }
    };
  }]);

agate.factory('PasswordResetResource', ['$http',
  function ($http) {
    return {
      post: function(data) {
        return $http.post('ws/users/_reset_password', $.param(data), {
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
      }
    };
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

agate.factory('AuthenticationSharedService', ['$rootScope', '$http', '$log', '$cookieStore', 'authService', 'Session', 'CurrentSession',
  function ($rootScope, $http, $log, $cookieStore, authService, Session, CurrentSession) {
    return {
      login: function (param) {
        var data = 'username=' + param.username + '&password=' + param.password;
        $http.post('ws/auth/sessions', data, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          ignoreAuthModule: 'ignoreAuthModule'
        }).success(function () {
          CurrentSession.get(function (data) {
            Session.create(data.username, data.role, data.realm);
            $cookieStore.put('agate_subject', JSON.stringify(Session));
            authService.loginConfirmed(data);
          });
        }).error(function () {
          Session.destroy();
        });
      },
      isAuthenticated: function () {
        if (!Session.login) {
          // check if the user has a cookie
          if ($cookieStore.get('agate_subject') !== null) {
            var account;

            try {
              account = JSON.parse($cookieStore.get('agate_subject'));
            } catch (e) {
              $log.info('Invalid agate_subject cookie value. Ignoring.');
            }

            if (account) {
              Session.create(account.login, account.role, account.realm);
              $rootScope.account = Session;
            }
          }
        }
        return !!Session.login;
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
        $http({method: 'DELETE', url: 'ws/auth/session/_current', errorHandler: true})
          .success(function () {
            Session.destroy();
            authService.loginCancelled(null, 'logout');
          }).error(function () {
            Session.destroy();
            authService.loginCancelled(null, 'logout failure');
          }
        );
      }
    };
  }]);
