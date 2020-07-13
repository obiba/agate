/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.config
  .controller('ConfigurationController', ['$rootScope', '$scope', '$resource', '$route', '$log', '$window', 'ConfigurationResource',
    'NOTIFICATION_EVENTS', '$uibModal', 'KeyStoreResource',

    function ($rootScope, $scope, $resource, $route, $log, $window, ConfigurationResource, NOTIFICATION_EVENTS, $uibModal, KeyStoreResource) {
      $scope.agateConfig = {userAttributes: []};
      $scope.availableLanguages = $resource(contextPath + '/ws/config/languages').get();

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
        $window.open(contextPath + '/ws/config/keystore/system/https', '_blank', '');
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

  .controller('ConfigurationEditController', ['$scope', '$resource', '$window', '$location', '$log', 'ConfigurationResource', 'FormServerValidation',

    function ($scope, $resource, $window, $location, $log, ConfigurationResource, FormServerValidation) {
      $scope.agateConfig = ConfigurationResource.get(function() {
        $scope.inactiveTimeoutDays = $scope.agateConfig.inactiveTimeout / 24;
      });

      var reload = false;
      $scope.agateConfig.$promise.then(function() {
        $scope.$watchGroup(['agateConfig.name', 'agateConfig.languages'], function(value, oldValue) {
          if(!angular.equals(value,oldValue)) {
            reload = true;
          }
        });
      });

      $scope.availableLanguages = $resource(contextPath + '/ws/config/languages').get();

      $scope.save = function () {

        if (!$scope.form.$valid) {
          $scope.form.saveAttempted = true;
          return;
        }

        $scope.agateConfig.inactiveTimeout = $scope.inactiveTimeoutDays * 24;
        $scope.agateConfig.$save(
          function () {
            $location.path('/admin/general');
            if(reload) {
              $window.location.reload();
            }
          },
          function (response) {
            FormServerValidation.error(response, $scope.form);
          });
      };

    }])

  .controller('ConfigurationStyleEditController', ['$scope', '$resource', '$window', '$location', '$log', 'ConfigurationResource',
    'FormServerValidation', 'StyleEditorService',
    function ($scope, $resource, $window, $location, $log, ConfigurationResource, FormServerValidation, StyleEditorService) {
      var reload = false;
      $scope.agateConfig = ConfigurationResource.get();

      $scope.agateConfig.$promise.then(function() {
        $scope.$watch('agateConfig.style', function(value, oldValue) {
          if(!angular.equals(value,oldValue)) {
            reload = true;
          }
        });
      });

      StyleEditorService.configureAcePaths();
      $scope.ace = StyleEditorService.getEditorOptions();

      $scope.save = function () {
        $scope.agateConfig.$save(
          function () {
            $location.path('/admin');
            if(reload) {
              $window.location.reload();
            }
          },
          function (response) {
            FormServerValidation.error(response, $scope.form);
          });
      };

    }])

  .controller('ConfigurationTranslationsEditController', ['$scope', '$q', '$resource', '$window', '$location', '$log', '$uibModal',
    'ConfigurationResource', 'FormServerValidation', 'TranslationsResource',
    function ($scope, $q, $resource, $window, $location, $log, $uibModal, ConfigurationResource, FormServerValidation, TranslationsResource) {
      var updates = {}, oldTranslations = {};
      $scope.agateConfig = ConfigurationResource.get();
      $scope.agateConfig.$promise.then(function() {
        var defaults = {};
        $scope.translations = {};
        $scope.tabs = $scope.agateConfig.languages.map(function (lang) {
          updates[lang] = jsonToPaths(
            JSON.parse((($scope.agateConfig.translations || []).filter(function(t) { return t.lang === lang; })[0] || {value: '{}'}).value)
          );
          defaults[lang] = TranslationsResource.get({id: lang, default: true}).$promise;
          return {lang: lang};
        });

        $q.all(defaults).then(function(res) {
          Object.keys(res).forEach(function(lang) {
            var defaultPaths = jsonToPaths(extractObjFromResouce(res[lang]));
            oldTranslations[lang] = angular.copy(defaultPaths);
            var newPaths = updates[lang].map(function(e) {
              if(!defaultPaths.some(function(u) {
                  return e.path === u.path ? (u.value = e.value , u.overwritten = true) : false;
                })) {
                e.overwritten = true;
                return e;
              }

              return null;
            }).filter(notNull);
            $scope.translations[lang] = defaultPaths.concat(newPaths);
          });
        });
      });

      function notNull (x) {
        return x;
      }

      function jsonToPaths (obj) {
        function inner(o, name, acc) {
          for(var k in o) {
            var tmp = [name, k].filter(function(x) {return x;}).join('.');
            if (angular.isObject(o[k])) {
              inner(o[k], tmp, acc);
            } else {
              acc.push({path: tmp, value: o[k]});
            }
          }

          return acc;
        }

        return inner(obj, null, []);
      }

      function pathsToJson(paths) {
        function inner(target, path, value) {
          if(path.length === 1) {
            target[path[0]] = value;
            return;
          }

          if(!target[path[0]]) { target[path[0]] = {}; }

          inner(target[path[0]], path.splice(1), value);
        }

        return paths.reduce(function(res, e) {
          inner(res, e.path.split('.'), e.value);
          return res;
        }, {});
      }

      function extractObjFromResouce(res) {
        return angular.fromJson(angular.toJson(res));
      }

      $scope.checkPresence = function (entry) {
        if (!isInDefault(entry)) {
          entry.isCustom = true;
        }
      };

      function isInDefault(entry) {
        var presence = [];
        $scope.agateConfig.languages.forEach(function (lang) {
          var found = oldTranslations[lang].filter(function (translation) {
            return translation.path === entry.path;
          }).pop();
          presence.push(found);
        });
        return presence.reduce(function (prev, curr) {
          return prev && curr;
        });
      }

      $scope.trash = function (entry) {
        var indices = [];
        $scope.agateConfig.languages.forEach(function (lang) {
          $scope.translations[lang].filter(function (translation, index) {
            var found = translation.path === entry.path;
            if (found) {
              indices.push({lang: lang, index: index});
            }
            return found;
          });
        });

        indices.forEach(function (i) {
          $scope.translations[i.lang].splice(i.index, 1);
        });
      };

      $scope.add = function () {
        var modal = $uibModal.open({
          templateUrl: 'app/config/views/config-translation-modal-form-template.html',
          controller: 'NewEntryModalController'
        });

        modal.result.then(function (entry) {
          $scope.agateConfig.languages.forEach(function (lang) {
            $scope.translations[lang].push({path: entry.path, value: entry.value});
          });
        });
      };

      $scope.setDirty = function(entry) {
        if(!entry.overwritten) { entry.overwritten = true; }
      };

      $scope.resetEntry = function(entry, lang) {
        entry.overwritten = false;

        var original = oldTranslations[lang].filter(function(e) {
          return e.path === entry.path;
        })[0];

        entry.value = original ? original.value : '';
      };

      $scope.save = function () {
        $scope.agateConfig.translations = $scope.agateConfig.languages.map(function(lang){
          var changes = $scope.translations[lang].filter(function (e) {
            var result = null;

            if (!oldTranslations[lang].some(function(o) {
                if (o.path === e.path) {
                  if (o.value !== e.value) {
                    result = e;
                  }

                  return true;
                }
              })) {
              result = e;
            }

            return result;
          }).filter(notNull);

          return {lang: lang, value: JSON.stringify(pathsToJson(changes))};
        });

        $scope.agateConfig.$save(
          function () {
            $location.path('/admin');
            $window.location.reload();
          },
          function (response) {
            FormServerValidation.error(response, $scope.form);
          });
      };
    }])

  .controller('NewEntryModalController', ['$scope', '$uibModalInstance',
    function ($scope, $uibModalInstance) {
      $scope.entry = {};

      $scope.accept = function () {
        $uibModalInstance.close($scope.entry);
      };

      $scope.cancel = function () {
        $uibModalInstance.dismiss();
      };
    }]);;
