var DashboadLayout = Marionette.Layout.extend({
	template: 'dashboard',

	regions: {
		filter: '#dashboard-filter',
		activity: '#activity',
		recentLogEntries: '#recentLogEntries',
		topApplications: '#topApplications',
		topServices: '#topServices',
		topOperations: '#topOperations',
		errorBreakdown: '#errorBreakdown'
	}
});

var DashboardFilterView = Marionette.ItemView.extend({
	template: 'dashboard-filter',

	startBack: {
		second : 60,
		second10 : 600,
		minute: 3600,
		minute5: 18000,
		minute15: 54000,
		minute30: 108000,
		hour: 86400,
		day: 2592000
	},

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.call(this);
		this.unit = options.unit;
		this.vent = options.vent;
	},

	events: {
		'click div.btn-group button' : 'onSelectUnit'
	},

	onSelectUnit : function(e) {
		var me = $(e.currentTarget);
		me.parent().find('.active').removeClass('active');
		me.addClass('active');

		this.unit = me.attr('data-unit');
		this.update(true);
	},

	update: function(scollTo) {
		this.setupParameters();
		this.vent.trigger('filterChanged', {
			unit: this.unit,
			start: this.start,
			scollTo: scollTo
		});
	},

	setupParameters: function() {
		var d = new Date();
		var end = Math.floor(d.getTime() / 1000);
		var start;

		if (this.unit == 'month') {
			var m = moment(d).subtract('months', 12);
			start = Math.floor(m.unix());
		} else {
			start = end - this.startBack[this.unit];
		}

		this.start = start;
	}
});

var RecentLogEntryView = Marionette.ItemView.extend({
	template : 'logEntries-table-item',
	tagName : 'tr'
});

var RecentLogEntriesView = Fantoccini.CollectionView.extend({
	itemView : RecentLogEntryView,
	filterView : NullView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	emptyView : Fantoccini.MessageView.extend({
		message : 'There are currently no log entries.'
	}),
	regionTemplate : 'recentLogEntries',
	regionSelector : 'tbody'
});