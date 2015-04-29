'use strict';

agate.user
  .factory('UsersResource', ['$resource',
    function ($resource) {
      return $resource('ws/users', {}, {
        'get': {method: 'GET', errorHandler: true},
        'save': {method: 'POST', errorHandler: true}
      });
    }])

  .factory('UserResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id', {}, {
        'save': {method: 'PUT', params: {id: '@id'}, errorHandler: true},
        'get': {method: 'GET', params: {id: '@id'}},
        'delete': {method: 'DELETE', params: {id: '@id'}, errorHandler: true}
      });
    }])

  .factory('UserStatusResource', ['$log','$filter', function($log, $filter) {
    var nameValueList = [
      {label: $filter('translate')('user_status.active'), value: 'ACTIVE'},
      {label: $filter('translate')('user_status.pending'), value: 'PENDING'},
      {label: $filter('translate')('user_status.approved'), value: 'APPROVED'},
      {label: $filter('translate')('user_status.inactive'), value: 'INACTIVE'}
    ];

    return {
      'activeIndex': function() {
        return 0;
      },

      'listAsNameValue': function() {
        return nameValueList;
      },

      'findIndex': function(status) {
        return status.indexOf(status);
      }
    };
  }]);
