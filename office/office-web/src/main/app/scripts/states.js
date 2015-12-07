require('angular-ui-router');

import ROLES from './Roles.js';

angular.module('zdo.office.states', [
    'ui.router'
]).config(config);

function config($locationProvider, $stateProvider, $urlRouterProvider) {
    $locationProvider.html5Mode(true);

    $stateProvider
        .state('login', {
            url: '/login',
            templateUrl: 'views/controllers/Login/default.html',
            onEnter: Page => {
                Page.setClass('external-page sb-l-c');
            }
        })
        .state('secured', {
            abstract: true,
            templateUrl: 'views/controllers/layout.html',
            onEnter: Page => {
                Page.setClass('sb-l-o');
            },
            data: {
                restricted: true
            }
        })
        .state('secured.home', {
            url: '/',
            views: {
                'main@secured': {
                    controller: 'HomeController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Home/default.html'
                }
            }
        })
        .state('secured.discovery', {
            url: '/discovery',
            views: {
                'main@secured': {
                    controller: 'DiscoveryController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Discovery/default.html'
                }
            },
            resolve: {
                settings: DiscoveryService => DiscoveryService.find()
            },
            data: {
                allowed: [ROLES.SYS_ADMIN]
            }
        })
        .state('secured.document', {
            url: '/document?state',
            views: {
                'main@secured': {
                    controller: 'DocumentController',
                    templateUrl: 'views/controllers/Document/default.html'
                }
            },
            data: {
                allowed: [ROLES.CURATOR]
            }
        })
        .state('secured.batch', {
            url: '/batch?state',
            views: {
                'main@secured': {
                    controller: 'BatchController',
                    templateUrl: 'views/controllers/Batch/default.html'
                }
            },
            data: {
                allowed: [ROLES.CURATOR]
            }
        })
        .state('secured.batch.edit', {
            url: '/{id}',
            resolve: {
                batch: ($stateParams, BatchService) => BatchService.find($stateParams.id)
            },
            views: {
                'main@secured': {
                    controller: 'BatchEditController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Batch/edit.html'
                }
            }
        })
        .state('secured.batch.edit.document', {
            url: '/document/{documentId}?print',
            resolve: {
                documents: ($stateParams, DocumentService) => DocumentService.find($stateParams.documentId)
            },
            views: {
                'main@secured': {
                    controller: 'DocumentEditController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Document/edit.html'
                }
            }
        })
        .state('secured.organization', {
            url: '/organizations',
            views: {
                'main@secured': {
                    controller: 'OrganizationController',
                    templateUrl: 'views/controllers/Organization/default.html'
                }
            },
            data: {
                allowed: [ROLES.SYS_ADMIN, ROLES.ORG_ADMIN]
            }
        })
        .state('secured.organization.edit', {
            url: '/{id}',
            resolve: {
                organization: ($stateParams, OrganizationService) => OrganizationService.find($stateParams.id)
            },
            views: {
                'main@secured': {
                    controller: 'OrganizationEditController',
                    templateUrl: 'views/controllers/Organization/edit.html'
                }
            }
        })
        .state('secured.gallery', {
            url: '/galleries',
            views: {
                'main@secured': {
                    controller: 'GalleryController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Gallery/default.html'
                }
            },
            data: {
                allowed: [ROLES.CURATOR]
            }
        })
        .state('secured.gallery.create', {
            url: '/create',
            onEnter: ($modal, $state) => {
                $modal.open({
                    templateUrl: 'views/forms/Gallery/form.html',
                    controller: 'GalleryCreateController',
                    controllerAs: 'vm'
                }).result.finally(() => {
                    $state.go('^');
                });
            }
        })
        .state('secured.gallery.edit', {
            url: '/{id:int}',
            onEnter: ($modal, $state, $stateParams) => {
                $modal.open({
                    templateUrl: 'views/forms/Gallery/form.html',
                    controller: 'GalleryEditController',
                    controllerAs: 'vm',
                    resolve: {
                        gallery: GalleryService => GalleryService.find($stateParams.id)
                    }
                }).result.finally(() => {
                    $state.go('^');
                });
            }
        })
        .state('secured.article', {
            url: '/articles',
            views: {
                'main@secured': {
                    controller: 'ArticleController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Article/default.html'
                }
            },
            data: {
                allowed: [ROLES.REDACTOR]
            }
        })
        .state('secured.article.create', {
            url: '/create',
            onEnter: ($modal, $state) => {
                $modal.open({
                    templateUrl: 'views/forms/Article/form.html',
                    controller: 'ArticleCreateController'
                }).result.finally(() => {
                    $state.go('^');
                });
            }
        })
        .state('secured.article.edit', {
            url: '/{id:int}',
            onEnter: ($modal, $state, $stateParams) => {
                $modal.open(
                    {
                        templateUrl: 'views/forms/Article/form.html',
                        controller: 'ArticleEditController',
                        resolve: {
                            article: ArticleService => ArticleService.find($stateParams.id)
                        }
                    }
                ).result.finally(() => {
                    $state.go('^');
                });
            }
        })
        .state('secured.upload', {
            url: '/upload',
            views: {
                'main@secured': {
                    controller: 'UploadController',
                    controllerAs: 'vm',
                    templateUrl: 'views/controllers/Upload/default.html'
                }
            }
        })
        .state('secured.settings', {
            url: '/settings',
            abstract: true,
            data: {
                allowed: [ROLES.SYS_ADMIN]
            }
        })
        .state('secured.settings.type', {
            url: '/type',
            views: {
                'main@secured': {
                    controller: 'TypeController',
                    templateUrl: 'views/controllers/Settings/Type/default.html'
                }
            }
        })
        .state('secured.settings.type.edit', {
            url: '/{id:int}',
            resolve: {
                type: ($stateParams, TypeService) => TypeService.find($stateParams.id)
            },
            views: {
                'main@secured': {
                    controller: 'TypeEditController',
                    templateUrl: 'views/controllers/Settings/Type/edit.html'
                }
            }
        })
        .state('secured.settings.notification', {
            url: '/notifications',
            views: {
                'main@secured': {
                    controller: 'NotificationController',
                    templateUrl: 'views/controllers/Settings/Notification/default.html'
                }
            }
        })
        .state('secured.settings.notification.edit', {
            url: '/{id:int}',
            onEnter: ($modal, $state, $stateParams) => {
                $modal.open({
                    size: 'lg',
                    controller: 'NotificationEditController',
                    templateUrl: 'views/forms/Settings/Notification/form.html',
                    resolve: {
                        notification: MailNotificationService => MailNotificationService.find($stateParams.id)
                    }
                }).result.finally(() => {
                    $state.go('^');
                });
            }
        })
        .state('secured.settings.source', {
            url: '/sources',
            abstract: true
        })
        .state('secured.settings.source.oai', {
            url: '/oai',
            views: {
                'main@secured': {
                    controller: 'SourceOAIController',
                    templateUrl: 'views/controllers/Sources/OAI/default.html'
                }
            }
        })
        .state('secured.settings.source.sru', {
            url: '/sru',
            views: {
                'main@secured': {
                    controller: 'SourceSRUController',
                    templateUrl: 'views/controllers/Sources/SRU/default.html'
                }
            }
        })
        .state('secured.settings.source.z3950', {
            url: '/z3950',
            views: {
                'main@secured': {
                    controller: 'SourceZ3950Controller',
                    templateUrl: 'views/controllers/Sources/Z3950/default.html'
                }
            }
        })
        .state('secured.administration', {
            url: '/administration',
            views: {
                'main@secured': {
                    templateUrl: 'views/controllers/Administration/default.html',
                    controller: 'AdministrationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                settings: OrganizationService => OrganizationService.getSettings()
            },
            data: {
                allowed: [ROLES.ORG_ADMIN]
            }
        })
        .state('secured.verify', {
            url: '/verify',
            views: {
                'main@secured': {
                    templateUrl: 'views/controllers/Verify/default.html',
                    controller: 'VerifyController',
                    controllerAs: 'vm'
                }
            },
            data: {
                allowed: [ROLES.SYS_ADMIN, ROLES.ORG_ADMIN]
            }
        });

    $urlRouterProvider.otherwise('/');
}
