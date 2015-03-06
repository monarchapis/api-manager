Editors.Service = Editors.Generic.extend({
	_actions : ["create", "read", "update", "delete"],

	onShow: function() {
		var operations = $('fieldset[role="operations"]', this.$el);

		operations.on('focus', ':input', function(e) {
			var $el = $(this);
			var tr = $el.parents('tr:first');

			tr.removeClass('collapsed').addClass('expanded');

			$('[role="toggle"] i', tr).removeClass('glyphicon-chevron-right').addClass('glyphicon-chevron-down');
		});

		operations.on('click', '[role="toggle"]', function(e) {
			e.preventDefault();
			var $el = $(this);
			var tr = $el.parents('tr:first');
			var i = $('i', $el);

			if (tr.hasClass('collapsed')) {
				i.removeClass('glyphicon-chevron-right').addClass('glyphicon-chevron-down');
				tr.removeClass('collapsed').addClass('expanded');
			} else {
				i.removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-right');
				tr.removeClass('expanded').addClass('collapsed');
			}
		});

		permissionIds = [];
		var operationList = this.model.get('operations');
		_.each(operationList, function(operation) {
			var add = function(permission) {
				var i = permission.indexOf(':');
				permission = i != -1 ? permission.substring(0, i) : permission;
				permissionIds.push(permission);
			};

			_.each(operation.clientPermissionIds, add);
			_.each(operation.delegatedPermissionIds, add);
		});
		permissionIds = _.unique(permissionIds);

		var promise = new $.Deferred();

		if (permissionIds.length > 0) {
			$.getJSON(serviceBaseUrl + 'management/v1/permissions?id=' + _.map(permissionIds, function(v) { return encodeURIComponent(v); }).join('|') + '&limit=10000').done(function(data) {
				promise.resolve(_.indexBy(data.items, 'id'));
			}).fail(function() {
				promise.reject();
			});
		} else {
			promise.resolve({});
		}

		this.referencedPermissions = promise;

		this._addSelect2(operations);
	},

	_addSelect2: function($el) {
		_.each(['client', 'delegated'], _.bind(function(scope) {
			$(':input[name="' + scope + 'PermissionIds"]', $el).select2({
				placeholder: "Search for a permission",
				minimumInputLength: 1,
				multiple: true,
				query: _.bind(function (query) {
					$.getJSON(serviceBaseUrl + 'management/v1/permissions?scope=' + scope + '&scope=both&name=' + encodeURIComponent(query.term) + '*').done(_.bind(function(data) {
						var results = [];

						_.each(data.items, _.bind(function(item) {
							if (item.type == "entity") {
								_.each(this._actions, function(action) {
									results.push({ id: item.id + ':' + action, text: item.name + ':' + action });
								});
							} else {
								results.push({ id: item.id, text: item.name });
							}
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
						this.referencedPermissions.done(function(referencedPermissions) {
							var data = [];

							_.each(ids, function(id) {
								var i = id.indexOf(':');
								var sub = i != -1 ? id.substring(i) : '';
								var pid = i != -1 ? id.substring(0, i) : id;

								if (referencedPermissions[pid]) {
									data.push({ id : id, text : referencedPermissions[pid].name + sub });
								}
							});

							callback(data);
						}).fail(modalErrorHandler);
					} else {
						callback([]);
					}
				}, this),
				allowClear: true
			});
		}, this));
	},

	addBindModels: function(models) {
		Editors.Traits.ExtendedProperties.addBindModels.apply(this, arguments);

		var operations = $('fieldset[role="operations"]', this.$el);

		//$(':input[name="delegatedPermissionIds"]', operations)

		var compute = _.bind(function(e) {
			var data = [];
			var errors = false;

			$('tbody > tr', operations).each(function() {
				var $el = $(this);
				var operationName = $(':input[name="operationName"]', $el).val();
				var method = $(':input[name="method"]', $el).text();
				var uriPattern = $(':input[name="uriPattern"]', $el).val();
				var clientPermissionIds = $(':input[name="clientPermissionIds"]', $el).val().trim();
				var delegatedPermissionIds = $(':input[name="delegatedPermissionIds"]', $el).val().trim();
				var claims = _.map($(':input[name="claims"]', $el).val().trim().split('\n'), function(v) { return v.trim(); });

				var claimList = [];

				_.each(claims, function(claim) {
					var i = claim.indexOf('=');

					if (i != -1) {
						var type = claim.substring(0, i).trim();
						var value = claim.substring(i + 1).trim();

						if (type.length > 0) {
							claimList.push({ type : type, value : value });
						}
					} else if (claim.length > 0) {
						claimList.push({ type : claim, value : null });
					}
				});

				if (operationName.length > 0 && method.length > 0 && uriPattern.length > 0) {
					data.push({
						name : operationName,
						method : method,
						uriPattern : uriPattern,
						clientPermissionIds : clientPermissionIds.length > 0 ? _.map(clientPermissionIds.split(','), function(value) { return value.trim(); }) : [],
						delegatedPermissionIds : delegatedPermissionIds.length > 0 ? _.map(delegatedPermissionIds.split(','), function(value) { return value.trim(); }) : [],
						claims : claimList
					});
				}
			});

			if (!errors) {
				this.model.set('operations', data);
			} else {
				alert("There is an error in the operations.  They will not be saved until the error is corrected.");
			}
		}, this);

		operations.on('click', 'a[role="menuitem"]', function(e) {
			e.preventDefault();
			var $el = $(this);
			var value = $el.text();
			var button = $el.parent().parent().prev();
			button.removeClass("POST PUT PATCH DELETE OPTIONS").addClass(value).val(value).text(value);
			compute();
		});

		operations.on('click', 'button[role="delete"]', _.bind(function(e) {
			var me = $(e.target);
			var tr = me.parents('tr:first');
			tr.remove();
			compute();
		}, this));

		operations.on('change', ':input', _.bind(function(e) {
			var me = $(e.target);
			var tr = me.parents('tr:first');

			if (tr.is(':last-child')) {
				var clone = $(window.JST['services-form-operation']({}));
				clone.appendTo(tr.parent());
				$('textarea.autosize', clone).autosize({append: "\n"});
				this._addSelect2(clone);
				$(':input[name="operationName"], :input[name="method"], :input[name="uriPattern"]', tr).attr('required', 'required');
			}

			compute();
		}, this));
	}
});