export class AccountSettingsChangePasswordController {
	/**
	 * @ngInject
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor(User, Notifications) {
		let vm = this;

		vm.form = null;

		vm.submit = submit;

		setInitialState();

		function setInitialState() {
			vm.loading = false;
			vm.values = {
				oldPassword: '',
				newPassword: '',
				newPasswordAgain: ''
			};
		}

		function submit() {
			if (vm.form.$valid) {
				if (vm.values.newPassword !== vm.values.newPasswordAgain) {
					Notifications.addError(__('Nové hesla se neshodují.'));
				} else {
					vm.loading = true;
					User.changePassword(vm.values.oldPassword, vm.values.newPassword).then(success).catch(failure);
				}
			}

			function success() {
				Notifications.addSuccess(__('Heslo bylo změněno.'));
				setInitialState();
			}

			function failure() {
				vm.loading = false;
				Notifications.addError(__('Současné heslo není správné.'));
			}
		}
	}
}