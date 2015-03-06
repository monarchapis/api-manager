Editors.Provider = Editors.Key.extend({
	events: {
		'click button[data-trigger="generate"]' : 'onGenerateString',
		'change section[name="permissions"] input[type="checkbox"]' : 'onPermissionChange',
		'click #add-policy-menu a' : 'addPolicy'
	},

	addBindModels: function(models) {
		var accessLevels = this.model.get('accessLevels');

		if (!accessLevels) {
			accessLevels = {};
			this.model.set('accessLevels', accessLevels);
		}

		var defaults = {};

		_.each([
			'application', 'client', 'developer',
			'service', 'plan', 'permission', 'message',
			'provider', 'role'], function(key) {
			defaults[key] = 'noaccess';
		});

		var model = Backbone.Model.extend({ defaults : defaults });
		var instance = new model(accessLevels);

		models['accessLevels'] = instance;

		instance.on('change', _.bind(function() {
			this.model.set('accessLevels', instance.toJSON());
		}, this));

		Editors.Traits.ExtendedProperties.addBindModels.apply(this, arguments);
	},

	onPermissionChange: function(e) {
		var cb = $(e.currentTarget);
		var name = cb.attr('name');
		var permissionIds = this.model.get('permissions');

		if (!permissionIds) {
			permissionIds = [];
			this.model.set('permissions', permissionIds);
		}

		if (cb.is(':checked')) {
			permissionIds.push(name);
		} else {
			permissionIds = _.filter(permissionIds, function(item) { return item != name; });
			this.model.set('permissions', permissionIds);
		}
	}
});