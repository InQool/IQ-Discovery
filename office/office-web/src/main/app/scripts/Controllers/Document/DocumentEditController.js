const moment = require('moment');

export class DocumentEditController {
    /**
     * @ngInject
     * @param $scope
     * @param $state
     * @param {Object} $stateParams
     * @param {String} $stateParams.documentId
     * @param {String} [$stateParams.print]
     * @param $timeout
     * @param $window
     * @param Restangular
     * @param {Object} batch
     * @param {Object} documents
     * @param {Function} documents.plain
     * @param {Object} documents.kdr
     * @param {Object} [documents.bach]
     * @param {Object} [documents.oai]
     * @param {Object} documents.concept
     * @param {String} documents.concept.zdoType
     * @param {DocumentService} DocumentService
     * @param {SuggestService} SuggestService
     * @param {Configuration} Configuration
     * @param {Notifications} Notifications
     * @param {Page} Page
     */
    constructor($scope, $state, $stateParams, $timeout, $window, Restangular, batch, documents, DocumentService, SuggestService, Configuration, Notifications, Page) {
        let vm = this,
            onStateChange = $scope.$on('$stateChangeStart', onStateChangeStart),
            plainConcept = Restangular.copy(documents.plain().concept),
            type = documents.concept.zdoType,
            choices = DocumentService.DCTerms,
            multiselectTerms = _.map(_.filter(choices, choice => choice.multiselect), choice => choice.key),
            allowedAttributes = {
                volume: ['created'],
                issue: ['alternative', 'description', 'created', 'subject']
            };

        vm.tab = 'kdr';
        vm.source = {};
        vm.batch = batch;
        vm.id = $stateParams.documentId;
        vm.documents = documents;
        vm.original = documents.kdr;
        vm.isPrint = $stateParams.print === 'true';
        vm.isPublished = documents.concept.group[0] !== 'ZDO_CONCEPT';
        vm.watermarkPositions = DocumentService.WatermarkPositions;
        vm.fetchedOptions = {};
        vm.languages = [];
        vm.treeIsCollapsed = false;
        vm.choices = DocumentService.DCTerms;
        vm.configuration = Configuration.document;

        vm.submit = submit;
        vm.addValue = addValue;
        vm.removeValue = removeValue;
        vm.addCustomValue = addCustomValue;
        vm.removeCustomValue = removeCustomValue;
        vm.addCustomField = addCustomField;
        vm.getOaiShortcuts = getOaiShortcuts;
        vm.hasMultiselect = hasMultiselect;
        vm.getValue = getValue;
        vm.setTab = setTab;
        vm.fetchOptions = fetchOptions;
        vm.isEditable = isEditable;
        vm.getWatermarkPosition = getWatermarkPosition;
        vm.revert = revert;
        vm.isDocumentChanged = isDocumentChanged;
        vm.getAttributes = getAttributes;
        vm.getChoiceName = getChoiceName;
        vm.getTreeStyle = getTreeStyle;
        vm.toggleTree = toggleTree;
        vm.getAdditionalMetadata = _.memoize(getAdditionalMetadata);

        setPrint();
        setInitialState();
        prepareFetchedOptions();
        prepareLanguages();

        $scope.$on('$destroy', () => {
            onStateChange();
        });

        function getTreeStyle() {
            return {
                width: vm.treeIsCollapsed ? '65px' : '300px'
            };
        }

        function getAdditionalMetadata() {
            if (vm.documents.kdr.hasOwnProperty('additionalMetadata')) {
                return angular.fromJson(vm.documents.kdr.additionalMetadata[0]);
            }

            return {};
        }

        function toggleTree() {
            vm.treeIsCollapsed = !vm.treeIsCollapsed;
        }

        function hasMultiselect(prop) {
            let allowEnums = Boolean(vm.configuration.allowEnums);
            if (allowEnums === false) {
                allowEnums = type !== 'cho';
            }

            return _.indexOf(multiselectTerms, prop) !== -1 && allowEnums;
        }

        function prepareFetchedOptions() {
            _.each(multiselectTerms, term => {
                vm.fetchedOptions[term] = [];
            });
        }

        function prepareLanguages() {
            angular.forEach(DocumentService.Languages, (value, key) => {
                vm.languages.push({
                    key: key,
                    value: value
                });
            });
        }

        function setTab(tab) {
            vm.tab = tab;
        }

        function fetchOptions(prop, query) {
            if (query.length > 0) {
                SuggestService.fetch(prop, query).then(setOptions);
            }

            function setOptions(results) {
                vm.fetchedOptions[prop] = results;
            }
        }

        function setPrint() {
            if (vm.isPrint) {
                Page.setPrint();
                let timeout;

                timeout = $timeout(() => {
                    $window.print();
                    $timeout.cancel(timeout);
                }, 1000);
            } else {
                Page.unsetPrint();
            }
        }

        function isEditable() {
            return vm.isPrint === false && vm.isPublished === false;
        }

        function getWatermarkPosition() {
            if (vm.document.watermarkPosition) {
                return _.find(vm.watermarkPositions, {key: vm.document.watermarkPosition}).name;
            }

            return '';
        }

        function setInitialState() {
            vm.document = plainConcept.clone();
        }

        function isDocumentChanged() {
            return _.isEqual(plainConcept.plain(), vm.document.plain(), comparator) === false;

            function comparator(original, edited) {
                if (moment.isMoment(original)) {
                    let otherDate = moment(edited * 1000);
                    otherDate.startOf('day');

                    return original.unix() === otherDate.unix();
                } else if (angular.isObject(edited)) {
                    for (const prop in edited) {
                        if (edited.hasOwnProperty(prop)) {
                            if (original.hasOwnProperty(prop) === false) {
                                return true;
                            }
                        }
                    }
                }

                return undefined;
            }
        }

        /**
         * @param {Event} event
         */
        function onStateChangeStart(event) {
            if (isDocumentChanged() && $window.confirm('Máte neuložené změny. Opravdu chcete opustit tuto stránku?') === false) {
                event.preventDefault();
            }
        }

        function isAllowedAttribute(key) {
            return allowedAttributes.hasOwnProperty(type) ? _.indexOf(allowedAttributes[type], key) !== -1 : true;
        }

        function getAttributes(filledOnly = false) {
            const choices = _.filter(DocumentService.DCTerms, item => {
                let exists = true;
                if (filledOnly) {
                    exists = vm.document.hasOwnProperty(item.key) && vm.document[item.key].length > 0;
                }

                return exists && isAllowedAttribute(item.key);
            });

            return _.sortBy(choices, item => {
                return _.indexOf(vm.document._order, item.key);
            });
        }

        function getChoiceName(key) {
            return _.find(choices, 'key', key).name;
        }

        function addValue(field) {
            if (vm.document.hasOwnProperty(field.key) === false) {
                vm.document[field.key] = [];
                vm.document._order.push(field.key);
            }

            if (Boolean(field.multiselect) === false || Configuration.document.allowEnums || vm.document.zdoType === 'cho') {
                vm.document[field.key].push('');
            }
        }

        function removeValue(field, index) {
            vm.document[field.key].splice(index, 1);
            if (vm.document[field.key].length === 0) {
                delete vm.document[field.key];
                vm.document._order.splice(vm.document._order.indexOf(field.key), 1);
            }
        }

        function addCustomValue(field) {
            field.values.push('');
        }

        function removeCustomValue(field, index) {
            field.values.splice(index, 1);
            if (field.values.length === 0) {
                field.deleted = true;
            }
        }

        function addCustomField() {
            let next = vm.document.customFields.length + 1;
            vm.document.customFields.push({
                name: '',
                deleted: false,
                visible: true,
                values: [''],
                order: next
            });
        }

        function submit() {
            if (vm.form.$valid) {
                vm.loading = true;
                DocumentService.save(vm.document).then(success).catch(failure).finally(complete);
            }

            function success() {
                Notifications.changesSaved();
                plainConcept = vm.document.clone();
                $scope.$broadcast('documentSaved');
            }

            function failure() {
                Notifications.addError('Při ukládání dokumentu došlo k chybě.');
            }

            function complete() {
                vm.loading = false;
            }
        }

        function revert() {
            if ($window.confirm('Opravdu chcete zahodit všechny změny?')) {
                $state.go('secured.batch.edit', {id: vm.document.batchId});
            }
        }

        function getOaiShortcuts() {
            return _.keys(documents.oai);
        }

        function getValue(field, value) {
            if (field === 'language') {
                return DocumentService.Languages[value];
            }

            return value;
        }
    }
}
