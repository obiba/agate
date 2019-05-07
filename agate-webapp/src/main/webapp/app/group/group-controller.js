/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.group

  .controller('GroupListController',
    ['$rootScope',
      '$scope',
      '$route',
      'GroupsResource',
      'GroupResource',
      'NOTIFICATION_EVENTS',
      'AlertBuilder',

    function ($rootScope, $scope, $route, GroupsResource, GroupResource, NOTIFICATION_EVENTS, AlertBuilder) {
      function onSuccess(response) {
        $scope.groups = response;
        $scope.loading = false;
      }

      function onError(response) {
        $scope.loading = false;
        AlertBuilder.newBuilder().response(response).delay(0).build();
      }

      $scope.loading = true;
      $scope.groups = GroupsResource.query({}, onSuccess, onError);

      $scope.deleteGroup = function (group) {
        $scope.groupToDelete = group.id;

        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {
            titleKey: 'group.delete-dialog.onerrortitle',
            messageKey:'group.delete-dialog.message',
            messageArgs: [group.name]
          }, group.id
        );
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.groupToDelete === id) {

          GroupResource.delete({id: id}).$promise
            .then(function () {
              $route.reload();
            })
            .catch(onError);
        }
      });

    }])

  .controller('GroupEditController',
    ['$scope',
      '$routeParams',
      '$location',
      'GroupsResource',
      'GroupResource',
      'ApplicationsResource',
      'AlertBuilder',

    function ($scope, $routeParams, $location, GroupsResource, GroupResource, ApplicationsResource, AlertBuilder) {
      $scope.group = $routeParams.id ? GroupResource.get({id: $routeParams.id}) : {};
      $scope.applicationList = [];

      function onError(response) {
        AlertBuilder.newBuilder().response(response).delay(0).build();
      }

      ApplicationsResource.query().$promise
        .then(function(applications){
          applications.forEach(function(application){
            $scope.applicationList.push(application.id);
          });
        })
        .catch(onError);

      $scope.save = function(form) {
        if (!form.$valid) {
          form.saveAttempted = true;
          return;
        }

        if (!$scope.group.id) {
          GroupsResource.save($scope.group).$promise
            .then(function () {
              $location.path('/groups');
            })
            .catch(onError);
        } else {
          GroupResource.update({id: $scope.group.id}, $scope.group).$promise
            .then(function () {
              $location.path('/groups');
            })
            .catch(onError);
        }
      };

      $scope.cancel = function () {
        if ($scope.group.id) {
          $location.path('/group' + ($scope.group.id ? '/' + $scope.group.id : '')).replace();
        } else {
          $location.path('/groups');
        }
      };
    }])

  .controller('GroupViewController', ['$scope', '$routeParams', 'GroupResource',

    function ($scope, $routeParams, GroupResource) {
      $scope.group = GroupResource.get({id: $routeParams.id, includeUsers: true});
    }]);
