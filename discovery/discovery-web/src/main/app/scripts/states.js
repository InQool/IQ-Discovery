require('angular-ui-router');

angular.module('zdo.discovery.states', ['ui.router', 'zdo.discovery.configuration'])
    .config(config);

/**
 * @param $locationProvider
 * @param $urlRouterProvider
 * @param $stateProvider
 * @param {Configuration} Configuration
 */
function config($locationProvider, $urlRouterProvider, $stateProvider, Configuration) {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise(function ($injector, $location) {
        let state = $injector.get('$state');
        state.go('secured.404');
        return $location.path();
    });

    $stateProvider
        .state('login', {
            url: '/authorization',
            templateUrl: 'views/controllers/Authorization/default.html'
        })
        .state('secured', {
            abstract: true,
            template: '<header></header><ui-view></ui-view><footer></footer>',
            data: {
                restricted: Configuration.development
            }
        })
        .state('secured.404', {
            templateUrl: 'views/controllers/Error/404.html'
        })
        .state('secured.login', {
            url: '/login',
            controller: 'LoginController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Login/default.html'
        })
        .state('secured.login.openid', {
            url: '/openid?firstName&lastName&city&street&streetNumber&userName&authctoken&zip&opNumber&verified',
            views: {
                '@secured': {
                    controller: 'LoginOpenIDController',
                    templateUrl: 'views/controllers/Login/openid.html'
                }
            }
        })
        .state('secured.login.openid.failure', {
            url: '/failure',
            views: {
                '@secured': {
                    templateUrl: 'views/controllers/Login/failure.html'
                }
            }
        })
        .state('secured.register', {
            url: '/register',
            controller: 'RegisterController',
            templateUrl: 'views/controllers/Register/default.html'
        })
        .state('secured.password', {
            url: '/password',
            abstract: true,
            template: '<div ui-view></div>'
        })
        .state('secured.password.forgotten', {
            url: '/forgotten',
            controller: 'PasswordForgottenController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Password/forgotten.html'
        })
        .state('secured.password.reset', {
            url: '/reset?hash',
            controller: 'PasswordResetController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Password/reset.html',
            resolve: {
                hash: ($stateParams) => $stateParams.hash,
                checkHash: ($state, User, hash) => {
                    User.checkPasswordHash(hash).catch(failure);

                    function failure() {
                        $state.go('secured.homepage');
                    }
                }
            }
        })
        .state('secured.homepage', {
            url: '/',
            controller: 'HomepageController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Homepage/default.html'
        })
        .state('secured.about', {
            url: '/about',
            templateUrl: 'views/controllers/About/default.html'
        })
        .state('secured.search', {
            url: '/search?term&query&fq[]&maxCount&page&layout&orderBy&orderDir&yearStart&yearEnd',
            controller: 'SearchController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Search/default.html',
            resolve: {
                query: ($stateParams) => $stateParams.query,
                facetQuery: ($stateParams) => $stateParams['fq[]'] || []
            }
        })
        .state('secured.searchAdvanced', {
            url: '/search/advanced',
            controller: 'SearchAdvancedController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Search/advanced.html'
        })
        .state('secured.document', {
            url: '/document/{id}?term&page&maxCount&query&fq[]&yearStart&yearEnd&expoId&numResults',
            onEnter: ($window) => {
                $window.scrollTo(0, 0);
            },
            resolve: {
                doc: (DocumentService, $stateParams) => DocumentService.getByInventoryId($stateParams.id).catch(request => {
                    if (request.status === 404) {
                        return null;
                    }
                }),
                settings: (doc, DataService) => DataService.getOrganizationSettings(doc.orgIdmId)
            },
            controller: 'DocumentController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Document/default.html'
        })
        .state('secured.document.issue', {
            url: '/{issue}',
            views: {
                'issue': {
                    controller: 'DocumentIssueController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Document/issue.html'
                }
            }
        })
        .state('secured.document.holdings', {
            url: '/holdings',
            templateUrl: 'views/controllers/Document/holdings.html'
        })
        .state('secured.document.reviews', {
            url: '/reviews',
            templateUrl: 'views/controllers/Document/reviews.html'
        })
        .state('secured.document.staffView', {
            url: '/staff-view',
            templateUrl: 'views/controllers/Document/staffView.html'
        })
        .state('secured.article', {
            url: '/articles',
            controller: 'ArticleController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Article/default.html'
        })
        .state('secured.article.view', {
            url: '/{id}',
            views: {
                '@secured': {
                    controller: 'ArticleViewController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Article/view.html'
                }
            },
            resolve: {
                article: (ArticleService, $stateParams) => ArticleService.find($stateParams.id)
            }
        })
        .state('secured.expo', {
            url: '/exposition/{id}?fq[]&maxCount&page&layout&orderBy&orderDir&yearStart&yearEnd',
            controller: 'ExpoController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Expo/default.html',
            resolve: {
                expo: (ExpoService, $stateParams) => ExpoService.find($stateParams.id),
                facetQuery: ($stateParams) => $stateParams['fq[]'] || []
            }
        })
        .state('secured.account', {
            abstract: true,
            url: '/account',
            template: '<div ui-view></div>',
            data: {
                loggedIn: true
            }
        })
        .state('secured.account.clipboard', {
            url: '/clipboard',
            controller: 'AccountClipboardController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Account/clipboard.html'
        })
        .state('secured.account.settings', {
            url: '/settings',
            views: {
                '': {
                    controller: 'AccountSettingsController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Account/settings.html'
                },
                '@secured.account.settings': {
                    templateUrl: 'views/controllers/Account/Settings/general.html'
                }
            }
        })
        .state('secured.account.settings.changePassword', {
            url: '/change-password',
            views: {
                '@secured.account.settings': {
                    controller: 'AccountSettingsChangePasswordController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Account/Settings/changePassword.html'
                }
            },
            onEnter: ($state, User) => {
                if (User.isLoggedInViaOpenId()) {
                    $state.go('secured.account.settings');
                }
            }
        })
        .state('secured.account.settings.favorited', {
            url: '/favorited',
            views: {
                '@secured.account.settings': {
                    controller: 'AccountSettingsFavoritedController',
                    templateUrl: 'views/controllers/Account/Settings/favorited.html'
                }
            }
        })
        .state('secured.account.settings.searches', {
            url: '/saved-searches',
            views: {
                '@secured.account.settings': {
                    controller: 'AccountSettingsSearchController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Account/Settings/searches.html'
                }
            }
        })
        .state('secured.account.settings.personal', {
            url: '/personal-information',
            views: {
                '@secured.account.settings': {
                    controller: 'AccountSettingsPersonalController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Account/Settings/personal.html'
                }
            }
        })
        .state('secured.history', {
            url: '/history',
            controller: 'HistoryController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/History/default.html'
        })
        .state('secured.catalog', {
            url: '/catalog',
            controller: 'CatalogController',
            controllerAs: 'vm',
            templateUrl: 'views/controllers/Catalog/default.html'
        });
}
