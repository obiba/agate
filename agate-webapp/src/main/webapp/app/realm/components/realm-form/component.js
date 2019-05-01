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

(function () {

  function Controller($q,
                      $scope,
                      $filter,
                      $location,
                      ConfigurationResource,
                      RealmConfigFormResource,
                      RealmsConfigResource,
                      RealmConfigResource,
                      GroupsResource,
                      JsonUtils,
                      AlertBuilder) {

    var ctrl = this;

    function extractRealmForm(type, config) {
        return JsonUtils.parseJsonSafely(config[type], {});
    }

    function onTypeChanged(/*type*/) {
      ctrl.realm.form = extractRealmForm(ctrl.model.type, ctrl.config);
    }

    function onStatusChanged(status) {
      if ('INACTIVE' === status) {
        ctrl.model.defaultRealm = false;
        ctrl.model.forSignup = false;
      }
    }

    /**
     * This is primarily to prevent using $watch()!
     *
     * @param model
     * @param typeChangeCallBack
     */
    function addTypeGetterSetter(model, typeChangeCallBack) {
      var type = model.type;
      delete model.type;
      var _type = type;

      Object.defineProperty(model, 'type', {
        get: function() { return _type; },
        set: function(value) {
          _type = value;
          typeChangeCallBack.call(ctrl, _type);
        },
        enumerable: true
      });
    }

    /**
     * This is primarily to prevent using $watch()!
     *
     * @param model
     * @param typeChangeCallBack
     */
    function addStatusGetterSetter(model, statusChangeCallBack) {
      var status = model.status;
      delete model.status;
      var _status = status;

      Object.defineProperty(model, 'status', {
        get: function() { return _status; },
        set: function(value) {
          _status = value;
          statusChangeCallBack.call(ctrl, _status);
        },
        enumerable: true
      });
    }

    function cloneRealm(realm) {
      var clone = angular.copy(realm);
      delete clone.id;
      delete clone.name;
      delete clone.title;
      clone.status = 'INACTIVE';
      clone.defaultRealm = false;
      clone.forSignup = false;
      return clone;
    }

    function invokeRealmConfigResource() {

      if (ctrl.name) {
        return RealmConfigResource.get({name: ctrl.name}).$promise;
      }

      var deferred = $q.defer();
      var search = $location.search();

      if (search && search.from) {
        RealmConfigResource.get({name: search.from}).$promise.then(function(realm) {
          deferred.resolve(cloneRealm(realm));
        });
      } else {
        deferred.resolve({});
      }

      return deferred.promise;
    }

    function onError(response) {
      AlertBuilder.newBuilder().response(response).delay(0).build();
    }


    function init() {
      ctrl.realm = $q.all(
        [
          ConfigurationResource.get().$promise,
          RealmConfigFormResource.get({locale: ctrl.locale.language}).$promise,
          invokeRealmConfigResource(),
          GroupsResource.query().$promise
        ])
        .then(function(responses) {
          ctrl.sfLanguages = responses[0].languages.reduce(function(acc, language) {
            acc[language] = $filter('translate')('language.' + language);
            return acc;
          }, {});
          ctrl.config = responses[1];
          ctrl.model = responses[2];
          ctrl.sfOptions = {formDefaults: {languages: ctrl.sfLanguages}};
          ctrl.sfOptions.formDefaults.items = responses[3].map(function(group) {
            return {value: group.id, label: group.name};
          });

          addTypeGetterSetter(ctrl.model, onTypeChanged);
          addStatusGetterSetter(ctrl.model, onStatusChanged);
          ctrl.model.safeTitle = ctrl.model.id ? ctrl.model.title || ctrl.model.name : null;

          ctrl.main = JsonUtils.parseJsonSafely(ctrl.config.form, {});
          ctrl.realm = {
            form: extractRealmForm(ctrl.model.type, ctrl.config),
            model: JsonUtils.parseJsonSafely(ctrl.model.content, {})
          };

          // $ctrl.model.title || $ctrl.model.name
          angular.extend(ctrl, this);
        }).catch(onError);
    }

    function onChanges(changed) {
      if (changed.locale.currentValue && changed.locale.currentValue.language) {
        init();
      }
    }

    function save() {
      $scope.$broadcast('schemaFormValidate');
      if (ctrl.form.main.$valid && ctrl.form.realm.$valid) {
        ctrl.model.content = JSON.stringify(ctrl.realm.model);

        (ctrl.model.id ? RealmConfigResource.save(ctrl.model).$promise : RealmsConfigResource.create(ctrl.model).$promise)
          .then(function() {
            ctrl.onSave({});
          });
      }
    }

    ctrl.$onChanges = onChanges.bind(this);
    ctrl.save = save;
  }

  var injections = [
    '$q',
    '$scope',
    '$filter',
    '$location',
    'ConfigurationResource',
    'RealmConfigFormResource',
    'RealmsConfigResource',
    'RealmConfigResource',
    'GroupsResource',
    'JsonUtils',
    'AlertBuilder'
  ];

  angular.module('agate.realm')
    .component('realmForm', {
      transclude: true,
      bindings: {
        name: '<',
        locale: '<',
        onSave: '&'
      },
      templateUrl: 'app/realm/components/realm-form/component.html',
      controller: [].concat(injections, Controller)
    });
})();
