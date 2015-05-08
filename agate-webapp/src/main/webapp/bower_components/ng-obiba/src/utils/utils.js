'use strict';

angular.module('obiba.utils', [])

  .service('StringUtils', function () {
    this.capitaliseFirstLetter = function (string) {
      return string ? string.charAt(0).toUpperCase() + string.slice(1) : null;
    };
  })

  .service('LocaleStringUtils', ['$filter', function ($filter) {
    this.translate = function (key, args) {

      function buildMessageArguments(args) {
        if (args &&  args instanceof Array) {
          var messageArgs = {};
          args.forEach(function(arg, index) {
            messageArgs['arg'+index] = arg;
          });

          return messageArgs;
        }

        return {};
      }

      return $filter('translate')(key, buildMessageArguments(args));
    };
  }]);
