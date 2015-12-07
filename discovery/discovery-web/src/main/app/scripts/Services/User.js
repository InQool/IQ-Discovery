const KEY = {
    TOKEN: 'token',
    USER: 'user',
    HISTORY: 'history'
};

export class User {
    /**
     * @ngInject
     * @param Restangular
     * @param {DocumentService} DocumentService
     * @param jwtHelper
     * @param $window
     */
    constructor(Restangular, DocumentService, jwtHelper, $window) {
        this.jwt = jwtHelper;
        this.rest = Restangular;
        this.user = Restangular.all('user');
        this.auth = Restangular.all('auth');
        this.document = DocumentService;
        this.window = $window;
    }

    /**
     * @returns {Storage}
     */
    get storage() {
        return this.window.localStorage;
    }

    /**
     * @returns {{firstName: String, lastName: String, verified: Boolean, openid: Boolean}}
     */
    get() {
        return angular.fromJson(this.storage.getItem(KEY.USER));
    }

    create(user) {
        return this.rest.all('user').post(user);
    }

    login(credentials) {
        return this.rest.withConfig(configurer => {
            configurer.setFullResponse(true);
        }).all('user/login').post(credentials).then(response => {
            let token = response.headers('authctoken');
            this.setToken(token);
            this.setUserInfo(response.data.plain());
        });
    }

    setUserInfo(userInfo) {
        this.storage.setItem(KEY.USER, angular.toJson(userInfo));
    }

    /**
     * @private
     */
    _clearStorage(item = null) {
        if (item !== null) {
            this.storage.removeItem(item);
        } else {
            this.storage.clear();
        }
    }

    setLogin(token, userInfo) {
        this.setToken(token);
        this.setUserInfo(userInfo);
    }

    logout() {
        this._clearStorage(KEY.TOKEN);
        this._clearStorage(KEY.USER);
    }

    setToken(token) {
        this.storage.setItem(KEY.TOKEN, token);
    }

    getToken() {
        return this.storage.getItem(KEY.TOKEN);
    }

    isLoggedIn() {
        let token = this.getToken();
        if (token && this.jwt.isTokenExpired(token)) {
            this.logout();
            return false;
        }

        return Boolean(token);
    }

    isLoggedInViaOpenId() {
        return this.get().hasOwnProperty('openid') && this.get().openid === true;
    }

    /**
     * @return {string}
     * @private
     */
    _getPassword() {
        return 'a29sdXM4N2JlYm8=';
    }

    /**
     * @param {String} password
     */
    allowEnterToPage(password) {
        password = btoa(password);
        if (password !== this._getPassword()) {
            throw __('Špatné heslo.');
        }

        try {
            this.storage.setItem('password', password)
        } catch (e) {
            throw __('Vypněte prosím inkognito mód.');
        }
    }

    isAllowedToEnterPage() {
        return Boolean(this.storage.getItem('password') === this._getPassword());
    }

    favoriteDocument(invId) {
        return this.auth.one('favDoc').post('', null, {invId: invId});
    }

    getFavoriteDocuments() {
        return this.auth.all('favDoc').getList();
    }

    update(data) {
        return this.auth.one('user').doPUT(data);
    }

    getClipboard() {
        return this.auth.all('clipDoc').getList();
    }

    addToClipboard(invId) {
        return this.auth.all('clipDoc').post(null, {invId: invId});
    }

    makeReservation(documentIds, reason) {
        return this.auth.all('reservation').post({
            fedoraIds: documentIds,
            reason: reason
        });
    }

    getSavedSearch() {
        return this.auth.all('favQuery').getList();
    }

    saveSearch(query, facets) {
        return this.auth.all('favQuery').post({
            solrQuery: query,
            restrictions: facets.join('|')
        });
    }

    makeDocumentQuery(docId, content) {
        return this.auth.all('query').post(content, {docId: docId}, {'Content-Type': 'text/plain'});
    }

    changePassword(oldPassword, newPassword) {
        return this.auth.one('user/password').doPUT({
            oldPassword: oldPassword,
            newPassword: newPassword
        });
    }

    makePasswordResetRequest(email) {
        return this.user.all('resetPassword').post(email, null, {'Content-Type': 'text/plain'});
    }

    checkPasswordHash(hash) {
        return this.user.all('checkPwdResetKey').post(hash, null, {'Content-Type': 'text/plain'});
    }

    resetPassword(hash, password) {
        return this.user.all('setNewPassword').post({
            pwdResetKey: hash,
            newPassword: password
        });
    }

    getLocalHistory() {
        let history = this.storage.getItem(KEY.HISTORY);
        return history ? angular.fromJson(history) : [];
    }

    _setLocalHistory(history) {
        this.storage.setItem(KEY.HISTORY, angular.toJson(history));
    }

    removeLocalHistory() {
        this.storage.removeItem(KEY.HISTORY);
    }

    getLatestSearchFromHistory() {
        return _.first(this.getLocalHistory());
    }

    createHistoryObject(query, facetQuery, yearStart, yearEnd, count) {
        return {
            query: query,
            facetQuery: facetQuery,
            yearStart: yearStart,
            yearEnd: yearEnd,
            count: count,
            timestamp: new Date()
        };
    }

    updateLatestSearchInLocalHistory(data) {
        let history = this.getLocalHistory();
        if (history.hasOwnProperty(0)) {
            let search = history[0];
            for (let prop in data) {
                if (data.hasOwnProperty(prop)) {
                    search[prop] = data[prop];
                }
            }

            search.timestamp = new Date();
        }

        this._setLocalHistory(history);
    }

    saveSearchToLocalHistory(query, facetQuery, yearStart, yearEnd, count) {
        let history = this.getLocalHistory();
        history.unshift(
            this.createHistoryObject(query, facetQuery, yearStart, yearEnd, count)
        );

        this._setLocalHistory(history);
    }
}
