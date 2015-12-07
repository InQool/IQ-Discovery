export class AccountSettingsController {
	/**
	 * @ngInject
	 * @param {User} User
	 */
	constructor(User) {
		let vm = this;

		vm.isLoggedInViaOpenId = isLoggedInViaOpenId;

		function isLoggedInViaOpenId() {
			return User.isLoggedInViaOpenId();
		}
	}
}