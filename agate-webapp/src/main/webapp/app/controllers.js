'use strict';

/* Controllers */

agate.controller('MainController', [ function () {} ]);

agate.controller('AdminController', [ function () {} ]);

agate.controller('LanguageController', ['$scope', '$translate',
  function ($scope, $translate) {
    $scope.changeLanguage = function (languageKey) {
      $translate.use(languageKey);
    };
  }]);

agate.controller('MenuController', [ function () {} ]);

agate.controller('LoginController', ['$scope', '$location', 'AuthenticationSharedService',
  function ($scope, $location, AuthenticationSharedService) {
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

agate.controller('LogoutController', ['$location', 'AuthenticationSharedService',
  function ($location, AuthenticationSharedService) {
    AuthenticationSharedService.logout({
      success: function () {
        $location.path('');
      }
    });
  }]);

agate.controller('ProfileController', ['$scope', '$location', '$modal', 'Account', 'ConfigurationResource', 'AttributesService',
  function ($scope, $location, $modal, Account, ConfigurationResource, AttributesService) {

    var getConfigAttributes = function() {
      ConfigurationResource.get(function(config) {
        $scope.attributesConfig = config.userAttributes || [];
        $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.settingsAccount.attributes, $scope.attributesConfig);
      });
    };

    var getSettingsAccount = function() {
      $scope.settingsAccount = Account.get(function(user) {
        ConfigurationResource.get(function(config) {
          $scope.userConfigAttributes = AttributesService.findConfigAttributes(user.attributes, config.userAttributes);
          $scope.userNonConfigAttributes = config.userAttributes ? AttributesService.findNonConfigAttributes(user.attributes, config.userAttributes) : user.attributes;
        });

        return user;
      });
    }

    getConfigAttributes();
    getSettingsAccount();

    $scope.success = null;

    $scope.onPasswordUpdated = function() {
      $scope.success = true;
    };

    $scope.cancel = function () {
      $location.path('/profile');
    };

    $scope.edit = function() {
      var settingsAccountClone = $.extend(true, {}, $scope.settingsAccount);
      var attributeConfigPairs =
        AttributesService.getAttributeConfigPairs(settingsAccountClone.attributes, $scope.attributesConfig);


      $modal
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
            }
          }
        })
        .result.then(function () {
          $scope.success = true;
          getSettingsAccount();
        }, function () {
        });
    };


  }]);

agate.controller('ProfileModalController', ['$scope', '$modalInstance', '$filter', 'Account', 'settingsAccount', 'attributeConfigPairs', 'attributesConfig', 'AttributesService',
  function ($scope, $modalInstance, $filter, Account, settingsAccount, attributeConfigPairs, attributesConfig, AttributesService) {
    $scope.settingsAccount = settingsAccount;
    $scope.attributeConfigPairs = attributeConfigPairs;
    $scope.attributesConfig = attributesConfig;
    $scope.status = null;
    $scope.status_codes = {
      ERROR: -2,
      SUCCESS: 1
    };

    $scope.requiredField = $filter('translate')('user.email');

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.save = function (form) {
      if (!form.$valid) {
        form.saveAttempted = true;
        $scope.status = $scope.status_codes.ERROR;
        return;
      }

      form.$pristine = true;
      $scope.settingsAccount.attributes =
        AttributesService.mergeConfigPairAttributes($scope.settingsAccount.attributes, $scope.attributeConfigPairs);

      Account.save($scope.settingsAccount,
        function () {
          $modalInstance.close($scope.attributeConfigPairs);
        },
        function () {
          $scope.status = $scope.status_codes.ERROR;
        });
    };
  }]);

agate.controller('ResetPasswordController', ['$scope', '$location', 'ConfirmResource', 'PasswordResetResource',
  function ($scope, $location, ConfirmResource, PasswordResetResource) {
    var isReset = $location.path() === '/reset_password' ? true: false;

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
        .success(function () {
          $location.url($location.path());
          $location.path('/');
        });
      }
    };
  }]);
