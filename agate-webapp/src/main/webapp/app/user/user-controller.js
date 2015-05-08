'use strict';

agate.user

  .constant('STUDY_EVENTS', {
    studyUpdated: 'event:study-updated'
  })

  .controller('UserListController', ['$rootScope', '$scope', '$translate', 'UsersResource', 'UserResource', 'UserResetPasswordResource', 'NOTIFICATION_EVENTS', 'LocaleStringUtils',
    function ($rootScope, $scope, $translate, UsersResource, UserResource, UserResetPasswordResource, NOTIFICATION_EVENTS, LocaleStringUtils) {
      $scope.users = UsersResource.query();

      /**
       * Deletes a user
       * @param index
       */
      $scope.delete = function (index) {
        var user = $scope.users[index];
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
              $scope.users = UsersResource.query();
            });

          delete $scope.userToDelete;
        }
      });

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

  .controller('UserEditController', ['$rootScope', '$scope', '$routeParams', '$log', '$location', 'UsersResource', 'UserResource', 'FormServerValidation', 'UserStatusResource', 'GroupsResource', 'ApplicationsResource', 'ConfigurationResource', 'AttributesService', 'AlertService',

    function ($rootScope, $scope, $routeParams, $log, $location, UsersResource, UserResource, FormServerValidation, UserStatusResource, GroupsResource, ApplicationsResource, ConfigurationResource, AttributesService, AlertService) {

      $scope.roles = ["agate-administrator", "agate-user"];
      $scope.attributesConfig = [];
      ConfigurationResource.get(function(config) {
        $scope.attributesConfig = config.userAttributes || [];
        $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.user.attributes, $scope.attributesConfig);
        $scope.usedAttributeNames = AttributesService.getUsedAttributeNames($scope.user.attributes, $scope.attributesConfig);
      });

      $scope.realmList = ["agate-user-realm"];
      $scope.groupList = [];
      GroupsResource.query().$promise.then(function(groups){
        groups.forEach(function(group){
          $scope.groupList.push(group.name);
        });
      });

      $scope.applicationList = [];
      ApplicationsResource.query().$promise.then(function(applications){
        applications.forEach(function(application){
          $scope.applicationList.push(application.name);
        });
      });

      var statusValueList = UserStatusResource.listAsNameValue();
      $scope.status = {
        list: statusValueList,
        selected: statusValueList[UserStatusResource.activeIndex()]
      };

      $scope.profile = {
        password: null,
        condfirmPassword: null
      };

      $scope.user = $routeParams.id ?
        UserResource.get({id: $routeParams.id}, function(user) {
          $scope.status.selected = $scope.status.list[UserStatusResource.findIndex(user.status)];
          $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.user.attributes, $scope.attributesConfig);
          $scope.usedAttributeNames = AttributesService.getUsedAttributeNames($scope.user.attributes, $scope.attributesConfig);
          $scope.profile = null;
          return user;
        }) : {};

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
      }

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

  .controller('UserViewController', ['$rootScope', '$scope', '$routeParams', '$log', '$location', 'UserResource', 'ConfigurationResource', 'AttributesService', 'AlertService',

    function ($rootScope, $scope, $routeParams, $log, $location, UserResource, ConfigurationResource, AttributesService, AlertService) {

      $scope.user = $routeParams.id ?
        UserResource.get({id: $routeParams.id}, function(user) {
          ConfigurationResource.get(function(config) {
            $scope.userConfigAttributes = AttributesService.findConfigAttributes(user.attributes, config.userAttributes);
            $scope.userNonConfigAttributes = config.userAttributes ? AttributesService.findNonConfigAttributes(user.attributes, config.userAttributes) : user.attributes;
          });

          return user;
        }) : {};

      $scope.onPasswordUpdated = function() {
        AlertService.alert({id: 'UserViewController', type: 'success', msgKey: 'password.success', delay: 5000});
      };

    }])

  .controller('UserRequestListController', ['$rootScope', '$scope', '$route', '$http', 'UsersResource', 'UserResource', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, $route, $http, UsersResource, UserResource, NOTIFICATION_EVENTS) {
      $scope.users = UsersResource.query({status: 'pending'});

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
