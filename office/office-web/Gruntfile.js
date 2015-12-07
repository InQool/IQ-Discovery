module.exports = function(grunt) {
    "use strict";

    var src = 'src/main/app',
        dist = 'target/office-web.war',
        context = 'office-web',
        rewriteModule = require('http-rewrite-middleware'),
        uglifyFiles = {};

    uglifyFiles[dist + '/bundle/bundle.js'] = dist + '/bundle/bundle.js';

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        clean: {
            initial: [dist, '.tmp'],
            after: [dist + '/scripts']
        },

        copy: {
            main: {
                expand: true,
                cwd: src,
                src: [
                    'index.html', 'favicon.ico', 'css/**', 'images/**', 'scripts/**', 'WEB-INF/*', 'fonts/**',
                    'bower_components/angular-ui-select/dist/select.css', 'bower_components/nprogress/nprogress.css'
                ],
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
                src: [dist + '/css/*.css', dist + '/bundle/bundle.js']
            }
        },

        connect: {
            server: {
                options: {
                    port: 9001,
                    base: src,
                    keepalive: true,
                    debug: false,

                    middleware: function(connect, options) {
                        var proxy = require('grunt-connect-proxy/lib/utils').proxyRequest,
                            middlewares = [proxy],
                            directory;

                        // RewriteRules support
                        middlewares.push(rewriteModule.getMiddleware([{
                            from: '^(?!.*\\.(html|js|css|jpg|png|gif|ttf|woff|eot|map|ico))[/\\w\\.\\-]+(\\?[\\w\\.\\=\\&]*){0,1}$',
                            to: '/index.html'
                        }, {
                            from: '^\/' + context + '\/(.*)$',
                            to: '/$1'
                        }], {
                            verbose: false
                        }));

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

        uglify: {
            options: {
                mangle: true,
                compress: true
            },
            target: {
                files: uglifyFiles
            }
        },

        ngtemplates: {
            'zdo.office.templates': {
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

        less: {
            development: {
                options: {
                    paths: []
                },
                files: {
                    "src/main/app/css/app.css": src + "/less/app.less"
                }
            }
        },

        browserify: {
            options: {
                transform: [
                    ['envify', {
                        TARGET: grunt.option('target'),
                        STAGE: grunt.option('stage')
                    }],
                    ['babelify', {
                        ignore: ['jquery.flot.js', 'node_modules']
                    }],
                    ['browserify-ngannotate']
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

        watch: {
            grunt: {
                files: ['Gruntfile.js']
            },
            less: {
                files: [src + '/less/**/*.less'],
                tasks: ['less:development']
            },
            html: {
                files: [src + '/views/**/*.html']
            },
            options: {
                livereload: true
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-filerev');
    grunt.loadNpmTasks('grunt-usemin');
    grunt.loadNpmTasks('grunt-browserify');
    grunt.loadNpmTasks('grunt-angular-templates');
    grunt.loadNpmTasks('grunt-connect-proxy');

    grunt.registerTask('watchify', [
        'browserify:development'
    ]);

    grunt.registerTask('watch!', [
        'watch'
    ]);

    grunt.registerTask('connect!', [
        'configureProxies:server',
        'connect'
    ]);

    grunt.registerTask('test', []);

    grunt.registerTask('build', [
        'clean:initial', 'less', 'copy', 'ngtemplates', 'browserify:production', 'useminPrepare', 'uglify', 'filerev', 'usemin', 'clean:after'
    ]);
};
