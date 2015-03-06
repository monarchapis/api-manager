<form role="form">
	{{#model "user"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} User</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<div class="row">
			<div class="col-md-6">
				{{input "userName" "User Name" required=true}}
			</div>
			<div class="col-md-6">
				{{#select "roleId" "Role" caption="In this environment"}}
					<option value="">No Access</option>
					{{#each roles}}
					<option value="{{id}}">{{displayName}}</option>
					{{/each}}
				{{/select}}
			</div>
		</div>
		<div class="row">
			<div class="col-md-6">
				{{input "firstName" "First Name" required=true}}
			</div>
			<div class="col-md-6">
				{{input "lastName" "Last Name" required=true}}
			</div>
		</div>
	</div>
	<div class="modal-footer">
		{{#if id}}
			{{#can "delete" "user"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "user"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "user"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>