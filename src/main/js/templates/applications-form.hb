<form role="form">
	{{#model "application"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Application</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<fieldset>
			<legend>Application Information</legend>
			<div class="row">
				<div class="col-md-6">
					{{input "name" "Name" required=true}}
				</div>
				<div class="col-md-6">
					{{#select "planId" "Plan"}}
						<option value=""></option>
						{{#each plans}}
						<option value="{{id}}">{{name}}</option>
						{{/each}}
					{{/select}}
				</div>
			</div>
			{{textarea "description" "Description"}}
			{{input "applicationUrl" "Website URL" required=true}}
			{{input "applicationImageUrl" "Image / Logo URL"}}
		</fieldset>
		<fieldset>
			<legend>Company Information</legend>
			{{input "companyName" "Name" required=true}}
			{{input "companyUrl" "Website URL" required=true}}
			{{input "companyImageUrl" "Image / Logo URL"}}
		</fieldset>
		<fieldset>
			<legend>Security / OAuth</legend>
			{{textarea "callbackUris" "Callback URLs" caption="One per line" formatter="stringList"}}
		</fieldset>
		<fieldset role="extended">
			<legend>Extended Properties</legend>
			<table class="table table-condensed table-hover last-row-hidden-buttons">
				<thead>
					<tr>
						<th style="width: 35%;">Name</th>
						<th style="width: 15%;">Type</th>
						<th style="width: 49%;">Value</th>
						<th style="width: 1%;"></th>
					</tr>
				</thead>
				<tbody>
					{{#each extended}}
					<tr>
						<td>
							<input type="text" name="key" class="form-control" value="{{@key}}" required="required" />
						</td>
						<td>
							{{dataType this}}
						</td>
						<td>
							{{dataValue this}}
						</td>
						<td>
							<button class="btn btn-default" role="delete"><i class="glyphicon glyphicon-trash"></i></button>
						</td>
					</tr>
					{{/each}}
					<tr>
						<td>
							<input type="text" name="key" class="form-control" value="" />
						</td>
						<td>
							<select name="type" class="form-control">
								<option selected="selected">string</option>
								<option>number</option>
								<option>boolean</option>
							</select>
						</td>
						<td>
							<input type="text" name="value" class="form-control" value="" />
						</td>
						<td>
							<button class="btn btn-default" role="delete"><i class="glyphicon glyphicon-trash"></i></button>
						</td>
					</tr>
				</tbody>
			</table>
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