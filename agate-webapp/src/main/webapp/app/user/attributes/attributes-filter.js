'use strict';

agate.user

  .filter('nonConfigAttributes', ['AttributesService', function(AttributesService) {
    return function(attributes, attributesConfig) {
      return AttributesService.findNonConfigAttributes(attributes, attributesConfig);
    };
  }])

  .filter('requiredConfigAttributes', ['AttributesService', function(AttributesService) {
    return function(attributeConfigPairs) {
      return AttributesService.findRequiredConfigAttributes(attributeConfigPairs);
    };
  }])

  .filter('nonRequiredConfigAttributes', ['AttributesService', function(AttributesService) {
    return function(attributeConfigPairs) {
      return AttributesService.findNonRequiredConfigAttributes(attributeConfigPairs);
    };
  }]);
