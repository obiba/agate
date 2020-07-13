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
    .factory('RealmsConfigResource', ['$resource', 'RealmTransformer',
      function($resource, RealmTransformer) {

        return $resource(contextPath + '/ws/config/realms', {},
          {
            'create': {
              url: contextPath + '/ws/config/realms',
              method: 'POST',
              errorHandler: true,
              transformRequest: RealmTransformer.transformForRequest
            },
            'summaries': {
              url: contextPath + '/ws/config/realms/summaries',
              method: 'GET',
              isArray: true,
              errorHandler: true
            }
          });
      }]);
})();

