Editors.Application = Editors.Generic.extend({
	load: function() {
		var plans = new PlanCollection(null, { limit : 1000 });
		return plans.fetch().done(_.bind(function() {
			this.addData({ plans : plans.toJSON() });
		}, this)).promise();
	},

	addBindModels: function(models) {
		Editors.Traits.ExtendedProperties.addBindModels.apply(this, arguments);
	},

	reloadRelatedCollections : function(action) {
		App.vent.trigger('clients:reload');
	}
});