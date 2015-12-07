angular.module('zdo.discovery.filters', [])
	.filter('truncate', () => (text, limit) => _.trunc(text, limit));