'use strict';

agate.user
  .factory('UsersResource', ['$resource',
    function ($resource) {
      return $resource('ws/users', {}, {
        'get': {method: 'GET', errorHandler: true}
      });
    }])

  .factory('UserResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id', {}, {
        'get': {method: 'GET', params: {id: '@id'}}
      });
    }]);