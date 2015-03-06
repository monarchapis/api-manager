<form role="form">
	{{#model "plan"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Plan</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<fieldset>
			<legend>Plan Information</legend>
			<div class="row">
				<div class="col-md-7">
					{{input "name" "Name" required=true}}
				</div>
				<div class="col-md-3">
					{{input "priceAmount" "Price" type="number"}}
				</div>
				<div class="col-md-2">
					{{input "priceCurrency" "Currency"}}
				</div>
			</div>
		</fieldset>
		<fieldset>
			<legend>Quotas / Rate Limits</legend>
			{{#model "limits"}}
			<label><i class="glyphicon glyphicon-time"></i> Maximum Requests Allowed</label>
			<div class="row">
				<div class="col-md-3">
					{{input "minute" "Per Minute" type="number" formatter="integer"}}
				</div>
				<div class="col-md-3">
					{{input "hour" "Per Hour" type="number" formatter="integer"}}
				</div>
				<div class="col-md-3">
					{{input "day" "Per Day" type="number" formatter="integer"}}
				</div>
				<div class="col-md-3">
					{{input "month" "Per Month" type="number" formatter="integer"}}
				</div>
			</div>
			{{/model}}
		</fieldset>
	</div>
	<div class="modal-footer">
		{{#if id}}
			{{#can "delete" "application"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "application"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "application"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>