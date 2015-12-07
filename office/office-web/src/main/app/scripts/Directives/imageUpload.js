/**
 * Created by kudlajz on 08.10.15.
 */
export function imageUpload() {
	return {
		restrict: 'E',
		replace: true,
		scope: {
			title: '@',
			image: '@',
			onSave: '&',
			onRemove: '&'
		},
		controller: controller,
		controllerAs: 'vm',
		templateUrl: 'views/directives/imageUpload.html'
	};

	/**
	 * @ngInject
	 * @param $scope
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	function controller($scope, User, Notifications) {
		let vm = this;

		vm.title = $scope.title;
		vm.image = $scope.image;
		vm.token = User.getToken();

		vm.submit = submit;
		vm.remove = remove;

		function submit() {
			if (vm.file) {
				vm.loading = true;
				$scope.onSave({file: vm.file}).then(success).catch(failure).finally(complete);
			} else {
				Notifications.addError('Musíte vybrat soubor.');
			}

			function success(image) {
				vm.file = null;
				vm.image = image;
				Notifications.addSuccess('Soubor byl uložen.');
			}

			function failure() {
				Notifications.addError('Objevil se problém při ukládání souboru.');
			}

			function complete() {
				vm.loading = false;
			}
		}

		function remove() {
			vm.loading = true;
			$scope.onRemove().then(success);

			function success() {
				Notifications.addSuccess('Soubor byl odstraněn.');
				vm.image = null;
				vm.loading = false;
			}
		}
	}
}