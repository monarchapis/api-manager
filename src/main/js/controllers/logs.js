Controllers.logs = function() {
	App.setActiveTab('logs');

	var logEntries = new LogEntryCollection();

	var layout = new LogsLayout();
	App.main.show(layout);

	var view = new PaginatedCollectionView({
		collection: logEntries,
		filterView: NullView,
		resultsView: LogEntryCollectionView,
		emptyView: Fantoccini.MessageView.extend({
			message: 'There are currently no log entries.'
		})
	});

	logEntries.fetch();

	layout.tabs.show(view);
}