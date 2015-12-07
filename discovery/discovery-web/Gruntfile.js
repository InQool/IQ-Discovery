module.exports = function(grunt) {
    "use strict";

    var src = 'src/main/app',
        dist = 'target/discovery-web.war',
        rewriteModule = require('http-rewrite-middleware'),
        uglifyFiles = {},
        TARGET = grunt.option('target'),
        STAGE = grunt.option('stage');

    uglifyFiles[dist + '/bundle/bundle.js'] = dist + '/bundle/bundle.js';

    var targetIndex = {};
    targetIndex[dist + '/index.html'] = src + '/index.html';

    var domains = {
        zlinskykraj: {
            development: 'https://ebadatelna-test.zlkraj.cz',
            production: 'https://ebadatelna.zlkraj.cz'
        },
        stredoceskykraj: {
            development: 'https://pkd-test.kr-stredocesky.cz',
            production: 'https://pkd.kr-stredocesky.cz'
        }
    };

    var googleAnalyticsIds = {
        zlinskykraj: {
            development: 'UA-67285332-2',
            production: 'UA-67285332-1'
        },
        stredoceskykraj: {
            development: null,
            production: null
        }
    };

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        clean: {
            init: [dist, '.tmp'],
            after: [dist + '/scripts']
        },

        copy: {
            main: {
                expand: true,
                cwd: src,
                src: ['favicon.ico', 'xrds.xml', 'css/**', 'images/**', 'scripts/**', 'fonts/**', 'WEB-INF/*', 'bower_components/angular-ui-select/dist/select.css', 'bower_components/font-awesome/fonts/**'],
                dest: dist
            }
        },

        useminPrepare: {
            html: [dist + '/index.html']
        },

        usemin: {
            html: [dist + '/index.html']
        },

        filerev: {
            files: {
                src: [dist + '/css/*.css', dist + '/bundle/*.js']
            }
        },

        sass: {
            dist: {
                options: {
                    includePaths: [
                        src + '/scss/instance/' + TARGET,
                        src + '/bower_components'
                    ],
                    outputStyle: 'compressed'
                },
                files: [{
                    expand: true,
                    cwd: src + '/scss',
                    src: ['app.scss'],
                    dest: src + '/css',
                    ext: '.css'
                }]
            }
        },

        connect: {
            server: {
                options: {
                    port: 9002,
                    base: src,
                    keepalive: true,
                    middleware: function(connect, options) {
                        var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest,
                            middlewares = [proxy],
                            directory;

                        // RewriteRules support
                        middlewares.push(rewriteModule.getMiddleware(
                            [{
                                from: '^(.*)\.(html|xml|css|js|jpeg|jpg|png|gif|ttf|woff|woff2|ico)',
                                to: '/$1.$2'
                            }, {
                                from: '^/(.*)$',
                                to: '/index.html'
                            }], {
                                verbose: false
                            }
                        ));

                        if (!Array.isArray(options.base)) {
                            options.base = [options.base];
                        }

                        directory = options.directory || options.base[options.base.length - 1];
                        options.base.forEach(function(base) {
                            // Serve static files.
                            middlewares.push(connect.static(base));
                        });

                        // Make directory browse-able.
                        middlewares.push(connect.directory(directory));

                        return middlewares;
                    }
                },
                proxies: [{
                    context: '/dcap/',
                    host: 'localhost',
                    port: 8080,
                    https: false,
                    changeOrigin: true
                }]
            }
        },

        browserify: {
            options: {
                transform: [
                    ['envify', {
                        TARGET: TARGET,
                        STAGE: STAGE
                    }],
                    ['babelify', {
                        ignore: ['node_modules', 'openseadragon.js', 'jquery.flot.js', 'jquery.flot.selection.js']
                    }],
                    ['browserify-ngannotate', {
                        single_quotes: true
                    }],
                    ['cssify']
                ]
            },
            development: {
                options: {
                    watch: true,
                    keepAlive: true
                },
                src: src + '/scripts/app.js',
                dest: src + '/bundle/bundle.js'
            },
            production: {
                src: dist + '/scripts/app.js',
                dest: dist + '/bundle/bundle.js'
            }
        },

        uglify: {
            options: {
                mangle: true
            },
            my_target: {
                files: uglifyFiles
            }
        },

        ngtemplates: {
            'zdo.discovery.templates': {
                cwd: src,
                src: 'views/**/*.html',
                dest: dist + '/scripts/templates.js',
                options: {
                    standalone: true,
                    htmlmin: {
                        collapseBooleanAttributes: true,
                        collapseWhitespace: true,
                        removeAttributeQuotes: true,
                        removeComments: true,
                        removeEmptyAttributes: true,
                        removeScriptTypeAttributes: true,
                        removeStyleLinkTypeAttributes: true
                    }
                }
            }
        },

        nggettext_extract: {
            pot: {
                options: {
                    markerName: '__'
                },
                files: {
                    'src/main/app/po/template.pot': [
                        src + '/views/**/*.html',
                        src + '/scripts/**/*.js'
                    ]
                }
            }
        },

        nggettext_compile: {
            options: {
                format: 'json'
            },
            development: {
                files: [{
                    src: [src + '/po/*.po'],
                    dest: src + '/languages/languages.json'
                }]
            },
            production: {
                files: [{
                    src: [src + '/po/*.po'],
                    dest: dist + '/languages/languages.json'
                }]
            }
        },

        watch: {
            sass: {
                files: [src + '/scss/**/*.{scss,sass}'],
                tasks: ['sass:dist']
            },
            html: {
                files: [src + '/views/**/*.html']
            },
            options: {
                livereload: true
            }
        },

        targethtml: {
            dist: {
                options: {
                    curlyTags: {
                        domain: TARGET && STAGE ? domains[TARGET][STAGE] : null,
                        googleAnalyticsId: TARGET && STAGE ? googleAnalyticsIds[TARGET][STAGE] : null
                    }
                },
                files: targetIndex
            }
        }
    });

    grunt.loadNpmTasks('grunt-sass');
    grunt.loadNpmTasks('grunt-browserify');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-connect-proxy');
    grunt.loadNpmTasks('grunt-filerev');
    grunt.loadNpmTasks('grunt-usemin');
    grunt.loadNpmTasks('grunt-angular-templates');
    grunt.loadNpmTasks('grunt-angular-gettext');
    grunt.loadNpmTasks('grunt-targethtml');

    grunt.registerTask('watchify', ['browserify:development']);
    grunt.registerTask('connect!', ['configureProxies:server', 'connect']);

    grunt.registerTask('templates', ['ngtemplates']);

    grunt.registerTask('test', []);

    grunt.registerTask('build', [
        'clean:init', 'sass', 'targethtml', 'copy', 'ngtemplates', 'nggettext_compile:production', 'browserify:production', 'useminPrepare', 'uglify', 'filerev', 'usemin', 'clean:after'
    ]);
};
