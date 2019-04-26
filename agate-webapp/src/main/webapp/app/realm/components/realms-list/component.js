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

  function Controller(RealmsConfigResource, RealmConfigResource) {
    var ctrl = this;

    function init() {
      ctrl.realms = RealmsConfigResource.summaries();
    }

    function activateRealm(realm) {
      (
        realm.status === 'ACTIVE' ?
          RealmConfigResource.deactivate({name: realm.name}).$promise :
          RealmConfigResource.activate({name: realm.name}).$promise

      ).then(init);
    }

    function deleteRealm(realm) {
      RealmConfigResource.delete({name: realm.name}).$promise.then(init);
    }

    ctrl.activateRealm = activateRealm;
    ctrl.deleteRealm = deleteRealm;
    ctrl.$onInit = init;
  }

  angular.module('agate.realm')
    .component('realmsList', {
      transclude: true,
      templateUrl: 'app/realm/components/realms-list/component.html',
      controller: ['RealmsConfigResource', 'RealmConfigResource', Controller]
    });
})();
