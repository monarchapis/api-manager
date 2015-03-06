<form role="form">
	{{#model "principalClaims"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Principal</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		{{input "name" "Name" required=true}}
		<fieldset role="operations">
			<legend>Authorization</legend>

			<div class="form-group">
				<label for="principalClaims.inherits">Inherits from</label>
				<input class="form-control" id="principalClaims.inherits" name="inherits" type="text" value="{{join inherits ","}}" />
			</div>

			<label for="principalClaims.inherits">Claims</label>
			<table data-region="claims" class="table table-condensed table-hover last-row-hidden-buttons">
				<thead>
					<tr>
						<th style="width: 49%;">Type</th>
						<th style="width: 50%;">Values <small>(One per line)</small></th>
						<th style="width: 1%;"></th>
					</tr>
				</thead>
				<tbody>
					{{#each claims}}
					<tr class="collapsed">
						<td>
							<input class="form-control" name="type" type="text" value="{{@key}}" />
						</td>
						<td>
							<textarea class="form-control" name="values">{{join this "\n"}}</textarea>
						</td>
						<td>
							<button class="btn btn-default" role="delete"><i class="glyphicon glyphicon-trash"></i></button>
						</td>
					</tr>
					{{/each}}
					{{include 'principalClaims-form-claim'}}
				</tbody>
			</table>
		</fieldset>
	</div>
	<div class="modal-footer">
		{{#if id}}
			{{#can "delete" "service"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "service"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "service"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>