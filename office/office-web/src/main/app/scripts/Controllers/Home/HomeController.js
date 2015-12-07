const moment = require('moment');

export class HomeController {
    /**
     * @ngInject
     * @param {Analytics} Analytics
     * @param {Statistics} Statistics
     * @param {DocumentService} DocumentService
     * @param {PeopleService} PeopleService
     * @param {OrganizationService} OrganizationService
     * @param {Configuration} Configuration
     */
    constructor(Analytics, Statistics, DocumentService, PeopleService, OrganizationService, Configuration) {
        const vm = this;

        vm.logo = Configuration.logo;
        vm.options = createOptions();
        vm.curator = {};
        vm.organization = {};

        vm.clear = clear;
        vm.reloadStatistics = reloadStatistics;

        loadToken();
        loadStatistics();
        loadOrganizationsAndUsers();

        function loadStatistics() {
            loadTypeStatistics();
            loadGraphStatistics();

            Statistics.findTopCurators().then(curators => {
                vm.topCurators = curators.plain();
            });

            Statistics.findTopOrganizations().then(organizations => {
                vm.topOrganizations = organizations.plain();
            });

            Statistics.findTopViewedDocs().then(viewedDocs => {
                vm.topViewedDocs = viewedDocs;
            });

            Statistics.findTopFavoriteDocs().then(favoritedDocs => {
                vm.topFavoritedDocs = favoritedDocs;
            });
        }

        function loadTypeStatistics() {
            Statistics.findTopDocTypes(vm.organization.types).then(docTypes => {
                vm.topDocTypes = docTypes.plain();
            });

            Statistics.findTopDocSubTypes(vm.organization.types).then(docSubTypes => {
                vm.topDocSubTypes = docSubTypes.plain();
            });

            Statistics.findTopZdoTypes(vm.organization.types).then(zdoTypes => {
                vm.topZdoTypes = zdoTypes.plain();
                vm.getZdoType = getZdoType;

                function getZdoType(type) {
                    return DocumentService.Types[type];
                }
            });
        }

        function loadGraphStatistics() {
            Statistics.findWeeklyDocumentStats(vm.organization.graph, vm.curator.graph).then(weeklyDocStats => {
                vm.weeklyDocStats = _.map(['docsConcepted', 'docsPublished', 'docsReserved'], process);

                function process(documentType) {
                    return [
                        {
                            data: _.map(weeklyDocStats, row => {
                                return [Number(row.week) * 1000, row[documentType]];
                            })
                        }
                    ];
                }
            });
        }

        function loadOrganizationsAndUsers() {
            OrganizationService.findAll().then(organizations => {
                vm.organizations = organizations;
            });

            PeopleService.findAllCurators().then(curators => {
                vm.curators = curators;
            });
        }

        function createOptions() {
            return {
                series: {
                    points: {
                        show: true,
                        radius: 3,
                        fill: false
                    },
                    lines: {
                        show: true
                    }
                },
                xaxis: {
                    mode: 'time',
                    timeformat: '%d.%m.',
                    tickSize: [7, 'day']
                },
                yaxis: {
                    tickDecimals: 0
                },
                grid: {
                    show: true,
                    margin: {
                        top: 10,
                        bottom: 10
                    },
                    color: '#bbbbbb',
                    hoverable: true,
                    labelMargin: 15,
                    axisMargin: 0,
                    borderWidth: 0,
                    borderColor: null,
                    autoHighlight: true,
                    mouseActiveRadius: 20
                },
                tooltip: {
                    show: true,
                    content: function (ignore, x, y) {
                        let docs;
                        if (y === 1) {
                            docs = 'dokument';
                        } else if (y < 5 && y > 1) {
                            docs = 'dokumenty';
                        } else {
                            docs = 'dokumentů';
                        }

                        return `<b>${moment.unix(x / 1000).format('DD. MM. YYYY') }</b> - %y.0 ${docs}`;
                    }
                },
                colors: ['#4a89dc', '#2ecc71', '#e74c3c', '#9b59b6']
            };
        }

        function loadToken() {
            if (Configuration.googleAnalyticsID) {
                Analytics.getToken().then(token => {
                    vm.token = token;
                });

                vm.chart = {
                    reportType: 'ga',
                    query: {
                        metrics: 'ga:users',
                        dimensions: 'ga:date',
                        'start-date': '30daysAgo',
                        'end-date': 'today',
                        ids: Configuration.googleAnalyticsID
                    },
                    chart: {
                        container: 'chart-container-1',
                        type: 'LINE',
                        options: {
                            width: '100%'
                        }
                    }
                };
            } else {
                vm.token = null;
            }
        }

        function reloadStatistics(type, who = null) {
            if (who === 'organization') {
                vm.curator[type] = null;
            } else if (who === 'curator') {
                vm.organization[type] = null;
            }

            if (type === 'graph') {
                loadGraphStatistics();
            } else if (type === 'types') {
                loadTypeStatistics();
            }
        }

        function clear(type, who) {
            vm[who][type] = null;
            reloadStatistics(type);
        }
    }
}
