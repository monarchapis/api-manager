Editors.PrincipalClaims = Editors.Generic.extend({
	onShow: function() {
		var profileId = Editors.PrincipalClaims.profileId;

		if (this.model.get('profileId') == null) {
			this.model.set('profileId', profileId);
		}

		$('input[name="inherits"]').select2({
			placeholder: "Search for a principal",
			minimumInputLength: 1,
			multiple: true,
			query: _.bind(function (query) {
				$.getJSON(serviceBaseUrl + 'management/v1/principalClaims?profileId=' + profileId + '&name=' + encodeURIComponent(query.term) + '*').done(_.bind(function(data) {
					var results = [];

					_.each(data.items, _.bind(function(item) {
						results.push({ id: item.id, text: item.name });
					}, this));

					query.callback({
						results : results,
						more: data.offset + data.count < data.total
					});
				}, this)).fail(modalErrorHandler);
			}, this),
			initSelection: _.bind(function(element, callback) {
				var ids = $(element).val().split(',');

				if (ids.length > 0) {
					$.getJSON(serviceBaseUrl + 'management/v1/principalClaims?id=' + _.map(ids, function(v) { return encodeURIComponent(v); }).join('|') + '&limit=10000').done(function(data) {
						var results = [];

						_.each(data.items, _.bind(function(item) {
							results.push({ id: item.id, text: item.name });
						}, this));

						callback(results);
					}).fail(modalErrorHandler);
				} else {
					callback([]);
				}
			}, this),
			allowClear: true
		});

		var claims = $('[data-region="claims"]', this.$el);

		this._addAutosize(claims);
	},

	_addAutosize: function($el) {
		var select = $(':input[name="values"]', $el);
		select.autosize({append: "\n"});
	},

	addBindModels: function(models) {
		var inherits = $('input[name="inherits"]', this.$el);

		inherits.change(_.bind(function() {
			var values = inherits.val();
			this.model.set('inherits', values.length > 0 ? _.map(values.split(','), function(value) { return value.trim(); }) : []);
		}, this));

		var claims = $('[data-region="claims"]', this.$el);

		var compute = _.bind(function(e) {
			var data = {};
			var errors = false;

			$('tbody > tr', claims).each(function() {
				var $el = $(this);
				var type = $(':input[name="type"]', $el).val().trim();
				var values = $(':input[name="values"]', $el).val().trim();

				if (type.length > 0 && values.length > 0) {
					data[type] = values.length > 0 ? _.map(values.split('\n'), function(value) { return value.trim(); }) : [];
				}
			});

			if (!errors) {
				this.model.set('claims', data);
			} else {
				alert("There is an error in the claims section.  They will not be saved until the error is corrected.");
			}
		}, this);

		claims.on('click', 'button[role="delete"]', _.bind(function(e) {
			var me = $(e.target);
			var tr = me.parents('tr:first');
			tr.remove();
			compute();
		}, this));

		claims.on('change', ':input', _.bind(function(e) {
			var me = $(e.target);
			var tr = me.parents('tr:first');

			if (tr.is(':last-child')) {
				var clone = $(window.JST['principalClaims-form-claim']({}));
				clone.appendTo(tr.parent());
				this._addAutosize(clone);
				$(':input[name="type"]', tr).attr('required', 'required');
			}

			compute();
		}, this));
	}
});

Editors.PrincipalClaims.profileId = null;