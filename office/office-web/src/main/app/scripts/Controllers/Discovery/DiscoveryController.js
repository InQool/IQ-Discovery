export class DiscoveryController {
    /**
     * @ngInject
     * @param $scope
     * @param $modal
     * @param $window
     * @param settings
     * @param {DiscoveryService} DiscoveryService
     * @param {Notifications} Notifications
     */
    constructor($scope, $modal, $window, $interval, settings, DiscoveryService, Notifications) {
        const vm = this;

        let defaultSettings = settings.clone();
        vm.settings = settings.clone();

        vm.submit = submit;
        vm.save = save;
        vm.remove = remove;
        vm.runImport = runImport;

        watchForChanges();
        checkImportProgress();

        let interval = null;

        function watchForChanges() {
            $scope.$on('$stateChangeStart', callback);

            /**
             * @param {Event} event
             */
            function callback(event) {
                if (angular.equals(vm.settings.plain(), defaultSettings.plain()) === false && $window.confirm('Máte neuložené změny. Opravdu chcete přejít na jinou stránku?') === false) {
                    event.preventDefault();
                } else {
                    $interval.cancel(interval);
                }
            }
        }

        function checkImportProgress() {
            DiscoveryService.getImportProgress().then(progress => {
                vm.progress = progress;

                if (progress > 0 && interval === null) {
                    interval = $interval(() => {
                        checkImportProgress();
                    }, 10000);
                }

                if (Number(progress) === 0) {
                    $interval.cancel(interval);
                    interval = null;
                }
            });
        }

        function submit() {
            if (vm.form.$valid) {
                vm.loading = true;
                DiscoveryService.update(vm.settings).then(success);
            }

            function success() {
                Notifications.changesSaved();
                defaultSettings = vm.settings;
                vm.loading = false;
            }
        }

        function save(type, file) {
            return DiscoveryService.uploadFile(type, file);
        }

        function remove(type) {
            return DiscoveryService.removeFile(type);
        }

        function runImport(type) {
            $modal.open({
                templateUrl: 'views/forms/Discovery/runImport.html'
            }).result.then(success);

            function success() {
                DiscoveryService.runImport(type);
                Notifications.addSuccess('Import byl spuštěn. Tato akce může trvat i několik hodin.');
            }
        }
    }
}
