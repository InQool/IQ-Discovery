export class PasswordResetController {
	/**
	 * @ngInject
	 * @param $state
	 * @param {String} hash
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor($state, hash, User, Notifications) {
		let vm = this;

		vm.password = '';
		vm.passwordAgain = '';

		vm.submit = submit;

		function submit() {
			if (vm.form.$valid) {
				if (vm.password !== vm.passwordAgain) {
					Notifications.addError(__('Hesla se neshodují.'));
				} else {
					vm.loading = true;
					User.resetPassword(hash, vm.password).then(success).catch(failure);
				}
			}

			function success() {
				Notifications.addSuccess(__('Vaše nové heslo bylo uloženo. Nyní se můžete přihlásit.'));
				$state.go('secured.login');
			}

			function failure() {
				Notifications.addError(__('Neplatný kód pro obnovu hesla.'));
				vm.loading = false;
			}
		}
	}
}