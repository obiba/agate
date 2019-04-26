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

  function Controller($q, $scope, RealmConfigFormResource, RealmsConfigResource, RealmConfigResource, JsonUtils) {
    var ctrl = this;

    function extractRealmForm(type, config) {
        return JsonUtils.parseJsonSafely(config[type], {});
    }

    function onTypeChanged(type) {
      ctrl.realm.form = extractRealmForm(ctrl.model.type, ctrl.config);
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

    function invokeRealmConfigResource() {
      if (ctrl.name) {
        return RealmConfigResource.get({name: ctrl.name}).$promise;
      }

      var deferred = $q.defer();
      deferred.resolve({});
      return deferred.promise;
    }

    function init() {
      ctrl.realm = $q.all([RealmConfigFormResource.get({locale: ctrl.locale.language}).$promise, invokeRealmConfigResource()])
        .then(function(responses) {
          ctrl.config = responses[0];
          ctrl.model = responses[1];
          addTypeGetterSetter(ctrl.model, onTypeChanged);
          ctrl.model.safeTitle = ctrl.model.id ? ctrl.model.title || ctrl.model.name : null;

          ctrl.main = JsonUtils.parseJsonSafely(ctrl.config.form, {});
          ctrl.realm = {
            form: extractRealmForm(ctrl.model.type, ctrl.config),
            model: JsonUtils.parseJsonSafely(ctrl.model.content, {})
          };
          angular.extend(ctrl, this);
        });
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

  angular.module('agate.realm')
    .component('realmForm', {
      transclude: true,
      bindings: {
        name: '<',
        locale: '<',
        onSave: '&'
      },
      templateUrl: 'app/realm/components/realm-form/component.html',
      controller: ['$q', '$scope', 'RealmConfigFormResource', 'RealmsConfigResource', 'RealmConfigResource', 'JsonUtils', Controller]
    });
})();
