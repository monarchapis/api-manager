<form role="form">
	{{#model "environment"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Environment</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		{{#if id}}
		<p><label>Environment Id:</label> {{id}}</p>
		{{/if}}
		{{input "name" "Name" required=true}}
		{{textarea "description" "Description"}}
		{{#if id}}
		{{input "systemDatabase" "System Database" required=true help="Don't change this unless you know what you are doing."}}
		{{else}}
		{{input "systemDatabase" "System Database" required=true help="Configuration of the database connection may be required.  (e.g. Create database account and grant access to database schema)"}}
		{{/if}}
		{{#if id}}
		{{input "analyticsDatabase" "Analytics Database" required=true help="Don't change this unless you know what you are doing."}}
		{{else}}
		{{input "analyticsDatabase" "Analytics Database" required=true help="Configuration of the database connection may be required.  (e.g. Create database account and grant access to database schema)"}}
		{{/if}}
	</div>
	<div class="modal-footer">
		{{#if id}}
			{{#can "delete" "environment"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "environment"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "environment"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>