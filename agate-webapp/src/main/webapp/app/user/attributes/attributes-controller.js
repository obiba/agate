'use strict';

agate.user

  .controller('AttributesFormController', ['$rootScope', '$scope', '$translate', '$log', '$modal', 'NOTIFICATION_EVENTS',
    function ($rootScope, $scope, $translate, $log, $modal, NOTIFICATION_EVENTS) {

      /**
       * Pops modal for adding new attribute
       */
      $scope.addAttribute = function() {
        $modal
          .open({
            templateUrl: 'app/user/attributes/views/attributes-form-modal-template.html',
            controller: 'AttributesFormModalController',
            resolve: {
              attribute: function () {
                return {};
              }
            }
          })
          .result.then(function (attribute) {
            if (attribute) {
              $scope.attributes.push(attribute);
            } else {
              $scope.attributes = [attribute];
            }

          }, function () {
          });
      };

      /**
       * Edits selected attribute
       * @param attribute
       */
      $scope.edit = function(attribute) {
        $modal
          .open({
            templateUrl: 'app/user/attributes/views/attribute-form-modal-template.html',
            controller: 'AttributeFormModalController',
            resolve: {
              attribute: function () {
                return attribute;
              }
            }
          })
          .result.then(function (attribute) {
            var index = $scope.attributes.map(function(item, index) {
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


  .controller('AttributesFormModalController', ['$scope', '$modalInstance', '$log', 'attribute',
    function ($scope, $modalInstance, $log, attribute) {

      $scope.attribute = attribute;

      /**
       * Saves attribute changes
       * @param form
       */
      $scope.save = function (form) {
        if (form.$valid) {
          $modalInstance.close($scope.attribute);
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
        $modalInstance.dismiss('cancel');
      };

    }])

  .controller('AttributeFormModalController', ['$scope', '$modalInstance', '$log', 'attribute',
    function ($scope, $modalInstance, $log, attribute) {
      $scope.attribute =  $.extend(true, {}, attribute);

      /**
       * Saves attribute changes
       * @param form
       */
      $scope.save = function (form) {
        if (form.$valid) {
          $modalInstance.close($scope.attribute);
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
        $modalInstance.dismiss('cancel');
      };

    }]);





