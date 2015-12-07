export class LoginController {
	/**
	 * @ngInject
	 * @param $state
	 * @param {User} User
	 * @param {Translator} Translator
	 * @param {Notifications} Notifications
	 */
	constructor($state, User, Translator, Notifications) {
		let vm = this;

		vm.language = Translator.getCurrent();
		vm.frontendUrl = encodeURI(location.origin + '/login/openid');

		vm.submit = submit;

		function submit() {
			if (vm.form.$valid) {
				vm.loading = true;

				User.login(vm.user).then(() => {
					Notifications.addSuccess(__('Byl jste přihlášen.'));
					$state.go('secured.homepage');
				}).catch(() => {
					Notifications.addError(__('Zadaná emailová adresa nebo heslo není správné.'));
					vm.loading = false;
				});
			}
		}
	}
}