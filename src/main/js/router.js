var Router = Marionette.AppRouter.extend({   
	appRoutes: {
		// Application routes
		"partners": "partners",
		"apis": "apis",
		"access": "access",
		"analytics": "analytics",
		"logs": "logs",
		"*default":	"dashboard"
	},

	// Our controller to handle the routes
	controller: Controllers
});