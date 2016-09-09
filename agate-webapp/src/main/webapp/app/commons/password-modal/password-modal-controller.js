/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.passwordModal

  .controller('PasswordModalController', ['$scope', '$uibModalInstance', '$uibModal', 'Password', 'FormServerValidation', 'userId', 'AlertService',
    function ($scope, $uibModalInstance, $uibModal, Password, FormServerValidation, userId, AlertService) {
      $scope.userId = userId;
      $scope.profile = {
        password: null,
        condfirmPassword: null
      };

      $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
      };

      $scope.save = function() {
        if ($scope.profile.password !== $scope.profile.confirmPassword) {
          AlertService.alert({id: 'PasswordModalController', type: 'danger', msgKey: 'password.error.dontmatch'});
        } else {
          Password.put($scope.userId, {password: $scope.profile.password})
            .success(function() {
              $uibModalInstance.close();
            })
            .error(function(response) {
              AlertService.alert({id: 'PasswordModalController', type: 'danger', msgKey: 'password.error.global'});
              FormServerValidation.error(response, $scope.form);
            });
        }
      };

    }])

  .controller('UpdatePasswordButtonController', ['$scope', '$uibModal',
    function ($scope, $uibModal) {
      $scope.updatePassword = function() {
        $uibModal
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
