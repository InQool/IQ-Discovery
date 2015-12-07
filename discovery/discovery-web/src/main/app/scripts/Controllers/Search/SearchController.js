export class SearchController {
    /**
     * @ngInject
     * @param $state
     * @param {Object} $stateParams
     * @param {String} $stateParams.layout
     * @param {String} $stateParams.term
     * @param {String} $stateParams.query
     * @param {String} $stateParams.page
     * @param {String} $stateParams.maxCount
     * @param {String} $stateParams.orderBy
     * @param {String} $stateParams.orderDir
     * @param {String} $stateParams.yearStart
     * @param {String} $stateParams.yearEnd
     * @param $q
     * @param {String} query
     * @param {String[]} facetQuery
     * @param {User} User
     * @param {SearchService} SearchService
     * @param {DocumentService} DocumentService
     * @param {Notifications} Notifications
     * @param {Translator} Translator
     */
    constructor($state, $stateParams, $q, query, facetQuery, User, SearchService, DocumentService, Notifications, Translator) {
        let vm = this;

        vm.external = [];
        vm.loading = false;
        vm.layout = $stateParams.layout || 'list';
        vm.term = $stateParams.term || '';
        vm.page = $stateParams.page || SearchService.PageDefault;
        vm.orderBy = $stateParams.orderBy || 'score';
        vm.orderDir = $stateParams.orderDir || 'DESC';
        vm.maxCount = $stateParams.maxCount || SearchService.MaxCountDefault;
        vm.yearStart = $stateParams.yearStart;
        vm.yearEnd = $stateParams.yearEnd;
        vm.options = SearchService.Options;

        vm.isGridLayout = isGridLayout;
        vm.getStateData = getStateData;
        vm.saveSearch = saveSearch;
        vm.isSelected = isSelected;
        vm.select = select;
        vm.setOrderBy = setOrderBy;
        vm.setOrderDir = setOrderDir;
        vm.setMaxCount = setMaxCount;

        vm.setLayout = setLayout;
        vm.setPage = setPage;
        vm.getFacetName = getFacetName;
        vm.getFacetValue = getFacetValue;
        vm.getFacetValues = getFacetValues;
        vm.submitYears = submitYears;
        vm.getValue = getValue;

        search();

        function reload(params = {}) {
            $state.go('.', params, {
                reload: true
            });
        }

        function isGridLayout() {
            return vm.layout === 'grid';
        }

        function setOrderBy(value) {
            reload({
                orderBy: value === 'score' ? null : value
            });
        }

        function setOrderDir(value) {
            reload({
                orderDir: value === 'DESC' ? null : value
            });
        }

        function setMaxCount(value) {
            value = Number(value);
            reload({
                maxCount: value === SearchService.MaxCountDefault ? null : value
            });
        }

        function setLayout(layout) {
            reload({
                layout: layout
            });
        }

        function setPage(value) {
            reload({
                page: value === 1 ? null : value
            });
        }

        function getFacetName(name) {
            return SearchService.FacetNames[name];
        }

        function getFacetValue(facet, value) {
            if (SearchService.FacetValues.hasOwnProperty(facet) && SearchService.FacetValues[facet].hasOwnProperty(value)) {
                return SearchService.FacetValues[facet][value];
            }

            if (value === 'unknown') {
                return Translator.translate(SearchService.StringUnknown);
            }

            if (facet === 'language') {
                if (DocumentService.Languages.hasOwnProperty(value)) {
                    return DocumentService.Languages[value];
                }
            }

            return value;
        }

        /**
         * @param {Object} facet
         * @param {String} facet.name
         * @param {Array} facet.hitMap
         */
        function getFacetValues(facet) {
            if (facet.name === 'datePublished') {
                let hitMap = {};
                angular.forEach(SearchService.FacetValues.datePublished, (value, key) => {
                    hitMap[key] = facet.hitMap[key];
                });

                return hitMap;
            }

            return facet.hitMap;
        }

        function select(facet, value) {
            if (vm.isSelected(facet, value)) { // clicked facet is selected, de-select
                removeFacetValue(facet, value);
            } else {
                if (isSelectedFacet(facet)) { // facet is not selected, but is from the same group
                    let index = _.indexOf(facetQuery, getFacetByName(facet));
                    if (index !== -1) {
                        facetQuery.splice(index, 1);
                    }
                }

                facetQuery.push(SearchService.formatFacetQuery(facet, value));
            }

            let params = {
                'fq[]': facetQuery
            };

            if (vm.page > 1) {
                params.page = null; // reset to page 1
            }

            reload(params);
        }

        function removeFacetValue(facet, value) {
            let index = _.indexOf(facetQuery, SearchService.formatFacetQuery(facet, value));
            if (index !== -1) {
                facetQuery.splice(index, 1);
            }
        }

        function isSelected(facet, value) {
            return _.indexOf(facetQuery, SearchService.formatFacetQuery(facet, value)) !== -1;
        }

        function isSelectedFacet(facet) {
            return angular.isDefined(getFacetByName(facet));
        }

        function getFacetByName(facet) {
            return _.find(facetQuery, entry => _.startsWith(entry, facet));
        }

        function getFacetQuery() {
            return facetQuery.concat(SearchService.formatFacetQuery('yearStart', [vm.yearStart, vm.yearEnd]));
        }

        function saveSearchToHistory(count) {
            let latestSearch = User.getLatestSearchFromHistory();
            if (latestSearch && latestSearch.query === query) {
                User.updateLatestSearchInLocalHistory({
                    query: query,
                    facetQuery: facetQuery,
                    yearStart: vm.yearStart,
                    yearEnd: vm.yearEnd,
                    count: count
                });
            } else {
                User.saveSearchToLocalHistory(query, facetQuery, vm.yearStart, vm.yearEnd, count);
            }
        }

        function getValue(result, property) {
            if (_.indexOf(result[property], 'unknown') !== -1) {
                return Translator.translate(SearchService.StringUnknown);
            }

            return result[property].join(', ');
        }

        function search() {
            vm.loading = true;

            SearchService.search(query, getFacetQuery(), {
                maxCount: vm.maxCount,
                orderBy: vm.orderBy,
                orderDir: vm.orderDir,
                start: (vm.page - 1) * vm.maxCount
            }).then(searchComplete);

            searchExternal();

            /**
             * @param {Object} response
             * @param {Array} response.searchResults
             * @param {Number} response.numResults
             * @param {Array} response.facets
             */
            function searchComplete(response) {
                vm.results = response.searchResults;
                vm.resultsCount = response.numResults;

                let facets = response.facets;
                if (isSelectedFacet('documentType') === false) {
                    _.remove(facets, {
                        name: 'documentSubType'
                    });
                }

                vm.facets = facets;
                vm.loading = false;

                saveSearchToHistory(vm.resultsCount);
            }
        }

        function getStateData(result) {
            return {
                id: result.inventoryId,
                maxCount: vm.maxCount !== SearchService.MaxCountDefault ? vm.maxCount : null,
                numResults: vm.resultsCount,
                page: vm.page !== SearchService.PageDefault ? vm.page : null,
                query: query,
                'fq[]': facetQuery,
                yearStart: vm.yearStart,
                yearEnd: vm.yearEnd
            };
        }

        function submitYears() {
            reload({
                'fq[]': facetQuery,
                yearStart: vm.yearStart,
                yearEnd: vm.yearEnd
            });
        }

        function saveSearch() {
            User.saveSearch(query || vm.term, facetQuery).then(() => {
                Notifications.addSuccess(__('Hledání bylo uloženo.'));
            });
        }

        function searchExternal() {
            if (query && !query.includes(':') && facetQuery.length === 0) {
                vm.externalLoading = true;

                let promises = [
                    SearchService.findZ3950Sources().then(processZ3950),
                    SearchService.findOaiSources().then(processOai)
                ];

                $q.all(promises).then(() => {
                    vm.loading = false;
                });
            } else {
                vm.sources = [];
            }

            function processZ3950(sources) {
                let promises = [];
                for (let source of sources) {
                    let promise = source.get({
                        query: query
                    }).then(results => {
                        source.results = results;
                    });

                    promises.push(promise);
                }

                $q.all(promises).then(() => {
                    vm.sources = sources;
                    vm.externalLoading = false;
                });
            }

            function processOai(sources) {
                vm.externalOai = sources;

                SearchService.searchOaiSources(query).then(results => {
                    angular.forEach(results, result => {
                        const source = _.find(sources, {
                            url: result.source[0]
                        });
                        
                        if (source.hasOwnProperty('results') === false) {
                            source.results = [];
                        }

                        source.results.push(result);
                    });
                });
            }
        }
    }
}
