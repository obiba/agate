'use strict';

agate.config.factory('ConfigurationResource', ['$resource',
  function ($resource) {
    return $resource('ws/config', {}, {
      // override $resource.save method because it uses POST by default
      'save': {method: 'PUT'},
      'get': {method: 'GET'}
    });
  }])
  .factory('KeyStoreResource', ['$resource',
    function ($resource) {
      return $resource('ws/config/keystore/system/https', {}, {
        'save': {method: 'PUT'}
      });
    }]);
