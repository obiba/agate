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

agate.group

  .controller('GroupListController', ['$rootScope', '$scope', '$route', 'GroupsResource', 'GroupResource', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, $route, GroupsResource, GroupResource, NOTIFICATION_EVENTS) {
      var onSuccess = function(response) {
        $scope.groups = response;
        $scope.loading = false;
      };

      var onError = function() {
        $scope.loading = false;
      };

      $scope.loading = true;
      $scope.groups = GroupsResource.query({}, onSuccess, onError);

      $scope.deleteGroup = function (group) {
        $scope.groupToDelete = group.id;

        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {
            titleKey: 'group.delete-dialog.title',
            messageKey:'group.delete-dialog.message',
            messageArgs: [group.name]
          }, group.id
        );
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.groupToDelete === id) {

          GroupResource.delete({id: id},
            function () {
              $route.reload();
            });
        }
      });

    }])

  .controller('GroupEditController', ['$scope', '$routeParams', '$location', 'GroupsResource', 'GroupResource', 'ApplicationsResource',

    function ($scope, $routeParams, $location, GroupsResource, GroupResource, ApplicationsResource) {
      $scope.group = $routeParams.id ? GroupResource.get({id: $routeParams.id}) : {};

      $scope.applicationList = [];
      ApplicationsResource.query().$promise.then(function(applications){
        applications.forEach(function(application){
          $scope.applicationList.push(application.id);
        });
      });

      $scope.save = function(form) {
        if (!form.$valid) {
          form.saveAttempted = true;
          return;
        }

        if (!$scope.group.id) {
          GroupsResource.save($scope.group, function () {
            $location.path('/groups');
          });
        } else {
          GroupResource.update({id: $scope.group.id}, $scope.group, function () {
            $location.path('/groups');
          });
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
      $scope.group = GroupResource.get({id: $routeParams.id});
    }]);
