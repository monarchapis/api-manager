var NoResultsView = Fantoccini.MessageView.extend({
	message: 'No records were found.'
});

var NoMatchesView = Fantoccini.MessageView.extend({
	message: 'No results were found that matched your criteria.'
});

var AccessDeniedView = Fantoccini.MessageView.extend({
	message: 'Access denied'
});

var ErrorView = Fantoccini.MessageView.extend({
	message: 'An error occurred'
});

var EmptyView = Marionette.ItemView.extend({
	template: 'empty',

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.apply(this, arguments);
		options = options || {};
		this.collection = options.collection || this.collection;
		this.singular = options.singular || this.singular;
		this.plural = options.plural || this.plural;
	},

	serializeData: function() {
		return {
			filtered: !_.isEmpty(this.collection.filterParams),
			singular: this.singular,
			plural: this.plural
		}
	}
});

var NullView = Backbone.View.extend({
});

var LoadingView = Marionette.ItemView.extend({
	template: 'loading'
});

var PaginationView = Marionette.View.extend({
	collectionEvents: {
		"fetching": "render",
		"fetched": "render",
		"reset": "render"
	},

	render: function() {
		this.$el.empty();

		if (this.collection.state != 'fetched' ||
			this.collection.pages <= 1) {
			return;
		}

		var ul = $('<ul class="pagination no-margin"/>');

		ul.pagination(this.collection.total, {
			items_per_page: this.collection.perPage,
			current_page: this.collection.page - 1,
			num_display_entries: 5,
			num_edge_entries: 1,
			link_to: 'javascript:void(0)',
			prev_text: "&laquo;",
			next_text: "&raquo;",
			ellipse_text: "&hellip;",
			callback: _.bind(function(page) {
				if (this.collection.page != page + 1) {
					this.collection.setPage(page + 1);
				}
			}, this)
		});

		this.$el.append(ul);
	}
});

var ViewingNumbersView = Fantoccini.ItemView.extend({
	template: 'viewingNumbers',

	loadingEmpty: true,

	serializeData: function() {
		return {
			from: ((this.collection.page - 1) * 10 + 1),
			to: Math.min(
				(this.collection.page -1) * 10 + this.collection.perPage,
				this.collection.total),
			total: this.collection.total
		};
	}
});

var CollectionOptionsView = Marionette.ItemView.extend({
	template: 'collectionOptions',

	events: {
		'change select[name="perPage"]' : 'changePerPage',
		'change select[name="orderBy"]' : 'changeOrderBy'
	},

	collectionEvents: {
		"fetching": "render",
		"fetched": "render"
	},

	render: function() {
		if (this.collection.state == 'fetched') {
			Marionette.ItemView.prototype.render.call(this);

			$('select[name="perPage"]', this.$el).val(this.collection.perPage);

			if (this.collection.orderBy) {
				$('select[name="orderBy"]', this.$el).val(this.collection.orderBy);
			}
		} else {
			this.$el.empty();
		}
	},

	changePerPage: function(e) {
		var perPage = parseInt($(e.currentTarget).val());
		this.collection.setPerPage(perPage);
	},

	changeOrderBy: function(e) {
		var orderBy = $(e.currentTarget).val();
		this.collection.setOrderBy(orderBy);
	}
});

var SortingAndPagingView = Marionette.ItemView.extend({
	template: 'sortingAndPaging',

	events: {
		'change select[name="perPage"]' : 'changePerPage',
		'change select[name="orderBy"]' : 'changeOrderBy'
	},

	collectionEvents: {
		"fetching": "render",
		"fetched": "render",
		"reset": "render"
	},

	serializeData: function() {
		return {
			perPageOptions : Marionette.getOption(this, 'perPageOptions') != null,
			orderByOptions : Marionette.getOption(this, 'orderByOptions') != null
		};
	},

	render: function() {
		if (this.collection.state == 'fetched' && this.collection.length > 0) {
			Marionette.ItemView.prototype.render.call(this);

			var selectPerPage = $('select[name="perPage"]', this.$el).empty();
			var selectOrderBy = $('select[name="orderBy"]', this.$el).empty();

			var perPageOptions = Marionette.getOption(this, 'perPageOptions');
			var orderByOptions = Marionette.getOption(this, 'orderByOptions');

			if (_.isArray(perPageOptions)) {
				_.each(perPageOptions, function(count) {
					$('<option/>')
						.attr('value', count)
						.text(count)
						.appendTo(selectPerPage);
				});

				selectPerPage.val(this.collection.perPage);
			}

			if (_.isArray(orderByOptions)) {
				_.each(orderByOptions, function(sortingOption) {
					$('<option/>')
						.attr('value', sortingOption.sortBy)
						.text(sortingOption.label)
						.appendTo(selectOrderBy);
				});

				selectOrderBy.val(this.collection.orderBy);
			}
		} else {
			this.$el.empty();
		}
	},

	changePerPage: function(e) {
		var perPage = parseInt($(e.currentTarget).val());
		this.collection.setPerPage(perPage);
	},

	changeOrderBy: function(e) {
		var orderBy = $(e.currentTarget).val();
		this.collection.setOrderBy(orderBy);
	}
});

var FilterView = Marionette.ItemView.extend({
	template: 'filter',

	lastValue: '',

	events: {
		'change input[name="filter"]': 'changedFilter',
		'keyup input[name="filter"]': 'setTimeout'
	},

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.apply(this, arguments);
		options = options || {};
		this.collection = options.collection || this.collection;
		this.singular = options.singular || this.singular;
		this.plural = options.plural || this.plural;
		this.label = options.label || this.label;
	},

	mixinTemplateHelpers: function(data) {
		return _.extend(data, {
			singular: this.singular,
			plural: this.plural,
			label: this.label
		});
	},

	changedFilter: function(e) {
		this.timeout = null;
		var value = $(e.currentTarget).val();
		var filter = {};

		if (value.length > 0) {
			filter[this.options.property] = value + '*';
		}

		if (value != this.lastValue) {
			this.lastValue = value;
			this.collection.setFilter(filter);
		}
	},

	setTimeout: function(e) {
		if (this.timeout) window.clearTimeout(this.timeout);
		this.timeout = window.setTimeout(_.bind(function() {
			this.changedFilter(e);
		}, this), 500);
	}
});

var DynamicFilterView = Marionette.ItemView.extend({
	lastValues: {},
	timeouts: {},
	filter: {},

	events: {
		'change :input': 'changedFilter',
		'keyup input[type="text"]': 'setTimeout'
	},

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.apply(this, arguments);
		options = options || {};
		this.collection = options.collection || this.collection;
	},

	mixinTemplateHelpers: function(data) {
		return _.extend(data, {
			singular: this.singular,
			plural: this.plural,
			label: this.label
		});
	},

	changedFilter: function(e) {
		var input = $(e.currentTarget);
		var name = input.attr('name');
		var value = input.val();

		delete this.timeouts[name];

		if (value.length > 0) {
			this.filter[name] = value + ((input.attr('type') == 'text') ? '*' : '');
		} else {
			delete this.filter[name];
		}

		if (value != this.lastValues[name]) {
			this.lastValues[name] = value;
			this.collection.setFilter(this.filter);
		}
	},

	setTimeout: function(e) {
		var input = $(e.currentTarget);
		var name = input.attr('name');

		if (this.timeouts[name]) window.clearTimeout(this.timeouts[name]);
		this.timeouts[name] = window.setTimeout(_.bind(function() {
			this.changedFilter(e);
		}, this), 500);
	}
});

var PaginatedCollectionView = Fantoccini.Layout.extend({
	template: 'pagination',

	filterView: FilterView,
	resultsView: NoResultsView,
	optionsView: SortingAndPagingView,

	constructor: function(options) {
		_.extend(this, _.pick(options, ['filterView', 'resultsView', 'optionsView']));
		Fantoccini.Layout.prototype.constructor.apply(this, arguments);
	},

	regionsAndViews: {
		filter: {
			selector: 'div.filterRegion',
			view: function() { return this.filterView; },
			type: 'function'
		},
		paginationTop: {
			selector: 'div.paginationRegion:first',
			view: PaginationView
		},
		paginationBottom: {
			selector: 'div.paginationRegion:last',
			view: PaginationView
		},
		results: {
			selector: 'div.resultsRegion',
			view: function() { return this.resultsView; },
			type: 'function'
		},
		viewingTop: {
			selector: 'div.viewingRegion:first',
			view: ViewingNumbersView
		},
		viewingBottom: {
			selector: 'div.viewingRegion:last',
			view: ViewingNumbersView
		},
		optionsTop: {
			selector: 'div.sortByRegion:first',
			view: function() { return this.optionsView; },
			type: 'function'
		},
		optionsBottom: {
			selector: 'div.sortByRegion:last',
			view: function() { return this.optionsView; },
			type: 'function'
		}
	}
});

var LazyLoadingTabView = Fantoccini.TabLayout.extend({
	onTabSelected : function(name) {
		var collection = this.viewInstances[name].collection;

		if (collection != null && collection.state == 'new') {
			collection.fetch();
		}
	}
});

var EditorCollectionView = PaginatedCollectionView.extend({
	initialize : function(options) {
		this.formView = options.formView || this.formView;
	},
	
	events : {
		'click [data-trigger="create"]' : 'onCreateEntity',
		'click [data-trigger="edit"]' : 'onEditEntity'
	},

	onCreateEntity : function(e) {
		e.preventDefault();
		var model = new this.collection.model();
		var editor = new this.formView({
			template : model.collection + '-form',
			model : model
		});

		App.modal.show(editor);
	},

	onEditEntity : function(e) {
		e.preventDefault();
		var id = $(e.currentTarget).attr('data-id');
		var model = new this.collection.model({id : id});

		model.fetch().done(_.bind(function() {
			var editor = new this.formView({
				template : model.collection + '-form',
				model : model
			});

			App.modal.show(editor);
		}, this));
	}
});

var ChangePasswordView = Marionette.ItemView.extend({
	template : 'change-password-form',

	events : {
		'submit form' : 'onSubmit'
	},

	onSubmit : function(e) {
		e.preventDefault();

		var password = this.model.get('password');
		var confirm = this.model.get('confirm');

		if (password.length == 0) {
			$('input[type="password"]:first', this.$el).focus();
			modalError($('#modal'), 'Please enter the password and confirmation.');
		} else if (password != confirm) {
			$('input[type="password"]', this.$el).val('')[0].focus();
			modalError($('#modal'), 'The passwords do not match.  Please try again.');
		} else {
			$('input[type="password"]', this.$el).val('');
			$.ajax({
				type: "PUT",
				dataType : 'json',
				contentType : 'application/json; charset=UTF-8',
				data : JSON.stringify({ password : password }),
				headers: { "X-Environment-Id" : App.environmentId },
				url: Fantoccini.baseUrl + this.collection + '/' + this.id + '/password'
			}).done(function() {
				$('#modal').modal('hide');
			});
		}

		return false;
	},

	render : function() {
		Marionette.ItemView.prototype.render.apply(this, arguments);
		rivets.bind(this.$el, { password : this.model });
		initModal($('#modal'), this.$el);
	}
});

var AccountEditorCollectionView = EditorCollectionView.extend({
	events : {
		'click [data-trigger="create"]' : 'onCreateEntity',
		'click [data-trigger="edit"]' : 'onEditEntity',
		'click [data-trigger="changePassword"]' : 'onChangePassword',
	},

	onChangePassword : function(e) {
		e.preventDefault();
		var id = $(e.currentTarget).attr('data-id');
		var model = new Backbone.Model();
		var editor = new ChangePasswordView({
			collection : this.collection.collection,
			id : id,
			model : model
		});

		App.modal.show(editor);
	}
});