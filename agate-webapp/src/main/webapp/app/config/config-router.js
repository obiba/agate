'use strict';

agate.config
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/config', {
          templateUrl: 'app/config/views/config-view.html',
          controller: 'ConfigurationController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/config/edit', {
          templateUrl: 'app/config/views/config-form.html',
          controller: 'ConfigurationEditController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        });
    }]);
