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
    .factory('RealmConfigResource', ['$resource', 'LocalizedValues',
      function($resource, LocalizedValues) {

        function transformRealmFromResponse(response, getResponseHeaderCallBack, status) {
          if (status < 400) {
            var realm = JSON.parse(response);
            realm.title = LocalizedValues.arrayToObject(realm.title);
            realm.description = LocalizedValues.arrayToObject(realm.description);
            return realm;
          }

          return response;
        }

        function transformRealmForRequest(realm) {
          delete realm.safeTitle;
          realm.title = LocalizedValues.objectToArray(realm.title);
          realm.description = LocalizedValues.objectToArray(realm.description);
          return JSON.stringify(realm);
        }

        return $resource('ws/config/realm/:name', {},
          {
            'get': {
              method: 'GET',
              params: {name: '@name'},
              errorHandler: true,
              transformResponse: transformRealmFromResponse
            },
            'save': {
              method: 'PUT',
              params: {name: '@name'},
              errorHandler: true,
              transformRequest: transformRealmForRequest
            },
            'delete': {method: 'DELETE', params: {name: '@name'}, errorHandler: true},
            'users': {url: 'ws/config/realm/:name/users', method: 'GET', params: {name: '@name'}, errorHandler: true},
            'activate': {url: 'ws/config/realm/:name/active', method: 'PUT', params: {name: '@name'}, errorHandler: true},
            'deactivate': {url: 'ws/config/realm/:name/active', method: 'DELETE', params: {name: '@name'}, errorHandler: true}
          });
      }]);
})();

