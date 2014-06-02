'use strict';

agate.config
  .controller('AgateConfigController', ['$scope', '$resource', '$log', 'AgateConfigResource',

    function ($scope, $resource, $log, AgateConfigResource) {
      $scope.agateConfig = AgateConfigResource.get();
      $scope.availableLanguages = $resource('ws/config/languages').get();
    }])

  .controller('AgateConfigEditController', ['$scope', '$resource', '$location', '$log', 'AgateConfigResource', 'FormServerValidation',

    function ($scope, $resource, $location, $log, AgateConfigResource, FormServerValidation) {

      $scope.agateConfig = AgateConfigResource.get();
      $scope.availableLanguages = $resource('ws/config/languages').get();

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
