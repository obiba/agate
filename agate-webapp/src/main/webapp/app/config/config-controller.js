'use strict';

agate.config
  .controller('ConfigurationController', ['$rootScope', '$scope', '$resource', '$route', '$log', '$window', 'ConfigurationResource',
    'NOTIFICATION_EVENTS', '$modal', 'KeyStoreResource',

    function ($rootScope, $scope, $resource, $route, $log, $window, ConfigurationResource, NOTIFICATION_EVENTS, $modal, KeyStoreResource) {
      $scope.agateConfig = {userAttributes: []};

      ConfigurationResource.get(function(config) {
        $scope.agateConfig = config;
        $scope.agateConfig.userAttributes = $scope.agateConfig.userAttributes || [];
      });

      $scope.createKeyPair = function () {
        $modal.open({
          templateUrl: 'app/config/views/config-modal-create-keypair.html',
          controller: 'CreateKeyPairModalController'
        }).result.then(function (data) {
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

      $scope.editAttribute = function (att) {
        $modal.open({
          templateUrl: 'app/config/views/attribute-modal-form.html',
          controller: 'AttributeModalController',
          resolve: {
            'attribute': function () {
              return angular.copy(att);
            }
          }
        }).result.then(function (attribute) {
            var idx = $scope.agateConfig.userAttributes.indexOf(att);
            var newConfig = angular.copy($scope.agateConfig);

            if (idx > -1) {
              newConfig.userAttributes.splice(idx, 1);
              newConfig.userAttributes.splice(idx, 0, attribute);
            } else {
              newConfig.userAttributes.push(attribute);
            }

            ConfigurationResource.save(newConfig, function () {
              $route.reload();
            });
          });
      };

      $scope.deleteAttribute = function(attribute) {
        $scope.attributeToDelete = $scope.agateConfig.userAttributes.indexOf(attribute);

        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {
            titleKey: 'attribute.delete-dialog.title',
            messageKey:'attribute.delete-dialog.message',
            messageArgs: [attribute.name]
          }, $scope.attributeToDelete
        );

        $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
          if ($scope.attributeToDelete === id) {
            $scope.agateConfig.userAttributes.splice(id, 1);
            ConfigurationResource.save($scope.agateConfig, function () {
              $route.reload();
            });
          }
        });
      };
    }])

  .controller('AttributeModalController', ['$scope', '$modalInstance', 'attribute', function($scope, $modalInstance, attribute) {
    var types = ['STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN'];
    $scope.availableTypes = types.map(function(e) {
      return {id: e, label: e};
    });

    $scope.attribute = attribute || {type: 'STRING'};
    $scope.attribute.values = !$scope.attribute.values ? '' : $scope.attribute.values.join(', ');
    $scope.attribute.required = attribute && attribute.required === true ? attribute.required : false;

    $scope.data = {selectedType: $scope.availableTypes[types.indexOf($scope.attribute.type)]};

    $scope.save = function (form) {
      if (!form.$valid) {
        form.saveAttempted = true;
        return;
      }

      $scope.attribute.values = $scope.data.selectedType.id !== 'BOOLEAN' &&  $scope.attribute.values.length > 0 ? $scope.attribute.values.split(',').map(function(s) {
        return s.trim();
      }) : null;

      $scope.attribute.type = $scope.data.selectedType.id;
      $modalInstance.close($scope.attribute);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
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

      $scope.agateConfig = {};

      ConfigurationResource.get(function(config) {
        $scope.agateConfig = config;
        $scope.inactiveTimeout = $scope.agateConfig.inactiveTimeout / 24;
      });

      $scope.save = function () {

        if (!$scope.form.$valid) {
          $scope.form.saveAttempted = true;
          return;
        }

        $scope.agateConfig.inactiveTimeout = $scope.inactiveTimeout * 24;
        $scope.agateConfig.$save(
          function () {
            $location.path('/config').replace();
          },
          function (response) {
            FormServerValidation.error(response, $scope.form);
          });
      };

    }]);
