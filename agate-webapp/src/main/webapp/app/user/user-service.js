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

  .factory('UserAuthorizationsResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id/authorizations', {}, {
        'get': {method: 'GET', params: {id: '@id'}}
      });
    }])

  .factory('UserAuthorizationResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id/authorization/:authz', {}, {
        'get': {method: 'GET', params: {id: '@id', authz: '@authz'}},
        'delete': {method: 'DELETE', params: {id: '@id', authz: '@authz'}, errorHandler: true}
      });
    }])

  .factory('UserStatusResource', ['$log', '$filter', function ($log, $filter) {
    var nameValueList = [
      {label: $filter('translate')('user.ACTIVE'), value: 'ACTIVE'},
      {label: $filter('translate')('user.PENDING'), value: 'PENDING'},
      {label: $filter('translate')('user.APPROVED'), value: 'APPROVED'},
      {label: $filter('translate')('user.INACTIVE'), value: 'INACTIVE'}
    ];

    return {
      'activeIndex': function () {
        return 0;
      },

      'listAsNameValue': function () {
        return nameValueList;
      },

      'findIndex': function (value) {
        return nameValueList.map(function(nameValue) {
          return nameValue.value;
        }).indexOf(value);
      }
    };
  }])

  .factory('UserResetPasswordResource', ['$resource',
    function ($resource) {
      return $resource('ws/user/:id/reset_password', {}, {
        'resetPassword': {method: 'PUT', params: {id: '@id'}}
      });
    }])

  .service('RealmsService', ['$q', '$translate', 'RealmsConfigResource', 'LocalizedValues',
    function($q, $translate, RealmsConfigResource, LocalizedValues) {
      var DEFAULT_AGATE_REALM = null;
      var REALMS = null;

      function createDefaultRealm() {
        var deferred = $q.defer();

        if (DEFAULT_AGATE_REALM) {
          deferred.resolve(DEFAULT_AGATE_REALM);
        } else {
          DEFAULT_AGATE_REALM = {
            name: 'agate-user-realm',
            title: [],
            description: []
          };

          var languages = $translate.getAvailableLanguageKeys();
          var promises = (languages || ['en']).map(function (language) {
            return $translate(['realm.agate-user-realm', 'realm.default-help'], null, null, null, language);
          });

          $q.all(promises).then(function(translations) {
            translations.forEach(function(translation, index) {
              var lang = languages[index];
              DEFAULT_AGATE_REALM.title =
                DEFAULT_AGATE_REALM.title.concat({value: translation['realm.agate-user-realm'], lang: lang});
              DEFAULT_AGATE_REALM.description =
                DEFAULT_AGATE_REALM.description.concat({value: translation['realm.default-help'], lang: lang});

              deferred.resolve(DEFAULT_AGATE_REALM);
            });
          });
        }

        return deferred.promise;
      }

      function getRealms() {
        var deferred = $q.defer();

        if (REALMS !== null) {
          deferred.resolve(REALMS);
        } else {
          $q.all([createDefaultRealm(), RealmsConfigResource.summaries().$promise])
            .then(function (realms) {
              REALMS = [].concat(realms[0], realms[1]);
              deferred.resolve(REALMS);
            });
        }

        return deferred.promise;
      }

      function getRealmsForLanguage(language) {
        var deferred = $q.defer();

        getRealms().then(function(realms) {
          deferred.resolve(
              realms.map(function(realm) {
                return {
                  name: realm.name,
                  title: LocalizedValues.forLang(realm.title, language),
                  description: LocalizedValues.forLang(realm.description, language)
                };
            })
          );
        });

        return deferred.promise;
      }

      function findRealm(target) {
        return REALMS.filter(function(realm) {
          return realm.name === target;
        }).pop() || DEFAULT_AGATE_REALM;
      }

      function findRealmForLanguage(target, language) {
        var realm = REALMS.filter(function(realm) {
          return realm.name === target;
        }).pop() || DEFAULT_AGATE_REALM;

        realm.title = LocalizedValues.forLang(realm.title, language);
        realm.description = LocalizedValues.forLang(realm.description, language);

        return realm;
      }

      function getAgateDefaultRealm() {
        return DEFAULT_AGATE_REALM;
      }

      this.getRealms = getRealms;
      this.getRealmsForLanguage = getRealmsForLanguage;
      this.findRealm = findRealm;
      this.findRealmForLanguage = findRealmForLanguage;
      this.getAgateDefaultRealm = getAgateDefaultRealm;

      return this;
    }]);
