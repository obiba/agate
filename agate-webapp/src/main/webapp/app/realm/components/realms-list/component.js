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

  function Controller() {
    var ctrl = this;

    function init() {
    }

    ctrl.$onInit = init;
  }

  angular.module('agate.realm')
    .component('realmsList', {
      transclude: true,
      bindings: {
        realms: '<'
      },
      templateUrl: 'app/realm/components/realms-list/component.html',
      controller: Controller
    });
})();
