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

agate.controller('ProfileController', ['$scope', '$location', '$modal', 'Account',
  function ($scope, $location, $modal, Account) {
    $scope.settingsAccount = Account.get();
    $scope.success = null;

    $scope.onPasswordUpdated = function() {
      $scope.success = true;
    };

    $scope.cancel = function () {
      $location.path('/profile');
    };

    $scope.edit = function() {
      $modal
        .open({
          templateUrl: 'app/views/profile/profile-form-modal.html',
          controller: 'ProfileModalController',
          resolve: {
            settingsAccount: function () {
              return $.extend(true, {}, $scope.settingsAccount);
            }
          }
        })
        .result.then(function () {
          $scope.settingsAccount = Account.get();
        }, function () {
        });
    };


  }]);

agate.controller('ProfileModalController', ['$scope', '$modalInstance', '$filter', 'Account', 'settingsAccount',
  function ($scope, $modalInstance, $filter, Account, settingsAccount) {
    $scope.settingsAccount = settingsAccount;
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

      Account.save($scope.settingsAccount,
        function () {
          $scope.status = $scope.status_codes.SUCCESS;
          $modalInstance.close();
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
