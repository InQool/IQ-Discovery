export class AccountClipboardController {
    /**
     * @ngInject
     * @param $modal
     * @param $window
     * @param {User} User
     * @param {Configuration} Configuration
     * @param {Notifications} Notifications
     */
    constructor($modal, $window, User, Configuration, Notifications) {
        let vm = this,
            checkedSelectAll = false;

        vm.documents = [];

        vm.remove = remove;
        vm.reserve = reserve;
        vm.selectAll = selectAll;
        vm.getSelectedDocuments = getSelectedDocuments;
        vm.hasSelectedDocuments = hasSelectedDocuments;

        load();

        function load() {
            User.getClipboard().then(documents => {
                vm.documents = documents;
            });
        }

        function selectAll() {
            checkedSelectAll = !checkedSelectAll;
            angular.forEach(vm.documents, d => {
                d.selected = checkedSelectAll;
            });
        }

        function hasSelectedDocuments() {
            return _.some(vm.documents, doc => doc.selected);
        }

        function getSelectedDocuments() {
            return _.pluck(_.filter(vm.documents, doc => doc.selected), 'fedoraId');
        }

        function remove() {
            if ($window.confirm(__('Opravdu chcete odstranit vybrané dokumenty ze schránky?'))) {
                let removed = _.remove(vm.documents, doc => doc.selected);

                angular.forEach(removed, doc => {
                    doc.remove();
                });

                Notifications.addSuccess(__('Dokumenty byly odebrány ze schránky.'));
            }
        }

        function reserve() {
            if (User.get().verified) {
                $modal.open({
                    templateUrl: 'views/forms/Account/Clipboard/reserve.html',
                    controller: controllerForVerified,
                    controllerAs: 'vm'
                });
            } else if (Configuration.clipoard.reservationOfferVerification) {
                $modal.open({
                    templateUrl: 'views/forms/Account/Clipboard/verificationNeeded.html'
                });
            } else {
                $window.alert('Musíte být ověřeným uživatelem.');
            }

            function controllerForVerified($scope, $state) {
                let vm = this;

                vm.submit = submit;

                function submit() {
                    if (vm.form.$valid) {
                        vm.loading = true;
                        User.makeReservation(getSelectedDocuments(), vm.reason).then(() => {
                            Notifications.addSuccess(__('Rezervace byla provedena.'));
                            $scope.$close();
                            $state.reload();
                        }).catch(() => {
                            Notifications.addError(__('Rezervace bohužel není možná.'));
                            vm.loading = false;
                        });
                    } else {
                        $window.alert(__('Vyplňte účel použití dokumentu.'));
                    }
                }
            }
        }
    }
}
