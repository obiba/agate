'use strict';

agate.user
  .config(['$routeProvider',
    function ($routeProvider) {
      $routeProvider
        .when('/users', {
          templateUrl: 'app/user/views/user-list.html',
          controller: 'UserListController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/user/new', {
          templateUrl: 'app/user/views/user-form.html',
          controller: 'UserEditController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/user/:id', {
          templateUrl: 'app/user/views/user-view.html',
          controller: 'UserViewController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        })
        .when('/user/:id/edit', {
          templateUrl: 'app/user/views/user-form.html',
          controller: 'UserEditController',
          access: {
            authorizedRoles: ['AGATE_ADMIN']
          }
        });
    }]);
