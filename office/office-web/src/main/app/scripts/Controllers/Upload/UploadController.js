/**
 * Created by kudlajz on 03.12.15.
 */
export default class UploadController {
    /**
     * @ngInject
     */
    constructor(MuseumService, Notifications) {
        const vm = this;

        vm.submit = submit;

        function submit() {
            if (vm.form.$valid) {
                vm.loading = true;
                MuseumService.upload(vm.file).then(() => {
                    Notifications.addSuccess('Soubor byl nahrán.');
                    vm.file = null;
                    vm.loading = false;
                })
            }
        }
    }
}
