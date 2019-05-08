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

  function Controller(RealmConfigResource, RealmsService, JsonUtils, AlertBuilder) {

    var ctrl = this;

    function onError(response) {
      AlertBuilder.newBuilder().response(response).delay(0).build();
    }

    function init() {
      RealmConfigResource.get({name: ctrl.name, includeUsers: true}).$promise
        .then(function(realm) {
          ctrl.realm = realm;
          ctrl.realm.safeTitle = RealmsService.ensureRealmTitle(ctrl.realm);
          ctrl.realm.content = JsonUtils.parseJsonSafely(realm.content);
        }).catch(onError);
    }

    function onChanges(changed) {
      if (changed.locale.currentValue && changed.locale.currentValue.language) {
        init();
      }
    }

    ctrl.$onChanges = onChanges.bind(this);
  }

  var injections = [
    'RealmConfigResource',
    'RealmsService',
    'JsonUtils',
    'AlertBuilder'
  ];

  angular.module('agate.realm')
    .component('realmView', {
      transclude: true,
      bindings: {
        name: '<',
        locale: '<',
        onSave: '&'
      },
      templateUrl: 'app/realm/components/realm-view/component.html',
      controller: [].concat(injections, Controller)
    });
})();
