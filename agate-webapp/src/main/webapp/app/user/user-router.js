/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.user
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/users', {
          templateUrl: 'app/user/views/user-list.html',
          controller: 'UserListController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/users/requests', {
          templateUrl: 'app/user/views/user-request-list.html',
          controller: 'UserRequestListController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/user/new', {
          templateUrl: 'app/user/views/user-form.html',
          controller: 'UserEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/user/:id', {
          templateUrl: 'app/user/views/user-view.html',
          controller: 'UserViewController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/user/:id/edit', {
          templateUrl: 'app/user/views/user-form.html',
          controller: 'UserEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        });
    }]);
