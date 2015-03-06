Controllers.dashboard = function() {
	App.setActiveTab('dashboard');
	var layout = new DashboadLayout();
	App.main.show(layout);

	var unit = 'hour';
	var vent = new Backbone.Wreqr.EventAggregator();
	var filter = new DashboardFilterView({
		unit: unit,
		vent: vent
	});

	layout.filter.show(filter);
	layout.activity.show(new GraphView({
		measure: 'status_code',
		unit: unit,
		vent: vent
	}));
	layout.topApplications.show(new DoughnutView({
		measure: 'application_id',
		measureType: 'values',
		unit: unit,
		vent: vent,
		getFilter: function(parameters) {
			return 'application_id.ne(null)';
		}
	}));
	layout.topServices.show(new DoughnutView({
		measure: 'service_id',
		unit: unit,
		vent: vent,
		getFilter: function(parameters) {
			return 'service_id.ne(null)';
		}
	}));

	filter.update();

	var logEntries = new LogEntryCollection();
	
	var logView = new RecentLogEntriesView({
		collection: logEntries
	});

	logEntries.fetch();

	layout.recentLogEntries.show(logView);
}