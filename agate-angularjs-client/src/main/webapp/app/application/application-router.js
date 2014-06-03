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

  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/applications', {
          templateUrl: 'app/application/views/application-list.html',
          controller: 'ApplicationListController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/application/new', {
          templateUrl: 'app/application/views/application-form.html',
          controller: 'ApplicationEditController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/application/:id', {
          templateUrl: 'app/application/views/application-view.html',
          controller: 'ApplicationViewController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/application/:id/edit', {
          templateUrl: 'app/application/views/application-form.html',
          controller: 'ApplicationEditController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        });
    }]);
