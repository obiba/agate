'use strict';

agate.config
  .controller('ConfigurationController', ['$rootScope', '$scope', '$resource', '$route', '$log', '$window', 'ConfigurationResource',
    'NOTIFICATION_EVENTS', '$uibModal', 'KeyStoreResource',

    function ($rootScope, $scope, $resource, $route, $log, $window, ConfigurationResource, NOTIFICATION_EVENTS, $uibModal, KeyStoreResource) {
      $scope.agateConfig = {userAttributes: []};

      ConfigurationResource.get(function(config) {
        $scope.agateConfig = config;
        $scope.agateConfig.userAttributes = $scope.agateConfig.userAttributes || [];
      });

      $scope.createKeyPair = function () {
        $uibModal.open({
          templateUrl: 'app/config/views/config-modal-create-keypair.html',
          controller: 'CreateKeyPairModalController'
        }).result.then(function (data) {
            KeyStoreResource.save(data, function () {
              $route.reload();
            });
          });
      };

      $scope.importKeyPair = function () {
        $uibModal.open({
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
        $uibModal.open({
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

  .controller('AttributeModalController', ['$scope', '$filter', '$uibModalInstance', 'attribute',
    function($scope, $filter, $uibModalInstance, attribute) {
      $scope.TYPES = [
        {name: 'STRING', label: $filter('translate')('config.attributes.types.STRING')},
        {name: 'INTEGER', label: $filter('translate')('config.attributes.types.INTEGER')},
        {name: 'NUMBER', label: $filter('translate')('config.attributes.types.NUMBER')},
        {name: 'BOOLEAN', label: $filter('translate')('config.attributes.types.BOOLEAN')}
      ];

      var index = -1;
      if (attribute) {
        index = $scope.TYPES.findIndex(function(type){
          return type.name === attribute.type;
        });
      }

      $scope.selectedType = index === -1 ? $scope.TYPES[0] : $scope.TYPES[index];
      $scope.editMode = attribute && attribute.name;
      $scope.attribute = attribute || {type: 'STRING'};
      $scope.attribute.values = attribute && attribute.values ? attribute.values.join(', ') : '';
      $scope.attribute.required = attribute && attribute.required === true ? attribute.required : false;

      $scope.save = function (form) {
        if (!form.$valid) {
          form.saveAttempted = true;
          return;
        }
        $scope.attribute.type = $scope.selectedType.name;
        $scope.attribute.values = $scope.attribute.type !== 'BOOLEAN' &&  $scope.attribute.values.length > 0 ? $scope.attribute.values.split(',').map(function(s) {
          return s.trim();
        }) : null;

        $uibModalInstance.close($scope.attribute);
      };

      $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
      };
    }])

  .controller('ImportKeyPairModalController', ['$scope', '$location', '$uibModalInstance',
    function($scope, $location, $uibModalInstance) {
      $scope.keyForm = {
        privateImport: '',
        publicImport: '',
        keyType: 0
      };

      $scope.save = function () {
        $uibModalInstance.close($scope.keyForm);
      };

      $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
      };
    }])

  .controller('CreateKeyPairModalController', ['$scope', '$location', '$uibModalInstance',
    function($scope, $location, $uibModalInstance) {
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
        $uibModalInstance.close($scope.keyForm);
      };

      $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
      };
    }])

  .controller('ConfigurationEditController', ['$scope', '$resource', '$location', '$log', 'ConfigurationResource', 'FormServerValidation',

    function ($scope, $resource, $location, $log, ConfigurationResource, FormServerValidation) {

      $scope.agateConfig = {};

      ConfigurationResource.get(function(config) {
        $scope.agateConfig = config;
        $scope.inactiveTimeoutDays = $scope.agateConfig.inactiveTimeout / 24;
      });

      $scope.save = function () {

        if (!$scope.form.$valid) {
          $scope.form.saveAttempted = true;
          return;
        }

        $scope.agateConfig.inactiveTimeout = $scope.inactiveTimeoutDays * 24;
        $scope.agateConfig.$save(
          function () {
            $location.path('/config').replace();
          },
          function (response) {
            FormServerValidation.error(response, $scope.form);
          });
      };

    }]);
