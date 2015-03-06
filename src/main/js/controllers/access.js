Controllers.access = function() {
	App.setActiveTab('access');

	var providers = new ProviderCollection();
	var users = new UserCollection(null, { expand : 'role' });
	var roles = new RoleCollection();
	var principalClaims = new PrincipalClaimsCollection();

	if (Editors.PrincipalClaims.profileId != null) {
		principalClaims.setFilter({ profileId : Editors.PrincipalClaims.profileId });
	} else {
		principalClaims.state = 'blank';	
	}

	var layout = new AccessLayout();
	App.main.show(layout);

	var tabs = new LazyLoadingTabView({
		tabs : {
			providers : {
				text : 'Providers',
				view : EditorCollectionView,
				options : {
					formView : Editors.Provider,
					collection : providers,
					singular: 'provider',
					plural: 'providers',
					label: 'Filter by label',
					property : 'label',
					resultsView : ProviderCollectionView,
					emptyView : EmptyView.extend({
						singular: 'provider',
						plural: 'providers'
					})
				}
			},
			users : {
				text : 'Users',
				view : AccountEditorCollectionView,
				options : {
					formView : Editors.User,
					collection : users,
					singular: 'user',
					plural: 'users',
					label: 'Filter by username',
					property : 'userName',
					resultsView : UserCollectionView,
					emptyView : EmptyView.extend({
						singular: 'user',
						plural: 'users'
					})
				}
			},
			roles : {
				text : 'Roles',
				view : EditorCollectionView,
				options : {
					formView : Editors.Role,
					collection : roles,
					singular: 'role',
					plural: 'roles',
					label: 'Filter by name',
					property : 'roleName',
					resultsView : RoleCollectionView,
					emptyView : EmptyView.extend({
						singular: 'role',
						plural: 'roles'
					})
				}
			},
			principals : {
				text : 'Principals',
				view : EditorCollectionView,
				options : {
					formView : Editors.PrincipalClaims,
					filterView : PrincipalFilterView,
					collection : principalClaims,
					singular: 'principal',
					plural: 'principals',
					property : 'name',
					resultsView : PrincipalClaimsCollectionView,
					emptyView : PrincipalEmptyView.extend({
						singular: 'principal',
						plural: 'principals'
					})
				}
			}
		}
	});

	layout.tabs.show(tabs);
}