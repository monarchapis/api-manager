<form id="activity-form" role="form" class="well">
	<div class="row">
		<div class="col-md-6">
			<div class="row">
				<div class="col-md-6">
					<div class="form-group">
						<label for="graph-measure" class="control-label">Measure</label>
						<select id="graph-measure" name="measure" class="form-control"></select>
					</div>
				</div>
				<div class="col-md-6">
					<div class="form-group">
						<label for="graph-timeframe" class="control-label">Timeframe</label>
						<select id="graph-timeframe" name="timeframe" class="form-control">
							<option value="second">Seconds - Last 60</option>
							<option value="second10">10 Seconds - Last 60</option>
							<option value="minute">Minutes - Last 60</option>
							<option value="minute5">5 Minutes - Last 60</option>
							<option value="minute15">15 Minutes - Last 60</option>
							<option value="minute30">30 Minutes - Last 60</option>
							<option value="hour" selected="selected">Hours - Last 24</option>
							<option value="day">Days - Last 30</option>
							<option value="month">Months - Last 12</option>
							<option value="custom">Custom (Automatic)</option>
							<option value="custom.second">Custom Seconds</option>
							<option value="custom.minute">Custom Minutes</option>
							<option value="custom.hour">Custom Hours</option>
							<option value="custom.day">Custom Days</option>
							<option value="custom.month">Custom Months</option>
						</select>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-md-6">
					<div class="form-group">
						<label for="graph-startDate" class="control-label">Start Date/Time</label>
						<div id="graph-startDate-picker" class="input-group date">
							<input type="text" class="form-control" id="graph-startDate" name="startDate" data-format="MM/DD/YYYY hh:mm A"
								pattern="([0-1][0-9])\/([0-3][0-9])\/([0-2][0-9]{3}) ([0-5][0-9])\:([0-5][0-9]) (AM|PM)" />
							<span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
						</div>
					</div>
				</div>
				<div class="col-md-6">
					<div class="form-group">
						<label for="graph-endDate" class="control-label">End Date/Time</label>
						<div id="graph-endDate-picker" class="input-group date">
							<input type="text" class="form-control" id="graph-endDate" name="endDate" data-format="MM/DD/YYYY hh:mm A"
								pattern="([0-1][0-9])\/([0-3][0-9])\/([0-2][0-9]{3}) ([0-5][0-9])\:([0-5][0-9]) (AM|PM)" />
							<span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="col-md-3">
			<div class="form-group">
				<label for="graph-applicationId" class="control-label">Application</label>
				<input id="graph-applicationId" name="applicationId" class="form-control" />
			</div>
			<div class="form-group">
				<label for="graph-clientId" class="control-label">Client</label>
				<select id="graph-clientId" name="clientId" class="form-control" disabled="disabled">
					<option value=""></option>
				</select>
			</div>
		</div>
		<div class="col-md-3">
			<div class="form-group">
				<label for="graph-serviceId" class="control-label">Service</label>
				<select id="graph-serviceId" name="serviceId" class="form-control">
					<option value=""></option>
				</select>
			</div>
			<div class="form-group">
				<label for="graph-operationName" class="control-label">Operation Name</label>
				<select id="graph-operationName" name="operationName" class="form-control" disabled="disabled">
					<option value=""></option>
				</select>
			</div>
			<div class="form-group text-right">
				<div><label class="control-label"></label></div>
				<button type="submit" class="btn btn-primary">Update</button>
			</div>
		</div>
	</div>
</form>