export class VerifyController {
	/**
	 * @ngInject
	 * @param {PeopleService} PeopleService
	 * @param {Notifications} Notifications
	 */
	constructor(PeopleService, Notifications) {
		let vm = this;

		vm.fetch = fetch;
		vm.submit = submit;

		setInitialState();

		function setInitialState() {
			vm.user = {
				opNumber: null
			};
		}

		function fetch() {
			if (vm.user.opNumber) {
				vm.loading = true;
				PeopleService.getUserForVerification(vm.user.opNumber).then(success).catch(failure).finally(complete);
			}

			function success(user) {
				vm.user = user;
			}

			function failure() {
				Notifications.addError('Kód je neplatný.');
			}

			function complete() {
				vm.loading = false;
			}
		}

		function submit() {
			if (vm.form.$valid) {
				vm.loading = true;
				PeopleService.verifyUser(vm.user).then(success).catch(failure);
			}

			function success() {
				Notifications.addSuccess('Uživatel byl ověřen.');
				setInitialState();
			}

			function failure() {
				vm.loading = false;
				Notifications.addError('Došlo k chybě při ověřování.');
			}
		}
	}
}