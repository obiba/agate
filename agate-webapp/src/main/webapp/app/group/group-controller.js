/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
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

      $scope.groups = GroupsResource.query();

      $scope.deleteGroup = function (id) {
        $scope.groupToDelete = id;
        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {title: 'Delete Group', message: 'Are you sure to delete the group?'}, id);
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
  .controller('GroupEditController', ['$scope', '$routeParams', '$location', 'GroupsResource', 'GroupResource',

    function ($scope, $routeParams, $location, GroupsResource, GroupResource) {
      $scope.group = $routeParams.id ? GroupResource.get({id: $routeParams.id}) : {};

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
    }])
  .controller('GroupViewController', ['$scope', '$routeParams', 'GroupResource',

    function ($scope, $routeParams, GroupResource) {
      $scope.group = GroupResource.get({id: $routeParams.id});
    }]);
