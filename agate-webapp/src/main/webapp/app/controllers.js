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

/* Controllers */

agate.controller('MainController', ['$rootScope', '$scope', '$window', '$log', '$sce', 'ConfigurationResource', 'PublicConfigurationResource', 'screenSize', 'AuthenticationSharedService', 'Account', '$translate',
  function ($rootScope, $scope, $window, $log, $sce, ConfigurationResource, PublicConfigurationResource, screenSize, AuthenticationSharedService, Account, $translate) {
    $rootScope.screen = $scope.screen = { size: null, device: null };
    var applyTitle = function (config) {
      $window.document.title = config.name;
    };
    if (AuthenticationSharedService.isAuthenticated()) {
      $scope.agateConfig = ConfigurationResource.get(applyTitle);
    } else {
      $scope.agateConfig = PublicConfigurationResource.get(applyTitle);
    }
    $rootScope.$on('event:auth-loginConfirmed', function () {
      if (AuthenticationSharedService.hasProfile()) {
        Account.get(function (user) { $translate.use(user.preferredLanguage); });
      }
      $scope.agateConfig = ConfigurationResource.get();
    });

    $rootScope.$on('$translateChangeSuccess', function () {

      $scope.currentLanguage = 'https://www.google.com/recaptcha/api.js?onload=vcRecaptchaApiLoaded&render=explicit&hl=' + $translate.use();
      loadScript($scope.currentLanguage);
    });

    function loadScript(src) {

      var script = document.createElement('script');
      script.type = 'text/javascript';
      script.src = src;
      document.getElementById('recaptcha').appendChild(script);
    }

    function getScreenSize() {
      var size = ['lg', 'md', 'sm', 'xs'].filter(function (size) {
        return screenSize.is(size);
      });

      $scope.screen.size = size ? size[0] : 'lg';
      $scope.screen.device = screenSize.is('md, lg') ? 'desktop' : 'mobile';
      $scope.screen.is = screenSize.is;

      $log.debug('Screen', $scope.screen);
    }

    getScreenSize();

    screenSize.on('lg, md, sm, xs', function () {
      getScreenSize();
    });
  }]);

agate.controller('AdminController', [function () { }]);

agate.controller('LanguageController', ['$scope', '$translate', 'amMoment', 'PublicConfigurationResource',
  function ($scope, $translate, amMoment, PublicConfigurationResource) {
    $scope.changeLanguage = function (languageKey) {
      $translate.use(languageKey);
      amMoment.changeLocale(languageKey);
    };
    $scope.getCurrentLanguage = $translate.use;

    $scope.publicMicaConfig = PublicConfigurationResource.get(function (config) {
      $scope.languages = config.languages;
    });
  }]);

agate.controller('MenuController', [function () { }]);

agate.controller('LoginController', ['$window',
  function ($window) {
    $window.location.href = '../signin'
  }]);

agate.controller('ErrorController', ['$scope', '$location', function ($scope, $location) {
  var search = $location.search();
  $scope.errorMessage = search.message;
  $scope.error = search.error;
}]);

agate.controller('CredentialsTestModalController', ['$scope', '$uibModalInstance', '$resource', 'provider', 'username',
  function ($scope, $uibModalInstance, $resource, provider, username) {
    $scope.provider = provider;
    $scope.username = username;

    $scope.cameWithUsername = username && username.length;

    $scope.cancel = function () {
      $uibModalInstance.dismiss({});
    };

    $scope.test = function () {
      $resource(contextPath + '/ws/users/_test', {}, {'test': {method: 'POST', errorHandler: true}})
      .test({provider: provider, username: $scope.username, password: $scope.password})
      .$promise.then(function (value) {
        $uibModalInstance.close({provider: provider, username: $scope.username});
      }, function (reason) {
        $uibModalInstance.dismiss({error: reason, provider: provider, username: $scope.username});
      });
    }
  }]);

agate.controller('LogoutController', ['$window',
  function ($window) {
    $window.location.href = '../signout'
  }]);

agate.controller('OAuthController', ['$log', '$scope', '$q', '$location', 'Account', 'AccountAuthorizations', 'ApplicationSummaryResource', 'OAuthAuthorize',
  function ($log, $scope, $q, $location, Account, AccountAuthorizations, ApplicationSummaryResource, OAuthAuthorize) {
    var OPENID_SCOPES = ['openid', 'profile', 'email', 'address', 'phone', 'offline_access'];
    // hide the form while we are not sure whether the scopes were already granted
    $scope.loading = true;
    $scope.authRequired = false;
    $scope.applicationAccess = false;
    $scope.auth = $location.search();
    $scope.client = ApplicationSummaryResource.get({ id: $scope.auth.client_id }, function () {
    }, function () {
      $scope.error = 'unknown-client-application';
      $scope.errorArgs = $scope.auth.client_id;
    });
    Account.get(function(user) {
      // check user has access to the app
      var apps = [];
      if (user.applications) {
        apps = user.applications;
      }
      if (user.groupApplications) {
        user.groupApplications.forEach(function(gapp) {
          apps.push(gapp.application);
        });
      }
      $scope.applicationAccess = apps.includes($scope.auth.client_id);
      if ($scope.applicationAccess) {
        // check if the authz was already granted
        AccountAuthorizations.query().$promise.then(function(authorizations){
          if (authorizations.length>0) {
            authorizations.forEach(function(authorization){
              if (authorization.application === $scope.auth.client_id) {
                var allScopesCovered = true;
                $scope.auth.scope.split(' ').forEach(function(sc) {
                  if (!authorization.scopes.includes(sc)) {
                    allScopesCovered = false;
                    $scope.authRequired = true;
                  }
                });
                if (allScopesCovered) {
                  document.getElementById('oauthForm').submit();
                }
              }
            });
          } else {
            $scope.authRequired = true;
          }
          $scope.loading = false;
        });
      } else {
        $scope.loading = false;
      }
    });

    $scope.scopes = $scope.auth.scope.split(' ').map(function (s) {
      var scopeParts = s.split(':');
      var appId = scopeParts[0];
      if (OPENID_SCOPES.indexOf(appId) < 0) {
        return { application: appId, name: scopeParts[1] };
      } else {
        return { application: 'openid', name: appId };
      }
    });

    var applications = $scope.scopes.reduce(function (applications, scope) {
      var application = scope.application;

      if (applications.indexOf(application) < 0) {
        applications.push(application);
      }

      return applications;
    }, []);

    $q.all(applications.map(function (application) {
      if (OPENID_SCOPES.indexOf(application) < 0) {
        return ApplicationSummaryResource.get({ id: application }).$promise;
      } else {
        var deferred = $q.defer();
        deferred.resolve({ id: application, scopes: OPENID_SCOPES.map(function (s) { return { name: s }; }) });
        return deferred.promise;
      }
    })).then(function (applications) {
      var res = $scope.scopes.map(function (scope) {
        var application, found;
        application = applications.filter(function (application) { return application.id === scope.application; })[0];
        found = application && application.scopes ? application.scopes.filter(function (s) { return s.name === scope.name; })[0] : {};

        if (!found && scope.name) {
          scope.isMissing = true;
        } else {
          scope.description = found ? found.description : null;
        }

        return scope;
      });

      var missingScopes = res.filter(function (scope) { return scope.isMissing; });

      if (missingScopes.length > 0) {
        $scope.error = 'unknown-resource-scope';
        $scope.errorArgs = missingScopes.map(function (s) { return s.application + ':' + s.name; }).join(', ');
      }

      $scope.applicationScopes = applications.map(function (application) {
        var scopes = res.filter(function (scope) { return scope.application === application.id; });

        return { application: application, scopes: scopes };
      });
    }).catch(function (e) {
      $log.error(e);
      $scope.error = 'unknown-resource-application';
      $scope.errorArgs = applications.join(', ');
    });
  }]);

agate.controller('ProfileController',
  ['$window',
  function ($window) {
    $window.location.href = '../profile';
  }]);
