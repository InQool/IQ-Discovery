import {SCREEN} from '../Enums/CssEnum.js';

export function header() {
    return {
        scope: {},
        restrict: 'E',
        templateUrl: 'views/directives/Header/header.html',
        controller: controller,
        controllerAs: 'vm',
        link: (scope, el) => {
            el.delegate('a', 'click', () => {
                if (scope.vm.isSmallScreen) {
                    scope.vm.isCollapsed = true;
                }
            });
        }
    };

    /**
     * @ngInject
     * @param $state
     * @param {Translator} Translator
     * @param {Configuration} Configuration
     * @param {User} User
     */
    function controller($state, Translator, Configuration, User) {
        let vm = this;

        vm.isSmallScreen = angular.element(window).width() < SCREEN.XS_MAX;
        vm.isCollapsed = vm.isSmallScreen;
        vm.language = Translator.getCurrent();
        vm.languages = Translator.Languages;
        vm.frontendUrl = encodeURI(location.origin + '/login/openid');

        vm.logout = logout;
        vm.toggle = toggle;
        vm.getName = getName;
        vm.isLoggedIn = isLoggedIn;
        vm.changeLanguage = changeLanguage;
        vm.isDisabledState = isDisabledState;
        vm.isCurrentLanguage = isCurrentLanguage;
        vm.getCurrentLanguage = getCurrentLanguage;

        function getName() {
            let user = User.get();
            return user.firstName + ' ' + user.lastName;
        }

        function changeLanguage(lang) {
            if (!isCurrentLanguage(lang)) {
                Translator.setCurrent(lang);
                location.reload();
            }
        }

        function getCurrentLanguage() {
            let current = Translator.getCurrent();
            return Translator.Languages[current];
        }

        function isCurrentLanguage(lang) {
            return Translator.isCurrent(lang);
        }

        function toggle() {
            vm.isCollapsed = !vm.isCollapsed;
        }

        function isLoggedIn() {
            return User.isLoggedIn();
        }

        function logout() {
            User.logout();
            $state.go('secured.homepage');
        }

        function isDisabledState(state) {
            return Configuration.hasOwnProperty('disabledStates') && _.indexOf(Configuration.disabledStates, state) !== -1;
        }
    }
}
