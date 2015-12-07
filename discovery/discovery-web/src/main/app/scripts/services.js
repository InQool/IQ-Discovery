import {Page} from './Services/Page.js';
import {User} from './Services/User.js';
import {DataService} from './Services/DataService.js';
import {SearchService} from './Services/SearchService.js';
import {DocumentService} from './Services/DocumentService.js';
import {ArticleService} from './Services/ArticleService.js';
import {ExpoService} from './Services/ExpoService.js';
import {Notifications} from './Services/Notifications.js';
import {Translator} from './Services/Translator.js';

angular.module('zdo.discovery.services', [])
	.service('RestangularCachable', Restangular => Restangular.withConfig(RestangularConfigurer => {
		RestangularConfigurer.setDefaultHttpFields({cache: true});
	}))
	.service('User', User)
	.service('Page', Page)
	.service('SearchService', SearchService)
	.service('DocumentService', DocumentService)
	.service('DataService', DataService)
	.service('ArticleService', ArticleService)
	.service('ExpoService', ExpoService)
	.service('Notifications', Notifications)
	.service('Translator', Translator)
;