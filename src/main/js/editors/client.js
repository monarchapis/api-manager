Editors.Client = Editors.Key.extend({
	events: {
		'click button[data-trigger="generate"]' : 'onGenerateString',
		'focus #auth-scheme-picker .form-control' : 'onPickerFocus',
		'click #auth-scheme-picker li a' : 'onAppSchemeSelect',
		'change #auth-scheme-picker .form-control' : 'onAuthSchemeNameChange',
		'click section[name="permissionSet"] button[data-trigger="remove"]' : 'onAuthSchemeRemove',
		'change section[name="appPermissions"] input[type="checkbox"]' : 'onAppPermissionChange',
		'change section[name="userPermissions"] input[type="checkbox"]' : 'onUserPermissionChange',
		'click #add-policy-menu a' : 'addPolicy',
		'click #add-claim-source-menu a' : 'addClaimSource'
	},

	loadPromises: function(promises) {
		Editors.Key.prototype.loadPromises.call(this, promises);

		promises.push($.getJSON(Fantoccini.baseUrl  + 'claimSources').done(_.bind(function(data) {
			this.addData({ claimSourceConfigs : data.items });
			this.claimSourceConfigs = data.items;
		}, this)).fail(modalErrorHandler));
		
		promises.push($.getJSON(Fantoccini.baseUrl  + 'permissions?limit=1000').done(_.bind(function(data) {
			var app = _.filter(data.items, function(item) { return _.contains(['app', 'both'], item.scope); });
			var user = _.filter(data.items, function(item) { return _.contains(['user', 'both'], item.scope); });

			this.addData({ permissions : {
				app : app,
				user : user
			}});
		}, this)).fail(modalErrorHandler));
	},

	onPickerFocus : function(e) {
		var me = e.currentTarget;

		if (me.value == "<<New>>") {
			me.select();
		}
	},

	onAppSchemeSelect : function(e) {
		e.preventDefault();
		var item = $(e.currentTarget);

		if (item.attr("data-trigger")) {
			var a = $('<a href="#">&lt;&lt;New&gt;&gt;</a>');
			var next = $('<li />').append(a);
			item.parent().prev().before(next);
			item = a;
		}

		item.closest('.input-group').find('li.active').removeClass('active');
		item.parent().addClass('active');

		var name = item.text();
		this.currentScheme = item;
		this.currentSchemeName = name;
		var input = $('#auth-scheme-picker .form-control').val(name);
		if (can('update', this.model.entity)) {
			input.prop('disabled', false).focus();
		}

		var section = $('section[name="permissionSet"]', this.$el);
		var permissionSets = this.model.get('permissionSets');

		if (!permissionSets) {
			permissionSets = {};
			this.model.set('permissionSets', permissionSets);
		}

		var _permissionSet = permissionSets[name];
		var permissionSet = new Backbone.Model(_permissionSet || { enabled : true });
		var names = permissionSet.get('permissionIds') || [];

		if (this.bindView) {
			this.bindView.unbind();
			delete this.bindView;
		}

		this.bindView = rivets.bind(section, {
			permissionSet : permissionSet
		});

		$('section[name="userPermissions"] input[type="checkbox"]', this.$el).each(function() {
			var me = $(this);
			var name = me.attr('name');
			me.prop('checked', _.contains(names, name));
		});

		permissionSet.on('change', _.bind(function() {
			this.model.get('permissionSets')[this.currentSchemeName] = permissionSet.toJSON();
		}, this));

		section.show();
	},

	onAuthSchemeNameChange : function(e) {
		var input = $(e.currentTarget);
		var oldName = this.currentScheme.text();
		var newName = input.val();

		this.currentScheme.text(newName);
		this.currentSchemeName = newName;

		var permissionSets = this.model.get('permissionSets');
		var permissionSet = permissionSets[oldName];
		delete permissionSets[oldName];
		permissionSets[newName] = permissionSet;
	},

	onAuthSchemeRemove : function(e) {
		var section = $('section[name="permissionSet"]', this.$el);
		var name = this.currentScheme.text();
		var permissionSets = this.model.get('permissionSets') || {};
		delete permissionSets[name];

		if (this.bindView) {
			this.bindView.unbind();
			delete this.bindView;
		}

		this.currentScheme.parent().remove();
		delete this.currentScheme;

		$('#auth-scheme-picker .form-control').val('').prop('disabled', true);

		section.hide();
	},

	onAuthSchemeChange : function(e) {
		var value = $(e.currentTarget).val();
		var section = $('section[name="permissionSet"]', this.$el);
		var permissionSets = this.model.get('permissionSets');
		var _permissionSet = permissionSets != null ? permissionSets[value] : null;
		var permissionSet = new Backbone.Model(_permissionSet || {});
		var names = permissionSet.get('permissionIds') || [];

		this.bindView = rivets.bind(section, {
			permissionSet : permissionSet
		});

		$('section[name="userPermissions"] input[type="checkbox"]', this.$el).each(function() {
			var me = $(this);
			var name = me.attr('name');
			me.prop('checked', _.contains(names, name));
		});

		permissionSet.on('change', _.bind(function() {
			this.model.get('permissionSets')[value] = permissionSet.toJSON();
		}, this));

		value.length > 0 ? section.show() : section.hide();
	},

	onAppPermissionChange: function(e) {
		var cb = $(e.currentTarget);
		var name = cb.attr('name');
		var permissionIds = this.model.get('clientPermissionIds');

		if (!permissionIds) {
			permissionIds = [];
			this.model.set('clientPermissionIds', permissionIds);
		}

		if (cb.is(':checked')) {
			permissionIds.push(name);
		} else {
			permissionIds = _.filter(permissionIds, function(item) { return item != name; });
			this.model.set('clientPermissionIds', permissionIds);
		}
	},

	onUserPermissionChange: function(e) {
		var cb = $(e.currentTarget);
		var name = cb.attr('name');
		var value = this.currentSchemeName;
		var section = $('section[name="permissionSet"]', this.$el);
		var permissionSet = this.model.get('permissionSets')[value];

		if (!permissionSet) {
			permissionSet = {};
			this.model.get('permissionSets')[value] = permissionSet;
		}

		if (!permissionSet.permissionIds) {
			permissionSet.permissionIds = [];
		}

		if (cb.is(':checked')) {
			permissionSet.permissionIds.push(name);
		} else {
			permissionSet.permissionIds = _.filter(permissionSet.permissionIds, function(item) { return item != name; });
		}
	},

	addBindModels: function(models) {
		Editors.Traits.ExtendedProperties.addBindModels.apply(this, arguments);
	},

	reloadRelatedCollections : function(action) {
		if (action == "create" || action == "delete") {
			var applicationId = this.model.get('applicationId');
			App.vent.trigger('applications:reload', applicationId);
		}
	},

	///

	bindOthers : function() {
		Editors.Key.prototype.bindOthers.apply(this, arguments);

		var claimSources = this.model.get('claimSources');
		_.each(claimSources, _.bind(function(claimSource) {
			var name = claimSource.name;
			var config = _.find(this.claimSourceConfigs, function(claimSource) { return claimSource.name == name; });

			if (config) {
				this._appendClaimSource(config, claimSource.properties);
			}
		}, this));

		var adjustment;

		$('#claim-source-accordion').sortable({
			group : '#claim-source-accordion',
			itemSelector : '.panel',
			handle : '.dnd-handle',
			pullPlaceholder: false,
			placeholder : '<div class="panel panel-default placeholder"><div class="panel-heading"><h4 class="panel-title">&nbsp;</h4></div></div>',
			// animation on drop
			onDrop: function  (item, targetContainer, _super) {
				var clonedItem = $('<div class="panel panel-default"><div class="panel-heading"><h4 class="panel-title">&nbsp;</h4></div></div>');
				item.before(clonedItem);

				item.animate(clonedItem.position(), function() {
					clonedItem.detach();
					_super(item);
				});
			},

			// set item relative to cursor position
			onDragStart: function ($item, container, _super) {
				$('.panel-collapse.collapse.in', $item).removeClass('in');

				var offset = $item.offset(),
					pointer = container.rootGroup.pointer;

				adjustment = {
					left: pointer.left - offset.left,
					top: pointer.top - offset.top
				}

				_super($item, container);
			},

			onDrag: function ($item, position) {
				$item.css({
					left: position.left - adjustment.left,
					top: position.top - adjustment.top
				});
			}
		});

		$('#claim-source-accordion').on('click', 'a[data-trigger="remove"]', function(e) {
			e.preventDefault();
			var me = $(e.target);
			me.parents('.panel:first').remove();
		});
	},

	addClaimSource: function(e) {
		e.preventDefault();
		var me = $(e.target);
		var name = me.attr('data-value');
		var claimSource = _.find(this.claimSourceConfigs, function(claimSource) { return claimSource.name == name; });

		if (claimSource) {
			var panel = this._appendClaimSource(claimSource, {});
			$('.panel-collapse.collapse', panel).addClass('in');
			$('h4 a.collapse', panel).removeClass('collapse');
			panel.collapse('show');
		}
	},

	_appendClaimSource: function(config, data) {
		var panel = $(JST['policy-panel']({
			id : new Date().getTime(),
			name : config.name,
			displayName : config.displayName,
			properties : config.properties
		}));

		var defaults = {};

		_.each(config.properties, function(property) {
			if (property.defaultValue) {
				defaults[property.propertyName] = property.defaultValue;
			}

			if (data[property.propertyName] && property.multi == false) {
				var array = data[property.propertyName];
				data[property.propertyName] = array.length > 0 ? array[0] : null;
			}
		});

		var model = Backbone.Model.extend({ defaults : defaults });
		var instance = new model(data);
		panel.data('model', instance);
		panel.data('name', config.name);
		var bind = {};
		bind['policy'] = instance;
		rivets.bind(panel, bind);

		$('#claim-source-accordion').append(panel);

		return panel;
	},

	beforeSave : function() {
		Editors.Key.prototype.beforeSave.apply(this, arguments);

		var claimSources = [];
		$('#claim-source-accordion .panel').each(function(i, el) {
			var me = $(el);
			var name = me.data('name');
			var properties = me.data('model').toJSON();

			for (key in properties) {
				var value = properties[key];

				if (value != null && !_.isArray(value)) {
					properties[key] = [value];
				}
			}

			claimSources.push({ name : name, properties : properties });
		});

		this.model.set('claimSources', claimSources);

		return true;
	}
});