Editors.Permission = Editors.Generic.extend({
	events: {
		'change section[name="permissions"] input[type="checkbox"]' : 'onPermissionChange',
		'change section[name="authenticators"] input[type="checkbox"]' : 'onAuthenticatorChange'
	},

	load: function() {
		var messages = new MessageCollection(null, { limit : 1000 });
		return messages.fetch().done(_.bind(function() {
			this.addData({ messages : messages.toJSON() });
		}, this)).promise();
	}
});