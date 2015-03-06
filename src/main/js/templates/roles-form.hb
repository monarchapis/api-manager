<form role="form">
	{{#model "role"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Role</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<fieldset>
			<legend>Basic Information</legend>
			<div class="row">
				<div class="col-md-6">
					{{input "roleName" "Role Name" required=true}}
				</div>
				<div class="col-md-6">
					{{input "displayName" "Display Name" required=true}}
				</div>
			</div>
			{{textarea "description" "Description" required=true}}
		</fieldset>
		<fieldset>
			<legend>Permissions</legend>

			{{include "service-permissions"}}

			{{include "management-permissions"}}
		</fieldset>
	</div>
	<div class="modal-footer">
		{{#if id}}
			{{#can "delete" "role"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "role"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "role"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>