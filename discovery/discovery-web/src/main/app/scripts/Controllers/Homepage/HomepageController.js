import {SCREEN} from '../../Enums/CssEnum.js';

const RANDOM_MAX_COUNT = 3;

export class HomepageController {
    /**
     * @ngInject
     * @param $state
     * @param $location
     * @param $modal
     * @param {ArticleService} ArticleService
     * @param {SearchService} SearchService
     * @param {ExpoService} ExpoService
     */
    constructor($state, $location, $modal, ArticleService, SearchService, ExpoService) {
        let vm = this,
            $window = angular.element(window);

        $window.on('resize', _.debounce(onWindowResize, 150));

        vm.getPages = getPages;
        vm.getHostname = getHostname;
        vm.getPageCount = getPageCount;
        vm.openRssModal = openRssModal;
        vm.getValuesByPage = (array, page, limit) => array.slice(page * limit, page * limit + limit);
        vm.generateDocumentTypeLink = generateDocumentTypeLink;

        load();
        onWindowResize();

        function load() {
            loadArticles();
            loadExpos();
            loadRandomDocuments();
            loadDocumentTypeFacet();

            function loadRandomDocuments() {
                SearchService.search(null, null, {
                    maxCount: 0
                }).then(success);

                function success(response) {
                    vm.random = [];

                    let starts = [];
                    for (let i = 0; i < RANDOM_MAX_COUNT; i++) {
                        let max = response.numResults, start;

                        do {
                            start = _.random(0, max - 1);
                        } while (starts.indexOf(start) !== -1 && response.numResults >= RANDOM_MAX_COUNT);

                        if (starts.indexOf(start) !== -1) {
                            continue;
                        }

                        SearchService.search(null, null, {
                            maxCount: 1,
                            start: start
                        }).then(response => {
                            vm.random = vm.random.concat(response.searchResults);
                        });

                        starts.push(start);
                    }
                }
            }

            function loadArticles() {
                ArticleService.findAll().then(articles => {
                    vm.articles = articles.slice(0, 4);
                });
            }

            function loadExpos() {
                ExpoService.findAll().then(expos => {
                    vm.expos = expos;
                });
            }

            function loadDocumentTypeFacet() {
                SearchService.findFacets().then(facets => {
                    vm.documentTypes = facets.documentType;
                });
            }
        }

        function generateDocumentTypeLink(type) {
            let fq = SearchService.formatFacetQuery('documentType', type);
            return $state.href('secured.search', {'fq[]': fq});
        }

        function onWindowResize() {
            let width = $window.width();
            if (width < SCREEN.XS_MAX) {
                vm.exposPerSlide = 1;
            } else {
                vm.exposPerSlide = 2;
            }
        }

        function getPageCount(array, limit) {
            if (_.isArray(array)) {
                return Math.ceil(array.length / limit);
            }

            return undefined;
        }

        function getHostname(url) {
            let link = document.createElement('a');
            link.href = url;
            return link.hostname;
        }

        function openRssModal() {
            $modal.open({
                templateUrl: 'views/controllers/Homepage/rss.html'
            });
        }

        /**
         * @param {Array} array
         * @param limit
         */
        function getPages(array, limit) {
            if (_.isArray(array)) {
                let pages = Math.ceil(array.length / limit);
                return _.range(0, pages);
            }

            return [];
        }
    }
}
