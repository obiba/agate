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

agate.ticket

  .controller('TicketListController', ['$rootScope', '$scope', 'TicketsResource',
    'TicketResource', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, TicketsResource, TicketResource, NOTIFICATION_EVENTS) {

      $scope.tickets = TicketsResource.query();

      $scope.deleteTicket = function (id) {
        $scope.groupToDelete = id;
        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {title: 'Delete Ticket', message: 'Are you sure to delete the ticket?'}, id);
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.groupToDelete === id) {

          TicketResource.delete({id: id},
            function () {
              $scope.tickets = TicketsResource.query();
            });
        }
      });
    }])
  .controller('TicketViewController', ['$scope', '$routeParams', 'TicketResource',

    function ($scope, $routeParams, TicketResource) {
      $scope.ticket = TicketResource.get({id: $routeParams.id});
    }]);
