<div class="well narrow">
	<div class="form-inline">
		<div class="row">
		<div class="form-group col-sm-1">
			<label class="control-label form-control-static">Filter by</label>
		</div>
		<div class="form-group col-sm-3">
			<label class="sr-only" for="client-application-filter">Application</label>
			<input id="client-application-filter" type="text" name="application.name" class="form-control" placeholder="Application" style="width: 100%;" />
		</div>
		<div class="form-group col-sm-2">
			<label class="sr-only" for="client-label-filter">Label</label>
			<input id="client-label-filter" type="text" name="label" class="form-control" placeholder="Label" style="width: 100%;" />
		</div>
		<div class="form-group col-sm-2">
			<label class="sr-only" for="client-status-filter">Status</label>
			<select id="client-status-filter" name="status" class="form-control" style="width: 100%;">
				<option value="">- Status -</option>
				<option value="waiting">Awaiting Review</option>
				<option value="approved">Approved</option>
				<option value="rejected">Rejected</option>
			</select>
		</div>
		</div>
	</div>
</div>