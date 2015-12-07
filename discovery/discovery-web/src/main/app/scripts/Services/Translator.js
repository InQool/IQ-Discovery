const COOKIE_LANG_KEY = 'language';

const TRANSLATIONS = require('../../languages/languages.json');

const LANGUAGES = {
	cs: 'Čeština',
	en: 'English'
};

export class Translator {
	/**
	 * @ngInject
	 */
	constructor(gettextCatalog, $cookies) {
		this.gettext = gettextCatalog;
		this.gettext.baseLanguage = 'cs';

		this.cookies = $cookies;
		this.loadTranslations();
	}

	get Languages() {
		return LANGUAGES;
	}

	loadTranslations() {
		for (var language in TRANSLATIONS) {
			if (TRANSLATIONS.hasOwnProperty(language)) {
				this.gettext.setStrings(language, TRANSLATIONS[language]);
			}
		}
	}

	getCurrent() {
		return this.gettext.getCurrentLanguage();
	}

	setCurrent(language) {
		this.gettext.setCurrentLanguage(language);
		this.cookies.put(COOKIE_LANG_KEY, language);
	}

	isCurrent(language) {
		return this.getCurrent() === language;
	}

	translate(string) {
		return this.gettext.getString(string);
	}

	_getSavedLanguage() {
		let language = this.cookies.get(COOKIE_LANG_KEY);
		if (angular.isUndefined(language)) {
			language = 'cs';
		}

		return language;
	}

	initialize() {
		let language = this._getSavedLanguage();
		this.gettext.setCurrentLanguage(language);
	}
}