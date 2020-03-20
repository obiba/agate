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
    .config(['$routeProvider',
      function($routeProvider) {
        $routeProvider.when('/admin/realms', {
          templateUrl: 'app/realm/views/realms.html',
          controller: 'RealmListController'
        });

        $routeProvider.when('/admin/realm/new', {
          templateUrl: 'app/realm/views/realm-form.html',
          controller: 'RealmFormController'
        });

        $routeProvider.when('/admin/realm/:name', {
          templateUrl: 'app/realm/views/realm-view.html',
          controller: 'RealmViewController'
        });

        $routeProvider.when('/admin/realm/:name/edit', {
          templateUrl: 'app/realm/views/realm-form.html',
          controller: 'RealmFormController'
        });
      }]);

})();
