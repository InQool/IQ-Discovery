export default function($timeout, Configuration) {
    'ngInject';

    return {
        scope: {},
        restrict: 'A',
        replace: true,
        templateUrl: 'views/directives/sidebar.html',
        link: link,
        controller: controller,
        controllerAs: 'vm'
    };

    function link(scope, el) {
        if (Configuration.development) {
            el.addClass('test');
        }

        var timeout = $timeout(() => {
            let body = angular.element('body'),
                userMenu = el.find('.user-menu'),
                usermenuItems = userMenu.find('a');

            el.find('.sidebar-menu li a.accordion-toggle').click(function(event) {
                // Any menu item with the accordion class is a dropdown submenu. Thus we prevent default actions
                event.preventDefault();

                // Any menu item with the accordion class is a dropdown submenu. Thus we prevent default actions
                if (body.hasClass('sb-l-m') && !angular.element(this).parents('ul.sub-nav').length) {
                    return;
                }

                // Any menu item with the accordion class is a dropdown submenu. Thus we prevent default actions
                if (angular.element(this).parents('ul.sub-nav').length) {
                    var activeMenu = angular.element(this).next('ul.sub-nav'),
                        siblingMenu = angular.element(this).parent().siblings('li').children('a.accordion-toggle.menu-open').next('ul.sub-nav');

                    activeMenu.slideUp('fast', 'swing', function() {
                        angular.element(this).attr('style', '').prev().removeClass('menu-open');
                    });

                    siblingMenu.slideUp('fast', 'swing', function() {
                        angular.element(this).attr('style', '').prev().removeClass('menu-open');
                    });
                } else {
                    angular.element('a.accordion-toggle.menu-open').next('ul').slideUp('fast', 'swing', function() {
                        angular.element(this).attr('style', '').prev().removeClass('menu-open');
                    });
                }

                // Now we expand targeted menu item, add the ".open-menu" class
                // and remove any left over inline jQuery animation styles
                if (!angular.element(this).hasClass('menu-open')) {
                    angular.element(this).next('ul').slideToggle('fast', 'swing', function() {
                        angular.element(this).attr('style', '').prev().toggleClass('menu-open');
                    });
                }

            });

            el.find('.sidebar-toggle-mini').on('click', event => {
                event.preventDefault();

                // Close Menu
                body.addClass('sb-l-c');
                Page.triggerResize();

                // After animation has occured we toggle the menu.
                // Upon the menu reopening the classes will be toggled
                // again, effectively restoring the menus state prior
                // to being hidden
                if (!body.hasClass('mobile-view')) {
                    var timeout = setTimeout(() => {
                        body.toggleClass('sb-l-m sb-l-o');
                        clearTimeout(timeout);
                    }, 250);
                }
            });

            el.find('.sidebar-menu-toggle').click(event => {
                event.preventDefault();

                // Toggle Class to signal state change
                userMenu.toggleClass('usermenu-open').slideToggle('fast');

                // If menu is closed apply animation
                if (userMenu.hasClass('usermenu-open')) {
                    usermenuItems.addClass('animated fadeIn');
                }
            });

            $timeout.cancel(timeout);
        }, 0);
    }

    /**
     * @ngInject
     * @param $state
     * @param {Configuration} Configuration
     * @param {User} User
     */
    function controller($state, Configuration, User) {
        let vm = this;

        vm.isActive = isActive;
        vm.isAllowed = isAllowed;
        vm.isEnabled = isEnabled;

        function isEnabled(state) {
            return Configuration.disabledStates.indexOf(state) === -1;
        }

        function isActive(state) {
            return $state.includes(state);
        }

        function isAllowed(state) {
            const stateObject = $state.get(state);
            return isEnabled(state) && User.isAllowed(stateObject);
        }
    }
}
