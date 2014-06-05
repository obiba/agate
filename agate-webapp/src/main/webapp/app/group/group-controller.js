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

  .controller('GroupListController', ['$scope', 'GroupsResource',

    function ($scope, GroupsResource) {

      $scope.groups = GroupsResource.query();

      $scope.deleteGroup = function (id) {
        //TODO ask confirmation
        GroupResource.delete({id: id},
          function () {
            $scope.groups = GroupsResource.query();
          });
      };

    }]);
