var AccessLayout = Marionette.Layout.extend({
	template: 'access',

	regions: {
		tabs : '#tabs'
	}
});

var EnvironmentItemView = Marionette.ItemView.extend({
	template : 'environments-table-item',
	tagName : 'tr'
});

var EnvironmentCollectionView = Fantoccini.CollectionView.extend({
	itemView : EnvironmentItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'environments-table',
	regionSelector : 'tbody'
});

var ProviderItemView = Marionette.ItemView.extend({
	template : 'providers-table-item',
	tagName : 'tr'
});

var ProviderCollectionView = Fantoccini.CollectionView.extend({
	itemView : ProviderItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'providers-table',
	regionSelector : 'tbody'
});

var UserItemView = Marionette.ItemView.extend({
	template : 'users-table-item',
	tagName : 'tr'
});

var UserCollectionView = Fantoccini.CollectionView.extend({
	itemView : UserItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'users-table',
	regionSelector : 'tbody'
});

var RoleItemView = Marionette.ItemView.extend({
	template : 'roles-table-item',
	tagName : 'tr'
});

var RoleCollectionView = Fantoccini.CollectionView.extend({
	itemView : RoleItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'roles-table',
	regionSelector : 'tbody'
});

var PrincipalClaimsItemView = Marionette.ItemView.extend({
	template : 'principalClaims-table-item',
	tagName : 'tr'
});

var PrincipalClaimsCollectionView = Fantoccini.CollectionView.extend({
	itemView : PrincipalClaimsItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'principalClaims-table',
	regionSelector : 'tbody'
});

var PrincipalFilterView = Marionette.ItemView.extend({
	template: 'principalClaims-filter',

	lastProfileId: '',
	lastName: '',

	events: {
		'change input[name="filter"]': 'changedFilter',
		'keyup input[name="filter"]': 'setTimeout',
		'change select[name="profileId"]' : 'changedFilter',
		'click button[data-trigger="newProfile"]' : 'newProfile',
		'click button[data-trigger="editProfile"]' : 'editProfile'
	},

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.apply(this, arguments);
		options = options || {};
		this.collection = options.collection || this.collection;

		this.listenTo(
				App.vent,
				'principalProfiles:reload',
				_.bind(function() {
					this.loadProfiles();
				}, this),
				this.collection);
	},

	mixinTemplateHelpers: function(data) {
		return _.extend(data, {
			singular: this.singular,
			plural: this.plural,
			label: this.label
		});
	},

	onShow : function() {
		this.loadProfiles();
	},

	loadProfiles: function() {
		$.getJSON(serviceBaseUrl + 'management/v1/principalProfiles?limit=1000').done(_.bind(function(data) {
			var profileIds = $('select[name="profileId"]', this.$el).empty();

			$('<option />').attr('value', '').text('Select a profile').appendTo(profileIds);
			_.each(data.items, function(profile) {
				var model = new PrincipalProfileModel(profile);
				var option = $('<option />').attr('value', profile.id).text(profile.name).data('model', model).appendTo(profileIds);
				if (Editors.PrincipalClaims.profileId == profile.id) {
					option.prop('selected', true);
				}
			});
		}, this));
	},

	changedFilter: function(e) {
		this.timeout = null;
		var profileId = $('select[name="profileId"]', this.$el).val();
		var name = $('input[name="filter"]', this.$el).val();
		var create = $('button[data-trigger="create"]', this.$el);
		var edit = $('button[data-trigger="editProfile"]', this.$el);

		if (profileId == '') {
			create.hide();
			edit.hide();
			this.lastProfileId = profileId;
			this.collection._reset();
			this.collection.trigger("reset");
			return;
		}

		create.show();
		edit.show();

		Editors.PrincipalClaims.profileId = profileId;
		
		var filter = {
			profileId : profileId
		};

		if (name.length > 0) {
			filter['name'] = name + '*';
		}

		if (name != this.lastValue || profileId != this.lastProfileId) {
			this.lastProfileId = profileId;
			this.lastValue = name;
			this.collection.setFilter(filter);
		}
	},

	setTimeout: function(e) {
		if (this.timeout) window.clearTimeout(this.timeout);
		this.timeout = window.setTimeout(_.bind(function() {
			this.changedFilter(e);
		}, this), 500);
	},

	newProfile: function(e) {
		e.preventDefault();

		var model = new PrincipalProfileModel();
		var editor = new Editors.Generic({
			template : model.collection + '-form',
			model : model
		});

		App.modal.show(editor);
	},

	editProfile: function(e) {
		e.preventDefault();

		var edit = $('select[name="profileId"]', this.$el);
		var model = $('option:selected', edit).data('model');
		var editor = new Editors.Generic({
			template : model.collection + '-form',
			model : model
		});

		App.modal.show(editor);
	}
});

var PrincipalEmptyView = Marionette.ItemView.extend({
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
			filtered: this.collection.filterParams["name"] != null,
			singular: this.singular,
			plural: this.plural
		}
	}
});