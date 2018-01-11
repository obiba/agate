/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
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
        'get': {method: 'GET', errorHandler: true}
      });
    }])

  .factory('GroupResource', ['$resource',
    function ($resource) {
      return $resource('ws/group/:id', {}, {
        'get': {method: 'GET', params: {id: '@id'}},
        'update': {method: 'PUT', params: {id: '@id'}}
      });
    }]);
