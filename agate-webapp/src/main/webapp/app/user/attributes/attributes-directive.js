'use strict';

agate.user
  .directive('attributesView', [function () {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        attributes: '='
      },
      templateUrl: 'app/user/attributes/views/attributes-view-template.html'
    };
  }])

  .directive('attributesForm', [function () {
    return {
      restrict: 'E',
      replace: true,
      controller: 'AttributesFormController',
      scope: {
        attributes: '=',
        attributesConfig: '='
      },
      templateUrl: 'app/user/attributes/views/attributes-form-template.html'
    };
  }])

  .directive('attributeItem', ['$compile', 'AttributesService', function ($compile, AttributesService) {
    var linker = function(scope, element, attrs) {
      element.html(AttributesService.getAttributeItemTemplate(scope.attributeConfig, scope.simple)).show();
      $compile(element.contents())(scope);
    };

    return {
      restrict: "E",
      link: linker,
      scope: {
        attribute:'=',
        attributeConfig:'=',
        simple:'='
      }
    };
  }]);
