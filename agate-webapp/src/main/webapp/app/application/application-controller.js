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

agate.application

  .controller('ApplicationListController',
    ['$rootScope',
      '$scope',
      'ApplicationsResource',
      'ApplicationResource',
      'NOTIFICATION_EVENTS',
      'AlertBuilder',

    function ($rootScope,
              $scope,
              ApplicationsResource,
              ApplicationResource,
              NOTIFICATION_EVENTS,
              AlertBuilder) {
      var onSuccess = function(response) {
        $scope.applications = response;
        $scope.loading = false;
      };

      var onError = function(response) {
        $scope.loading = false;
        AlertBuilder.newBuilder().response(response).delay(0).build();
      };

      $scope.loading = true;
      ApplicationsResource.query({}, onSuccess, onError);

      $scope.deleteApplication = function (application) {
        $scope.applicationToDelete = application.id;

        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {
            titleKey: 'application.delete-dialog.title',
            messageKey:'application.delete-dialog.message',
            messageArgs: [application.name]
          }, application.id
        );
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.applicationToDelete === id) {

          ApplicationResource.delete({id: id}).$promise
            .then(function () {
              $scope.loading = true;
              ApplicationsResource.query({}, onSuccess, onError);
            })
            .catch(onError);
        }
      });

    }])

  .controller('ApplicationViewController', ['$scope', '$location', '$routeParams', 'ApplicationResource', '$uibModal','$log',

    function ($scope, $location, $routeParams, ApplicationResource, $uibModal, $log) {
      $scope.application = $routeParams.id ? ApplicationResource.get({id: $routeParams.id}) : {};

      $scope.editRealmGroup = function (rg) {
        $uibModal.open({
          templateUrl: 'app/application/views/application-realm-group-modal-form.html',
          controller: 'ApplicationRealmGroupModalController',
          resolve: {
            'realmGroup': function () {
               return rg ? angular.copy(rg) : {};
            }
          }
        }).result.then(function (realmGroup) {
          var onSuccess = function () {
            $scope.status = $scope.statusCodes.SUCCESS;
            $location.path('/application' + ($scope.application.id ? '/' + $scope.application.id : '')).replace();
          };

          var onError = function() {
            $log.debug('DEBUG', $scope, $scope.$parent);
            $scope.status = $scope.statusCodes.ERROR;
          };

          if(!$scope.application.realmGroups) {
            $scope.application.realmGroups = [];
          }

          var idx = $scope.application.realmGroups.findIndex((val) => val.realm === realmGroup.realm);
          if(idx > -1) {
            $scope.application.realmGroups[idx] = realmGroup;
          } else {
            $scope.application.realmGroups.push(realmGroup);
          }
          ApplicationResource.update({id: $scope.application.id}, $scope.application, onSuccess, onError);
        });
      };

      $scope.deleteRealmGroup = function(rg) {
        var idx = $scope.application.realmGroups.indexOf(rg);
        if(idx > -1) {
          $scope.application.realmGroups.splice(idx, 1);
          ApplicationResource.update({id: $scope.application.id}, $scope.application);
         }
      };

      $scope.editScope = function (scp) {
        $uibModal.open({
          templateUrl: 'app/application/views/application-scope-modal-form.html',
          controller: 'ApplicationScopeModalController',
          resolve: {
            'scope': function () {
              return angular.copy(scp);
            }
          }
        }).result.then(function (scope) {
            var onSuccess = function () {
              $scope.status = $scope.statusCodes.SUCCESS;
              $location.path('/application' + ($scope.application.id ? '/' + $scope.application.id : '')).replace();
            };

            var onError = function() {
              $log.debug('DEBUG', $scope, $scope.$parent);
              $scope.status = $scope.statusCodes.ERROR;
            };

            if(!$scope.application.scopes) {
              $scope.application.scopes = [];
            }

            var idx = $scope.application.scopes.indexOf(scp);
            if(idx > -1) {
              $scope.application.scopes[idx] = scope;
            } else {
              $scope.application.scopes.push(scope);
            }

            ApplicationResource.update({id: $scope.application.id}, $scope.application, onSuccess, onError);
          });
      };

      $scope.deleteScope = function(scope) {
        var idx = $scope.application.scopes.indexOf(scope);
        if(idx > -1) {
          $scope.application.scopes.splice(idx, 1);
          ApplicationResource.update({id: $scope.application.id}, $scope.application);
        }
      };
    }])

  .controller('ApplicationEditController', ['$scope', '$location', '$routeParams', 'ApplicationsResource', 'ApplicationResource', '$log',

    function ($scope, $location, $routeParams, ApplicationsResource, ApplicationResource, $log) {
      $scope.statusCodes = {
        ERROR: -2,
        SUCCESS: 1
      };
      $scope.status = null;
      $scope.application = $routeParams.id ? ApplicationResource.get({id: $routeParams.id}) : {};

      $scope.save = function(form) {
        if (!form.$valid) {
          $scope.status = $scope.statusCodes.ERROR;
          form.saveAttempted = true;
          return;
        }

        var onSuccess = function () {
          $scope.status = $scope.statusCodes.SUCCESS;
          $location.path('/applications');
        };

        var onError = function () {
          $log.debug('DEBUG', $scope, $scope.$parent);
          $scope.status = $scope.statusCodes.ERROR;
        };

        if ($scope.key) {
          $scope.application.key = $scope.key;
        } else {
          delete $scope.application.key;
        }

        if ($scope.application.id) {
          ApplicationResource.update({id: $scope.application.id}, $scope.application, onSuccess, onError);
        } else {
          ApplicationsResource.save($scope.application, onSuccess, onError);
        }
      };

      $scope.generateKey = function() {
        var text = '';
        var possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

        for( var i=0; i < 30; i++ ) {
          text += possible.charAt(Math.floor(Math.random() * possible.length));
        }

        $scope.key = text;
      };

      $scope.cancel = function () {
        if ($scope.application.id) {
          $location.path('/application' + ($scope.application.id ? '/' + $scope.application.id : '')).replace();
        } else {
          $location.path('/applications');
        }
      };
    }])

  .controller('ApplicationScopeModalController', ['$scope', '$filter', '$uibModalInstance', 'scope',
    function($scope, $filter, $uibModalInstance, scope) {
      $scope.editMode = scope && scope.name;
      $scope.scope = scope;

      $scope.save = function (form) {
        if (!form.$valid) {
          form.saveAttempted = true;
          return;
        }

        $uibModalInstance.close($scope.scope);
      };

      $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
      };
    }])

    .controller('ApplicationRealmGroupModalController', ['$scope', '$filter', '$uibModalInstance', 'GroupsResource', 'realmGroup',
      function($scope, $filter, $uibModalInstance, GroupsResource, realmGroup) {
        $scope.editMode = realmGroup && realmGroup.realm;
        $scope.realmGroup = realmGroup;
        if (!$scope.realmGroup.groups) {
          $scope.realmGroup.groups = [];
        }

        $scope.groupList = [];
        GroupsResource.query().$promise.then((groups) => {
          $scope.groupList = groups.map((grp) => grp.name);
        });

        $scope.save = function (form) {
          if (!form.$valid) {
            form.saveAttempted = true;
            return;
          }

          $uibModalInstance.close($scope.realmGroup);
        };

        $scope.cancel = function () {
          $uibModalInstance.dismiss('cancel');
        };
      }]);
