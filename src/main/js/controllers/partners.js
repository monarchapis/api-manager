Controllers.partners = function() {
	App.setActiveTab('partners');

	var applications = new ApplicationCollection();
	var clients = new ClientCollection();
	var developers = new DeveloperCollection();

	var layout = new PartnersLayout();
	App.main.show(layout);

	var tabs = new LazyLoadingTabView({
		tabs : {
			applications : {
				text : 'Applications',
				view : EditorCollectionView,
				options : {
					formView : Editors.Application,
					collection : applications,
					singular: 'application',
					plural: 'applications',
					label: 'Filter by name',
					property : 'name',
					resultsView : ApplicationCollectionView,
					emptyView : EmptyView.extend({
						singular: 'application',
						plural: 'applications'
					})
				}
			},
			clients : {
				text : 'Clients',
				view : EditorCollectionView,
				options : {
					formView : Editors.Client,
					collection : clients,
					filterView: ClientFilterView,
					resultsView : ClientCollectionView,
					emptyView : ClientEmptyView
				}	
			},
			developers : {
				text : 'Developers',
				view : AccountEditorCollectionView,
				options : {
					formView : Editors.Developer,
					collection : developers,
					singular: 'developer',
					plural: 'developers',
					label: 'Filter by user name',
					property : 'username',
					resultsView : DeveloperCollectionView,
					emptyView : EmptyView.extend({
						singular: 'developer',
						plural: 'developers'
					})
				}	
			}
		}
	});

	layout.tabs.show(tabs);
}