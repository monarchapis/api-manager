Controllers.apis = function() {
	App.setActiveTab('apis');

	var services = new ServiceCollection();
	var plans = new PlanCollection();
	var permissions = new PermissionCollection();
	var messages = new MessageCollection();

	var layout = new ApisLayout();
	App.main.show(layout);

	var tabs = new LazyLoadingTabView({
		tabs : {
			services : {
				text : 'Services',
				view : EditorCollectionView,
				options : {
					formView : Editors.Service,
					collection : services,
					singular: 'service',
					plural: 'services',
					label: 'Filter by name',
					property : 'name',
					resultsView : ServiceCollectionView,
					emptyView : EmptyView.extend({
						singular: 'service',
						plural: 'services'
					})
				}
			},
			plans : {
				text : 'Plans',
				view : EditorCollectionView,
				options : {
					formView : Editors.Plan,
					collection : plans,
					singular: 'plan',
					plural: 'plans',
					label: 'Filter by name',
					property : 'name',
					resultsView : PlanCollectionView,
					emptyView : EmptyView.extend({
						singular: 'plan',
						plural: 'plans'
					})
				}
			},
			permissions : {
				text : 'Permissions',
				view : EditorCollectionView,
				options : {
					formView : Editors.Permission,
					collection : permissions,
					singular: 'permission',
					plural: 'permissions',
					label: 'Filter by name',
					property : 'name',
					resultsView : PermissionCollectionView,
					emptyView : EmptyView.extend({
						singular: 'permission',
						plural: 'permissions'
					})
				}
			},
			messages : {
				text : 'Messages',
				view : EditorCollectionView,
				options : {
					formView : Editors.Message,
					collection : messages,
					singular: 'message',
					plural: 'messages',
					label: 'Filter by key',
					property : 'key',
					resultsView : MessageCollectionView,
					emptyView : EmptyView.extend({
						singular: 'message',
						plural: 'messages'
					})
				}	
			}
		}
	});

	layout.tabs.show(tabs);
}