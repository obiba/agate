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

    function onChanges(changes) {
      if (changes.users) {
        (ctrl.users || []).forEach(function(user) {
          user.fullName = user.firstName && user.lastName ? user.firstName + ' ' + user.lastName : user.name;
        });
      }
    }

    ctrl.$onChanges = onChanges.bind(this);
  }

  angular.module('agate')
    .component('usersSummaries', {
      transclude: true,
      bindings: {
        users: '<'
      },
      templateUrl: 'app/commons/users-summaries/component.html',
      controller: Controller
    });
})();
