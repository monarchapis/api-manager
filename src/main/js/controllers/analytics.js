Controllers.analytics = function() {
	App.setActiveTab('analytics');
	var layout = new AnalyticsLayout();
	App.main.show(layout);

	var unit = 'hour';
	var vent = new Backbone.Wreqr.EventAggregator();
	var filter = new AnalyticsFilterView({
		unit: unit,
		vent: vent
	});

	layout.filter.show(filter);
	layout.activity.show(new GraphView({
		measure: 'status_code',
		unit: unit,
		vent: vent,
		getFilter: function(parameters) {
			var filter = '';

			if (parameters['client_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'client_id.eq("' + parameters['client_id'] + '")';
			} else if (parameters['application_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'application_id.eq("' + parameters['application_id'] + '")';
			}

			if (parameters['service_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'service_id.eq("' + parameters['service_id'] + '")';

				if (parameters['operation_name']) {
					if (filter.length > 0) { filter += " + "; }
					filter += 'operation_name.eq("' + parameters['operation_name'] + '")';
				}
			}

			return filter;
		}
	}));
	layout.topApplications.show(new DoughnutView({
		measure: 'application_id',
		measureType: 'values',
		unit: unit,
		vent: vent,
		getFilter: function(parameters) {
			var filter = 'application_id.ne(null)';

			if (parameters['service_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'service_id.eq("' + parameters['service_id'] + '")';

				if (parameters['operation_name']) {
					if (filter.length > 0) { filter += " + "; }
					filter += 'operation_name.eq("' + parameters['operation_name'] + '")';
				}
			}

			return filter;
		}
	}));
	layout.topServices.show(new DoughnutView({
		measure: 'service_id',
		unit: unit,
		vent: vent,
		getFilter: function(parameters) {
			var filter = 'service_id.ne(null)';

			if (parameters['client_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'client_id.eq("' + parameters['client_id'] + '")';
			} else if (parameters['application_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'application_id.eq("' + parameters['application_id'] + '")';
			}

			return filter;
		}
	}));

	layout.topOperations.show(new DoughnutView({
		measure: 'operation_name',
		unit: unit,
		vent: vent,
		getFilter: function(parameters) {
			var filter = 'operation_name.ne(null)';

			if (parameters['client_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'client_id.eq("' + parameters['client_id'] + '")';
			} else if (parameters['application_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'application_id.eq("' + parameters['application_id'] + '")';
			}

			if (parameters['service_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'service_id.eq("' + parameters['service_id'] + '")';
			}

			return filter;
		}
	}));

	layout.errorBreakdown.show(new DoughnutView({
		measure: 'error_reason',
		unit: unit,
		vent: vent,
		getFilter: function(parameters) {
			var filter = 'error_reason.ne("ok")';

			if (parameters['client_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'client_id.eq("' + parameters['client_id'] + '")';
			} else if (parameters['application_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'application_id.eq("' + parameters['application_id'] + '")';
			}

			if (parameters['service_id']) {
				if (filter.length > 0) { filter += " + "; }
				filter += 'service_id.eq("' + parameters['service_id'] + '")';

				if (parameters['operation_name']) {
					if (filter.length > 0) { filter += " + "; }
					filter += 'operation_name.eq("' + parameters['operation_name'] + '")';
				}
			}

			return filter;
		}
	}));
}