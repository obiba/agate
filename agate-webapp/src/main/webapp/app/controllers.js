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

agate.controller('ProfileController', ['$scope', 'Account',
  function ($scope, Account) {
    $scope.success = null;
    $scope.error = null;
    $scope.settingsAccount = Account.get();

    $scope.save = function () {
      Account.save($scope.settingsAccount,
        function () {
          $scope.error = null;
          $scope.success = 'OK';
          $scope.settingsAccount = Account.get();
        },
        function () {
          $scope.success = null;
          $scope.error = 'ERROR';
        });
    };
  }]);

agate.controller('PasswordController', ['$scope', 'Password',
  function ($scope, Password) {
    $scope.success = null;
    $scope.error = null;
    $scope.doNotMatch = null;
    $scope.changePassword = function () {
      if ($scope.password !== $scope.confirmPassword) {
        $scope.doNotMatch = 'ERROR';
      } else {
        $scope.doNotMatch = null;
        Password.save($scope.password,
          function () {
            $scope.error = null;
            $scope.success = 'OK';
          },
          function () {
            $scope.success = null;
            $scope.error = 'ERROR';
          });
      }
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
