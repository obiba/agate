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

      var script = document.createElement("script");
      script.type = "text/javascript";
      script.src = src;
      document.getElementById("recaptcha").appendChild(script);
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

agate.controller('LoginController', ['$scope', '$location', '$translate', 'AuthenticationSharedService', 'OidcProvidersResource',
  function ($scope, $location, $translate, AuthenticationSharedService, OidcProvidersResource) {
    OidcProvidersResource.get({locale: $translate.use()}).$promise.then(function(providers) {
      $scope.providers = providers;
    });

    $scope.login = function () {
      AuthenticationSharedService.login({
        username: $scope.username,
        password: $scope.password,
        success: function () {
          $location.path('');
        }
      });
    };
  }]);

agate.controller('JoinController', ['$rootScope', '$scope', '$location', '$translate', 'JoinConfigResource', 'JoinResource', 'ClientConfig',
  'NOTIFICATION_EVENTS', 'ServerErrorUtils', 'AlertService', 'vcRecaptchaService',
  function ($rootScope, $scope, $location, $translate, JoinConfigResource, JoinResource, ClientConfig, NOTIFICATION_EVENTS, ServerErrorUtils, AlertService, vcRecaptchaService) {

    $scope.joinConfig = JoinConfigResource.get();
    $scope.model = {};
    $scope.response = null;
    $scope.widgetId = null;
    $scope.config = ClientConfig;

    $scope.setResponse = function (response) {
      $scope.response = response;
    };

    $scope.setWidgetId = function (widgetId) {
      $scope.widgetId = widgetId;
    };

    $scope.onSubmit = function (form) {
      // First we broadcast an event so all fields validate themselves
      $scope.$broadcast('schemaFormValidate');

      if (!$scope.response) {
        AlertService.alert({ id: 'JoinController', type: 'danger', msgKey: 'missing-reCaptcha-error' });
        return;
      }

      if (form.$valid) {
        var model = $scope.model;
        if (!model.locale) {
          model.locale = $translate.use();
        }
        JoinResource.post(angular.extend({}, model, { reCaptchaResponse: $scope.response }))
          .then(function () {
            $location.url($location.path());
            $location.path('/');
          }, function (data) {
            $rootScope.$broadcast(NOTIFICATION_EVENTS.showNotificationDialog, {
              message: ServerErrorUtils.buildMessage(data)
            });

            vcRecaptchaService.reload($scope.widgetId);
          });
      }
    };

  }]);

agate.controller('LogoutController', ['$location', 'AuthenticationSharedService',
  function ($location, AuthenticationSharedService) {
    AuthenticationSharedService.logout({
      success: function () {
        $location.path('');
      }
    });
  }]);

agate.controller('OAuthController', ['$log', '$scope', '$q', '$location', 'AccountAuthorizations', 'ApplicationSummaryResource', 'OAuthAuthorize',
  function ($log, $scope, $q, $location, AccountAuthorizations, ApplicationSummaryResource, OAuthAuthorize) {
    var OPENID_SCOPES = ['openid', 'profile', 'email', 'address', 'phone', 'offline_access'];
    // hide the form while we are not sure whether the scopes were already granted
    $scope.visible = false;
    $scope.auth = $location.search();
    $scope.client = ApplicationSummaryResource.get({ id: $scope.auth.client_id }, function () {
    }, function () {
      $scope.error = 'unknown-client-application';
      $scope.errorArgs = $scope.auth.client_id;
    });
    AccountAuthorizations.query().$promise.then(function(authorizations){
      if (authorizations.length === 0) {
        $scope.visible = true;
      } else {
        authorizations.forEach(function(authorization){
          if (authorization.application === $scope.auth.client_id) {
            var allScopesCovered = true;
            $scope.auth.scope.split(' ').forEach(function(sc) {
              if (!authorization.scopes.includes(sc)) {
                allScopesCovered = false;
              }
            });
            if (allScopesCovered) {
              document.getElementById("oauthForm").submit();
            } else {
              $scope.visible = true;
            }
          }
        });
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

agate.controller('ProfileController', ['$scope', '$location', '$uibModal', 'Account', 'AccountAuthorizations', 'AccountAuthorization', 'ConfigurationResource', 'AttributesService', 'AlertService',
  function ($scope, $location, $uibModal, Account, AccountAuthorizations, AccountAuthorization, ConfigurationResource, AttributesService, AlertService) {
    var getConfigAttributes = function () {
      ConfigurationResource.get(function (config) {
        $scope.attributesConfig = config.userAttributes || [];
        $scope.languages = config.languages || [];
        $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.settingsAccount.attributes, $scope.attributesConfig);
        $scope.usedAttributeNames = AttributesService.getUsedAttributeNames($scope.settingsAccount.attributes, $scope.attributesConfig);
      });
    };

    var getSettingsAccount = function () {
      $scope.settingsAccount = Account.get(function (user) {
        $scope.authorizations = AccountAuthorizations.query();
        ConfigurationResource.get(function (config) {
          $scope.userConfigAttributes = AttributesService.findConfigAttributes(user.attributes, config.userAttributes);
          $scope.userNonConfigAttributes = config.userAttributes ? AttributesService.findNonConfigAttributes(user.attributes, config.userAttributes) : user.attributes;
          $scope.usedAttributeNames = AttributesService.getUsedAttributeNames($scope.settingsAccount.attributes, $scope.attributesConfig);
        });

        return user;
      });
    }

    getConfigAttributes();
    getSettingsAccount();

    $scope.deleteAuthorization = function (authz) {
      AccountAuthorization.delete({ authz: authz.id }, function () {
        $scope.authorizations = AccountAuthorizations.query();
      });
    };

    $scope.onPasswordUpdated = function () {
      AlertService.alert({ id: 'ProfileController', type: 'success', msgKey: 'profile.success.updated', delay: 5000 });
    };

    $scope.cancel = function () {
      $location.path('/profile');
    };

    $scope.edit = function () {
      var settingsAccountClone = $.extend(true, {}, $scope.settingsAccount);
      var attributeConfigPairs =
        AttributesService.getAttributeConfigPairs(settingsAccountClone.attributes, $scope.attributesConfig);


      $uibModal
        .open({
          templateUrl: 'app/views/profile/profile-form-modal.html',
          controller: 'ProfileModalController',
          resolve: {
            settingsAccount: function () {
              return settingsAccountClone;
            },
            attributeConfigPairs: function () {
              return attributeConfigPairs;
            },
            attributesConfig: function () {
              return $scope.attributesConfig;
            },
            usedAttributeNames: function () {
              return $scope.usedAttributeNames;
            },
            configLanguages: function () {
              return $scope.languages;
            }
          }
        })
        .result.then(function () {
          AlertService.alert({
            id: 'ProfileController',
            type: 'success',
            msgKey: 'profile.success.updated',
            delay: 5000
          });
          getSettingsAccount();
        }, function () {
        });
    };


  }]);

agate.controller('ProfileModalController', ['$scope', '$uibModalInstance', '$filter', '$translate', 'Account', 'settingsAccount', 'attributeConfigPairs', 'attributesConfig', 'usedAttributeNames', 'configLanguages', 'AttributesService', 'AlertService',
  function ($scope, $uibModalInstance, $filter, $translate, Account, settingsAccount, attributeConfigPairs, attributesConfig, usedAttributeNames, configLanguages, AttributesService, AlertService) {
    $scope.settingsAccount = settingsAccount;
    $scope.attributeConfigPairs = attributeConfigPairs;
    $scope.attributesConfig = attributesConfig;
    $scope.usedAttributeNames = usedAttributeNames;
    $scope.languages = configLanguages;

    $scope.requiredField = $filter('translate')('user.email');

    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };

    $scope.save = function (form) {
      if (!form.$valid) {
        form.saveAttempted = true;
        AlertService.alert({ id: 'ProfileModalController', type: 'danger', msgKey: 'fix-error' });
        return;
      }

      form.$pristine = true;
      $scope.settingsAccount.attributes =
        AttributesService.mergeConfigPairAttributes($scope.settingsAccount.attributes, $scope.attributeConfigPairs);

      Account.save($scope.settingsAccount,
        function () {
          $translate.use($scope.settingsAccount.preferredLanguage);
          $uibModalInstance.close($scope.attributeConfigPairs);
        },
        function () {
          AlertService.alert({ id: 'ProfileModalController', type: 'danger', msgKey: 'fix-error' });
        });
    };
  }]);

agate.controller('ResetPasswordController', ['$scope', '$location', 'ConfirmResource', 'PasswordResetResource',
  function ($scope, $location, ConfirmResource, PasswordResetResource) {
    var isReset = $location.path() === '/reset_password' ? true : false;

    $scope.title = isReset ? 'Reset password' : 'Confirm';
    $scope.key = $location.search().key;

    $scope.changePassword = function () {
      var resource = isReset ? PasswordResetResource : ConfirmResource;

      if ($scope.password !== $scope.confirmPassword) {
        $scope.doNotMatch = 'ERROR';
      } else {
        $scope.doNotMatch = null;
        resource.post({
          key: $scope.key,
          password: $scope.password
        })
          .then(function () {
            $location.url($location.path());
            $location.path('/');
          });
      }
    };
  }]);

agate.controller('ForgotLoginDetailsController', ['$scope', 'AlertService', 'ForgotPasswordResource',
  function ($scope, AlertService, ForgotPasswordResource) {
    $scope.username = '';

    $scope.sendRequest = function (form) {
      if (!form.$valid) {
        form.saveAttempted = true;
        AlertService.alert({ id: 'ForgotLoginDetailsController', type: 'danger', msgKey: 'fix-error', delay: 5000 });
        return;
      }

      var successHandler = function () {
        AlertService.alert({ id: 'ForgotLoginDetailsController', type: 'success', msgKey: 'forgot-login.success' });
        $scope.email = '';
        $scope.username = '';
        form.$setPristine();
      };

      var errorHandler = function () {
        AlertService.alert({ id: 'ForgotLoginDetailsController', type: 'danger', msgKey: 'error', delay: 5000 });
      };

      ForgotPasswordResource.post({ username: $scope.username }).then(successHandler, errorHandler);
    };
  }]);
