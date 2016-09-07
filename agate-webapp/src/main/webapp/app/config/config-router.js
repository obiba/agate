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
