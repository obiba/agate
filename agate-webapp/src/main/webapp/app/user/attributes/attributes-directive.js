/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.user
  .directive('attributesView', [function () {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        attributes: '=',
        attributesConfig: '='
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
        attributesConfig: '=',
        usedAttributeNames: '='
      },
      templateUrl: 'app/user/attributes/views/attributes-form-template.html'
    };
  }])

  .directive('attributeItem', ['$compile', 'AttributesService', function ($compile, AttributesService) {
    var linker = function(scope, element, attrs) {
      if (scope.attributeConfig.type === 'BOOLEAN') {
        scope.attribute.boolValue = scope.attribute.value === "true";
      }

      scope.$watch('attribute.boolValue', function () {
        if (scope.attributeConfig.type === 'BOOLEAN') {
          scope.attribute.value = scope.attribute.boolValue ? 'true' : 'false';
        }
      });

      element.html(AttributesService.getAttributeItemTemplate(scope.attributeConfig)).show();
      $compile(element.contents())(scope);
    };

    return {
      restrict: "E",
      link: linker,
      scope: {
        attribute:'=',
        attributeConfig:'='
      }
    };
  }]);
