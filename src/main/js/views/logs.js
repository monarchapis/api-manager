var LogsLayout = Marionette.Layout.extend({
	template: 'logs',

	regions: {
		tabs : '#tabs'
	}
});

var LogEntryItemView = Marionette.ItemView.extend({
	template : 'logEntries-table-item',
	tagName : 'tr'
});

var LogEntryCollectionView = Fantoccini.CollectionView.extend({
	itemView : LogEntryItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'logEntries-table',
	regionSelector : 'tbody'
});