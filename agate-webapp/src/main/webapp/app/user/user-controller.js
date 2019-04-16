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

  .constant('STUDY_EVENTS', {
    studyUpdated: 'event:study-updated'
  })

  .controller('UserListController', ['$rootScope', '$scope', '$translate', 'UsersResource', 'UserResource', 'UserResetPasswordResource', 'NOTIFICATION_EVENTS', 'LocaleStringUtils',
    function ($rootScope, $scope, $translate, UsersResource, UserResource, UserResetPasswordResource, NOTIFICATION_EVENTS, LocaleStringUtils) {
      var onSuccess = function(response) {
        $scope.users = response;
        response.forEach(function(u) {
          u.applicationsGroups = {};

          if (u.groupApplications) {
            u.groupApplications.forEach(function (ga) {
              if (!u.applicationsGroups[ga.application])
                u.applicationsGroups[ga.application] = [];

              u.applicationsGroups[ga.application].push(ga.group);
            });
          }
        });
        $scope.loading = false;
      };

      var onError = function() {
        $scope.loading = false;
      };

      $scope.loading = true;
      $scope.users = UsersResource.query({}, onSuccess, onError);

      /**
       * Deletes a user
       * @param index
       */
      $scope.delete = function (user) {
        $scope.userToDelete = user.id;
        if (user) {
          $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
            {
              titleKey: 'user.delete-dialog.title',
              messageKey:'user.delete-dialog.message',
              messageArgs: [user.name]
            }, user.id
          );
        }
      };

      /**
       * Delete use confirmation callback
       */
      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.userToDelete === id) {
          UserResource.delete({id: id},
            function () {
              UsersResource.query({}, onSuccess, onError);
            });

          delete $scope.userToDelete;
        }
      });

      $scope.getStatusTitle = function(status) {
        return LocaleStringUtils.translate('user.' + status);
      };

      $scope.resetPassword = function (user) {
        $scope.selectedUser = user.id;
        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {title: 'Reset Password', message: 'Are you sure to send a reset password message for ' + user.name + '?'}, user.id);
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.selectedUser === id) {
          UserResetPasswordResource.resetPassword({id: id});
        }
      });
    }])

  .controller('UserEditController',
    ['$rootScope',
      '$scope',
      '$routeParams',
      '$log',
      '$location',
      '$translate',
      'UsersResource',
      'UserResource',
      'FormServerValidation',
      'UserStatusResource',
      'GroupsResource',
      'ApplicationsResource',
      'ConfigurationResource',
      'AttributesService',
      'AlertService',
      'RealmsService',

    function ($rootScope,
              $scope,
              $routeParams,
              $log,
              $location,
              $translate,
              UsersResource,
              UserResource,
              FormServerValidation,
              UserStatusResource,
              GroupsResource,
              ApplicationsResource,
              ConfigurationResource,
              AttributesService,
              AlertService,
              RealmsService) {

      $scope.roles = ["agate-administrator", "agate-user"];
      $scope.attributesConfig = [];
      ConfigurationResource.get(function(config) {
        $scope.languages = config.languages;
        $scope.attributesConfig = config.userAttributes || [];
        $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.user.attributes, $scope.attributesConfig);
        $scope.usedAttributeNames = AttributesService.getUsedAttributeNames($scope.user.attributes, $scope.attributesConfig);
      });

      RealmsService.getRealms().then(function(realms){
        $scope.realmList = realms;
        $scope.realm = {selected: RealmsService.getAgateDefaultRealm()};
      });

      $scope.groupList = [];
      GroupsResource.query().$promise.then(function(groups){
        groups.forEach(function(group){
          $scope.groupList.push(group.name);
        });
      });

      $scope.applicationList = [];
      ApplicationsResource.query().$promise.then(function(applications){
        applications.forEach(function(application){
          $scope.applicationList.push(application.id);
        });
      });

      var statusValueList = UserStatusResource.listAsNameValue();
      $scope.status = {
        list: statusValueList,
        selected: statusValueList[UserStatusResource.activeIndex()]
      };

      $scope.profile = {
        password: null,
        confirmPassword: null
      };

      $scope.user = $routeParams.id ?
        UserResource.get({id: $routeParams.id}, function(user) {
          $scope.status.selected = $scope.status.list[UserStatusResource.findIndex(user.status)];
          $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.user.attributes, $scope.attributesConfig);
          $scope.usedAttributeNames = AttributesService.getUsedAttributeNames($scope.user.attributes, $scope.attributesConfig);
          $scope.realm.selected = RealmsService.findRealm(user.realm);
          $scope.profile = null;
          return user;
        }) : { role: 'agate-user'};

      /**
       * Updated an existing user properties and attributes
       */
      var updateUser = function () {
        $scope.user.attributes =
          AttributesService.mergeConfigPairAttributes($scope.user.attributes, $scope.attributeConfigPairs);

        $scope.user.$save(
          function (user) {
            $location.path('/user/' + user.id).replace();
          },
          saveErrorHandler);
      };

      /**
       * Create a new user with properties and attributes
       */
      var createUser = function() {
        if ($scope.profile.password !== $scope.profile.confirmPassword) {
          $scope.form.saveAttempted = true;
          $scope.form.$invalid = true;
          AlertService.alert({id: 'UserEditController', type: 'danger', msgKey: 'password.messages.error.dontmatch'});
          return;
        }

        $scope.profile.user = $scope.user;
        $scope.user.attributes = $scope.attributeConfigPairs.map(function(attributeConfigPair){
          return attributeConfigPair.attribute;
        });

        UsersResource.save($scope.profile,
          function (resource, getResponseHeaders) {
            var parts = getResponseHeaders().location.split('/');
            $location.path('/user/' + parts[parts.length - 1]).replace();
          },
          saveErrorHandler);
      };

      var saveErrorHandler = function (response) {
        $scope.form.saveAttempted = true;
        AlertService.alert({id: 'UserEditController', type: 'danger', msgKey: 'fix-error'});
        FormServerValidation.error(response, $scope.form);
      };

      $scope.save = function () {
        if (!$scope.form.$valid) {
          $scope.form.saveAttempted = true;
          AlertService.alert({id: 'UserEditController', type: 'danger', msgKey: 'fix-error'});
          return;
        }

        $scope.user.status = $scope.status.selected.value;
        $scope.user.realm = $scope.realm.selected.name;

        if ($scope.user.id) {
          updateUser();
        } else {
          createUser();
        }
      };

      /**
       * Cancels the edit mode
       */
      $scope.cancel = function () {
        if ($scope.user.id) {
          $location.path('/user' + ($scope.user.id ? '/' + $scope.user.id : '')).replace();
        } else {
          $location.path('/users');
        }
      };

    }])

  .controller('UserViewController',
    ['$rootScope',
      '$scope',
      '$routeParams',
      '$log',
      '$location',
      'UserResource',
      'UserAuthorizationsResource',
      'UserAuthorizationResource',
      'ConfigurationResource',
      'AttributesService',
      'AlertService',
      'RealmsService',

    function ($rootScope,
              $scope,
              $routeParams,
              $log,
              $location,
              UserResource,
              UserAuthorizationsResource,
              UserAuthorizationResource,
              ConfigurationResource,
              AttributesService,
              AlertService,
              RealmsService) {


      RealmsService.getRealms().then(function(realms){
        $scope.user = $routeParams.id ?
          UserResource.get({id: $routeParams.id}, function(user) {
            ConfigurationResource.get(function(config) {
              $scope.userConfigAttributes = AttributesService.findConfigAttributes(user.attributes, config.userAttributes);
              $scope.userNonConfigAttributes = config.userAttributes ? AttributesService.findNonConfigAttributes(user.attributes, config.userAttributes) : user.attributes;
            });

            user.realmTitle = RealmsService.findRealm(user.realm).title;
            return user;
          }) : {};
      });

      $scope.authorizations = $routeParams.id ?
        UserAuthorizationsResource.query({id: $routeParams.id}) : [];

      $scope.deleteAuthorization = function(authz) {
        UserAuthorizationResource.delete({id: $routeParams.id, authz: authz.id}, function() {
          $scope.authorizations = UserAuthorizationsResource.query({id: $routeParams.id});
        });
      };

      $scope.onPasswordUpdated = function() {
        AlertService.alert({id: 'UserViewController', type: 'success', msgKey: 'password.success', delay: 5000});
      };

    }])

  .controller('UserRequestListController', ['$rootScope', '$scope', '$route', '$http', 'UsersResource', 'UserResource', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, $route, $http, UsersResource, UserResource, NOTIFICATION_EVENTS) {
      var onSuccess = function(response) {
        $scope.users = response;
        response.forEach(function(u) {
          u.applicationsGroups = {};

          if (u.groupApplications) {
            u.groupApplications.forEach(function (ga) {
              if (!u.applicationsGroups[ga.application])
                u.applicationsGroups[ga.application] = [];

              u.applicationsGroups[ga.application].push(ga.group);
            });
          }
        });
        $scope.loading = false;
      };

      var onError = function() {
        $scope.loading = false;
      };

      $scope.loading = true;
      UsersResource.query({status: 'pending'}, onSuccess, onError);

      $scope.reject = function (user) {
        $scope.requestToDelete = user.id;
        $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
          {title: 'Delete Request', message: 'Are you sure to delete the request?'}, user.id);
      };

      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        if ($scope.requestToDelete === id) {
          UserResource.delete({id: id}, function() {
            $route.reload();
          });
        }
      });

      $scope.approve = function (user) {
        $http.put('ws/user/' + user.id + '/status', $.param({status: 'approved'}), {
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function() {
          $route.reload();
        });
      };
    }]);
