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

agate.ticket

  .controller('TicketListController', ['$rootScope', '$scope', 'TicketsResource',
    'TicketResource', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, TicketsResource, TicketResource, NOTIFICATION_EVENTS) {
      var onSuccess = function(response) {
        $scope.tickets = response;
        $scope.loading = false;
      };

      var onError = function() {
        $scope.loading = false;
      };

      $scope.loading = true;
      TicketsResource.query({}, onSuccess, onError);

      $scope.deleteTicket = function (id) {
        $scope.groupToDelete = id;
        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {title: 'Delete Ticket', message: 'Are you sure to delete the ticket?'}, id);
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.groupToDelete === id) {

          TicketResource.delete({id: id},
            function () {
              $scope.loading = true;
              TicketsResource.query({}, onSuccess, onError);
            });
        }
      });

      // Extract header and claims from Base64 encoded token string
      $scope.decodeToken = function() {
        if (!$scope.token) {
          $scope.jwt = null;
        } else {
          try {
            var jwt = $scope.token.split('.');
            var header = JSON.stringify(JSON.parse(window.atob(jwt[0])), null, 4);
            var claims = JSON.stringify(JSON.parse(window.atob(jwt[1])), null, 4);
            $scope.jwt = { header: header, claims: claims };
          } catch(err) {
            $scope.jwt = null;
          }
        }
      };

      $scope.token = null;
      $scope.jwt = null;
    }])
  .controller('TicketViewController', ['$scope', '$routeParams', 'TicketResource',

    function ($scope, $routeParams, TicketResource) {


      TicketResource.get({id: $routeParams.id}, function(ticket) {
        $scope.ticket = ticket;
      });
    }]);
