Editors.Plan = Editors.Generic.extend({
	units: ['minute', 'hour','day', 'month'],

	addBindModels: function(models) {
		var quotas = this.model.get('quotas');

		if (!quotas) {
			quotas = {};
			this.model.set('quotas', quotas);
		}

		var limits = {};

		_.each(quotas, function(quota) {
			limits[quota.timeUnit] = quota.requestCount;
		});

		var instance = new Backbone.Model(limits);

		models['limits'] = instance;

		instance.on('change', _.bind(function() {
			var quotas = [];
			var limits = instance.toJSON();

			_.each(this.units, function(item) {
				if (limits[item]) {
					quotas.push({ requestCount : limits[item], timeUnit : item });
				}
			});

			this.model.set('quotas', quotas);
		}, this));
	}
});