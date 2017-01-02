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

agate.config
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/admin/general', {
          templateUrl: 'app/config/views/config-view.html',
          controller: 'ConfigurationController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/admin/general/edit', {
          templateUrl: 'app/config/views/config-form.html',
          controller: 'ConfigurationEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/admin/style/edit', {
          templateUrl: 'app/config/views/config-style-form.html',
          controller: 'ConfigurationStyleEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/admin/translations/edit', {
          templateUrl: 'app/config/views/config-translations-form.html',
          controller: 'ConfigurationTranslationsEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        });
    }]);
