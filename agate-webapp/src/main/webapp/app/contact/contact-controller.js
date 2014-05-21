'use strict';

agate.contact
  .controller('ContactController', ['$rootScope', '$scope', '$modal', '$translate', '$log',
    function ($rootScope, $scope, $modal, $translate, $log) {

      $scope.viewContact = function (contact) {
        $modal.open({
          templateUrl: 'app/contact/contact-modal-view.html',
          controller: 'ContactViewModalController',
          resolve: {
            contact: function () {
              return contact;
            }
          }
        });
      };

      $scope.editInvestigator = function (contactable, contact) {
        $scope.editInvestigatorOrContact(contactable, contact, true);
      };

      $scope.editContact = function (contactable, contact) {
        $scope.editInvestigatorOrContact(contactable, contact, false);
      };

      $scope.editInvestigatorOrContact = function (contactable, contact, isInvestigator) {
        $modal
          .open({
            templateUrl: 'app/contact/contact-modal-form.html',
            controller: 'ContactEditModalController',
            resolve: {
              contact: function () {
                return contact;
              },
              isInvestigator: function () {
                return isInvestigator;
              }
            }
          })
          .result.then(function (contact) {
            $scope.$emit('contactUpdatedEvent', contactable, contact);
          }, function () {
            $scope.$emit('contactEditionCanceledEvent', contactable);
          });
      };

      $scope.addInvestigator = function (contactable) {
        $scope.addInvestigatorOrContact(contactable, true);
      };

      $scope.addContact = function (contactable) {
        $scope.addInvestigatorOrContact(contactable, false);
      };

      $scope.addInvestigatorOrContact = function (contactable, isInvestigator) {
        $modal
          .open({
            templateUrl: 'app/contact/contact-modal-form.html',
            controller: 'ContactEditModalController',
            resolve: {
              contact: function () {
                return {};
              },
              isInvestigator: function () {
                return isInvestigator;
              }
            }
          })
          .result.then(function (contact) {
            if (isInvestigator) {
              $scope.$emit('addInvestigatorEvent', contactable, contact);
            } else {
              $scope.$emit('addContactEvent', contactable, contact);
            }
          }, function () {
            $scope.$emit('contactEditionCanceledEvent', contactable);
          });
      };

      $scope.deleteInvestigator = function (contactable, contact) {
        $scope.deleteInvestigatorOrContact(contactable, contact, true);
      };

      $scope.deleteContact = function (contactable, contact) {
        $scope.deleteInvestigatorOrContact(contactable, contact, false);
      };

      $scope.deleteInvestigatorOrContact = function (contactable, contact, isInvestigator) {

        var titleKey = 'contact.delete.' + (isInvestigator ? 'investigator' : 'contact') + '.title';
        var messageKey = 'contact.delete.' + (isInvestigator ? 'investigator' : 'contact') + '.confirm';
        $translate([titleKey, messageKey], { name: contact.title + " " + contact.firstName + " " + contact.lastName })
          .then(function (translation) {
            $rootScope.$broadcast('showConfirmDialogEvent',
              {"title": translation[titleKey], "message": translation[messageKey]},
              contact);
          });

        $scope.$on('confirmDialogAcceptedEvent', function (event, contactConfirmed) {
          if (contactConfirmed == contact) {
            $scope.$emit('contactDeletedEvent', contactable, contact, isInvestigator);
          }
        });
      };

    }])
  .controller('ContactViewModalController', ['$scope', '$modalInstance', '$log', 'AgateConfigResource', 'contact',
    function ($scope, $modalInstance, $log, AgateConfigResource, contact) {

      $scope.contact = contact;

      AgateConfigResource.get(function (agateConfig) {
        $scope.tabs = [];
        agateConfig.languages.forEach(function (lang) {
          $scope.tabs.push({ lang: lang, labelKey: 'language.' + lang });
        });
      });

      $scope.close = function () {
        $modalInstance.dismiss('close');
      };

    }])
  .controller('ContactEditModalController', ['$scope', '$modalInstance', '$log', 'AgateConfigResource', 'contact', 'isInvestigator',
    function ($scope, $modalInstance, $log, AgateConfigResource, contact, isInvestigator) {

      $scope.contact = contact;
      $scope.isInvestigator = isInvestigator;

      AgateConfigResource.get(function (agateConfig) {
        $scope.tabs = [];
        agateConfig.languages.forEach(function (lang) {
          $scope.tabs.push({ lang: lang, labelKey: 'language.' + lang });
        });
      });

      $scope.save = function (form) {
        if (form.$valid) {
          $modalInstance.close($scope.contact);
        } else {
          $scope.form = form;
          $scope.form.saveAttempted = true;
        }
      };

      $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
      };

    }]);

