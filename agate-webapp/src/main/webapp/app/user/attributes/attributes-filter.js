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
  }])

  .filter('namesFromAttributeConfigPairs', ['AttributesService', function(AttributesService) {
    return function(attributeConfigPairs) {
      return AttributesService.getUsedAttributeNames(attributeConfigPairs);
    };
  }]);
