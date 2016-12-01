'use strict';

angular.module('obiba.form')

  // http://codetunes.com/2013/server-form-validation-with-angular
  .directive('formServerError', [function () {
    return {
      restrict: 'A',
      require: '?ngModel',
      link: function (scope, element, attrs, ctrl) {
        return element.on('change', function () {
          return scope.$apply(function () {
            return ctrl.$setValidity('server', true);
          });
        });
      }
    };
  }])

  .directive('formInput', [function () {
    return {
      restrict: 'AE',
      require: '^form',
      scope: {
        name: '@',
        model: '=',
        disabled: '=',
        type: '@',
        pattern: '=',
        label: '@',
        required: '=',
        min: '@',
        max: '@',
        step: '@',
        help: '@',
        placeholder: '@',
        readonly: '@'
      },
      templateUrl: 'form/form-input-template.tpl.html',
      compile: function(elem, attrs) {
        if (!attrs.type) { attrs.type = 'text'; }
        return {
          post: function (scope, elem, attr, ctrl) {
            scope.form = ctrl;
          }
        };
      }
    };
  }])

  .directive('formTextarea', [function () {
    return {
      restrict: 'AE',
      require: '^form',
      scope: {
        name: '@',
        model: '=',
        disabled: '=',
        label: '@',
        required: '=',
        help: '@'
      },
      templateUrl: 'form/form-textarea-template.tpl.html',
      compile: function(elem, attrs) {
        if (!attrs.type) { attrs.type = 'text'; }
        return {
          post: function ($scope, elem, attr, ctrl) {
            $scope.form = ctrl;
          }
        };
      }
    };
  }])

  .directive('formLocalizedInput', [function () {
    return {
      restrict: 'AE',
      require: '^form',
      scope: {
        locales: '=',
        name: '@',
        model: '=',
        label: '@',
        required: '=',
        help: '@'
      },
      templateUrl: 'form/form-localized-input-template.tpl.html',
      link: function ($scope, elem, attr, ctrl) {
        $scope.form = ctrl;
      }
    };
  }])

  .directive('formRadio', [function () {
    return {
      restrict: 'AE',
      require: '^form',
      scope: {
        name: '@',
        gid: '@',
        model: '=',
        value: '=',
        label: '@',
        help: '@'
      },
      templateUrl: 'form/form-radio-template.tpl.html',
      link: function ($scope, elem, attr, ctrl) {
        $scope.form = ctrl;
      }
    };
  }])

  .directive('formRadioGroup', [function() {
    return {
      restrict: 'AE',
      scope: {
        options: '=',
        model: '='
      },
      templateUrl: 'form/form-radio-group-template.tpl.html',
      link: function ($scope) {
        $scope.gid = $scope.$id;
      }
    };
  }])

  .directive('formCheckbox', [function () {
    return {
      restrict: 'AE',
      require: '^form',
      scope: {
        name: '@',
        gid: '@',
        model: '=',
        required: '=',
        disabled: '=',
        label: '@',
        help: '@'
      },
      templateUrl: 'form/form-checkbox-template.tpl.html',
      link: function ($scope, elem, attr, ctrl) {
        $scope.form = ctrl;
      }
    };
  }])

  .directive('formCheckboxGroup', [function() {
    return {
      restrict: 'A',
      scope: {
        options: '=',
        model: '='
      },
      template: '<div form-checkbox ng-repeat="item in items" name="{{item.name}}" model="item.value" gid="${{gid}}" label="{{item.label}}">',
      link: function ($scope, elem, attrs) {
        $scope.gid = $scope.$id;
        $scope.$watch('model', function(selected) {
          if (!selected || !$scope.options) {
            return;
          }

          $scope.items = $scope.options.map(function(n) {
            var value = angular.isArray(selected) && (selected.indexOf(n) > -1 ||
              selected.indexOf(n.name) > -1);
            return {
              name: attrs.model + '.' + (n.name || n),
              label: n.label || n,
              value: value
            };
          });
        }, true);

        $scope.$watch('items', function(items) {
          if (items && angular.isArray(items)) {
            $scope.model = items.filter(function(e) { return e.value; })
              .map(function(e) { return e.name.replace(attrs.model + '.', ''); });
          }
        }, true);

        $scope.$watch('options', function(opts) {
          if (!opts) {
            return;
          }

          $scope.items = opts.map(function(n) {
            var value = angular.isArray($scope.model) && ($scope.model.indexOf(n) > -1 ||
              $scope.model.indexOf(n.name) > -1);
            return {
              name: attrs.model + '.' + (n.name || n),
              label: n.label || n,
              value: value
            };
          });
        }, true);
      }
    };
  }])

  .directive('formUiSelect', [function() {
    return {
      restrict: 'AE',
      scope: {
        title: '=',
        showTitle: '=',
        description: '=',
        autoComplete: '=',
        disabled: '=',
        items: '=',
        model: '=',
        multiple: '='
      },
      templateUrl: 'form/form-ui-select.tpl.html',
      link: function ($scope) {

        var defaultAutoComplete = {
          format: ':label (:value)',
          label: 'label',
          value: 'value'
        };

        $scope.data = {};

        $scope.$watch('model', function(){
          if ($scope.model) {
            $scope.data.selected = $scope.model;
          }
        });

        $scope.$watchCollection('data.selected', function() {
          if ($scope.data.selected) {
            $scope.model = $scope.data.selected;
          }
        });

        function initializeAutoComplete() {
          if ($scope.autoComplete) {
            $scope.autoComplete.format = $scope.autoComplete.format || defaultAutoComplete.format;
            $scope.autoComplete.label = $scope.autoComplete.label || defaultAutoComplete.label;
            $scope.autoComplete.value = $scope.autoComplete.value || defaultAutoComplete.value;
          } else {
            $scope.autoComplete = defaultAutoComplete;
          }
        }

        $scope.formatList = function(item) {
          return $scope.autoComplete.format
            .replace(':label', item[$scope.autoComplete.label])
            .replace(':value', item[$scope.autoComplete.value]);
        };

        $scope.$watch('autoComplete', initializeAutoComplete);
        $scope.$watch('items', function() {
          if ($scope.items) {
            initializeAutoComplete();
          }
        });

      }
    };

  }]);
