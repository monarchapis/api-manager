module.exports = function(grunt) {

	var processContent = function(content) {
		content = content.replace(/\r\n/g, '\n');
		//return src.replace(/(^\s+|\s+$)/gm, '');
		//content = content.replace(/^[\r\n]+/, '').replace(/[\r\n]*$/, '\n');
		content = content.replace(/^[\x20\t]+/mg, '').replace(/[\x20\t]+$/mg, '');
		return content;
	};

	var processTemplatePath = function(filePath) {
		var ext = filePath.lastIndexOf('.');

		if (ext != -1) {
			filePath = filePath.substring(0, ext);
		}

		filePath = filePath.replace('src/main/js/templates/', '');

		return filePath;
	};

	// Project configuration.
	grunt.initConfig({
    	pkg: grunt.file.readJSON('package.json'),
    	/*preprocess: {
    		options: {
    			inline: true
    		},
			framework: {
				src: "framework/views/build.js",
				dest: "compiled/framework/views.js"
			}
		},*/
		handlebars: {
			compile: {
				options: {
					namespace: "JST",
					processContent: processContent,
					processName: processTemplatePath
				},
				files: {
					"src/main/webapp/js/templates.handlebars.js": ["src/main/js/templates/**/*.hb"]
				}
  			}
		},
		concat: {
			options: {
				stripBanners: true,
				banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ' +
				'<%= grunt.template.today("yyyy-mm-dd") %> */\n'
			},
			apimanagement: {
				src: [
					'src/main/webapp/js/templates.*.js',
					'src/main/js/init.js',
					'src/main/js/utils/*.js',
					'src/main/js/model.js',
					'src/main/js/views/*.js',
					'src/main/js/controllers/*.js',
					'src/main/js/editors/*.js',
					'src/main/js/router.js'
				],
				dest: 'src/main/webapp/js/api-management.js'
			}
		},
		uglify: {
			my_target: {
				files: {
					'src/main/webapp/js/api-management.min.js': ['src/main/webapp/js/api-management.js']
				}
			}
		}
	});

	//grunt.loadNpmTasks('grunt-preprocess');
	grunt.loadNpmTasks('grunt-contrib-handlebars');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');

	// Default task(s).
	grunt.registerTask('default', ['handlebars', 'concat', 'uglify']);
	grunt.registerTask('build', ['handlebars', 'concat', 'uglify']);
	grunt.registerTask('test', []);
};