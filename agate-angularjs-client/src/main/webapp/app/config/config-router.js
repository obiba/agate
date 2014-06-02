'use strict';

agate.config
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/config', {
          templateUrl: 'app/config/config-view.html',
          controller: 'AgateConfigController'
        })
        .when('/config/edit', {
          templateUrl: 'app/config/config-form.html',
          controller: 'AgateConfigEditController'
        });
    }]);
