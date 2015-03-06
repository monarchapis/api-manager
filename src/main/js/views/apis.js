var ApisLayout = Marionette.Layout.extend({
	template: 'apis',

	regions: {
		tabs : '#tabs'
	}
});

var ServiceItemView = Marionette.ItemView.extend({
	template : 'services-table-item',
	tagName : 'tr'
});

var ServiceCollectionView = Fantoccini.CollectionView.extend({
	itemView : ServiceItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'services-table',
	regionSelector : 'tbody'
});

var PlanItemView = Marionette.ItemView.extend({
	template : 'plans-table-item',
	tagName : 'tr'
});

var PlanCollectionView = Fantoccini.CollectionView.extend({
	itemView : PlanItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'plans-table',
	regionSelector : 'tbody'
});

var PermissionItemView = Marionette.ItemView.extend({
	template : 'permissions-table-item',
	tagName : 'tr'
});

var PermissionCollectionView = Fantoccini.CollectionView.extend({
	itemView : PermissionItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'permissions-table',
	regionSelector : 'tbody'
});

var MessageItemView = Marionette.ItemView.extend({
	template : 'messages-table-item',
	tagName : 'tr'
});

var MessageCollectionView = Fantoccini.CollectionView.extend({
	itemView : MessageItemView,
	emptyView : NoMatchesView,
	loadingView : LoadingView,
	deniedView : AccessDeniedView,
	errorView : ErrorView,
	regionTemplate : 'messages-table',
	regionSelector : 'tbody'
});