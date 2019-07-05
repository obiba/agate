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
    .factory('RealmTransformer', ['LocalizedValues', function(LocalizedValues) {

      function transformUserInfoMappingForRequest(realm) {
        if (realm.userInfoMapping) {
          realm.userInfoMappings = [];
          Object.keys(realm.userInfoMapping).forEach(function(key) {
            if (realm.userInfoMapping[key]) {
              realm.userInfoMappings.push({key: key, value: realm.userInfoMapping[key]});
            }
          });

          // client side only a DTOs do not have maps
          delete realm.userInfoMapping;
        }
      }

      function transformUserInfoMappingForResponse(realm) {
        if (realm.userInfoMappings) {
          realm.userInfoMapping = {};
          realm.userInfoMappings.forEach(function(entry) {
            if (entry.key && entry.value) {
              realm.userInfoMapping[entry.key] = entry.value;
            }
          });
        }
      }

      function transformRealmFromResponse(response, getResponseHeaderCallBack, status) {
        if (status < 400) {
          var realm = JSON.parse(response);
          realm.title = LocalizedValues.arrayToObject(realm.title);
          realm.description = LocalizedValues.arrayToObject(realm.description);
          transformUserInfoMappingForResponse(realm);
          return realm;
        }

        return response;
      }

      function transformRealmForRequest(realm) {
        delete realm.safeTitle;
        realm.title = LocalizedValues.objectToArray(realm.title);
        realm.description = LocalizedValues.objectToArray(realm.description);
        transformUserInfoMappingForRequest(realm);

        return JSON.stringify(realm);
      }

      return {
        transformForResponse: transformRealmFromResponse,
        transformForRequest: transformRealmForRequest
      };
    }]);
})();
