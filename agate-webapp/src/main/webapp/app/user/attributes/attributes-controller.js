/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.user

  .controller('AttributesFormController', ['$rootScope', '$scope', '$translate', '$log', '$uibModal', 'AttributesService', 'NOTIFICATION_EVENTS',
    function ($rootScope, $scope, $translate, $log, $uibModal, AttributesService, NOTIFICATION_EVENTS) {

      $scope.$watch('attributes', function() {
        $scope.nonConfigAttributes = AttributesService.findNonConfigAttributes($scope.attributes, $scope.attributesConfig);
      }, true);

      /**
       * Pops modal for adding new attribute
       */
      $scope.addAttribute = function() {
        $uibModal
          .open({
            templateUrl: 'app/user/attributes/views/attribute-creation-form-modal.html',
            controller: 'AttributeCreateFormModalController',
            resolve: {
              usedNames: function() {
                return $scope.usedAttributeNames;
              },
              attribute: function () {
                return {};
              }
            }
          })
          .result.then(function (attribute) {
            if ($scope.attributes) {
              $scope.attributes.push(attribute);
            } else {
              $scope.attributes = [attribute];
            }

            $scope.usedAttributeNames.push(attribute.name);

          }, function () {
          });
      };

      /**
       * Edits selected attribute
       * @param attribute
       */
      $scope.edit = function(attribute) {
        $uibModal
          .open({
            templateUrl: 'app/user/attributes/views/attribute-form-modal.html',
            controller: 'AttributeFormModalController',
            resolve: {
              attribute: function () {
                return attribute;
              }
            }
          })
          .result.then(function (attribute) {
            var index = $scope.attributes.map(function(item) {
              return item.name === attribute.name;
            }).indexOf(true);

            if (index !== -1) {
              $scope.attributes[index] = attribute;
            }
          }, function () {
          });
      };

      /**
       * Deletes a selected attribute from the list
       * @param index
       */
      $scope.delete = function(attribute) {
        var titleKey = 'attribute.delete-dialog.title';
        var messageKey = 'attribute.delete-dialog.message';
        $translate([titleKey, messageKey], {name: attribute.name})
          .then(function (translation) {
            $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
              {title: translation[titleKey], message: translation[messageKey]}, attribute);
          });
      };

      /**
       * Delete confirmation callback
       */
      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, attribute) {
        var index = $scope.attributes.map(function(item, index) {
          return item.name === attribute.name;
        }).indexOf(true);

        if (index !== -1) {
          $scope.attributes.splice(index, 1);
        }

      });

    }])


  .controller('AttributeCreateFormModalController', ['$scope', '$uibModalInstance', '$log', 'attribute', 'usedNames', 'AlertService',
    function ($scope, $uibModalInstance, $log, attribute, usedNames, AlertService) {

      $scope.attribute = attribute;
      $scope.usedNames = usedNames;
      $scope.duplicated = false;

      /**
       * Saves attribute changes
       * @param form
       */
      $scope.save = function (form) {
        var duplicated = !form['attribute.name'].$error.required && usedNames.indexOf($scope.attribute.name) !== -1;
        var duplicatedName = duplicated ? $scope.attribute.name : ''; // For validation message only

        if (form.$valid && !duplicated) {
          $uibModalInstance.close($scope.attribute);
        }
        else {
          if (duplicated) {
            AlertService.alert({id: 'AttributeCreateFormModalController', type: 'danger', msgKey: 'attribute.error.duplicated', msgArgs: [duplicatedName]});
          } else {
            AlertService.alert({id: 'AttributeCreateFormModalController', type: 'danger', msgKey: 'required', msgArgs: [duplicatedName]});
          }

          form['attribute.name'].$error.duplicated = duplicated;
          $scope.form = form;
          $scope.form.$invalid = duplicated ? true : $scope.form.$invalid;
          $scope.form.saveAttempted = true;
        }
      };

      /**
       * Closes modal
       */
      $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
      };

    }])

  .controller('AttributeFormModalController', ['$scope', '$uibModalInstance', '$log', 'attribute',
    function ($scope, $uibModalInstance, $log, attribute) {
      $scope.attribute = $.extend(true, {}, attribute);

      /**
       * Saves attribute changes
       * @param form
       */
      $scope.save = function (form) {
        if (form.$valid) {
          $uibModalInstance.close($scope.attribute);
        }
        else {
          $scope.form = form;
          $scope.form.saveAttempted = true;
        }
      };

      /**
       * Closes modal
       */
      $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
      };

    }]);





