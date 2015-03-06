Editors.Message = Editors.Generic.extend({
	events: {
		'focus #locale-picker .form-control' : 'onPickerFocus',
		'click #locale-picker li a' : 'onLocaleSelect',
		'change #locale-picker .form-control' : 'onLocaleNameChange',
		'click section[name="locale"] button[data-trigger="remove"]' : 'onLocaleRemove'
	},

	load: function() {
		var messages = new MessageCollection(null, { limit : 1000 });
		return messages.fetch().done(_.bind(function() {
			var id = this.model.get("id");

			// filter out self
			if (id) {
				messages = messages.filter(function(item) {
					return item.get("id") != id;
				});
			} else {
				messages = messages.toJSON();
			}
			
			this.addData({ messages : messages });
		}, this)).promise();
	},

	onPickerFocus : function(e) {
		var me = e.currentTarget;

		if (me.value == "<<New>>") {
			me.select();
		}
	},

	onLocaleSelect : function(e) {
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

		this.currentScheme = item;
		var name = item.text();
		var input = $('#locale-picker .form-control').val(name);
		if (can('update', this.model.entity)) {
			input.prop('disabled', false).focus();
		}

		var section = $('section[name="locale"]', this.$el);
		var locales = this.model.get('locales');

		if (locales == null) {
			locales = {};
			this.model.set('locales', locales);
		}

		var _locale = locales != null ? locales[name] : null;

		if (_locale == null) {
			_locale = { format : '', content : '' };
			locales[name] = _locale;
		}

		var locale = new Backbone.Model(_locale);

		if (this.bindView) {
			this.bindView.unbind();
			delete this.bindView;
		}

		section.html(JST['messageLocales-form'](_locale));

		if (!can('update', this.model.entity)) {
			$(':input:not(.btn, button)', section).prop('disabled', true);
		}

		this.bindView = rivets.bind(section, {
			messageLocale : locale
		});

		locale.on('change', function() {
			var name = item.text();
			locales[name] = locale.toJSON();
		});

		section.show();
	},

	onLocaleNameChange : function(e) {
		var input = $(e.currentTarget);
		var oldName = this.currentScheme.text();
		var newName = input.val();
		this.currentScheme.text(newName);

		var locales = this.model.get('locales') || {};
		var locale = locales[oldName];
		delete locales[oldName];
		locales[newName] = locale;
	},

	onLocaleRemove : function(e) {
		var section = $('section[name="locale"]', this.$el);
		var name = this.currentScheme.text();
		var locales = this.model.get('locales') || {};
		delete locales[name];

		if (this.bindView) {
			this.bindView.unbind();
			delete this.bindView;
		}

		this.currentScheme.parent().remove();
		delete this.currentScheme;

		$('#locale-picker .form-control').val('').prop('disabled', true);

		section.hide();
	},

	reloadRelatedCollections : function(action) {
		if (action == "update") {
			App.vent.trigger('permissions:reload');
		}
	}
});