<form role="form">
	{{#model "developer"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Developer</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<fieldset>
			<legend>Account Information</legend>
			<div class="row">
				<div class="col-md-4">
					{{input "username" "Username" required=true}}
				</div>
				<div class="col-md-4">
					{{input "firstName" "First Name" required=true}}
				</div>
				<div class="col-md-4">
					{{input "lastName" "Last Name" required=true}}
				</div>
			</div>
		</fieldset>
		<fieldset>
			<legend>Contact Information</legend>
			<div class="row">
				<div class="col-md-6">
					{{input "company" "Company Name"}}
				</div>
				<div class="col-md-6">
					{{input "title" "Job Title"}}
				</div>
			</div>
			<div class="row">
				<div class="col-md-6">
					<div class="form-group">
						<label for="email">Email <i class="glyphicon glyphicon-asterisk" title="Required"></i></label>
						<div class="input-group">
							<span class="input-group-addon"><i class="glyphicon glyphicon-envelope"></i></span>
							<input type="email" class="form-control" {{bind "email" required=true}} />
						</div>
					</div>
				</div>
				<div class="col-md-3">
					<div class="form-group">
						<label for="phone">Phone</label>
						<div class="input-group">
							<span class="input-group-addon"><i class="glyphicon glyphicon-earphone"></i></span>
							<input type="text" class="form-control" {{bind "phone"}} />
						</div>
					</div>
				</div>
				<div class="col-md-3">
					<div class="form-group">
						<label for="mobile">Mobile</label>
						<div class="input-group">
							<span class="input-group-addon"><i class="glyphicon glyphicon-phone"></i></span>
							<input type="text" class="form-control" {{bind "mobile"}} />
						</div>
					</div>
				</div>
			</div>
		</fieldset>
		<fieldset>
			<legend>Address</legend>
			<div class="form-group">
				<label for="address1">Address</label>
				<input type="text" class="form-control" {{bind "address1"}} />
			</div>
			<div class="form-group">
				<input type="text" class="form-control" {{bind "address2"}} />
			</div>
			<div class="row">
				<div class="col-md-4">
					{{input "locality" "City / Locality"}}
				</div>
				<div class="col-md-3">
					{{input "region" "State / Region"}}
				</div>
				<div class="col-md-3">
					{{input "postalCode" "Zipcode / Postal Code"}}
				</div>
				<div class="col-md-2">
					{{input "countryCode" "Country"}}
				</div>
			</div>
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
			{{#can "delete" "developer"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "developer"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "developer"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>