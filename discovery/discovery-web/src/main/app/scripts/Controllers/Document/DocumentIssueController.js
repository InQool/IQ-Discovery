export class DocumentIssueController {
    /**
     * @ngInject
     * @param $scope
     * @param {Object} $stateParams
     * @param {DocumentEntity} doc
     * @param {Object} settings
     * @param {DocumentService} DocumentService
     * @param {SearchService} SearchService
     * @param {User} User
     * @param {Translator} Translator
     * @param {Notifications} Notifications
     */
    constructor($scope, $stateParams, doc, settings, DocumentService, SearchService, User, Translator, Notifications) {
        let vm = this;

        vm.clipboard = clipboard;
        vm.openViewer = openViewer;

        $scope.$emit('issueViewLoaded', $stateParams.issue);

        load();

        function load() {
            DocumentService.getByInventoryId($stateParams.issue).then(success);

            /**
             * @param {DocumentEntity} issue
             */
            function success(issue) {
                vm.issue = issue;
                vm.getAttributes = getAttributes;
                vm.getValues = getValues;
            }
        }

        function getAttributes() {
            return _.filter(DocumentService.Attributes, term => {
                return vm.issue.hasOwnProperty(term.key);
            });
        }

        function getValues(property) {
            let values = vm.issue.getProperty(property);
            if (values[0] === 'unknown') {
                return [Translator.translate(SearchService.StringUnknown)];
            }

            return values;
        }

        function openViewer() {
            DocumentService.createViewer(vm.issue.imageIds, vm.issue.thumbIds, doc, vm.issue, settings);
        }

        function clipboard(invId) {
            User.addToClipboard(invId).then(() => {
                Notifications.addSuccess(__('Dokument byl přidán do schránky.'));
            });
        }
    }
}
