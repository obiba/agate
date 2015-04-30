'use strict';

agate.user
  .factory('AttributesService', ['$log',
    function ($log) {

      return {
        'findNonConfigAttributes' : function(attributes, attributesConfig) {

          if (attributes && attributesConfig) {
            return attributes.filter(function (attribute) {
              return attributesConfig.filter(function (attributeConfig) {
                  return attributeConfig.name === attribute.name;
              }).length === 0;
            });
          }

          return [];
        },

        'findConfigAttributes' : function(attributes, attributesConfig) {
          if (attributes && attributesConfig) {
            return attributes.filter(function (attribute) {
              return attributesConfig.filter(function (attributeConfig) {
                  var found = attributeConfig.name === attribute.name;
                  if (found) attribute.type = attributeConfig.type;
                  return found;
              }).length > 0;
            });
          }

          return [];
        },

        'findNewAttributes': function(attributes, newAttributes) {
          if (attributes) {
            var newones =
              newAttributes.filter(function (newAttribute) {
              return attributes.filter(function (attribute) {
                  return newAttribute.name === attribute.name;
                }).length === 0;
            });

            return newones;
          }

          return newAttributes;
        },

        'findRequiredConfigAttributes': function(attributeConfigPairs) {
          if (attributeConfigPairs && attributeConfigPairs.length > 0) {
            return attributeConfigPairs.filter(function(attributeConfigPair) {
              return attributeConfigPair.attributeConfig.required;
            });
          }

          return [];
        },

        'findNonRequiredConfigAttributes': function(attributeConfigPairs) {
          if (attributeConfigPairs) {
            return attributeConfigPairs.filter(function(attributeConfigPair) {
              return !attributeConfigPair.attributeConfig.required;
            });
          }

          return [];
        },

        'getAttributeConfigPairs' :function(attributes, attributesConfig) {
          var result = [];

          if (attributesConfig && attributesConfig.length > 0) {
            if (attributes) {
              result = createAttributeConfigPair(attributes, attributesConfig);
              var unused = findUnusedAttributesConfig(attributes, attributesConfig);
              if (unused) {
                unused.forEach(function(attributeConfig){
                  result.push({attribute: createAttributeFromConfig(attributeConfig), attributeConfig: attributeConfig});
                });
              }
            } else {
              attributesConfig.forEach(function(attributeConfig) {
                result.push({attribute: createAttributeFromConfig(attributeConfig), attributeConfig: attributeConfig});
              });
            }
          }

          return result;
        },

        'getAttributeItemTemplate' : function(attributeConfig) {
          var required = attributeConfig.required;
          var template = '';
          switch (attributeConfig.type) {
            case 'BOOLEAN':
                template = booleanTemplate();
              break;
            case 'DECIMAL':
            case 'INTEGER':
            case 'STRING':
              if (attributeConfig.values && attributeConfig.values.length > 0) {
                template = singleChosenTemplate(required);
              } else {
                template = inputTemplate(required);
              }
              break;

          }

          return template;
        }
      };

      function findUnusedAttributesConfig(attributes, attributesConfig) {
        return attributes ? attributesConfig.filter(findUnusedAttributesConfigInternal(attributes.map(function(attribute){
          return attribute.name;
        }))) : attributesConfig;
      }

      function createAttributeConfigPair(attributes, attributesConfig) {
        var result = [];
        if (attributes && attributes.length > 0) {
          attributes.forEach(function(attribute){

            var attributeConfig = attributesConfig.filter(filterAttributeConfig(attribute))[0];
            if (attributeConfig) {
              result.push(
                {
                  attribute: attribute,
                  attributeConfig: attributesConfig.filter(filterAttributeConfig(attribute))[0]
                });
            }
          });
        }

        return result;
      }

      function createAttributeFromConfig(attributeConfig) {
        var attribute = {name: attributeConfig.name};
        switch (attributeConfig.type) {
          case 'BOOLEAN':
            attribute.value = 'false';
            break;
          case 'DECIMAL':
          case 'INTEGER':
          case 'STRING':
            // leave empty
            break;
        }

        return attribute;
      }

      /**
       * Filters the array of config to match the corresponding attribute's config
       * @param attribute
       * @returns {Function}
       */
       function filterAttributeConfig(attribute) {
        return function(conf) {
          return conf.name === attribute.name;
        };
      }

      /**
       * Returns attributes that are not in the input list
       * @param attribute
       * @returns {Function}
       */
      function findUnusedAttributesConfigInternal(attributes) {
        return function(conf) {
          return attributes.indexOf(conf.name) === -1;
        };
      }

      function inputTemplate(required) {
        var requiredAttr = required ? 'required="true"' : '';
        return '<div form-input name="attribute.name" model="attribute.value" label="{{attribute.name}}" ' + requiredAttr + '></div>';
      }

      function singleChosenTemplate(required) {
        var requiredAttr = required ? 'required' : '';
        var requiredMarker = required ? '*' : '';
        return '<div class="form-group"> <label for="attribute.name" class="control-label"> <span>{{attribute.name}}</span> '+ requiredMarker +' </label> <select id="attribute.name" name="attribute.name" class="form-control" ng-model="attribute.value" ng-options="t for t in attributeConfig.values"></select> </div>';
      }

      function booleanTemplate() {
        return '<div form-checkbox name="{{attribute.name}}" model="attribute.boolValue" help="{{attribute.name}}"></div>';
      }

    }]);
