export class RegisterController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param $state
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor($scope, $state, User, Notifications) {
		$scope.submit = () => {
			if ($scope.form.$valid) {
				$scope.loading = true;
				User.create($scope.user).then(() => {
					Notifications.addSuccess(__('Váš účet byl vytvořen. Nyní se můžete přihlásit.'));
					$state.go('secured.login');
				}).catch(() => {
					Notifications.addError(__('Účet s touto emailovou adresou již existuje.'));
					$scope.loading = false;
				});
			}
		};

		$scope.hasError = (field) => {
			let hasError = $scope.form.$submitted && ($scope.form[field].$error.required || $scope.form[field].$error.email);
			return hasError ? 'has-error' : '';
		};
	}
}