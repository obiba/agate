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
    .factory('RealmConfigResource', ['$resource', 'RealmTransformer',
      function($resource, RealmTransformer) {


        return $resource(contextPath + '/ws/config/realm/:name', {},
          {
            'get': {
              method: 'GET',
              params: {name: '@name'},
              errorHandler: true,
              transformResponse: RealmTransformer.transformForResponse
            },
            'save': {
              method: 'PUT',
              params: {name: '@name'},
              errorHandler: true,
              transformRequest: RealmTransformer.transformForRequest
            },
            'delete': {method: 'DELETE', params: {name: '@name'}, errorHandler: true},
            'users': {url: contextPath + '/ws/config/realm/:name/users', method: 'GET', params: {name: '@name'}, errorHandler: true},
            'activate': {url: contextPath + '/ws/config/realm/:name/active', method: 'PUT', params: {name: '@name'}, errorHandler: true},
            'deactivate': {url: contextPath + '/ws/config/realm/:name/active', method: 'DELETE', params: {name: '@name'}, errorHandler: true}
          });
      }]);
})();

