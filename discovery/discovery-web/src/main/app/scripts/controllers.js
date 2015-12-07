import {HomepageController} from './Controllers/Homepage/HomepageController.js';
//
import {ArticleController} from './Controllers/Article/ArticleController.js';
import {ArticleViewController} from './Controllers/Article/ArticleViewController.js';
//
import {DocumentController} from './Controllers/Document/DocumentController.js';
import {DocumentIssueController} from './Controllers/Document/DocumentIssueController.js';
//

import {SearchController} from './Controllers/Search/SearchController.js';
import {SearchAdvancedController} from './Controllers/Search/SearchAdvancedController.js';

import {ExpoController} from './Controllers/Expo/ExpoController.js';

import {HistoryController} from './Controllers/History/HistoryController.js';

import {LoginController} from './Controllers/Login/LoginController.js';
import {LoginOpenIDController} from './Controllers/Login/LoginOpenIDController.js';
import {RegisterController} from './Controllers/Register/RegisterController.js';

import {AccountClipboardController} from './Controllers/Account/AccountClipboardController';
import {AccountSettingsController} from './Controllers/Account/AccountSettingsController.js';
import {AccountSettingsSearchController} from './Controllers/Account/AccountSettingsSearchController.js';
import {AccountSettingsPersonalController} from './Controllers/Account/AccountSettingsPersonalController.js';
import {AccountSettingsFavoritedController} from './Controllers/Account/AccountSettingsFavoritedController.js';
import {AccountSettingsChangePasswordController} from './Controllers/Account/AccountSettingsChangePasswordController.js';

import {CatalogController} from './Controllers/Catalog/CatalogController.js';

import {PasswordResetController} from './Controllers/Password/PasswordResetController.js';
import {PasswordForgottenController} from './Controllers/Password/PasswordForgottenController.js';

angular.module('zdo.discovery.controllers', [])
	.controller('HomepageController', HomepageController)
	//
	.controller('ArticleController', ArticleController)
	.controller('ArticleViewController', ArticleViewController)
	//
	.controller('SearchController', SearchController)
	.controller('SearchAdvancedController', SearchAdvancedController)
	//
	.controller('ExpoController', ExpoController)
	//
	.controller('DocumentController', DocumentController)
	.controller('DocumentIssueController', DocumentIssueController)
	//
	.controller('HistoryController', HistoryController)
	.controller('CatalogController', CatalogController)
	//
	.controller('LoginController', LoginController)
	.controller('LoginOpenIDController', LoginOpenIDController)
	.controller('RegisterController', RegisterController)
	//
	.controller('PasswordResetController', PasswordResetController)
	.controller('PasswordForgottenController', PasswordForgottenController)
	//
	.controller('AccountClipboardController', AccountClipboardController)
	.controller('AccountSettingsController', AccountSettingsController)
	.controller('AccountSettingsSearchController', AccountSettingsSearchController)
	.controller('AccountSettingsPersonalController', AccountSettingsPersonalController)
	.controller('AccountSettingsFavoritedController', AccountSettingsFavoritedController)
	.controller('AccountSettingsChangePasswordController', AccountSettingsChangePasswordController)
;