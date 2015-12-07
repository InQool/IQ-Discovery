export class AdministrationController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param settings
	 * @param {OrganizationService} OrganizationService
	 * @param {Notifications} Notifications
	 */
	constructor($scope, settings, OrganizationService, Notifications) {
		let vm = this;

		vm.form = null;
		vm.settings = settings.clone();

		vm.submit = submit;
		vm.save = save;
		vm.remove = remove;
		vm.addIp = addIp;
		vm.removeIp = removeIp;

		watchForChanges();

		function watchForChanges() {
			$scope.$on('$stateChangeStart', callback);

			/**
			 * @param {Event} event
			 */
			function callback(event) {
				if (angular.equals(vm.settings.plain(), settings.plain()) === false && confirm('Máte neuložené změny. Opravdu chcete přejít na jinou stránku?') === false) {
					event.preventDefault();
				}
			}
		}

		function submit() {
			if (vm.form.$valid) {
				vm.loading = true;
				OrganizationService.saveSettings(vm.settings).then(success);
			}

			function success() {
				vm.loading = false;
				settings = vm.settings;
				Notifications.changesSaved();
			}
		}

		function save(type, file) {
			return OrganizationService.uploadFile(type, file);
		}

		function remove(type) {
			return OrganizationService.removeFile(type);
		}

		function addIp() {
			vm.settings.ipMaskPairs.push({});
		}

		function removeIp(entry) {
			_.remove(vm.settings.ipMaskPairs, entry);
		}
	}
}