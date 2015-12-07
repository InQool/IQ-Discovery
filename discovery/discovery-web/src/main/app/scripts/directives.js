// directives
import {header} from './Directives/header.js';
import {headerSearch} from './Directives/headerSearch.js';
import {footer} from './Directives/footer.js';
import {timeline} from './Directives/timeline.js';
import {exposition} from './Directives/exposition.js';
import {seadragon} from './Directives/seadragon.js';
import {flashes} from './Directives/flashes.js';
import {watermark} from './Directives/watermark.js';
import {carousel} from './Directives/carousel.js';
import {selectpicker} from './Directives/selectpicker.js';
import {lazyLoadImages} from './Directives/lazyLoadImages.js';
import head from './Directives/head';

import FilterSection from './Directives/Filter/FilterSection.js';

angular.module('zdo.discovery.directives', [])
    .directive('head', head)
    .directive('title', (Page) => ({
        restrict: 'E',
        link: (scope) => {
            scope.getTitle = () => Page.getTitle();
        }
    }))
    .directive('background', () => ({
        restrict: 'A',
        link: (scope, el, attrs) => {
            el.css('backgroundImage', `url(${attrs.background})`);
        }
    }))
    .directive('articleLink', ($state) => ({
        restrict: 'E',
        replace: true,
        scope: {
            article: '=',
            title: '@'
        },
        template: '<a></a>',
        link: (scope, el) => {
            let url = scope.article.url;
            if (url) {
                el.prop('target', '_blank');
            } else {
                url = $state.href('secured.article.view', {id: scope.article.id});
            }

            if (angular.isUndefined(scope.title)) {
                scope.title = scope.article.title;
            }

            el.prop('href', url);
            el.text(scope.title);
        }
    }))
    .directive('header', header)
    .directive('headerSearch', headerSearch)
    .directive('footer', footer)
    .directive('zdoFilterSection', FilterSection)
    .directive('selectpicker', selectpicker)
    .directive('carousel', carousel)
    .directive('seadragon', seadragon)
    .directive('flashes', flashes)
    .directive('watermark', watermark)
    .directive('timeline', timeline)
    .directive('exposition', exposition)
    .directive('lazyLoadImages', lazyLoadImages)
    .directive('loginForm', () => ({
        restrict: 'E',
        scope: {},
        replace: true,
        templateUrl: 'views/directives/Login/form.html',
        controller: ($scope, $state, User, Notifications) => {
            $scope.submit = () => {
                if ($scope.form.$valid) {
                    try {
                        User.allowEnterToPage($scope.password);
                        $state.go('secured.homepage');
                    } catch (error) {
                        Notifications.addError(error);
                    }
                }
            };
        }
    }))
;
