export class AccountSettingsPersonalController {
	/**
	 * @ngInject
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor(User, Notifications) {
		let vm = this;

		vm.user = User.get();

		vm.submit = () => {
			vm.loading = true;
			User.update(vm.user).then(success);

			function success() {
				User.setUserInfo(vm.user);
				Notifications.addSuccess(__('Údaje byly aktualizovány.'));
				vm.loading = false;
			}
		};
	}
}