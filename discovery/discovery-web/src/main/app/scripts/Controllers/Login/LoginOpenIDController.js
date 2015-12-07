export class LoginOpenIDController {
	/**
	 * @ngInject
	 * @param $state
	 * @param {Object} $stateParams
	 * @param {String} $stateParams.authctoken
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor($state, $stateParams, User, Notifications) {
		let userInfo = angular.copy($stateParams);
		userInfo.openid = true;
		userInfo.verified = userInfo.verified === 'true'; // gotta test this
		delete userInfo.authctoken;

		User.setLogin($stateParams.authctoken, userInfo);

		Notifications.addSuccess(__('Byl jste přihlášen.'));
		$state.go('secured.homepage');
	}
}