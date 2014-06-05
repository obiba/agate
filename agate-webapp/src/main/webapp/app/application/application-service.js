/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.application

  .factory('ApplicationsResource', ['$resource',
    function ($resource) {
      return $resource('ws/applications', {}, {
        'get': {method: 'GET', errorHandler: true}
      });
    }])

  .factory('ApplicationResource', ['$resource',
    function ($resource) {
      return $resource('ws/application/:id', {}, {
        'get': {method: 'GET', params: {id: '@id'}}
      });
    }]);