export class DocumentController {
    /**
     * @ngInject
     * @param $scope
     * @param $modal
     * @param $state
     * @param {Object} $stateParams
     * @param {DocumentEntity} doc
     * @param {Object} settings
     * @param {SearchService} SearchService
     * @param {DocumentService} DocumentService
     * @param {User} User
     * @param {Configuration} Configuration
     * @param {Translator} Translator
     * @param {Notifications} Notifications
     */
    constructor($scope, $modal, $state, $stateParams, doc, settings, SearchService, DocumentService, User, Configuration, Translator, Notifications) {
        let vm = this,
            term = $stateParams.term,
            query = $stateParams.query,
            facetQuery = $stateParams['fq[]'],
            page = $stateParams.page || SearchService.PageDefault,
            maxCount = $stateParams.maxCount || SearchService.MaxCountDefault,
            expoId = $stateParams.expoId,
            currentIndex = null,
            latestSearch = User.getLatestSearchFromHistory();

        vm.hideSignature = Boolean(Configuration.document.hideSignature);
        vm.hideInventoryId = Boolean(Configuration.document.hideInventoryId);
        vm.hideAlternativeTitle = angular.isDefined(Configuration.document.showFieldsForAnonymousUser);

        if (doc === null) {
            vm.document = doc;
            vm.term = term;
            vm.settings = {};
        } else {
            vm.document = doc;
            vm.issue = null;
            vm.settings = settings;

            if (latestSearch) {
                vm.term = latestSearch.term;
                vm.query = latestSearch.query;
            } else {
                vm.term = null;
                vm.query = null;
            }

            vm.share = share;
            vm.goToNext = goToNext;
            vm.goToPrevious = goToPrevious;
            vm.favorite = favorite;
            vm.clipboard = clipboard;
            vm.getValues = getValues;
            vm.getAttributes = getAttributes;
            vm.getCustomAttributes = getCustomAttributes;
            vm.isArray = (value) => _.isArray(value);
            vm.openViewer = openViewer;
            vm.openPlayer = openPlayer;
            vm.getGoBackLink = getGoBackLink;
            vm.makeDocumentQuery = makeDocumentQuery;

            load();
            setOpenGraphTags();
        }

        $scope.$on('issueViewLoaded', issueViewLoaded);

        function setOpenGraphTags() {
            $scope.$emit('document', [{
                'property': 'og:title',
                'content': doc.title
            }, {
                'property': 'og:image',
                'content': location.origin + '/dcap/discovery/data/file/' + doc.imgThumb
            }]);
        }

        function load() {
            loadIssues();
            loadRelatedDocuments();
            loadOtherByQuery();

            function loadIssues() {
                if (doc.zdoType === 'periodical') {
                    SearchService.getTree(doc.id).then(processTree);

                    if ($stateParams.term) {
                        SearchService.searchChildren($stateParams.term, doc.id).then(results => {
                            if (results.length === 1) {
                                vm.issue = results[0].id;
                                vm.onIssueSelect(results[0]);
                            }
                        });
                    }
                }

                function processTree(results) {
                    vm.issues = mapIssues();
                    vm.onIssueSelect = onIssueSelect;

                    /**
                     * @param {DocumentEntity} issue
                     */
                    function onIssueSelect(issue) {
                        $state.go('secured.document.issue', {
                            issue: issue.inventoryId
                        });
                    }

                    function mapIssues() {
                        let result = [];
                        angular.forEach(results, volume => {
                            angular.forEach(volume.children, issue => {
                                let volumeName = `${__('Ročník') }: ${volume.title}`;
                                if (volume.created) {
                                    volumeName = volumeName.concat(` (${volume.created})`);
                                }

                                result.push({
                                    inventoryId: issue.inventoryId,
                                    title: issue.title,
                                    volume: volumeName
                                });
                            });
                        });

                        return result;
                    }
                }
            }

            function loadRelatedDocuments() {
                DocumentService.getMoreLikeThis(doc.inventoryId).then(response => {
                    vm.related = response;
                });
            }

            function loadOtherByQuery() {
                SearchService.search(query, facetQuery, {
                    maxCount: $stateParams.numResults
                }).then(response => {
                    let results = response.searchResults,
                        _doc = _.find(results, item => item.inventoryId === doc.inventoryId);

                    currentIndex = _.indexOf(results, _doc);

                    vm.results = results;
                    vm.currentIndex = currentIndex + 1;
                });
            }
        }

        function getAttributes() {
            return _.filter(DocumentService.Attributes, term => {
                let exists = vm.document.hasOwnProperty(term.key),
                    isVisible = angular.isUndefined(Configuration.document.showFieldsForAnonymousUser) || (User.isLoggedIn() === false && Configuration.document.showFieldsForAnonymousUser.indexOf(term.key) === -1),
                    isNotAllowed = term.key === 'alternative' && (exists === false || vm.document.getProperty(term.key).length === 0 || vm.document.getProperty(term.key)[0] === 'unknown');

                if (isNotAllowed || (Boolean(Configuration.document.allowDocumentSubType) === false && term.key === 'documentSubType')) {
                    return false;
                }

                return exists && isVisible;
            });
        }

        function getCustomAttributes() {
            const customAttributes = Object.assign({}, vm.document.customAttributes);
            for (let key in customAttributes) {
                if (customAttributes.hasOwnProperty(key)) {
                    if (angular.isUndefined(Configuration.document.showCustomFieldsForAnonymousUser) || (User.isLoggedIn() === false && Configuration.document.showCustomFieldsForAnonymousUser.indexOf(key) === -1)) {
                        delete customAttributes[key];
                    }
                }
            }

            return customAttributes;
        }

        function getValues(key) {
            let values = doc.getProperty(key);

            if (angular.isArray(values) === false) {
                values = [values];
            }

            if (values.length === 1 && values[0] === 'unknown') {
                return [Translator.translate(SearchService.StringUnknown)];
            }

            if (key === 'language') {
                values = _.map(values, language => {
                    return DocumentService.Languages.hasOwnProperty(language) ? DocumentService.Languages[language] : language;
                });
            }

            return values;
        }

        function openViewer() {
            DocumentService.createViewer(doc.imageIds, doc.thumbIds, doc, null, vm.settings);
        }

        function openPlayer() {
            $modal.open({
                templateUrl: 'views/controllers/Document/player.html',
                controller: controller,
                controllerAs: 'vm',
                backdrop: 'static'
            });

            function controller() {
                let vm = this;

                vm.url = `/dcap/discovery/data/stream/${doc.videoId}`;
            }
        }

        function go(id) {
            $state.go('secured.document', {
                id: id
            });
        }

        function goToPrevious() {
            go(vm.results[currentIndex - 1].inventoryId);
        }

        function goToNext() {
            go(vm.results[currentIndex + 1].inventoryId);
        }

        function getSearchStateParams() {
            let params = {
                term: term,
                query: query,
                'fq[]': facetQuery,
                yearStart: $stateParams.yearStart,
                yearEnd: $stateParams.yearEnd
            };

            if (page > SearchService.PageDefault) {
                params.page = page;
            }

            if (maxCount !== SearchService.MaxCountDefault) {
                params.maxCount = maxCount;
            }

            return params;
        }

        function getGoBackLink() {
            let params = getSearchStateParams();
            if (expoId) {
                params.id = expoId;
                return $state.href('secured.expo', params);
            } else {
                return $state.href('secured.search', params);
            }
        }

        function favorite(invId) {
            User.favoriteDocument(invId).then(() => {
                Notifications.addSuccess(__('Dokument byl přidán do oblíbených.'));
            });
        }

        function clipboard(invId) {
            User.addToClipboard(invId).then(() => {
                Notifications.addSuccess(__('Dokument byl přidán do schránky.'));
            });
        }

        function makeDocumentQuery(id) {
            if (User.isLoggedIn() === false) {
                Notifications.loginRequired();
                return;
            }

            $modal.open({
                templateUrl: 'views/forms/Document/makeQuery.html',
                controller: controller,
                controllerAs: 'vm'
            });

            function controller($scope) {
                let vm = this;

                vm.query = '';
                vm.submit = submit;

                function submit() {
                    if (vm.form.$valid) {
                        vm.loading = true;
                        User.makeDocumentQuery(id, vm.query).then(() => {
                            Notifications.addSuccess(__('Dotaz byl odeslán.'));
                            $scope.$close();
                        });
                    }
                }
            }
        }

        function share() {
            FB.ui({
                method: 'share',
                href: location.href
            }, response);

            function response() {

            }
        }

        function issueViewLoaded(event, inventoryId) {
            $scope.$watch('vm.issues', issuesLoaded);

            function issuesLoaded() {
                if (vm.issues) {
                    vm.issue = _.find(vm.issues, {
                        inventoryId: inventoryId
                    });
                }
            }
        }
    }
}
