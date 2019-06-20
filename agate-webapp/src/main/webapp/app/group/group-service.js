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

agate.group

  .factory('GroupsResource', ['$resource',
    function ($resource) {
      return $resource('ws/groups', {}, {
        'get': {method: 'GET', errorHandler: true},
        'save': {method: 'POST', errorHandler: true}
      });
    }])

  .factory('GroupResource', ['$resource',
    function ($resource) {
      return $resource('ws/group/:id', {}, {
        'get': {method: 'GET', params: {id: '@id'}, errorHandler: true},
        'update': {method: 'PUT', params: {id: '@id'}, errorHandler: true},
        'delete': {method: 'DELETE', params: {id: '@id'}, errorHandler: true},
        'users': {url: 'ws/group/:id/users', method: 'PUT', params: {id: '@id'}, errorHandler: true}
      });
    }]);
