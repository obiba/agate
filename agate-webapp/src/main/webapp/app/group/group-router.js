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

agate.group

  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/groups', {
          templateUrl: 'app/group/views/group-list.html',
          controller: 'GroupListController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/group/new', {
          templateUrl: 'app/group/views/group-form.html',
          controller: 'GroupEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/group/:id', {
          templateUrl: 'app/group/views/group-view.html',
          controller: 'GroupViewController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/group/:id/edit', {
          templateUrl: 'app/group/views/group-form.html',
          controller: 'GroupEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        });
    }]);
