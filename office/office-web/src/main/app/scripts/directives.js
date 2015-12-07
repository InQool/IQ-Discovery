import {documentPreview} from './Directives/Document/documentPreview.js';
import {imagePreview} from './Directives/imagePreview.js';
import {documentType} from './Directives/Document/documentType.js';
import {customCss} from './Directives/Administration/customCss.js';
import {imageUpload} from './Directives/imageUpload.js';
import Sidebar from './Directives/Sidebar.js';

angular.module('zdo.office.directives', [])
    .directive('customCss', customCss)
    .directive('imageUpload', imageUpload)
    .directive('imagePreview', imagePreview)
    .directive('autofocus', ($timeout) => ({
        restrict: 'A',
        link: (scope, el) => {
            let timeout;
            timeout = $timeout(() => {
                el.focus();
                $timeout.cancel(timeout);
            }, 0);
        }
    }))
    .directive('header', (Page, User, $state, Configuration, Notifications) => ({
        scope: {},
        restrict: 'A',
        replace: true,
        templateUrl: 'views/directives/header.html',
        link: (scope, el) => {
            const body = angular.element('body');

            scope.user = User.getInfo();
            scope.developmentShowWarning = Configuration.developmentShowWarning;

            scope.logout = () => {
                User.logout();
                Notifications.addSuccess('Byl jste odhlášen.');

                if (Configuration.logoutRedirect) {
                    location.href = location.origin + '/' + Configuration.logoutRedirect;
                } else {
                    $state.go('login');
                }
            };

            el.find('.sidebar-menu li a.accordion-toggle').click(function (event) {
                // Any menu item with the accordion class is a dropdown submenu. Thus we prevent default actions
                event.preventDefault();

                // Any menu item with the accordion class is a dropdown submenu. Thus we prevent default actions
                if (body.hasClass('sb-l-m') && !$(this).parents('ul.sub-nav').length) {
                    return;
                }

                // Any menu item with the accordion class is a dropdown submenu. Thus we prevent default actions
                if ($(this).parents('ul.sub-nav').length) {
                    const activeMenu = $(this).next('ul.sub-nav'), siblingMenu = $(this).parent().siblings('li').children('a.accordion-toggle.menu-open').next('ul.sub-nav');

                    activeMenu.slideUp('fast', 'swing', function () {
                        $(this).attr('style', '').prev().removeClass('menu-open');
                    });

                    siblingMenu.slideUp('fast', 'swing', function () {
                        $(this).attr('style', '').prev().removeClass('menu-open');
                    });
                } else {
                    $('a.accordion-toggle.menu-open').next('ul').slideUp('fast', 'swing', function () {
                        $(this).attr('style', '').prev().removeClass('menu-open');
                    });
                }

                // Now we expand targeted menu item, add the ".open-menu" class
                // and remove any left over inline jQuery animation styles
                if (!$(this).hasClass('menu-open')) {
                    $(this).next('ul').slideToggle('fast', 'swing', function () {
                        $(this).attr('style', '').prev().toggleClass('menu-open');
                    });
                }
            });

            el.find('#toggle_sidemenu_l').click(() => {
                body.removeClass('sb-l-c');
                body.toggleClass('sb-l-m').removeClass('sb-r-o').addClass('sb-r-c');
                Page.triggerResize();
            });
        }
    }))
    .directive('sidebar', Sidebar)
    .directive('treeView', (BatchService, $state) => ({
        restrict: 'E',
        scope: {
            batchId: '@',
            documentId: '@'
        },
        replace: true,
        template: '<div class="tree-view"></div>',
        link: (scope, el) => {
            load();
            onDocumentSave();

            function load() {
                el.fancytree({
                    selectMode: 1,
                    clickFolderMode: 2,
                    source: getPromise(),
                    strings: {
                        loading: 'Nahrávám'
                    },
                    click: (event, data) => {
                        if (!data.node.folder) {
                            if (data.targetType === 'title') {
                                $state.go('.', {documentId: data.node.data.id});
                            }

                            if (data.targetType !== 'expander') {
                                event.preventDefault();
                            }
                        }
                    }
                });
            }

            function getPromise() {
                return BatchService.findTree(scope.batchId).then(formatTree);
            }

            function formatTree(tree) {
                const source = tree.plain();
                source.children = _.map(source.children, prepare);
                return [source];
            }

            function onDocumentSave() {
                scope.$on('documentSaved', reload);

                function reload() {
                    const tree = el.fancytree('getTree');
                    const promise = getPromise();

                    tree.reload(promise);
                }
            }

            function prepare(node, parent = null, parentOfParent = null) {
                if (node.id === scope.documentId) {
                    node.active = true;

                    if (_.isObject(parent)) {
                        parent.expanded = true;

                        if (_.isObject(parentOfParent)) {
                            parentOfParent.expanded = true;
                        }
                    }
                }

                if (node.validToPublish === false) {
                    node.extraClasses = 'invalid';
                }

                if (node.children.length > 0) {
                    node.children = _.map(node.children, child => {
                        return prepare(child, node, parent);
                    });
                }

                return node;
            }
        }
    }))
    .directive('tableView', (DataTable) => ({
        scope: {
            instance: '@',
            options: '=',
            columns: '='
        },
        replace: true,
        restrict: 'E',
        template: '<div><table class="table table-hover" datatable dt-instance="setInstance" dt-options="options" dt-columns="columns" dt-disable-deep-watcher="true"></table></div>',
        controller: ($scope) => {
            $scope.setInstance = (instance) => {
                DataTable.addInstance(instance, $scope.instance);
            };
        }
    }))
    .directive('documentPreview', documentPreview)
    .directive('documentUnpublish', ($modal) => ({
        restrict: 'A',
        scope: {
            batch: '@',
            document: '@'
        },
        link: (scope, el) => {
            el.on('click', () => {
                $modal.open({
                    templateUrl: 'views/forms/Document/unpublish.html',
                    controller: ($scope, DocumentService, Notifications) => {
                        $scope.document = _.find($scope.getTableData(), {id: scope.document});

                        $scope.submit = () => {
                            $scope.loading = true;
                            DocumentService.unpublish(scope.batch, scope.document).then(() => {
                                Notifications.addSuccess('Dokument byl znepřístupněn.');
                                $scope.reloadTable();
                                $scope.$dismiss();
                            });
                        };
                    }
                });
            });
        }
    }))
    .directive('documentType', documentType)
    .directive('datetimePicker', ($timeout) => ({
        restrict: 'A',
        require: 'ngModel',
        link: (scope, el, attrs, ngModel) => {
            const picker = el.datetimepicker({
                format: 'D. M. YYYY',
                useCurrent: false,
                sideBySide: true,
                locale: 'cs'
            }).data('DateTimePicker');

            el.on('dp.change', event => {
                let date = event.date.unix();
                ngModel.$setViewValue(date);
            });

            var timeout = $timeout(() => {
                if (ngModel.$modelValue) {
                    picker.date(ngModel.$modelValue);
                }

                $timeout.cancel(timeout);
            }, 0);
        }
    }))
    .directive('highlightCss', ($timeout) => ({
        restrict: 'A',
        require: 'ngModel',
        link: (scope, el, attrs, ngModel) => {
            var timeout = $timeout(() => {
                const code = CodeMirror.fromTextArea(el[0], {
                    mode: 'css',
                    lineNumbers: true
                });

                code.on('change', instance => {
                    let value = instance.getValue();
                    ngModel.$setViewValue(value);
                });

                scope.$on('reset', () => {
                    code.setValue(scope.css);
                });

                $timeout.cancel(timeout);
            });
        }
    }))
    .directive('flot', ($timeout) => ({
        restrict: 'E',
        replace: true,
        template: '<div></div>',
        scope: {
            dataset: '=',
            options: '=',
            height: '@'
        },
        link: (scope, el) => {
            scope.options = scope.options || {};

            el.css('height', scope.height);

            let plot;
            var timeout = $timeout(() => {
                plot = el.plot(scope.dataset, scope.options).data('plot');
                $timeout.cancel(timeout);
            }, 0);

            scope.$watch('dataset', setData);

            function setData() {
                if (plot) {
                    plot.setData(scope.dataset);
                    redraw();
                }
            }

            function redraw() {
                plot.setupGrid();
                plot.draw();
            }

            angular.element(window).on('resize', _.debounce(() => {
                if (plot) {
                    plot.resize();
                    redraw();
                }
            }, 100));
        }
    }))
;
