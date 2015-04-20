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

  .controller('ApplicationListController', ['$scope', 'ApplicationsResource', 'ApplicationResource',

    function ($scope, ApplicationsResource, ApplicationResource) {

      $scope.applications = ApplicationsResource.query();

      $scope.deleteApplication = function (application) {
        ApplicationResource.delete({id: application.id},
          function () {
            $scope.applications = ApplicationsResource.query();
          });
      };
    }])

  .controller('ApplicationViewController', ['$scope', '$location', '$routeParams', 'ApplicationResource',

    function ($scope, $location, $routeParams, ApplicationResource) {
      $scope.application = $routeParams.id ? ApplicationResource.get({id: $routeParams.id}) : {};
    }])

  .controller('ApplicationEditController', ['$scope', '$location', '$routeParams', 'ApplicationsResource', 'ApplicationResource',

    function ($scope, $location, $routeParams, ApplicationsResource, ApplicationResource) {
      $scope.showKey = false;
      $scope.application = $routeParams.id ? ApplicationResource.get({id: $routeParams.id}) : {};

      $scope.save = function(form) {
        if (!form.$valid || $scope.confirmKey !== $scope.key) {
          form.saveAttempted = true;
          return;
        }

        var onSuccess = function () {
          $location.path('/applications');
        };

        if ($scope.key) {
          $scope.application.key = $scope.key;
        } else {
          delete $scope.application.key;
        }

        if ($scope.application.id) {
          ApplicationResource.update({id: $scope.application.id}, $scope.application, onSuccess);
        } else {
          ApplicationsResource.save($scope.application, onSuccess);
        }
      };
    }]);
