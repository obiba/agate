'use strict';

agate.config
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/config', {
          templateUrl: 'app/config/views/config-view.html',
          controller: 'AgateConfigController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/config/edit', {
          templateUrl: 'app/config/views/config-form.html',
          controller: 'AgateConfigEditController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        });
    }]);
