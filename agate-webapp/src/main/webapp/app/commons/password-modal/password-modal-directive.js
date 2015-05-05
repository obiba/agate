agate.passwordModal

  .directive('updatePasswordButton', [function() {
    return {
      restrict: 'E',
      replace: true,
      controller: 'UpdatePasswordButtonController',
      scope: {
        profile: '=',
        userId: '@',
        updated: '&',
        closed: '&'
      },
      templateUrl: 'app/commons/password-modal/update-password-button-template.html'
    };
  }]);
