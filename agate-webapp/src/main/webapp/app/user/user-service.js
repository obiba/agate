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

agate.user
  .factory('UsersResource', ['$resource',
    function ($resource) {
      return $resource('ws/users', {}, {
        'get': {method: 'GET', errorHandler: true},
        'save': {method: 'POST', errorHandler: true}
      });
    }])

  .factory('UserResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id', {}, {
        'save': {method: 'PUT', params: {id: '@id'}, errorHandler: true},
        'get': {method: 'GET', params: {id: '@id'}},
        'delete': {method: 'DELETE', params: {id: '@id'}, errorHandler: true}
      });
    }])

  .factory('UserAuthorizationsResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id/authorizations', {}, {
        'get': {method: 'GET', params: {id: '@id'}}
      });
    }])

  .factory('UserAuthorizationResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id/authorization/:authz', {}, {
        'get': {method: 'GET', params: {id: '@id', authz: '@authz'}},
        'delete': {method: 'DELETE', params: {id: '@id', authz: '@authz'}, errorHandler: true}
      });
    }])

  .factory('UserStatusResource', ['$log', '$filter', function ($log, $filter) {
    var nameValueList = [
      {label: $filter('translate')('user.ACTIVE'), value: 'ACTIVE'},
      {label: $filter('translate')('user.PENDING'), value: 'PENDING'},
      {label: $filter('translate')('user.APPROVED'), value: 'APPROVED'},
      {label: $filter('translate')('user.INACTIVE'), value: 'INACTIVE'}
    ];

    return {
      'activeIndex': function () {
        return 0;
      },

      'listAsNameValue': function () {
        return nameValueList;
      },

      'findIndex': function (value) {
        return nameValueList.map(function(nameValue) {
          return nameValue.value;
        }).indexOf(value);
      }
    };
  }])

  .factory('UserResetPasswordResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id/reset_password', {}, {
        'resetPassword': {method: 'PUT', params: {id: '@id'}}
      });
    }]);
