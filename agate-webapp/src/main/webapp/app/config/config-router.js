'use strict';

agate.config
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/config', {
          templateUrl: 'app/config/views/config-view.html',
          controller: 'ConfigurationController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        })
        .when('/config/edit', {
          templateUrl: 'app/config/views/config-form.html',
          controller: 'ConfigurationEditController',
          access: {
            authorizedRoles: ['agate-administrator']
          }
        });
    }]);
