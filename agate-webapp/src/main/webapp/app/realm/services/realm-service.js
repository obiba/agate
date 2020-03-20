/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

(function() {

angular.module('agate.realm')
  .service('RealmsService',
    ['$rootScope',
      '$q',
      '$translate',
      'Session',
      'RealmsConfigResource',
      'RealmConfigResource',
      'RealmsResource',
      'LocalizedValues',
      'NOTIFICATION_EVENTS',

    function($rootScope,
             $q,
             $translate,
             Session,
             RealmsConfigResource,
             RealmConfigResource,
             RealmsResource,
             LocalizedValues,
             NOTIFICATION_EVENTS) {
      var DEFAULT_AGATE_REALM = null;
      var REALMS = null;
      var service = this;

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
        var realmResource = 'agate-administrator' === Session.role ? RealmsConfigResource.summaries() : RealmsResource.query();
        var deferred = $q.defer();

        if (REALMS !== null) {
          deferred.resolve(REALMS);
        } else {
          $q.all([createDefaultRealm(), realmResource.$promise])
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

      function getRealmsNameTitleMap(language, config) {
        var deferred = $q.defer();

        getRealmsForLanguage(language, config).then(function (realms) {
          deferred.resolve(
            realms.reduce(function (acc, realm) {
              acc[realm.name] = realm.  title;
              return acc;
            }, {})
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

      function onDeactivateRealm(event, args) {
        RealmConfigResource.deactivate({name: args.realm.name}).$promise
          .then(function() {
            service.unbindActivate();
            args.successCallback();
          })
          .catch(args.failCallback);
      }

      function ensureRealmTitle(realm) {
        return realm.title && Object.keys(realm.title).length > 0 ? LocalizedValues.forLang(realm.title, $translate.use()) : realm.name;
      }

      function deactivateRealm(realm, $scope, successCallback, failCallback) {
        if (realm.userCount > 0) {
          service.unbindActivate = $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, onDeactivateRealm.bind(service));
          $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
            {
              titleKey: 'realm.deactivate-dialog.title',
              messageKey: 'realm.deactivate-dialog.message',
              messageArgs: [ensureRealmTitle(realm)]
            }, {realm: realm, successCallback: successCallback, failCallback: failCallback}
          );
        }
      }

      function deserialize(realmData) {
        var realm = angular.fromJson(realmData);
        if (realm.userInfoMappings && realm.userInfoMappings.length > 0) {
          realm.userInfoMapping = {};
          realm.userInfoMappings.forEach(function(userInfoMapping) {
            if (realm.userInfoMapping.key) {
              realm.userInfoMapping[realm.userInfoMapping.key] = userInfoMapping.value;
            }
          });
        }

        return realm;
      }

      this.ensureRealmTitle = ensureRealmTitle;
      this.getRealms = getRealms;
      this.getRealmsForLanguage = getRealmsForLanguage;
      this.getRealmsNameTitleMap = getRealmsNameTitleMap;
      this.findRealm = findRealm;
      this.findRealmForLanguage = findRealmForLanguage;
      this.getAgateDefaultRealm = getAgateDefaultRealm;
      this.deactivateRealm = deactivateRealm;
      this.deserialize = deserialize;

      return this;
    }]);

})();
