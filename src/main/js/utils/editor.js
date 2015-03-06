var initModal = function($modal, $content) {
	$modal.modal('show').on('shown.bs.modal', function() {
		var textareas = $('textarea', $content).autosize({append: "\n"});
		document.body.offsetWidth;
		textareas.addClass('autosize').css('resize', '');
	});
}

var modalError = function($modal, message) {
	var errors = $('#modal-errors', $modal);
	errors.html(JST['alert-error']({ message : message }));
	errors.hide().slideDown();
}

var modalErrorHandler = function(xhr, status, error) {
	var response = JSON.parse(xhr.responseText);
	modalError($('#modal'), response.message || 'Sorry.  There was an issue loading permission information.');
}

Editors.Generic = Marionette.ItemView.extend({
	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.apply(this, arguments);
		this.loadedData = {};
		this.originalModel = this.model.clone();

		if (options.afterSave) {
			this.afterSave = options.afterSave;
		}
		
		if (options.afterDelete) {
			this.afterDelete = options.afterDelete;
		}
	},

	defaultEvents: {
		'submit form' : 'onSave',
		'click button[data-trigger="delete"]' : 'onDelete'
	},

	_delegateDOMEvents: function(events){
		events = events || this.events;
		if (_.isFunction(events)){ events = events.call(this); }

		var combinedEvents = {};
		var triggers = this.configureTriggers();
		_.extend(combinedEvents, this.defaultEvents, events, triggers);

		Backbone.View.prototype.delegateEvents.call(this, combinedEvents);
	},

	mixinTemplateHelpers: function(data) {
		data = Marionette.ItemView.prototype.mixinTemplateHelpers.call(this, data);
		return _.extend(data, this.loadedData);
	},

	addData : function(data) {
		_.extend(this.loadedData, data);
	},

	load: function() {
		return null;
	},

	bindOthers: function() {},
	addBindModels: function(models) {},

	render: function() {
		var promise = this.load();

		var render = _.bind(function() {
			Marionette.ItemView.prototype.render.apply(this, arguments);
			var bind = {};
			bind[this.model.entity] = this.model;
			this.addBindModels(bind);
			rivets.bind(this.$el, bind);
			this.bindOthers();
			if (!can('update', this.model.entity)) {
				$(':input:not(.btn, button)', this.$el).prop('disabled', true);
			}
			initModal($('#modal'), this.$el);
		}, this);

		if (promise) {
			promise.done(render);
		} else {
			render();
		}
	},

	reloadRelatedCollections : function(action) {},

	beforeSave: function() { return true; },

	afterSave: function() {},
	afterDelete: function() {},

	onSave: function(e) {
		e.preventDefault();
		var id = this.model.get('id');

		if (can(id ? 'update' : 'create', this.model.entity)) {
			if (this.beforeSave()) {
				this.model.save().done(_.bind(function() {
					App.vent.trigger(this.model.collection + ':reload', id);
					this.reloadRelatedCollections(id ? 'update' : 'create');
					$('#modal').modal('hide');
					this.afterSave();
				}, this)).fail(modalErrorHandler);
			}
		}
	},

	onDelete: function(e) {
		if (can('delete', this.model.entity) && confirm("Are you sure you want to delete this " + this.model.displayName + "?")) {
			this.model.destroy().done(_.bind(function() {
				App.vent.trigger(this.model.collection + ':reload');
				this.reloadRelatedCollections('delete');
				$('#modal').modal('hide');
				this.afterDelete();
			}, this)).fail(modalErrorHandler);
		}
	}
});

Editors.Key = Editors.Generic.extend({
	load: function() {
		var promises = [];

		this.loadPromises(promises);

		return $.when.apply(null, promises);
	},

	loadPromises: function(promises) {
		promises.push($.getJSON(Fantoccini.baseUrl  + 'authenticators').done(_.bind(function(data) {
			this.addData({ authenticatorConfigs : data.items });
			this.authenticatorConfigs = data.items;
		}, this)).fail(modalErrorHandler));

		promises.push($.getJSON(Fantoccini.baseUrl  + 'policies').done(_.bind(function(data) {
			this.addData({ policyConfigs : data.items });
			this.policyConfigs = data.items;
		}, this)).fail(modalErrorHandler));
	},

	bindOthers: function() {
		this.authenticationModels = [];
		var authenticators = this.model.get('authenticators');

		if (!authenticators) {
			authenticators = {};
			this.model.set('authenticators', authenticators);
		}

		$('section[name="authenticators"] .panel-body', this.$el).each(_.bind(function(index, item) {
			var config = this.authenticatorConfigs[index];
			var defaults = {};
			var data = authenticators[config.name] || {};
			var enabledCb = $('#authenticator-enabled-' + index);
			var collapse = enabledCb.parents(".panel:first").find('.panel-collapse');

			if (authenticators[config.name]) {
				enabledCb.prop('checked', true);
				collapse.addClass('in');
			}

			enabledCb.click(function() {
				if ($(this).prop('checked')) {
					collapse.collapse('show');
				}
			});

			_.each(config.properties, function(property) {
				if (property.defaultValue) {
					defaults[property.propertyName] = property.defaultValue;
				}

				if (data[property.propertyName] && property.multi == false) {
					var array = data[property.propertyName];
					data[property.propertyName] = array.length > 0 ? array[0] : null;
				}
			});

			for (key in data) {
				if (!_.find(config.properties, function(item) { return item.propertyName == key; })) {
					delete data[key];
				}
			}

			authenticators[config.name] = data;

			var me = $(item);
			var model = Backbone.Model.extend({ defaults : defaults });
			var instance = new model(data);

			// Handle checkbox arrays outside of Rivets
			$(':input[data-array]', me).each(_.bind(function(index, item) {
				var me = $(item);
				var property = me.attr('data-array');
				var value = me.attr('value');
				var array = instance.get(property) || [];
				me.prop('checked', _.contains(array, value));
			}, this));

			me.on('change', ':input', function() {
				enabledCb.prop('checked', true);
			});

			me.on('change', ':input[data-array]', function() {
				var me = $(this);
				var property = me.attr('data-array');
				var value = me.attr('value');
				var checked = me.is(':checked');
				var array = instance.get(property) || [];

				if (checked) {
					array.push(value);
				} else {
					array = _.filter(array, function(item) { return item != value });
				}
				
				instance.set(property, array);
			});

			$('.an-integer', this.$el).numeric();
			$('.an-decimal', this.$el).numeric({ allow : '.' });

			rivets.bind(me, { authenticator : instance });

			this.authenticationModels.push(instance);
		}, this));

		////

		var policies = this.model.get('policies');
		_.each(policies, _.bind(function(policy) {
			var name = policy.name;
			var config = _.find(this.policyConfigs, function(policy) { return policy.name == name; });

			if (config) {
				this._appendPolicy(config, policy.properties);
			}
		}, this));

		var adjustment;

		$('#policy-accordion').sortable({
			group : '#policy-accordion',
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

		$('#policy-accordion').on('click', 'a[data-trigger="remove"]', function(e) {
			e.preventDefault();
			var me = $(e.target);
			me.parents('.panel:first').remove();
		});
	},

	onGenerateString : function(e) {
		var me = $(e.target);
		var length = me.parent().prev();
		var target = length.parent().prev().find('input');

		$.getJSON(Fantoccini.baseUrl + 'generateRandomString?length=' + length.val()).done(function(data) {
			target.val(data.value).change();
		}).fail(modalErrorHandler);
	},

	addPolicy: function(e) {
		e.preventDefault();
		var me = $(e.target);
		var name = me.attr('data-value');
		var policy = _.find(this.policyConfigs, function(policy) { return policy.name == name; });

		if (policy) {
			var panel = this._appendPolicy(policy, {});
			$('.panel-collapse.collapse', panel).addClass('in');
			$('h4 a.collapse', panel).removeClass('collapse');
			panel.collapse('show');
		}
	},

	_appendPolicy: function(config, data) {
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

		$('#policy-accordion').append(panel);

		return panel;
	},

	beforeSave: function() {
		authenticators = {};

		for (var i=0; i<this.authenticatorConfigs.length; i++) {
			var name = this.authenticatorConfigs[i].name;
			var properties = this.authenticationModels[i].toJSON();

			if ($('#authenticator-enabled-' + i).prop('checked')) {
				for (key in properties) {
					var value = properties[key];

					if (value != null && !_.isArray(value)) {
						properties[key] = [value];
					}
				}

				authenticators[name] = properties;
			}
		}

		this.model.set('authenticators', authenticators);

		var policies = [];
		$('#policy-accordion .panel').each(function(i, el) {
			var me = $(el);
			var name = me.data('name');
			var properties = me.data('model').toJSON();

			for (key in properties) {
				var value = properties[key];

				if (value != null && !_.isArray(value)) {
					properties[key] = [value];
				}
			}

			policies.push({ name : name, properties : properties });
		});

		this.model.set('policies', policies);

		return true;
	}
});

Editors.Traits = Editors.Traits || {};

Editors.Traits.ExtendedProperties = {
	addBindModels: function(models) {
		var extended = $('fieldset[role="extended"]', this.$el);

		var compute = _.bind(function(e) {
			var data = {};
			var errors = false;

			$('tbody > tr', extended).each(function() {
				var $el = $(this);
				var key = $(':input[name="key"]', $el).val();
				var type = $(':input[name="type"]', $el).val();
				var valueEl = $(':input[name="value"]', $el);
				valueEl.removeClass('has-error');
				var value = valueEl.val();

				if (key.length > 0 && type.length > 0 && value.length > 0) {
					if (type == "number") {
						if ($.isNumeric(value)) {
							value = parseFloat(value);
						} else {
							valueEl.addClass('has-error');
							errors = true;
						}
					} else if (type == "boolean") {
						value = valueEl.is(':checked');
					}

					data[key] = value;
				}
			});

			if (!errors) {
				this.model.set('extended', data);
			} else {
				alert("There is an error in the extended properties.  They will not be saved until the error is corrected.");
			}
		}, this);

		extended.on('click', 'button[role="delete"]', _.bind(function(e) {
			var me = $(e.target);
			var tr = me.parents('tr:first');
			tr.remove();
			compute();
		}, this));

		extended.on('change', ':input', _.bind(function(e) {
			var me = $(e.target);
			var tr = me.parents('tr:first');

			if (me.attr('name') == 'type') {
				var type = me.val();
				var valueEl = $(':input[name="value"]', tr);
				var td = valueEl.parents('td:first');
				var value = valueEl.val();
				td.empty();

				if (type == "string") {
					$('<input type="text" name="value" class="form-control" required="required" />').val(value).appendTo(td);
				} else if (type == "number") {
					$('<input type="number" name="value" class="form-control" required="required" />').appendTo(td);
				} else if (type == "boolean") {
					$('<div class="checkbox"><label><input type="checkbox" name="value" value="true" /> Enabled</label></div>').val(value).appendTo(td);
				}
			}

			if (tr.is(':last-child')) {
				var clone = tr.clone();
				$(':input[name="key"]', clone).val('');
				$(':input[name="type"]', clone).val('string');
				$(':input[name="value"]', clone).parents('td:first').empty().append('<input type="text" name="value" class="form-control" />');
				clone.appendTo(tr.parent());
				$('input', tr).attr('required', 'required');
			}

			compute();
		}, this));
	}
}