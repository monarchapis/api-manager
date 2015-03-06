var Custom = {};

Custom.LayoutWithTabs = Marionette.Layout.extend({
	onShow: function() {
		$('.nav-tabs a:first', this.$el).tab('show');
	}
});

Custom.PaginatedCollection = Backbone.Collection.extend({
	initialize: function() {
		_.bindAll(this, 'parse', 'url', 'pageInfo', 'nextPage', 'previousPage');
		typeof(options) != 'undefined' || (options = {});
		this.page = 1;
		typeof(this.perPage) != 'undefined' || (this.perPage = 10);
	},
	fetch: function(options) {
		typeof(options) != 'undefined' || (options = {});
		this.trigger("fetching");
		var self = this;
		var success = options.success;
		options.success = function(resp) {
			self.trigger("fetched");
			if(success) { success(self, resp); }
		};
		return Backbone.Collection.prototype.fetch.call(this, options);
	},
	parse: function(resp) {
		this.page = (resp.offset / resp.limit) + 1;
		this.perPage = resp.limit;
		this.total = resp.total;
		return resp.items;
	},
	url: function() {
		return this.baseUrl + this.querystring();
	},
	querystring: function() {
		return '?' + $.param({offset: (this.page - 1) * this.perPage, limit: this.perPage});
	},
	pageInfo: function() {
		var info = {
			total: this.total,
			page: this.page,
			perPage: this.perPage,
			pages: Math.ceil(this.total / this.perPage),
			prev: false,
			next: false
		};

		var max = Math.min(this.total, this.page * this.perPage);

		if (this.total == this.pages * this.perPage) {
			max = this.total;
		}

		info.range = [(this.page - 1) * this.perPage + 1, max];

		if (this.page > 1) {
			info.prev = this.page - 1;
		}

		if (this.page < info.pages) {
			info.next = this.page + 1;
		}

		return info;
	},
	setPage: function(page) {
		this.page = page;
		return this.fetch();
	},
	nextPage: function() {
		if (!this.pageInfo().next) {
			return false;
		}
		this.page = this.page + 1;
		return this.fetch();
	},
	previousPage: function() {
		if (!this.pageInfo().prev) {
			return false;
		}
		this.page = this.page - 1;
		return this.fetch();
	}
});