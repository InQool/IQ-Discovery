import {DocumentController} from './Controllers/Document/DocumentController.js';
import {DocumentEditController} from './Controllers/Document/DocumentEditController.js';

import {BatchController} from './Controllers/Batch/BatchController.js';
import {BatchEditController} from './Controllers/Batch/BatchEditController.js';

import {TypeController} from './Controllers/Type/TypeController.js';
import {TypeEditController} from './Controllers/Type/TypeEditController.js';

import {SourceOAIController} from './Controllers/Source/SourceOAIController.js';
import {SourceSRUController} from './Controllers/Source/SourceSRUController.js';
import {SourceZ3950Controller} from './Controllers/Source/SourceZ3950Controller.js';

import {OrganizationController} from './Controllers/Organization/OrganizationController.js';
import {OrganizationEditController} from './Controllers/Organization/OrganizationEditController.js';

import {NotificationController} from './Controllers/Notification/NotificationController.js';
import {NotificationEditController} from './Controllers/Notification/NotificationEditController.js';

import {ArticleController} from './Controllers/Article/ArticleController.js';
import {ArticleCreateController} from './Controllers/Article/ArticleCreateController.js';
import {ArticleEditController} from './Controllers/Article/ArticleEditController.js';

import {GalleryController} from './Controllers/Gallery/GalleryController.js';
import {GalleryCreateController} from './Controllers/Gallery/GalleryCreateController.js';
import {GalleryEditController} from './Controllers/Gallery/GalleryEditController.js';

import {AdministrationController} from './Controllers/Administration/AdministrationController.js';

import UploadController from './Controllers/Upload/UploadController';

import {VerifyController} from './Controllers/Verify/VerifyController.js';

import {HomeController} from './Controllers/Home/HomeController.js';

import {DiscoveryController} from './Controllers/Discovery/DiscoveryController.js';

angular.module('zdo.office.controllers', [])
	.controller('HomeController', HomeController)
	.controller('DocumentController', DocumentController)
	.controller('DocumentEditController', DocumentEditController)
	.controller('BatchController', BatchController)
	.controller('BatchEditController', BatchEditController)
	.controller('TypeController', TypeController)
	.controller('TypeEditController', TypeEditController)
	.controller('SourceOAIController', SourceOAIController)
	.controller('SourceSRUController', SourceSRUController)
	.controller('SourceZ3950Controller', SourceZ3950Controller)
	.controller('OrganizationController', OrganizationController)
	.controller('OrganizationEditController', OrganizationEditController)
	.controller('NotificationController', NotificationController)
	.controller('NotificationEditController', NotificationEditController)
	.controller('ArticleController', ArticleController)
	.controller('ArticleCreateController', ArticleCreateController)
	.controller('ArticleEditController', ArticleEditController)
	.controller('GalleryController', GalleryController)
	.controller('GalleryCreateController', GalleryCreateController)
	.controller('GalleryEditController', GalleryEditController)
	.controller('AdministrationController', AdministrationController)
	.controller('VerifyController', VerifyController)
	.controller('DiscoveryController', DiscoveryController)
	.controller('UploadController', UploadController)
;
