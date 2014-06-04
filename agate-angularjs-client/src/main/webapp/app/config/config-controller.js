'use strict';

agate.config
  .controller('ConfigurationController', ['$scope', '$resource', '$log', 'ConfigurationResource',

    function ($scope, $resource, $log, ConfigurationResource) {
      $scope.agateConfig = ConfigurationResource.get();
    }])

  .controller('ConfigurationEditController', ['$scope', '$resource', '$location', '$log', 'ConfigurationResource', 'FormServerValidation',

    function ($scope, $resource, $location, $log, ConfigurationResource, FormServerValidation) {

      $scope.agateConfig = ConfigurationResource.get();

      $scope.save = function () {

        if (!$scope.form.$valid) {
          $scope.form.saveAttempted = true;
          return;
        }

        $scope.agateConfig.$save(
          function () {
            $location.path('/config').replace();
          },
          function (response) {
            FormServerValidation.error(response, $scope.form);
          });
      };

    }]);
