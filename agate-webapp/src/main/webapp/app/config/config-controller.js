'use strict';

agate.config
  .controller('ConfigurationController', ['$scope', '$resource', '$route', '$log', '$window', 'ConfigurationResource',
    '$modal', 'KeyStoreResource',

    function ($scope, $resource, $route, $log, $window, ConfigurationResource, $modal, KeyStoreResource) {
      $scope.agateConfig = ConfigurationResource.get();

      $scope.createKeyPair = function () {
        $modal.open({
          templateUrl: 'app/config/views/config-modal-create-keypair.html',
          controller: 'CreateKeyPairModalController'
        }).result.then(function(data) {
            KeyStoreResource.save(data, function () {
              $route.reload();
            });
          });
      };

      $scope.importKeyPair = function () {
        $modal.open({
          templateUrl: 'app/config/views/config-modal-import-keypair.html',
          controller: 'ImportKeyPairModalController'
        }).result.then(function(data) {
            KeyStoreResource.save(data, function () {
              $route.reload();
            });
          });
      };

      $scope.downloadCertificate = function () {
        $window.open('ws/config/keystore/system/https', '_blank', '');
      };

    }])

  .controller('ImportKeyPairModalController', ['$scope', '$location', '$modalInstance',
    function($scope, $location, $modalInstance) {
      $scope.keyForm = {
        privateImport: '',
        publicImport: '',
        keyType: 0
      };

      $scope.save = function () {
        $modalInstance.close($scope.keyForm);
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };
    }])

  .controller('CreateKeyPairModalController', ['$scope', '$location', '$modalInstance',
    function($scope, $location, $modalInstance) {
      $scope.showAdvanced = false;

      $scope.keyForm = {
        privateForm: {
          algo: 'RSA',
          size: 2048
        },
        publicForm: {},
        keyType: 0
      };

      $scope.save = function () {
        $modalInstance.close($scope.keyForm);
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };
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
