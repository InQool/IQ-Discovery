export class PasswordForgottenController {
	/**
	 * @ngInject
	 * @param $state
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor($state, User, Notifications) {
		let vm = this;

		vm.email = '';
		vm.submit = submit;

		function submit() {
			if (vm.form.$valid) {
				vm.loading = true;
				User.makePasswordResetRequest(vm.email).then(success).catch(failure);
			}

			function success() {
				Notifications.addSuccess(__('Váš požadavek na změnu hesla byl odeslán.'));
				$state.go('secured.homepage');
			}

			function failure(response) {
				if (response.status === 403) {
					Notifications.addError(__('Nelze obnovit heslo uživateli přihlášenému přes MojeID.'));
				} else if (response.status === 404) {
					Notifications.addError(__('Uživatel s touto emailovou adresou neexistuje.'));
				}

				vm.loading = false;
			}
		}
	}
}