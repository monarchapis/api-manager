// Added per https://github.com/ivaynberg/select2/issues/1436
$.fn.modal.Constructor.prototype.enforceFocus = function() {};

rivets.adapters[':'] = {
	subscribe: function(obj, keypath, callback) {
		obj && obj.on('change:' + keypath, callback);
	},
	unsubscribe: function(obj, keypath, callback) {
		obj && obj.off('change:' + keypath, callback);
	},
	read: function(obj, keypath) {
		if (obj) return obj.get(keypath);
	},
	publish: function(obj, keypath, value) {
		if (_.isString(value)) {
			value = value.trim();
			if (value.length == 0) value = null;
		}

		obj && obj.set(keypath, value);
	}
};

var serviceBaseUrl = window.location.pathname;

if (serviceBaseUrl.substring(serviceBaseUrl.length - 1) != "/") {
	serviceBaseUrl += '/'
}

Fantoccini.baseUrl = serviceBaseUrl + 'management/v1/';

var App = new Marionette.Application({
	environmentId: null,

	setActiveTab: function (name) {
		$('#navigation li').removeClass('active');
		$('#navigation a[href="#' + name + '"]').parent().addClass('active');
	},

	getActiveTab: function() {
		return $('#navigation li.active > a').attr('href').substring(1);
	}
});

Fantoccini.setGlobalEventAggregator(App.vent);

jQuery.oldAjax = jQuery.ajax;
jQuery.ajax = function(options) {
	if (App.environmentId != null && App.environmentId.length > 0) {
		options.headers = options.headers || {};
		options.headers["X-Environment-Id"] = App.environmentId;
	}

	return jQuery.oldAjax(options);
}

App.addRegions({
	environmentInfo: '#environment-info',
	main: '#main',
	modal: '#modal .modal-content'
});

var EnvironmentInfoView = Marionette.ItemView.extend({
	template: 'environment-info'
});

var Controllers = {};
var Editors = {};

/////////////////////////////////////////////////////////////////////

function refreshEnvironmentInfo() {
	var loading = $.Deferred();

	$.getJSON(Fantoccini.baseUrl + 'environment/summary').done(function(data) {
		$('#environment-name').text(data.name);
		$('#environment-partners').html(data.applications + ' <small>Applications</small> / ' + data.clients + ' <small>Clients</small> / ' + data.developers + ' <small>Developers</small>');
		$('#environment-apis').html(data.services + ' <small>Services</small> / ' + data.plans + ' <small>Plans</small>');
		$('#environment-access').html(data.providers + ' <small>Providers</small> / ' + data.users + ' <small>Users</small> / ' + data.roles + ' <small>Roles</small>');
		loading.resolve();
	});

	return loading.promise();
}

function setCookie(c_name, value, exdays) {
	var exdate = new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
	document.cookie = c_name + "=" + c_value;
}

function getCookie(c_name) {
	var i, x, y, ARRcookies = document.cookie.split(";");

	for (i = 0; i < ARRcookies.length; i++) {
		x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
		y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
		x = x.replace(/^\s+|\s+$/g, "");

		if (x == c_name) {
			return unescape(y);
		}
	}
}

var g_permissions = { administrator : false, permissions : [], accessLevels : {} };

function createEnvironment() {
	var model = new EnvironmentModel();
	var editor = new Editors.Generic({
		template : 'environments-form',
		model : model,
		afterSave: function() {
			App.environmentId = model.get('id');
			setCookie('environmentId', App.environmentId, 365);
			document.title = model.get('name') + ' \xb7 Monarch API Manager';
			g_permissions.permissions = [];
			g_permissions.accessLevels = {};
			refreshEnvironmentInfo().done(function() {
				var route = App.getActiveTab();
				Controllers[route]();
			});
		}
	});

	App.modal.show(editor);
}

App.addInitializer(function(options) {
	$('#modal').prevAll().remove();
	$('body').prepend(JST['app']());
	$('#user-name').text(g_permissions.name);

	$('#settings-menu').click(function() {
		var list = $('#environment-menu').html('<li role="presentation" class="dropdown-header">Loading...</li>');

		$.getJSON(Fantoccini.baseUrl + 'environments?limit=1000').done(function(data) {
			list.empty().append('<li role="presentation" class="dropdown-header">Switch environment</li>');
			var foundActive = false;

			_.each(data.items, function(item) {
				if (item.id == App.environmentId) {
					foundActive = true;
				}

				$('<li>')
					.toggleClass('active', item.id == App.environmentId)
					.append($('<a href="#" data-trigger="select-environment" />')
						.text(item.name)
						.attr('data-id', item.id)
					).appendTo(list);
			});

			if (can("create", "envirionment") || (foundActive && can("update", "envirionment"))) {
				list.append('<li class="divider" role="presentation"></li>');
				list.append('<li role="presentation" class="dropdown-header">Administration</li>');

				if (foundActive && can("update", "envirionment")) {
					$('<li/>')
						.append($('<a href="#" data-trigger="edit-environment"><i class="glyphicon glyphicon-edit"></i> Edit current</a>'))
						.appendTo(list);
				}

				if (can("create", "envirionment")) {
					$('<li/>')
						.append($('<a href="#" data-trigger="create-environment"><i class="glyphicon glyphicon-plus-sign"></i> Create</a>'))
						.appendTo(list);
				}
			}
		});
	});

	$('#environment-menu').on('click', 'a[data-trigger="select-environment"]', function(e) {
		e.preventDefault();
		App.environmentId = $(this).attr('data-id');
		setCookie('environmentId', App.environmentId, 365);
		$.getJSON(Fantoccini.baseUrl + 'environments/' + App.environmentId).done(function(data) {
			document.title = data.name + ' \xb7 Monarch API Manager';
		});
		g_permissions.permissions = [];
		g_permissions.accessLevels = {};
		$.getJSON(Fantoccini.baseUrl + 'me/permissions').done(function(data) {
			g_permissions = data;
			$('#user-name').text(g_permissions.name);
			refreshEnvironmentInfo();
			var route = App.getActiveTab();
			Controllers[route]();
		});
	});

	$('#environment-menu').on('click', 'a[data-trigger="edit-environment"]', function(e) {
		e.preventDefault();
		var model = new EnvironmentModel({ id : App.environmentId });
		var editor = new Editors.Generic({
			template : 'environments-form',
			model : model,
			afterSave: function() {
				document.title = model.get('name') + ' \xb7 Monarch API Manager';
			},
			afterDelete: function() {
				$.getJSON(Fantoccini.baseUrl + 'environments?limit=1').done(function(data) {
					if (data.items.length > 0) {
						App.environmentId = data.items[0].id;
						document.title = data.items[0].name + ' \xb7 Monarch API Manager';
						setCookie('environmentId', App.environmentId, 365);
						g_permissions.permissions = [];
						g_permissions.accessLevels = {};
						$.getJSON(Fantoccini.baseUrl + 'me/permissions').done(function(data) {
							g_permissions = data;
							$('#user-name').text(g_permissions.name);
							refreshEnvironmentInfo();
							var route = App.getActiveTab();
							Controllers[route]();
						});
					} else {
						// TODO handle no environments exist
					}
				});
			}
		});

		model.fetch().done(function() {
			App.modal.show(editor);
		});
	});

	$('#environment-menu').on('click', 'a[data-trigger="create-environment"]', function(e) {
		e.preventDefault();
		createEnvironment();
	});

	$('#back-to-top a').click(function(e) {
		e.preventDefault();
		$("html, body").animate({ scrollTop: 0 }, "slow");
	});

	App.environmentInfo.show(new EnvironmentInfoView());
	refreshEnvironmentInfo();
	App.router = new Router();
	Backbone.history.start();

	_.each([
		'applications:reload', 'clients:reload', 'developers:reload',
		'services:reload', 'providers:reload',
		'users:reload', 'roles:reload'], function(event) {
		App.vent.on(event, refreshEnvironmentInfo);
	});
});

function can(action, entity) {
	if (g_permissions.administrator) return entity != 'user' || g_permissions.usersLocked == false;

	if (entity) {
		var accessLevel = g_permissions.accessLevels[entity] || 'noaccess';
		if (action == 'create') {
			return accessLevel == 'fullaccess';
		} else if (action == 'read') {
			return _.contains(['redacted', 'read', 'readwrite', 'fullaccess'], accessLevel);
		} else if (action == 'update') {
			return _.contains(['readwrite', 'fullaccess'], accessLevel);
		} else if (action == 'delete') {
			return accessLevel == 'fullaccess';
		}
		
		return false;
	} else {
		return _.contains(g_permissions.permissions, action);
	}
}

$(function() {
	$('#modal').prevAll().remove();
	$('body').prepend(JST['initializing']());

	$('#modal').on('shown.bs.modal', function() {
		first = $(this).find(':input:enabled:visible:not(.btn, button):first');
		
		if (first.length > 0) {
			first.focus();
		} else {
			$(this).find(':input:enabled:visible:first').focus();
		}
	});

	App.environmentId = getCookie('environmentId');
	var environmentIdLoading = $.Deferred();

	$('#create-initial-environment').click(function() {
		var model = new EnvironmentModel();
		var editor = new Editors.Generic({
			template : 'environments-form',
			model : model,
			afterSave: function() {
				App.environmentId = model.get('id');
				setCookie('environmentId', App.environmentId, 365);
				document.title = model.get('name') + ' \xb7 Monarch API Manager';
				environmentIdLoading.resolve();
			}
		});

		App.modal.show(editor);
	});

	var loadFirst = function() {
		$.getJSON(Fantoccini.baseUrl + 'environments?limit=1').done(function(data) {
			if (data.items.length > 0) {
				App.environmentId = data.items[0].id;
				document.title = data.items[0].name + ' \xb7 Monarch API Manager';
				setCookie('environmentId', App.environmentId, 365);
				environmentIdLoading.resolve();
			} else {
				$.getJSON(Fantoccini.baseUrl + 'me/permissions').done(function(data) {
					g_permissions = data;
					$('#user-name').text(data.name);
					$('#initializing-message').hide();

					if (g_permissions.administrator) {
						$('#initial-environment').show();
					} else {
						$('#access-denied').show();
					}
				});
			}
		}).fail(function() {
			alert("Could not load environment information");
			environmentIdLoading.reject();
		});
	}

	if (App.environmentId == null || App.environmentId.length == 0) {
		loadFirst();
	} else {
		$.getJSON(Fantoccini.baseUrl + 'environments/' + App.environmentId).done(function(data) {
			document.title = data.name + ' \xb7 Monarch API Manager';
			environmentIdLoading.resolve();
		}).fail(function() {
			loadFirst();
		});
	}

	environmentIdLoading.done(function() {
		$.getJSON(Fantoccini.baseUrl + 'me/permissions').done(function(data) {
			g_permissions = data;
			App.start();
		});
	});
});