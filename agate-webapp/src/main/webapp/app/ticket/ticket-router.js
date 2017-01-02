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

agate.ticket

  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/tickets', {
          templateUrl: 'app/ticket/views/ticket-list.html',
          controller: 'TicketListController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/ticket/:id', {
          templateUrl: 'app/ticket/views/ticket-view.html',
          controller: 'TicketViewController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        });
    }]);
