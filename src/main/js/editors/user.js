Editors.User = Editors.Generic.extend({
	load: function() {
		var roles = new RoleCollection(null, { limit : 1000 });
		return roles.fetch().done(_.bind(function() {
			this.addData({ roles : roles.toJSON() });
		}, this)).promise();
	}
});