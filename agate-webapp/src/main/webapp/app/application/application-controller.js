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

agate.application

  .controller('ApplicationListController', ['$rootScope', '$scope', 'ApplicationsResource', 'ApplicationResource', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, ApplicationsResource, ApplicationResource, NOTIFICATION_EVENTS) {
      var onSuccess = function(response) {
        $scope.applications = response;
        $scope.loading = false;
      };

      var onError = function() {
        $scope.loading = false;
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

          ApplicationResource.delete({id: id},
            function () {
              $scope.loading = true;
              ApplicationsResource.query({}, onSuccess, onError);
            });
        }
      });

    }])

  .controller('ApplicationViewController', ['$scope', '$location', '$routeParams', 'ApplicationResource',

    function ($scope, $location, $routeParams, ApplicationResource) {
      $scope.application = $routeParams.id ? ApplicationResource.get({id: $routeParams.id}) : {};
    }])

  .controller('ApplicationEditController', ['$scope', '$location', '$routeParams', 'ApplicationsResource', 'ApplicationResource','$log',

    function ($scope, $location, $routeParams, ApplicationsResource, ApplicationResource, $log) {
      $scope.status_codes = {
        NO_MATCH: -1,
        ERROR: -2,
        SUCCESS: 1
      };
      $scope.status = null;
      $scope.application = $routeParams.id ? ApplicationResource.get({id: $routeParams.id}) : {};

      $scope.save = function(form) {
        if (!form.$valid || $scope.confirmKey !== $scope.key) {
          $scope.status = $scope.confirmKey !== $scope.key ? $scope.status_codes.NO_MATCH : $scope.status_codes.ERROR;
          form.saveAttempted = true;
          return;
        }

        var onSuccess = function () {
          $scope.status = $scope.status_codes.SUCCESS;
          $location.path('/applications');
        };

        var onError = function () {
          $log.debug('DEBUG', $scope, $scope.$parent);
          $scope.status = $scope.status_codes.ERROR;
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

      $scope.cancel = function () {
        if ($scope.application.id) {
          $location.path('/application' + ($scope.application.id ? '/' + $scope.application.id : '')).replace();
        } else {
          $location.path('/applications');
        }
      };
    }]);
