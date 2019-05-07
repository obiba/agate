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

(function () {

  function Controller(
    $rootScope,
    $scope,
    $translate,
    RealmsConfigResource,
    RealmConfigResource,
    NOTIFICATION_EVENTS,
    AlertBuilder) {

    var ctrl = this;

    function onError(response) {
      AlertBuilder.newBuilder().response(response).delay(0).build();
    }

    function onInit() {
      ctrl.loading = true;
      RealmsConfigResource.summaries().$promise
        .then(function(realms) {
          ctrl.loading = false;
          ctrl.realms = realms;
          $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted,  onDelete.bind(ctrl));
        })
        .catch(onError);
    }

    function activateRealm(realm) {
      (
        realm.status === 'ACTIVE' ?
          RealmConfigResource.deactivate({name: realm.name}).$promise :
          RealmConfigResource.activate({name: realm.name}).$promise

      ).then(onInit).catch(onError);
    }

    function deleteRealm(realm) {
      $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
        {
          titleKey: 'realm.delete-dialog.title',
          messageKey: 'realm.delete-dialog.message',
          messageArgs: [realm.name]
        }, realm
      );
    }

    function onDelete(event, realm) {
      RealmConfigResource.delete({name: realm.name}).$promise
        .then(onInit)
        .catch(onError);
    }

    ctrl.activateRealm = activateRealm;
    ctrl.deleteRealm = deleteRealm;
    ctrl.$onInit = onInit.bind(this);
  }

  angular.module('agate.realm')
    .component('realmsList', {
      transclude: true,
      bindings: {
        locale: '<'
      },
      templateUrl: 'app/realm/components/realms-list/component.html',
      controller: [
        '$rootScope',
        '$scope',
        '$translate',
        'RealmsConfigResource',
        'RealmConfigResource',
        'NOTIFICATION_EVENTS',
        'AlertBuilder',
        Controller
      ]
    });
})();
