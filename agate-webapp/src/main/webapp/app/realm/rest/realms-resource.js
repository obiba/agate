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

(function() {
  angular.module('agate.realm')
    .factory('RealmsResource', ['$resource',
      function($resource) {

        return $resource('ws/realms', {},
          {
            'get': {
              url: 'ws/realms',
              method: 'GET',
              errorHandler: true
            }
          });
      }]);
})();

