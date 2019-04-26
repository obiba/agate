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

(function() {

  angular.module('agate.realm')
    .controller('RealmListController',
      function() {
      }
    )

    .controller('RealmFormController',
      ['$rootScope', '$scope', '$routeParams', '$translate', '$location',
        function($rootScope, $scope, $routeParams, $translate, $location) {
          function onSave() {
            $location.path('/admin/realms').replace();
          }
          $scope.name = $routeParams.name;
          $scope.locale = {language: $translate.use()};
          $scope.onSave = onSave;
          $rootScope.$on('$translateChangeSuccess', function (event, locale) {
            $scope.locale = locale;
          });
        }
      ]);

})();
