'use strict';

agate.user

  .constant('STUDY_EVENTS', {
    studyUpdated: 'event:study-updated'
  })

  .controller('UserListController', ['$rootScope', '$scope', '$translate', 'UsersResource', 'UserResource', 'NOTIFICATION_EVENTS',

    function ($rootScope, $scope, $translate, UsersResource, UserResource, NOTIFICATION_EVENTS) {

      $scope.users = UsersResource.query();

      /**
       * Deletes a user
       * @param index
       */
      $scope.delete = function (index) {
        var user = $scope.users[index];
        if (user) {
          var titleKey = 'user.delete-dialog.title';
          var messageKey = 'user.delete-dialog.message';
          $translate([titleKey, messageKey], {name: user.name})
            .then(function (translation) {
              $rootScope.$broadcast(NOTIFICATION_EVENTS.showConfirmDialog,
                {title: translation[titleKey], message: translation[messageKey]}, user.id);
            });
        }
      };

      /**
       * Delete use confirmation callback
       */
      $scope.$on(NOTIFICATION_EVENTS.confirmDialogAccepted, function (event, id) {
        UserResource.delete({id: id},
          function () {
            $scope.users = UsersResource.query();
          });
      });

    }])

  .controller('UserEditController', ['$rootScope', '$scope', '$routeParams', '$log', '$location', 'UsersResource', 'UserResource', 'FormServerValidation', 'UserStatusResource', 'GroupsResource', 'ApplicationsResource', 'ConfigurationResource', 'AttributesService',

    function ($rootScope, $scope, $routeParams, $log, $location, UsersResource, UserResource, FormServerValidation, UserStatusResource, GroupsResource, ApplicationsResource, ConfigurationResource, AttributesService) {

      $scope.roles = ["agate-administrator", "agate-user"];
      $scope.attributesConfig = [];
      ConfigurationResource.get(function(config) {
        $scope.attributesConfig = config.userAttributes || [];
        $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.user.attributes, $scope.attributesConfig);
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

      $scope.userStatusList = UserStatusResource.listAsNameValue();
      $scope.userStatus = $scope.userStatusList[UserStatusResource.activeIndex()];

      $scope.user = $routeParams.id ?
        UserResource.get({id: $routeParams.id}, function(user) {
          $scope.userStatus = $scope.userStatusList[UserStatusResource.findIndex(user.status)]
          $scope.attributeConfigPairs = AttributesService.getAttributeConfigPairs($scope.user.attributes, $scope.attributesConfig);
          return user;
        }) : {};

      /**
       * Updated an existing user properties and attributes
       */
      var updateUser = function () {
        var pairedAttributes = $scope.attributeConfigPairs.map(function(attributeConfigPair){
          return attributeConfigPair.attribute;
        });

        if ($scope.user.attributes && $scope.user.attributes.length > 0) {
          $scope.user.attributes = $scope.user.attributes.concat(AttributesService.findNewAttributes($scope.user.attributes, pairedAttributes));
        } else {
          $scope.user.attributes = pairedAttributes;
        }

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
        $scope.user.status = $scope.userStatus.value;
        $scope.user.attributes = $scope.attributeConfigPairs.map(function(attributeConfigPair){
          return attributeConfigPair.attribute;
        });

        UsersResource.save($scope.user,
          function (resource, getResponseHeaders) {
            var parts = getResponseHeaders().location.split('/');
            $location.path('/user/' + parts[parts.length - 1]).replace();
          },
          saveErrorHandler);
      }

      var saveErrorHandler = function (response) {
        FormServerValidation.error(response, $scope.form);
      };

      $scope.save = function () {
        if (!$scope.form.$valid) {
          $scope.form.saveAttempted = true;
          return;
        }

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

  .controller('UserViewController', ['$rootScope', '$scope', '$routeParams', '$log', '$location', 'UserResource', 'ConfigurationResource', 'AttributesService',

    function ($rootScope, $scope, $routeParams, $log, $location, UserResource, ConfigurationResource, AttributesService) {
      $scope.user = $routeParams.id ?
        UserResource.get({id: $routeParams.id}, function(user) {
          ConfigurationResource.get(function(config) {
            $scope.userConfigAttributes = AttributesService.findConfigAttributes(user.attributes, config.userAttributes);
            $scope.userNonConfigAttributes = AttributesService.findNonConfigAttributes(user.attributes, config.userAttributes);
          });

          return user;
        }) : {};

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
