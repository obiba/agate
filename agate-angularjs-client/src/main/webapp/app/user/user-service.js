'use strict';

agate.user
  .factory('UsersResource', ['$resource',
    function ($resource) {
      return $resource('ws/users');
    }])

  .factory('UserResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id', {}, {
        'get': {method: 'GET'}
      });
    }]);