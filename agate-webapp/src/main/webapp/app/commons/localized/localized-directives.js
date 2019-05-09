/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

agate.localized

  .directive('localized', ['LocalizedValues', function (LocalizedValues) {
    return {
      restrict: 'AE',
      scope: {
        value: '=',
        lang: '=',
        ellipsisSize: '=',
        markdownIt: '=',
        keyLang: '@',
        keyValue: '@'
      },
      templateUrl: 'app/commons/localized/localized-template.html',
      link: function(scope) {
        scope.keyLang = scope.keyLang || 'lang';
        scope.keyValue = scope.keyValue || 'value';
        scope.LocalizedValues = LocalizedValues;
      }
    };
  }])

  .directive('localizedInput', [function () {
    return {
      restrict: 'AE',
      require: '^form',
      scope: {
        name: '@',
        model: '=',
        label: '@',
        required: '@',
        lang: '=',
        help: '@'
      },
      templateUrl: 'app/commons/localized/localized-input-template.html',
      link: function ($scope, elem, attr, ctrl) {
        if (angular.isUndefined($scope.model) || $scope.model === null) {
          $scope.model = [
            {lang: $scope.lang, value: ''}
          ];
        }
        $scope.fieldName = $scope.name + '-' + $scope.lang;
        $scope.form = ctrl;
//        console.log('localizedInput', $scope);
      }
    };
  }])

  .directive('localizedTextarea', [function () {
    return {
      restrict: 'AE',
      require: '^form',
      scope: {
        name: '@',
        model: '=',
        label: '@',
        required: '@',
        disabled: '=',
        lang: '=',
        help: '@',
        rows: '@',
        customValidator: '='
      },
      templateUrl: 'app/commons/localized/localized-textarea-template.html',
      link: function ($scope, elem, attr, ctrl) {
        if (angular.isUndefined($scope.model) || $scope.model === null) {
          $scope.model = [
            {lang: $scope.lang, value: ''}
          ];
        }

        $scope.$watch('model', function(newModel) {
          if (angular.isUndefined(newModel) || newModel === null) {
            $scope.model = [{lang: $scope.lang, value: ''}];
          }

          var currentLang = $scope.model.filter(function(e) {
            if (e.lang === $scope.lang) {
              return e;
            }
          });

          if (currentLang.length === 0) {
            $scope.model.push({lang:$scope.lang, value: ''});
          }
        }, true);

        $scope.fieldName = $scope.name + '-' + $scope.lang;
        $scope.form = ctrl;
      }
    };
  }]);
