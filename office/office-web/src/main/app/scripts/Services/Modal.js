export class Modal {
    /**
     * @ngInject
     * @param $modal
     */
    constructor($modal) {
        this.modal = $modal;
    }

    /**
     * @param {Object} options
     * @param {Number} options.id
     * @param {String} options.subject
     * @param {String} [options.value]
     * @param {String} [options.table]
     * @param {Function} [options.success]
     * @returns {Promise}
     */
    deleteWindow(options) {
        if (options.hasOwnProperty('id') === false) {
            throw new Error('id not defined');
        }

        return this.modal.open({
            templateUrl: 'views/services/Modal/delete.html',
            controller: controller,
            controllerAs: 'vm'
        });

        /**
         * @ngInject
         * @param $scope
         * @param {DataTable} DataTable
         */
        function controller($scope, DataTable) {
            const vm = this;
            const table = DataTable.getInstance(options.table);
            const data = DataTable.getData(table);

            vm.value = _.find(data, {id: options.id});
            vm.subject = options.subject;

            vm.getValue = getValue;
            vm.submit = submit;

            function getValue() {
                return options.hasOwnProperty('value') ? _.get(vm.value, options.value) : vm.value.name;
            }

            function submit() {
                vm.loading = true;
                vm.value.remove().then(() => {
                    if (options.hasOwnProperty('success') && _.isFunction(options.success)) {
                        options.success();
                    }

                    DataTable.reload(table);
                    $scope.$dismiss();
                }).catch(() => {
                    vm.loading = false;
                });
            }
        }
    }
}
