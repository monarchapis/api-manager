<form role="form">
	{{#model "permission"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Permission</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		{{input "name" "Name" required=true}}
		{{textarea "description" "Description"}}
		{{#select "type" "Type" required=true}}
			<option value="action">Action</option>
			<option value="entity">Entity</option>
		{{/select}}
		{{#select "scope" "Scope" required=true}}
			<option value="client">Client only</option>
			<option value="user">User only</option>
			<option value="both">Client and User</option>
		{{/select}}
		{{#select "messageId" "Message" required=true}}
			{{#each messages}}
			<option value="{{id}}">{{key}}</option>
			{{/each}}
		{{/select}}
		{{textarea "flags" "Flags" caption="One per line" formatter="stringList"}}
	</div>
	<div class="modal-footer">
		{{#if id}}
			{{#can "delete" "permission"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "permission"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "permission"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>