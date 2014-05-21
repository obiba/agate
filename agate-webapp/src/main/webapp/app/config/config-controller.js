'use strict';

agate.config
  .controller('AgateConfigController', ['$scope', '$resource', '$log', 'AgateConfigResource',

    function ($scope, $resource, $log, AgateConfigResource) {
      $scope.agateConfig = AgateConfigResource.get();
      $scope.availableLanguages = $resource('ws/config/languages').get();
    }])

  .controller('AgateConfigEditController', ['$scope', '$resource', '$location', '$log', 'AgateConfigResource',

    function ($scope, $resource, $location, $log, AgateConfigResource) {

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
//            $log.debug('response:', response);
//        [{
//          "message": "ne peut pas être vide",
//          "messageTemplate": "{org.hibernate.validator.constraints.NotBlank.message}",
//          "path": "AgateConfig.name",
//          "invalidValue": ""
//        }]

            $scope.errors = [];
            response.data.forEach(function (error) {
              //$log.debug('error: ', error);
              var field = error.path.substring(error.path.indexOf('.') + 1);
              $scope.form[field].$dirty = true;
              $scope.form[field].$setValidity('server', false);
              $scope.errors[field] = error.message;
            });
          });
      };

    }]);
