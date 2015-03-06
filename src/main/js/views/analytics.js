var AnalyticsLayout = Marionette.Layout.extend({
	template: 'analytics',

	regions: {
		filter: '#analytics-filter',
		activity: '#activity',
		topApplications: '#topApplications',
		topServices: '#topServices',
		topOperations: '#topOperations',
		errorBreakdown: '#errorBreakdown'
	}
});

function Colour(col, amt) {
	var usePound = false;

	if (col[0] == "#") {
		col = col.slice(1);
		usePound = true;
	}

	var num = parseInt(col,16);

	var r = (num >> 16) + amt;

	if (r > 255) r = 255;
	else if  (r < 0) r = 0;

	var b = ((num >> 8) & 0x00FF) + amt;

	if (b > 255) b = 255;
	else if  (b < 0) b = 0;

	var g = (num & 0x0000FF) + amt;

	if (g > 255) g = 255;
	else if (g < 0) g = 0;

	return (usePound?"#":"") + (g | (b << 8) | (r << 16)).toString(16);
}

var AnalyticsFilterView = Marionette.ItemView.extend({
	template: 'analytics-filter',

	start: null,
	end: null,

	startBack: {
		second : 60,
		second10 : 600,
		minute: 3600,
		minute5: 18000,
		minute15: 54000,
		minute30: 108000,
		hour: 86400,
		day: 2592000
	},

	parameters: {},

	events: {
		'change :input[name="applicationId"]' : 'onApplicationChange',
		'change :input[name="serviceId"]' : 'onServiceChange',
		'submit form' : 'onFormSubmit'
	},

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.call(this);
		this.unit = options.unit;
		this.vent = options.vent;
	},

	onShow: function() {
		$('.input-group.date', this.$el).datetimepicker();
		$('.input-group.date', this.$el).on('change.dp', function(e) {
			var me = $(':input[name="timeframe"]', this.$el);
			var current = me.val();

			if (current.substring(0, 6) != 'custom') {
				me.val('custom');
			}
		});

		var measurePromise = new $.Deferred();
		var servicePromise = new $.Deferred();

		$.getJSON(serviceBaseUrl + 'analytics/v1/traffic').done(_.bind(function(data) {
			var measures = _.filter(data.fields, function(field) { return field.usage == 'measure'; });
			var input = $(':input[name="measure"]').empty();

			_.each(measures, function(measure) {
				if (measure.type == 'string' || measure.type == 'code') {
					input.append($('<option />')
						.attr('value', measure.name + '|values')
						.text(measure.display));
				} else {
					input.append($('<option />')
						.attr('value', measure.name + '|average')
						.text(measure.display + ' (Average)'));
					input.append($('<option />')
						.attr('value', measure.name + '|sum')
						.text(measure.display + ' (Total)'));
				}
			});

			measurePromise.resolve();
		}, this));

		$.getJSON(serviceBaseUrl + 'management/v1/services?limit=10000').done(function(data) {
			var serviceId = $(':input[name="serviceId"]')
				.empty()
				.append($('<option />')
					.attr('value', '')
					.text('All services'));

			_.each(data.items, function(item) {
				serviceId.append($('<option />')
					.attr('value', item.id)
					.text(item.name));
			});

			servicePromise.resolve();
		});

		$(':input[name="applicationId"]', this.$el).select2({
			placeholder: "Search for an application",
			minimumInputLength: 1,
			query: function (query) {
				$.getJSON(serviceBaseUrl + 'management/v1/applications?name=' + encodeURIComponent(query.term) + '*').done(function(data) {
					var results = [];

					_.each(data.items, function(item) {
						results.push({ id: item.id, text: item.name });
					});

					query.callback({
						results : results,
						more: data.offset + data.count < data.total
					});
				});
			},
			allowClear: true
		});

		$.when(measurePromise, servicePromise).done(_.bind(function() {
			this.update();
		}, this));
	},

	onApplicationChange : function(e) {
		var applicationId = $(':input[name="applicationId"]').val();
		var clientId = $(':input[name="clientId"]');
		clientId.empty();
		clientId.prop('disabled', true);

		if (applicationId) {
			$.getJSON(serviceBaseUrl + 'management/v1/clients?applicationId=' + encodeURIComponent(applicationId)).done(function(data) {
				clientId.append($('<option />')
						.attr('value', '')
						.text('All clients'));

				_.each(data.items, function(item) {
					clientId.append($('<option />')
						.attr('value', item.id)
						.text(item.label));
				});

				clientId.prop('disabled', false);
			});
		}
	},

	onServiceChange : function(e) {
		var serviceId = $(':input[name="serviceId"]').val();
		var operationName = $(':input[name="operationName"]');
		operationName.empty();
		operationName.prop('disabled', true);

		var d = new Date();
		var end = Math.floor(d.getTime() / 1000);
		var m = moment(d).subtract('months', 12);
		var start = Math.floor(m.unix());

		if (serviceId) {
			var query = 'service_id.eq("' + serviceId + '")';
			$.getJSON(serviceBaseUrl + 'analytics/v1/traffic/metrics/operation_name/distinct?start=' + start + '&query=' + encodeURIComponent(query)).done(function(data) {
				operationName.append($('<option />')
						.attr('value', '')
						.text('All operations'));

				_.each(data.values, function(name) {
					operationName.append($('<option />')
						.attr('value', name)
						.text(name));
				});

				operationName.prop('disabled', false);
			});
		}
	},

	update: function(scollTo) {
		this.setupParameters();
		this.sendUpdate(scollTo);
	},

	sendUpdate: function(scollTo) {
		this.vent.trigger('filterChanged', {
			unit: this.unit,
			start: this.start,
			end: this.end,
			measureName: this.measureName,
			measureType: this.measureType,
			unit: this.unit,
			parameters: this.parameters,
			scollTo: scollTo
		});
	},

	setupParameters: function() {
		var applicationId = trimToNull($(':input[name="applicationId"]', this.$el).val());
		var clientId      = trimToNull($(':input[name="clientId"]', this.$el).val());
		var serviceId     = trimToNull($(':input[name="serviceId"]', this.$el).val());
		var operationName = trimToNull($(':input[name="operationName"]', this.$el).val());

		var parameters = {};

		if (clientId) {
			parameters['client_id'] = clientId;
		} else if (applicationId) {
			parameters['application_id'] = applicationId;
		}

		if (serviceId) {
			parameters['service_id'] = serviceId;

			if (operationName) {
				parameters['operation_name'] = operationName;
			}
		}

		this.parameters = parameters;

		var measure = $(':input[name="measure"]', this.$el).val().split('|');
		this.measureName = measure[0];
		this.measureType = measure[1];
		this.unit = $(':input[name="timeframe"]', this.$el).val();

		var d = new Date();
		var start, end;

		if (this.unit.substring(0, 6) == 'custom') {
			this.unit = this.unit.length > 6 ? this.unit.substring(7) : 'second';
			var startDate = $(':input[name="startDate"]', this.$el).val();
			var endDate = $(':input[name="endDate"]', this.$el).val();

			if (startDate && endDate) {
				if (!startDate.match(this.dateTimeRegex)) {
					alert('Start Date/Time is contains an invalid value');
					return false;
				}

				if (!endDate.match(this.dateTimeRegex)) {
					alert('End Date/Time is contains an invalid value');
					return false;
				}

				startDate = moment(startDate);
				endDate = moment(endDate);

				start = Math.floor(startDate.unix());
				end = Math.floor(endDate.unix());

				var now = Math.floor(d.getTime() / 1000);
				start = Math.min(now, start);
				end = Math.min(now, end);

				if (end < start) {
					var temp = end;
					end = start;
					start = temp;
				}

				var diff = end - start + 1;
				var found = false;

				for (var unit in this.resolution) {
					if (unit == this.unit) {
						found = true;
					}

					if (found && diff / this.resolution[unit] <= 1000) {
						this.unit = unit;
						break;
					}
				}
			} else {
				if (!startDate) {
					alert('Start Date/Time is required for custom ranges');
					return false;
				}
				if (!endDate) {
					alert('End Date/Time is required for custom ranges');
					return false;
				}
				
				return false;
			}
		} else if (this.unit == 'month') {
			var m = moment(d).subtract('months', 12);
			start = Math.floor(m.unix());
		} else {
			var now = Math.floor(d.getTime() / 1000);
			start = now - this.startBack[this.unit];
		}

		this.start = start;
		this.end = end;

		return true;
	},

	onFormSubmit: function(e) {
		e.preventDefault();

		//this.draw(true);
		this.update(true);
	}
});

var DoughnutView = Marionette.ItemView.extend({
	template: 'doughnut',

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.call(this);
		this.measureName = options.measure;
		this.vent = options.vent;

		if (options.getFilter) this.getFilter = options.getFilter;

		this.listenTo(this.vent, 'filterChanged', this.onFilterChanged, this);

		var red = "#bf616a",
			blue = "#5B90BF",
			orange = "#d08770",
			yellow = "#ebcb8b",
			green = "#a3be8c",
			teal = "#96b5b4",
			pale_blue = "#8fa1b3",
			purple = "#b48ead",
			brown = "#ab7967";

		this.colorLookup = [blue, teal, green, brown, orange, purple, red, yellow, pale_blue];
	},

	onFilterChanged: function(data) {
		if (data.unit !== undefined) this.unit = data.unit;
		if (data.start !== undefined) this.start = data.start;
		if (data.end !== undefined) this.end = data.end;
		if (data.parameters !== undefined) this.parameters = data.parameters;
		this.update();
	},

	update: function() {
		var canvas = $('canvas', this.$el)[0];

		var helpers = Chart.helpers;

		Chart.defaults.global.responsive = true;

		// Call analytics service to get initial data
		$.getJSON(this.getCountsUrl()).done(_.bind(function(result) {
			var labels = result.labels || {};
			var data = result.data;

			var moduleData = [];

			for (var i=0; i<data.length; i++) {
				var color = this.colorLookup[i % data.length];
				moduleData.push({
					value: data[i].count,
					color: color,
					highlight: Colour(color, 10),
					label: labels[data[i].value] || trimToNull(data[i].value) || 'Unknown'
				});
			}

			if (moduleData.length == 0) {
				moduleData.push({
					value: 1,
					color: '#eaeaea',
					highlight: Colour('#eaeaea', 10),
					label: 'No results'
				});
			}

			if (!this.moduleDoughnut) {
				this.moduleDoughnut = new Chart(canvas.getContext('2d')).Doughnut(moduleData, {
					tooltipTemplate : "<%if (label){%><%=label%>: <%}%><%= value %> hits",
					animation: true,
					animationEasing : "easeOut",
					animationSteps : 20
				});
			} else {
				this.moduleDoughnut.segments = [];

				for (var i=0; i<moduleData.length; i++) {
					this.moduleDoughnut.addData(moduleData[i]);
				}

				this.moduleDoughnut.update();
			}

			// 
			$('ul.doughnut-legend', this.$el).remove();
			var legendHolder = document.createElement('div');
			legendHolder.innerHTML = this.moduleDoughnut.generateLegend();
			// Include a html legend template after the module doughnut itself
			helpers.each(legendHolder.firstChild.childNodes, _.bind(function(legendNode, index) {
				helpers.addEvent(legendNode, 'mouseover', _.bind(function() {
					var activeSegment = this.moduleDoughnut.segments[index];
					activeSegment.save();
					activeSegment.fillColor = activeSegment.highlightColor;
					this.moduleDoughnut.showTooltip([activeSegment]);
					activeSegment.restore();
				}, this));
			}, this));
			helpers.addEvent(legendHolder.firstChild, 'mouseout', _.bind(function() {
				this.moduleDoughnut.draw();
			}, this));
			canvas.parentNode.parentNode.appendChild(legendHolder.firstChild);
		}, this));
	},

	getFilter: function() {
		return null;
	},

	getCountsUrl: function() {
		var url = serviceBaseUrl + 'analytics/v1/traffic/metrics/' + this.measureName + '/' + this.unit + '/counts' + '?start=' + this.start;

		if (this.end) {
			url += '&end=' + this.end;
		}

		var filter = this.getFilter(this.parameters || {});

		if (filter && filter.length > 0) {
			url += '&query=' + encodeURIComponent(filter);
		}

		return url;
	}
});

var GraphView = Marionette.ItemView.extend({
	template: 'graph',

	measures : {
		status_code : {
			label : "hits",
			getSeries : function() {
				return [
					{
						color: '#9CC1E0',
						data: [],
						name: 'Successes'
					}, {
						color: '#D9534F',
						data: [],
						name: 'Failures'
					}
				]
			},
			pushData : function(x, stat) {
				var codes = stat.counts;
				var ys = 0, yf = 0;

				if (codes != null) {
					for (var key in codes) {
						if (key.substring(0, 1) == '2') {
							ys += codes[key];
						} else {
							yf += codes[key];
						}
					}
				}

				this.series[0].data.push({ x : x, y : ys });
				this.series[1].data.push({ x : x, y : yf });
			}
		},
		request_size : {
			label : "bytes",
			getSeries : function() {
				return [
					{
						color: '#67a139',
						data: [],
						name: 'Size'
					}
				]
			},
			pushData : function(x, stat) {
				var y = 0;
				if (stat.value) {
					y = this.measureType == "average" ? (stat.value.sum / stat.value.count) : stat.value.sum;
				}
				this.series[0].data.push({ x : x, y : y });
			}
		},
		response_size : {
			label : "bytes",
			getSeries : function() {
				return [
					{
						color: '#67a139',
						data: [],
						name: 'Size'
					}
				]
			},
			pushData : function(x, stat) {
				var y = 0;
				if (stat.value) {
					y = this.measureType == "average" ? (stat.value.sum / stat.value.count) : stat.value.sum;
				}
				this.series[0].data.push({ x : x, y : y });
			}
		},
		response_time : {
			label : "ms",
			getSeries : function() {
				return [
					{
						color: '#67a139',
						data: [],
						name: 'Time'
					}
				]
			},
			pushData : function(x, stat) {
				var y = 0;
				if (stat.value) {
					y = this.measureType == "average" ? (stat.value.sum / stat.value.count) : stat.value.sum;
				}
				this.series[0].data.push({ x : x, y : y });
			}
		}
	},

	graphLength: {
		second : 60,
		second10 : 60,
		minute: 60,
		minute5: 60,
		minute15: 60,
		minute30: 60,
		hour: 24,
		day: 30,
		month: 12
	},

	resolution: {
		second : 1,
		second10 : 10,
		minute: 60,
		minute5: 300,
		minute15: 900,
		minute30: 1800,
		hour: 3600,
		day: 86400
	},

	startBack: {
		second : 60,
		second10 : 600,
		minute: 3600,
		minute5: 18000,
		minute15: 54000,
		minute30: 108000,
		hour: 86400,
		day: 2592000
	},

	intervals: {
		second : 1000,
		second10 : 10000,
		minute: 60000,
		minute5: 300000,
		minute15: 300000,
		minute30: 300000,
		hour: 300000,
		day: 300000,
		month: 300000
	},

	refreshing: {
		second : true,
		second10 : true,
		minute: true,
		minute5: true,
		minute15: true,
		minute30: true,
		hour: false,
		day: false
	},

	parameters : {},

	dateTimeRegex: /([0-1][0-9])\/([0-3][0-9])\/([0-2][0-9]{3}) ([0-5][0-9])\:([0-5][0-9]) (AM|PM)/,

	constructor: function(options) {
		Marionette.ItemView.prototype.constructor.call(this);
		this.measureName = options.measure;
		this.measureType = options.measureType;
		this.vent = options.vent;

		if (options.getFilter) this.getFilter = options.getFilter;

		this.listenTo(this.vent, 'filterChanged', this.onFilterChanged, this);
	},

	onFilterChanged: function(data) {
		if ('unit' in data) this.unit = data.unit;
		if ('start' in data) this.start = data.start;
		if ('end' in data) this.end = data.end;
		if ('parameters' in data) this.parameters = data.parameters;
		this.update(data.scollTo);
	},

	update: function(scollTo) {
		this.loadGraphData(scollTo == true);
	},

	loadGraphData: function(scrollIntoView) {					
		this.cleanup();

		this.measure = this.measures[this.measureName];
		this.series = this.measure.getSeries();

		// Call analytics service to get initial data
		$.getJSON(this.getAnalyticsUrl()).done(_.bind(function(data) {
			var stats = data.data;
			var lastTimestamp = 0;

			for (var i=0; i<stats.length; i++) {
				var stat = stats[i];
				var x = moment(stat.time).unix();

				this.measure.pushData.call(this, x, stat);

				lastTimestamp = x;
			}

			this.start = lastTimestamp;

			this.initializeGraph(scrollIntoView);
		}, this));
	},

	initializeGraph: function(scrollIntoView) {
		$('#activity-graph').empty();

		var graph = new Rickshaw.Graph( {
			element: document.getElementById("activity-graph"),
			width: 1140,
			height: 300,
			renderer: 'area',
			stroke: true,
			preserve: true,
			series: this.series
		} );

		graph.render();

		var hoverDetail = new Rickshaw.Graph.HoverDetail( {
			graph: graph,
			xFormatter: function(x) {
				return new Date(x * 1000).toString();
			},
			yFormatter: _.bind(function(y) { return Math.floor(y) + " " + this.measure.label; }, this)
		} );

		var ticksTreatment = 'glow';

		var xAxis = new Rickshaw.Graph.Axis.Time( {
			graph: graph,
			ticksTreatment: ticksTreatment,
			timeFixture: new Rickshaw.Fixtures.Time.Local()
		} );

		xAxis.render();

		var yAxis = new Rickshaw.Graph.Axis.Y( {
			graph: graph,
			tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
			ticksTreatment: ticksTreatment
		} );

		yAxis.render();

		if (!this.end) {
			this.interval = setInterval( _.bind(function() {
				$.getJSON(this.getAnalyticsUrl()).done(_.bind(function(data) {
					var stats = data.data;

					for (var i=0; i<stats.length; i++) {
						var stat = stats[i];
						var x = moment(stat.time).unix();

						if (x > this.start) {
							this.measure.pushData.call(this, x, stat);

							this.start = x;
						}
					}

					var maxLength = this.graphLength[this.unit];

					for (var i=0; i<this.series.length; i++) {
						var data = this.series[i].data;

						while (data.length > maxLength) {
							data.shift();
						}
					}

					graph.update();
				}, this));
			}, this), this.intervals[this.unit] );
		}

		if (scrollIntoView) {
			$('#activity-graph').parent().scrollIntoView();
		}
	},

	getFilter: function() {
		return null;
	},

	getAnalyticsUrl: function() {
		var url = serviceBaseUrl + 'analytics/v1/traffic/metrics/' + this.measureName + '/' + this.unit + '/series?start=' + this.start;

		if (this.end) {
			url += '&end=' + this.end;
		}

		var filter = this.getFilter(this.parameters || {});

		if (filter && filter.length > 0) {
			url += '&query=' + encodeURIComponent(filter);
		}

		url += '&fillGaps=true&refreshing=' + this.refreshing[this.unit];

		return url;
	},

	onBeforeClose: function() {
		this.cleanup();
	},

	cleanup: function() {
		if (this.interval) {
			clearInterval(this.interval);
			delete this.interval;
		}

		var graph = $('#activity-graph');
		graph.parent().empty().append('<div id="activity-graph"></div>');
	}
});