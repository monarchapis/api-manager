window.Fantoccini = {};

Fantoccini.baseUrl = '';

(function(Marionette) {
	Marionette.TemplateCache.prototype.load = function() {
		if (this.compiledTemplate) {
			return this.compiledTemplate;
		}

		var path = this.templateId;

		if (window.JST && window.JST[path]) {
			this.compiledTemplate = window.JST[path];
		} else {
			throw "Error loading " + this.templateId;
		}

		return this.compiledTemplate;
	};
}(Marionette));

(function(Fantoccini) {

var defaultModelRootExpand;
var defaultModelExpand;
var defaultCollectionExpand;
var globalEventAggregator;

var globalTemplateHelpers = [];

Fantoccini.addGlobalTemplateHelper = function(helper) {
	globalTemplateHelpers.push(helper);
};

Fantoccini.setDefaultModelRootExpand = function(expand) {
	defaultModelRootExpand = expand;
};

Fantoccini.setDefaultModelExpand = function(expand) {
	defaultModelExpand = expand;
};

Fantoccini.setDefaultCollectionExpand = function(expand) {
	defaultCollectionExpand = expand;
};

Fantoccini.setGlobalEventAggregator = function(vent) {
	globalEventAggregator = vent;
};

Fantoccini.Model = Backbone.Model.extend({
	constructor: function(options) {
		Backbone.Model.prototype.constructor.apply(this, arguments);
		_.bindAll(this, 'urlRoot');
		typeof(options) != 'undefined' || (options = {});
		_.extend(this, options);
		this.state = 'new';
		typeof(this.baseUrl) != 'undefined' || (this.baseUrl = Fantoccini.baseUrl);
	},

	fetch: function(options) {
		typeof(options) != 'undefined' || (options = {});
		this.state = 'fetching';
		this.trigger("fetching");
		var self = this;
		var success = options.success;
		var error = options.error;
		options.success = function(resp) {
			self.state = 'fetched';
			self.trigger("fetched");

			if (success) { success(self, resp); }
		};
		options.error = function(resp) {
			if (self.xhr.status == 401) {
				self.state = 'denied';
				self.trigger("denied");
			} else {
				self.state = 'error';
				self.trigger("error");
			}

			if (error) { error(self, resp); }
		}
		return Backbone.Model.prototype.fetch.call(this, options);
	},

	urlRoot: function() {
		var url = this.baseUrl + '/' + this.collection;
		var expand = this.expand || defaultModelRootExpand;

		if (expand) {
			url += '?expand=' + _.escape(expand);
		}
		
		return url;
	},

	url: function() {
		var url = this.baseUrl;
		if (url.substring(url.length - 1) != '/') url += '/';

		url += this.collection;
		if (this.id) url += '/' + this.id;
		var expand = this.expand || defaultModelExpand;

		if (expand) {
			url += '?expand=' + _.escape(expand);
		}
		
		return url;
	},

	get: function(attr) {
		var value = Backbone.Model.prototype.get.call(this, attr);
		return _.isFunction(value) ? value.call(this) : value;
	},

	set: function(attr, value) {
		var func = Backbone.Model.prototype.get.call(this, attr);
		return _.isFunction(func) ?
			func.call(this, value) :
			Backbone.Model.prototype.set.apply(this, arguments);
	},

	toJSON: function() {
		var json = Backbone.Model.prototype.toJSON.apply(this, arguments);
		_.each(json, function (value, key) {
			if (_.isFunction(value)) {
				delete json[key];
			}
		});
		return json;
	},

	relationshipFor: function(collection, expand) {
		return new collection([], {
			collection: collection.prototype.collection,
			id: this.id,
			relationship: this.collection,
			expand: expand
		});
	},

	associationFor: function(association, target, property, expand, orderBy) {
		return new association([], {
			collection: target.prototype.collection,
			id: this.id,
			association: association.prototype.collection,
			source: this.collection,
			property: property,
			expand: expand,
			orderBy: orderBy
		});
	},

	checkForReload: function(id) {
		//console.log("Model checkForReload: " + id);

		if (this.id == id) {
			this.fetch();
		}
	}
});

Fantoccini.PaginatedCollection = Backbone.Collection.extend({
	constructor: function(models, options) {
		Backbone.Collection.prototype.constructor.apply(this, arguments);
		_.bindAll(this, 'parse', 'url', 'setPage', 'nextPage', 'previousPage',
			'setPerPage', 'setFilter', 'setExpand', 'setOrderBy');
		typeof(options) != 'undefined' || (options = {});
		_.extend(this, options);
		this.state = 'new';
		this.page = 1;
		typeof(this.perPage) != 'undefined' || (this.perPage = 10);
		typeof(this.baseUrl) != 'undefined' || (this.baseUrl = Fantoccini.baseUrl);
	},

	_reset: function() {
		this.state = 'new';
		this.page = 1;
		Backbone.Collection.prototype._reset.apply(this, arguments);
	},

	fetch: function(options) {
		typeof(options) != 'undefined' || (options = {});
		if (this.state == 'fetching') return this.xhr;
		this.state = 'fetching';
		this.trigger("fetching");

		var self = this;
		var success = options.success;
		var error = options.error;

		options.success = function(resp) {
			self.state = 'fetched';
			self.trigger("fetched");
			
			if(success) { success(self, resp); }
		};
		options.error = function(resp) {
			if (self.xhr.status == 401) {
				self.state = 'denied';
				self.trigger("denied");
			} else {
				self.state = 'error';
				self.trigger("error");
			}

			if(error) { error(self, resp); }
		}

		Backbone.Collection.prototype._reset.call(this);
		this.xhr = Backbone.Collection.prototype.fetch.call(this, options);
		return this.xhr;
	},

	fetchIfNeeded: function() {
		if (this.state != 'fetching' && this.state != 'fetched') {
			return this.fetch();
		}
	},

	parse: function(resp) {
		if (resp.items) {
			this.page = (resp.offset / resp.limit) + 1;
			this.perPage = resp.limit;
			this.total = resp.total;
			this.pages = Math.ceil(this.total / this.perPage);

			if (this.property) {
				return _.map(resp.items, _.bind(function(item) {
					var entity = item[this.property];
					delete item[this.property];
					entity._assoc = item;
					return entity;
				}, this));
			} else {
				return resp.items;
			}
		} else {
			delete this.total;
			delete this.pages;
			return resp;
		}
	},

	url: function() {
		var url = this.baseUrl;
		if (url.substring(url.length - 1) != '/') url += '/';

		if (this.id && this.relationship) {
			url += this.relationship + '/' + this.id + '/' + this.collection;
		} else if (this.id && this.association && this.source) {
			url += this.source + '/' + this.id + '/' + this.association;
		} else {
			url += this.collection;
		}

		return url + this.querystring();
	},

	querystring: function() {
		var qs = '?' + $.param({offset: (this.page - 1) * this.perPage, limit: this.perPage});

		if (this.filterParams && !_.isEmpty(this.filterParams)) {
			qs += '&' + $.param(this.filterParams);
		}

		var expand = this.expand || defaultCollectionExpand;
		if (expand) {
			qs += '&' + $.param({expand: expand});
		}

		if (this.orderBy) {
			qs += '&' + $.param({orderBy: this.orderBy});
		}

		return qs;
	},

	setPage: function(page) {
		if (page < 1) {
			return false;
		}

		if (this.pages && page > this.pages) {
			return false;
		}

		if (page == this.page) {
			return false;
		}

		this.page = page;
		return this.fetch();
	},

	nextPage: function() {
		return this.setPage(this.page + 1);
	},

	previousPage: function() {
		return this.setPage(this.page - 1);
	},

	setPerPage: function(perPage, fetch) {
		typeof(fetch) != 'undefined' || (fetch = true);
		if (perPage < 1) {
			return false;
		}

		this.perPage = perPage;
		this.page = 1;
		return fetch ? this.fetch() : this;
	},

	setFilter: function(filter, fetch) {
		typeof(fetch) != 'undefined' || (fetch = true);
		this.filterParams = filter;
		this.page = 1;
		return fetch ? this.fetch() : this;
	},

	setExpand: function(expand, fetch) {
		typeof(fetch) != 'undefined' || (fetch = true);
		this.expand = expand;
		return fetch ? this.fetch() : this;
	},

	setOrderBy: function(orderBy, fetch) {
		typeof(fetch) != 'undefined' || (fetch = true);
		this.orderBy = orderBy;
		this.page = 1;
		return fetch ? this.fetch() : this;
	},

	clone: function() {
		var clone = Backbone.Collection.prototype.clone.apply(this, arguments);

		clone.state = this.state;
		clone.collection = this.collection;
		clone.id = this.id;
		clone.relationship = this.relationship;
		clone.page = this.page;
		clone.perPage = this.perPage;
		clone.baseUrl = this.baseUrl;
		clone.filterParams = this.filterParams;
		clone.expand = this.expand;
		clone.orderBy = this.orderBy;
		clone.total = this.total;
		clone.pages = this.pages;

		return clone;
	},

	checkForReload: function(id) {
		//console.log("Collection checkForReload: " + id);

		if (!id || this.get({id: id})) {
			this.fetch();
		}
	}
});

var itemViewOptions = ['loadingEmpty', 'loadingTemplate'];

Fantoccini.ItemView = Marionette.ItemView.extend({
	loadingEmpty: false,

	loadingTemplate: "loading",

	modelEvents: {
		"fetching": "render",
		"fetched": "render",
		"denied": "render",
		"error": "render",
		"reset": "render"
	},

	collectionEvents: {
		"fetching": "render",
		"fetched": "render",
		"denied": "render",
		"error": "render",
		"reset": "render"
	},

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.apply(this, arguments);

		if (globalEventAggregator) {
			if (this.model) {
				this.listenTo(
					globalEventAggregator,
					this.model.collection + ':reload',
					_.bind(this.model.checkForReload, this.model),
					this.model);
			}
			if (this.collection) {
				this.listenTo(
					globalEventAggregator,
					(this.collection.relationship || this.collection.collection) + ':reload',
					_.bind(this.collection.checkForReload, this.collection),
					this.collection);
			}
		}
	},

	_configure: function(options) {
		if (this.options) options = _.extend({}, _.result(this, 'options'), options);
		_.extend(this, _.pick(options, itemViewOptions));
		Marionette.ItemView.prototype._configure.apply(this, arguments);
	},

	// Internal method to trigger the before render callbacks
	// and events
	triggerBeforeRender: function() {
		this.triggerMethod("before:render", this);
		this.triggerMethod("item:before:render", this);
	},

	// Internal method to trigger the rendered callbacks and
	// events
	triggerRendered: function() {
		this.triggerMethod("render", this);
		this.triggerMethod("item:rendered", this);
	},

	_calculateState: function(state) {
		var args = Array.prototype.slice.call(arguments, 1);

		_.each(args, function(collection) {
			var next = (collection && collection.state);

			if (!next) {
				return;
			} else if (!state) {
				state = next;
			} else if (next == 'error') {
				state = 'error';
				return false;
			} else if (state != 'fetching' && next == 'fetching') {
				state = 'fetching';
			}
		});

		return state;
	},

	getState: function() {
		return this._calculateState(undefined, this.model, this.collection);
	},

	render: function() {
		this.isClosed = false;
		this.triggerBeforeRender();

		var state = this.getState();

		if (state == 'fetched') {
			//if (this.collection && this.collection.length == 0) {
			//	this.$el.empty();
			//} else {
				this.renderItem();
			//}
		} else if (state == 'fetching') {
			this.renderLoading();
		} else {
			this.$el.empty();
		}

		this.triggerRendered();

		return this;
	},

	getRenderData: function() {
		var data = this.serializeData();
		data = this.mixinTemplateHelpers(data);
		return data;
	},

	mixinTemplateHelpers: function(data) {
		data = Marionette.ItemView.prototype.mixinTemplateHelpers.call(this, data);

		_.extend(data, {
			'_collection': this.model && this.model.collection
		});

		_.each(globalTemplateHelpers, _.bind(function(helper) {
			data = helper.call(this, data);
		}, this));

		return data;
	},

	renderItem: function() {
		var data = this.getRenderData();
		var template = this.getTemplate();
		var html = Marionette.Renderer.render(template, data);

		this.$el.html(html);
		this.bindUIElements();
	},

	renderLoading: function() {
		if (this.loadingEmpty) {
			this.$el.empty();
		} else {
			this.$el.html(Marionette.Renderer.render(this.loadingTemplate, {}));
		}
	}
});

var expandedItemViewOptions = ['relationships'];

Fantoccini.ExpandedItemView = Fantoccini.ItemView.extend({
	relationships : {},

	constructor: function(options) {
		Fantoccini.ItemView.prototype.constructor.apply(this, arguments);

		_.each(this.relationships, _.bind(function(data) {
			this.listenTo(data, "fetching", this.render, this);
			this.listenTo(data, "fetched", this.render, this);

			if (globalEventAggregator) {
				this.listenTo(
					globalEventAggregator,
					data.collection + ':reload',
					_.bind(data.checkForReload, data),
					data);
			}
		}, this));
	},

	_configure: function(options) {
		if (this.options) options = _.extend({}, _.result(this, 'options'), options);
		_.extend(this, _.pick(options, expandedItemViewOptions));
		Fantoccini.ItemView.prototype._configure.apply(this, arguments);
	},

	getState: function() {
		var state = Fantoccini.ItemView.prototype.getState.call(this);

		var args = _.map(this.relationships, function(item) { return item; });
		args.unshift(state);
		state = this._calculateState.apply(this, args);

		return state;
	},

	mixinTemplateHelpers: function(data) {
		data = Fantoccini.ItemView.prototype.mixinTemplateHelpers.call(this, data);
		_.each(this.relationships, function(object, relationship) {
			data[relationship] = object.toJSON();
		});

		return data;
	}
});

var collectionViewOptions = [
	'template', 'region', 'regionTemplate', 'regionSelector', 'regionTagName',
	'regionId', 'regionClassName', 'regionAttributes', 'regionModifier', 'appendEffect'];

Fantoccini.CollectionView = Marionette.CollectionView.extend({
	constructor: function(options) {
		Marionette.CollectionView.prototype.constructor.apply(this, arguments);

		if (globalEventAggregator && this.collection) {
			this.listenTo(
				globalEventAggregator,
				(this.collection.relationship || this.collection.collection) + ':reload',
				_.bind(this.collection.checkForReload, this.collection),
				this.collection);
		}
	},

	_configure: function(options) {
		if (this.options) options = _.extend({}, _.result(this, 'options'), options);
		_.extend(this, _.pick(options, collectionViewOptions));
		Marionette.CollectionView.prototype._configure.apply(this, arguments);
	},

	_initialEvents: function() {
		Marionette.CollectionView.prototype._initialEvents.call(this);

		if (this.collection) {
			this.listenTo(this.collection, "fetching", this.render, this);
			this.listenTo(this.collection, "fetched", this.afterFetched, this);
			this.listenTo(this.collection, "denied", this.afterFetched, this);
			this.listenTo(this.collection, "error", this.afterFetched, this);
			this.listenTo(this.collection, "reset", this.render, this);
			this.listenTo(this.collection, "remove", this.render, this);
			this.listenTo(this, "itemview:item:closed", this.onItemViewClosed, this);
		}
	},

	// Handle a child item added to the collection
	addChildView: function(item, collection, options){
		this.closeEmptyView();
		this._createListEl();

		var ItemView = this.getItemView(item);
		var index = this.collection.indexOf(item);
		this.addItemView(item, ItemView, index);
	},

	onAfterItemAdded: function(view) {
		if (!this.rendering && this.collection.state == 'fetched' && this.appendEffect) {
			if (this.appendEffect == 'slideDown') {
				$(view.el).hide().slideDown();
			} else if (this.appendEffect == 'fadeIn') {
				$(view.el).hide().fadeIn();
			}
		}
	},

	onItemViewClosed: function(itemView) {
		this.collection.remove(itemView.model);
	},

	itemViewOptions: function(item, index) {
		return {
			collection: this.collection,
			templateHelpers: this.getTemplateHelpers(index)
		};
	},

	getTemplateHelpers: function(index) {
		var data = {
			'_index': index,
			'_collection': this.collection.collection,
			'_association': this.collection.association,
			'_source': this.collection.source
		};

		_.each(globalTemplateHelpers, _.bind(function(helper) {
			data = helper.call(this, data);
		}, this));

		return data;
	},

	afterFetched: function() {
		this.closeLoadingView();
		this.closeDeniedView();
		this.closeErrorView();
		this.checkEmpty();
	},

	_renderChildren: function() {
		this.rendering = true;
		this.closeLoadingView();
		this.closeDeniedView();
		this.closeErrorView();
		this.closeEmptyView();
		this.closeChildren();

		this._createRegionEl();

		if (this.collection) {
			if (this.collection.state == 'fetching') {
				this.showLoadingView();
			} else if (this.collection.state == 'denied') {
				this.showDeniedView();
			} else if (this.collection.state == 'error') {
				this.showErrorView();
			} else if (this.collection.state == 'blank') {
			} else if (this.collection.state == 'fetched' && this.collection.length > 0) {
				this.showCollection();
			} else if (this.collection.state == 'new') {
				this._removeListEl();
			} else {
				this.showEmptyView();
			}
		}

		delete this.rendering;
	},

	_createRegionEl: function() {
		if (!this.regionEl && this.template && this.region) {
			var html = Marionette.Renderer.render(this.template, this.getTemplateHelpers());
			this.$el.html(html);
			this.regionEl = $(this.region, this.$el);
		}
	},

	_createListEl: function() {
		if (!this.listEl) {
			if (this.regionTemplate && this.regionSelector) {
				var html = Marionette.Renderer.render(this.regionTemplate, this.getTemplateHelpers());
				(this.regionEl || this.$el).html(html);
				this.listEl = $(this.regionSelector, this.regionEl || this.$el);
			} else if (this.regionTagName) {
				this._ensureListElement();
				this.listEl.appendTo(this.regionEl || this.$el);
			}
		}
	},

	_ensureListElement: function() {
		if (!this.listEl) {
			var attrs = _.extend({}, _.result(this, 'regionAttributes'));
			if (this.regionId) attrs.id = _.result(this, 'regionId');
			if (this.regionClassName) attrs['class'] = _.result(this, 'regionClassName');
			this.listEl = $('<' + _.result(this, 'regionTagName') + '>').attr(attrs);
			if (_.isFunction(this.regionModifier)) {
				this.regionModifier();
			}
		}
	},

	_removeListEl: function() {
		if (this.listEl) {
			this.listEl.remove();
			(this.regionEl || this.$el).empty();
			delete this.listEl;
		}
	},

	showCollection: function() {
		this._createListEl();
		Marionette.CollectionView.prototype.showCollection.apply(this, arguments);
	},

	// Internal method to show an empty view in place of
	// a collection of item views, when the collection is
	// empty
	showEmptyView: function() {
		this._removeListEl();
		var EmptyView = Marionette.getOption(this, "emptyView");
		if (EmptyView && !this._showingEmptyView) {
			this._showingEmptyView = true;
			var model = new Backbone.Model();
			this.addItemView(model, EmptyView, 0);
			this.ev = this.children.findByIndex(0);
		}
	},

	// Internal method to close an existing emptyView instance
	// if one exists. Called when a collection view has been
	// rendered empty, and then an item is added to the collection.
	closeEmptyView: function() {
		if (this._showingEmptyView) {
			this.closeChildren();
			this.removeChildView(this.ev);
			delete this.ev;
			delete this._showingEmptyView;
		}
	},

	// Internal method to show a loading view in place of
	// a collection of item views, when the collection is
	// being fetched
	showLoadingView: function() {
		this._removeListEl();
		var LoadingView = Marionette.getOption(this, "loadingView");
		if (LoadingView && !this._showingLoadingView) {
			this._showingLoadingView = true;
			var model = new Backbone.Model();
			this.addItemView(model, LoadingView, 0);
			this.lv = this.children.findByIndex(0);
		}
	},

	// Internal method to close an existing loadingView instance
	// if one exists. Called when a collection view has been
	// fetched.
	closeLoadingView: function() {
		if (this._showingLoadingView) {
			this.removeChildView(this.lv);
			delete this.lv;
			delete this._showingLoadingView;
		}
	},

	// Internal method to show an 'access denied' view in place of
	// a collection of item views, when the collection is
	// being fetched
	showDeniedView: function() {
		this._removeListEl();
		var DeniedView = Marionette.getOption(this, "deniedView");
		if (DeniedView && !this._showingDeniedView) {
			this._showingDeniedView = true;
			var model = new Backbone.Model();
			this.addItemView(model, DeniedView, 0);
			this.dv = this.children.findByIndex(0);
		}
	},

	// Internal method to close an existing deniedView instance
	// if one exists. Called when a collection view has been
	// fetched.
	closeDeniedView: function() {
		if (this._showingDeniedView) {
			this.removeChildView(this.dv);
			delete this.dv;
			delete this._showingDeniedView;
		}
	},

	// Internal method to show an error view in place of
	// a collection of item views, when the collection is
	// being fetched
	showErrorView: function() {
		this._removeListEl();
		var ErrorView = Marionette.getOption(this, "errorView");
		if (ErrorView && !this._showingErrorView) {
			this._showingErrorView = true;
			var model = new Backbone.Model();
			this.addItemView(model, ErrorView, 0);
			this.ev = this.children.findByIndex(0);
		}
	},

	// Internal method to close an existing errorView instance
	// if one exists. Called when a collection view has been
	// fetched.
	closeErrorView: function() {
		if (this._showingErrorView) {
			this.removeChildView(this.ev);
			delete this.ev;
			delete this._showingErrorView;
		}
	},

	// helper to show the empty view if the collection is empty
	checkEmpty: function() {
		// check if we're empty now, and if we are, show the
		// empty view
		if (!this.collection ||
			(this.collection.length === 0 && this.collection.state == 'fetched')) {
			this.showEmptyView();
		} else if (this.collection) {
			if (this.collection.state == 'denied') {
				this.showDeniedView();
			} else if (this.collection.state == 'error') {
				this.showErrorView();
			}
		}
	},

	appendHtml: function(collectionView, itemView, index) {
		var el = collectionView.listEl || collectionView.regionEl || collectionView.$el;
		el.append(itemView.el);
		/*if (this.collection.state == 'fetched' && this.appendEffect) {
			$(itemView.el).hide().slideDown();
		}*/
	}
});

Fantoccini.Layout = Marionette.Layout.extend({
	constructor: function(options) {
		options = options || {};

		this.regions = this.regions || options.regions || {};
		this.vent = this.vent || options.vent || new Backbone.Wreqr.EventAggregator();
		this.viewInstances = {};
		this.options = _.clone(options);		

		var regionsAndViews = options.regionsAndViews || this.regionsAndViews;
		var views = options.views || this.views;

		delete options.template;
		delete options.regionsAndViews;
		delete options.views;

		_.each(regionsAndViews, _.bind(function(item, name) {
			var region = this[name];
			this.regions[name] = item.selector;

			var o = _.extend({}, options, item.options);
			o.vent = this.vent;
			var view = item.type && item.type == 'function' ? item.view.call(this) : item.view;
			this.viewInstances[name] = new view(o);
		}, this));

		_.each(views, _.bind(function(item, name) {
			var o = _.extend({}, options, item.options);
			o.vent = this.vent;
			var view = item.type && item.type == 'function' ? item.view.call(this) : item.view;
			this.viewInstances[name] = new view(o);
		}, this));

		this._firstRender = true;
		this._initializeRegions(options);
	
		// Skipping the layout constructor
		Marionette.ItemView.prototype.constructor.call(this, options);
	},

	onShow: function() {
		_.each(this.viewInstances, _.bind(function(viewInstance, name) {
			this[name].show(viewInstance);
		}, this));
	}
});

Fantoccini.TabLayout = Marionette.Layout.extend({
	template: 'tabs',

	constructor: function(options) {
		Marionette.Layout.prototype.constructor.apply(this, arguments);
		options = options || {};

		this.options = options;
		this.tabs = options.tabs || this.tabs;
		this.maxHeight = options.maxHeight || this.maxHeight;
		this.onTabSelected = options.onTabSelected || this.onTabSelected;

		this.regions = this.regions || {};
		this.viewInstances = {};

		_.each(this.tabs, _.bind(function(tab, name) {
			this.initialTab = this.initialTab || name;
			var id = this.cid + '-' + name;
			this.regions[name] = '#' + id;

			if (tab.view) {
				var o = _.extend({}, options, tab.options);
				this.viewInstances[name] = new tab.view(o);
			}
		}, this));

		this.currentTab = this.initialTab;

		this._initializeRegions(options);
	},

	serializeData: function() {
		return {
			tabs: this.tabs,
			maxHeight: this.maxHeight,
			$cid: this.cid
		};
	},

	render: function() {
		Marionette.Layout.prototype.render.apply(this, arguments);

		$('.nav-tabs a[data-toggle="tab"]', this.$el).on('shown.bs.tab', _.bind(function(e) {
			var name = $(e.target).attr('data-name');

			if (!this[name].currentView) {
				this[name].show(this.viewInstances[name]);
			}

			this.triggerMethod('tab:selected', name);
		}, this));
	},

	onShow: function() {
		this[this.initialTab].show(this.viewInstances[this.initialTab]);
		this.triggerMethod('tab:selected', this.initialTab);
	},

	setTab: function(name) {
		this.currentTab = name;
		$('.nav-tabs a[href="#' + this.cid + '-' + name + '"]', this.$el).tab('show');	
	}
});

Fantoccini.MessageView = Marionette.ItemView.extend({
	template: 'message',

	message: 'No records were found.',

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.apply(this, arguments);
		options = options || {};
		this.message = options.message || this.message;
		this.tagName = options.tagName || this.tagName;
	},

	serializeData: function() {
		return {
			message: this.message
		}
	}
});

}(Fantoccini));