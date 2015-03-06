var PartnersLayout = Marionette.Layout.extend({
	template: 'partners',

	regions: {
		tabs : '#tabs'
	}
});

/////

var AppDevelopersLayout = Marionette.Layout.extend({
	template: 'app-developers-form',

	events: {
		'click [data-trigger="add-developers"]' : 'showDeveloperSearch',
		'click [data-trigger="cancel"]' : 'showCurrentDevelopers',
		'change :input[name="role"]': 'onSetRole',
		'click [data-trigger="delete"]' : 'onDelete'
	},

	regions: {
		body : '#appDevelopersBody'
	},

	showCurrentDevelopers: function(e) {
		if (e) e.preventDefault();

		var developers = new DeveloperCollection([], {
			relationship : 'applications',
			id : this.id
		});
		developers.fetch();

		var collectionView = new PaginatedCollectionView({
			filterView: Marionette.ItemView.extend({
				template: 'app-developers-filter'
			}),
			collection: developers,
			resultsView : AppDeveloperCollectionView,
			emptyView : Fantoccini.MessageView.extend({
				message: 'There are currently no developers for this application.'
			})
		});

		this.body.show(collectionView);
	},

	showDeveloperSearch: function(e) {
		if (e) e.preventDefault();

		var developers = new DeveloperCollection();
		var applicationId = this.id;

		var search = new PaginatedCollectionView({
			filterView: Marionette.ItemView.extend({
				template : 'app-developers-search',

				events: {
					'submit form': 'onSubmit'
				},

				constructor: function(options) {
					Marionette.ItemView.prototype.constructor.apply(this, arguments);
					options = options || {};
					this.collection = options.collection || this.collection;
				},

				onSubmit: function(e) {
					e.preventDefault();
					var email = trimToNull($('#email').val());
					var firstName = trimToNull($('#firstName').val());
					var lastName = trimToNull($('#lastName').val());

					var filter = {};

					if (email != null) {
						filter['email'] = email;
					} else if (firstName != null || lastName != null) {
						if (firstName != null) {
							filter['firstName'] = firstName;
						}

						if (lastName != null) {
							filter['lastName'] = lastName;
						}
					}

					filter.withRolesFor = applicationId;

					this.collection.setFilter(filter);
				}
			}),
			collection: developers,
			resultsView : AppDeveloperSearchResultsView,
			emptyView : EmptyView.extend({
				singular: 'developer',
				plural: 'developers'
			})
		});

		this.body.show(search);
	},

	onSetRole: function(e) {
		var me = $(e.currentTarget);
		var developerId = me.attr('data-id');
		var role = trimToNull(me.val());
		var processing = me.parents('tr:first').find('.processing');

		processing.html('<i class="fa fa-spinner fa-lg fa-spin"></i><span class="sr-only">Processing</span>');

		var success = function() {
			processing.html('<i class="fa fa-check fa-lg"></i><span class="sr-only">Success</span>');
		}

		if (role != null) {
			$.ajax({
				type: "PUT",
				dataType : 'json',
				contentType : 'application/json; charset=UTF-8',
				data : JSON.stringify({ role : role }),
				headers: { "X-Environment-Id" : App.environmentId },
				url: Fantoccini.baseUrl + 'applications/' + this.id + '/developers/' + developerId
			}).done(success);
		} else {
			$.ajax({
				type: "DELETE",
				dataType : 'json',
				headers: { "X-Environment-Id" : App.environmentId },
				url: Fantoccini.baseUrl + 'applications/' + this.id + '/developers/' + developerId
			}).done(success);
		}
	},

	onDelete: function(e) {
		var me = $(e.currentTarget);
		var developerId = me.attr('data-id');

		var success = function() {
			var row = me.parents('tr:first');
			row.fadeOut(function() {
				row.remove();
			});
		}

		$.ajax({
			type: "DELETE",
			dataType : 'json',
			headers: { "X-Environment-Id" : App.environmentId },
			url: Fantoccini.baseUrl + 'applications/' + this.id + '/developers/' + developerId
		}).done(success);
	},

	onShow: function() {
		this.showCurrentDevelopers();
	}
});

//////

var DeveloperAppsLayout = Marionette.Layout.extend({
	template: 'developer-apps-form',

	events: {
		'click [data-trigger="add-applications"]' : 'showApplicationSearch',
		'click [data-trigger="cancel"]' : 'showCurrentApplications',
		'change :input[name="role"]': 'onSetRole',
		'click [data-trigger="delete"]' : 'onDelete'
	},

	regions: {
		body : '#appDevelopersBody'
	},

	showCurrentApplications: function(e) {
		if (e) e.preventDefault();

		var applications = new ApplicationCollection([], {
			relationship : 'developers',
			id : this.id
		});
		applications.fetch();

		var collectionView = new PaginatedCollectionView({
			filterView: Marionette.ItemView.extend({
				template: 'developer-apps-filter'
			}),
			collection: applications,
			resultsView : DeveloperAppCollectionView,
			emptyView : Fantoccini.MessageView.extend({
				message: 'There are currently no applications for this developer.'
			})
		});

		this.body.show(collectionView);
	},

	showApplicationSearch: function(e) {
		if (e) e.preventDefault();

		var applications = new ApplicationCollection();
		var developerId = this.id;

		var search = new PaginatedCollectionView({
			filterView: Marionette.ItemView.extend({
				template : 'developer-apps-search',

				events: {
					'submit form': 'onSubmit'
				},

				constructor: function(options) {
					Marionette.ItemView.prototype.constructor.apply(this, arguments);
					options = options || {};
					this.collection = options.collection || this.collection;
				},

				onSubmit: function(e) {
					e.preventDefault();
					var name = trimToNull($('#name').val());

					var filter = {};

					if (name != null) {
						filter['name'] = name;
					}

					filter.withRolesFor = developerId;

					this.collection.setFilter(filter);
				}
			}),
			collection: applications,
			resultsView : DeveloperAppSearchResultsView,
			emptyView : EmptyView.extend({
				singular: 'application',
				plural: 'applications'
			})
		});

		this.body.show(search);
	},

	onSetRole: function(e) {
		var me = $(e.currentTarget);
		var applicationId = me.attr('data-id');
		var role = trimToNull(me.val());
		var processing = me.parents('tr:first').find('.processing');

		processing.html('<i class="fa fa-spinner fa-lg fa-spin"></i><span class="sr-only">Processing</span>');

		var success = function() {
			processing.html('<i class="fa fa-check fa-lg"></i><span class="sr-only">Success</span>');
		}

		if (role != null) {
			$.ajax({
				type: "PUT",
				dataType : 'json',
				contentType : 'application/json; charset=UTF-8',
				data : JSON.stringify({ role : role }),
				headers: { "X-Environment-Id" : App.environmentId },
				url: Fantoccini.baseUrl + 'developers/' + this.id + '/applications/' + applicationId
			}).done(success);
		} else {
			$.ajax({
				type: "DELETE",
				dataType : 'json',
				headers: { "X-Environment-Id" : App.environmentId },
				url: Fantoccini.baseUrl + 'developers/' + this.id + '/applications/' + applicationId
			}).done(success);
		}
	},

	onDelete: function(e) {
		var me = $(e.currentTarget);
		var applicationId = me.attr('data-id');

		var success = function() {
			var row = me.parents('tr:first');
			row.fadeOut(function() {
				row.remove();
			});
		}

		$.ajax({
			type: "DELETE",
			dataType : 'json',
			headers: { "X-Environment-Id" : App.environmentId },
			url: Fantoccini.baseUrl + 'developers/' + this.id + '/applications/' + applicationId
		}).done(success);
	},

	onShow: function() {
		this.showCurrentApplications();
	}
});

//////

var ApplicationItemView = Marionette.ItemView.extend({
	template : 'applications-table-item',
	tagName : 'tr',
	events : {
		'click [data-trigger="manage-developers"]' : 'onShowAppDevelopers',
		'click [data-trigger="clients"]' : 'onShowClientList',
		'click [data-trigger="create-client"]' : 'onCreateEditClient',
		'click [data-trigger="edit-client"]' : 'onCreateEditClient'
	},

	onShowAppDevelopers: function(e) {
		e.preventDefault();
		var me = $(e.currentTarget);
		var id = me.attr('data-id');

		var view = new AppDevelopersLayout();
		view.id = id;
		App.modal.show(view);
		
		initModal($('#modal'), view.$el);
	},

	onShowClientList : function(e) {
		var me = $(e.currentTarget);
		var ul = me.next();
		ul.html('<li role="presentation" class="disabled"><a role="menuitem" tabindex="-1" href="#">Loading...</a></li>');

		var clients = new ClientCollection(null, {
			filterParams : { applicationId : me.attr('data-id') },
			perPage : 1000,
			expand : null
		});
		clients.fetch().done(function() {
			ul.empty();

			if (clients.length > 0) {
				clients.each(function(item) {
					var li = $('<li role="presentation" />');
					var a = $('<a role="menuitem" tabindex="-1" href="#" data-trigger="edit-client" />').attr('data-id', item.get('id')).text(item.get('label'));
					ul.append(li.append(a));
				});

				ul.append('<li role="presentation" class="divider"></li>');
			}

			ul.append('<li role="presentation"><a role="menuitem" tabindex="-1" href="#" data-trigger="create-client"><i class="glyphicon glyphicon-plus-sign"></i> Create</a></li>');
		});
	},

	onCreateEditClient : function(e) {
		e.preventDefault();
		var me = $(e.currentTarget);
		var id = me.attr('data-id');

		if (id) {
			var model = new ClientModel({id : id});

			model.fetch().done(_.bind(function() {
				var editor = new Editors.Client({
					template : 'clients-form',
					model : model
				});

				App.modal.show(editor);
			}, this));
		} else {
			var model = new ClientModel({ applicationId : me.closest('[data-id]').attr('data-id'), apiKey : '' });

			var editor = new Editors.Client({
				template : 'clients-form',
				model : model
			});

			App.modal.show(editor);	
		}
	}
});

var ApplicationCollectionView = Fantoccini.CollectionView.extend({
	itemView : ApplicationItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'applications-table',
	regionSelector : 'tbody'
});

var ClientEmptyView = EmptyView.extend({
	template: 'clients-empty'
});

var ClientFilterView = DynamicFilterView.extend({
	template: 'clients-filter'
})

var ClientItemView = Marionette.ItemView.extend({
	template : 'clients-table-item',
	tagName : 'tr'
});

var ClientCollectionView = Fantoccini.CollectionView.extend({
	itemView : ClientItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'clients-table',
	regionSelector : 'tbody'
});

var DeveloperItemView = Marionette.ItemView.extend({
	template : 'developers-table-item',
	tagName : 'tr',

	events : {
		'click [data-trigger="manage-applications"]' : 'onShowDeveloperApps'
	},

	onShowDeveloperApps: function(e) {
		e.preventDefault();
		var me = $(e.currentTarget);
		var id = me.attr('data-id');

		var view = new DeveloperAppsLayout();
		view.id = id;
		App.modal.show(view);
		
		initModal($('#modal'), view.$el);
	}
});

var DeveloperCollectionView = Fantoccini.CollectionView.extend({
	itemView : DeveloperItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'developers-table',
	regionSelector : 'tbody'
});

//////

var AppDeveloperItemView = Marionette.ItemView.extend({
	template : 'app-developers-table-item',
	tagName : 'tr'
});

var AppDeveloperCollectionView = Fantoccini.CollectionView.extend({
	itemView : AppDeveloperItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'app-developers-table',
	regionSelector : 'tbody'
});

var AppDeveloperSearchResultView = Marionette.ItemView.extend({
	template : 'app-developers-search-result',
	tagName : 'tr'
});

var AppDeveloperSearchResultsView = Fantoccini.CollectionView.extend({
	itemView : AppDeveloperSearchResultView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'app-developers-search-results',
	regionSelector : 'tbody'
});

//////

var DeveloperAppItemView = Marionette.ItemView.extend({
	template : 'developer-apps-table-item',
	tagName : 'tr'
});

var DeveloperAppCollectionView = Fantoccini.CollectionView.extend({
	itemView : DeveloperAppItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'developer-apps-table',
	regionSelector : 'tbody'
});

var DeveloperAppSearchResultView = Marionette.ItemView.extend({
	template : 'developer-apps-search-result',
	tagName : 'tr'
});

var DeveloperAppSearchResultsView = Fantoccini.CollectionView.extend({
	itemView : DeveloperAppSearchResultView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'developer-apps-search-results',
	regionSelector : 'tbody'
});