agate.passwordModal

  .controller('PasswordModalController', ['$scope', '$modalInstance', '$modal', 'Password', 'FormServerValidation', 'userId',
    function ($scope, $modalInstance, $modal, Password, FormServerValidation, userId) {
      $scope.userId = userId;
      $scope.status = null;
      $scope.status_codes = {
        NO_MACTH: -1,
        ERROR: -2,
        SUCCESS: 1
      };

      $scope.profile = {
        password: null,
        condfirmPassword: null
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };

      $scope.save = function() {
        if ($scope.profile.password !== $scope.profile.confirmPassword) {
          $scope.status = $scope.status_codes.NO_MACTH;
        } else {
          Password.put($scope.userId, {password: $scope.profile.password})
            .success(function() {
              $scope.status = $scope.status_codes.SUCCESS;
              $modalInstance.close();
            })
            .error(function(response) {
              $scope.status = $scope.status_codes.ERROR;
              FormServerValidation.error(response, $scope.form);
            });
        }
      };

    }])

  .controller('UpdatePasswordButtonController', ['$scope', '$modal',
    function ($scope, $modal) {
      $scope.updatePassword = function() {
        $modal
          .open({
            templateUrl: 'app/commons/password-modal/password-modal-template.html',
            controller: 'PasswordModalController',
            resolve: {
              userId: function () {
                return $scope.userId;
              }
            }
          })
          .result.then(function () {
            if ($scope.updated) $scope.updated();
          }, function () {
            if ($scope.closed) $scope.closed();
          });
      }


    }]);
