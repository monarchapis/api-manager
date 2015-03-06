var Entities = {
	Application : {
		entity : 'application',
		collection : 'applications',
		displayName : 'application'
	},
	Client : {
		entity : 'client',
		collection : 'clients',
		expand : 'application',
		displayName : 'client'
	},
	Developer : {
		entity : 'developer',
		collection : 'developers',
		displayName : 'developer'
	},
	Service : {
		entity : 'service',
		collection : 'services',
		displayName : 'service'
	},
	Plan : {
		entity : 'plan',
		collection : 'plans',
		displayName : 'plan'
	},
	Permission : {
		entity : 'permission',
		collection : 'permissions',
		expand : 'message',
		displayName : 'permission'
	},
	Message : {
		entity : 'message',
		collection : 'messages',
		displayName : 'message'
	},
	Provider : {
		entity : 'provider',
		collection : 'providers',
		displayName : 'provder'
	},
	Environment : {
		entity : 'environment',
		collection : 'environments',
		displayName : 'environment'
	},
	User : {
		entity : 'user',
		collection : 'users',
		displayName : 'user'
	},
	Role : {
		entity : 'role',
		collection : 'roles',
		displayName : 'role'
	},
	LogEntry : {
		entity : 'logEntry',
		collection : 'logEntries',
		displayName : 'log entry'
	},
	PrincipalProfile : {
		entity : 'principalProfile',
		collection : 'principalProfiles',
		displayName : 'principal profiles'
	},
	PrincipalClaims : {
		entity : 'principalClaims',
		collection : 'principalClaims',
		displayName : 'principal claims'
	}
}

for (var prefix in Entities) {
	var options = $.extend({}, Entities[prefix]);

	var model = Fantoccini.Model.extend(options);
	options.model = model;
	window[prefix + 'Model'] = model;

	var collection = Fantoccini.PaginatedCollection.extend(options);
	window[prefix + 'Collection'] = collection;
}