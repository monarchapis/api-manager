<form role="form">
	{{#model "client"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Client</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<fieldset>
			<div class="row">
				<div class="col-md-4">
					<div class="form-group">
						<div class="checkbox">
							<label>
								<input type="checkbox" name="enabled" rv-checked="client:enabled" /> Enabled
							</label>
						</div>
					</div>
					<span class="help-block">If checked, allows requests from the application to be processed.</span>
				</div>
				<div class="col-md-4">
					{{input "label" "Label" required=true help="Please use the label to refer to this client instead of the API Key."}}
				</div>
				<div class="col-md-4">
					{{#select "status" "Workflow Status"}}
						<option value=""></option>
						<option value="waiting">Awaiting Review</option>
						<option value="approved">Approved</option>
						<option value="rejected">Rejected</option>
					{{/select}}
				</div>
			</div>
		</fieldset>
		<fieldset>
			<legend>API Credentials</legend>
			<div class="row">
				<div class="col-md-6">
					<div class="form-group">
						{{input "apiKey" "API Key" extras="autocomplete=\"off\" autocapitalize=\"off\" autocorrect=\"off\"
							spellcheck=\"false\"" required=true}}
						{{#can "update" "client"}}
						<div class="input-group">
							<span class="input-group-addon">Length</span>
							<input type="number" class="form-control text-right" value="24" />
							<span class="input-group-btn">
								<button class="btn btn-default" type="button" data-trigger="generate">Generate</button>
							</span>
						</div><!-- /input-group -->
						{{/can}}
					</div>
				</div>
				<div class="col-md-6">
					<div class="form-group">
						{{input "sharedSecret" "Shared Secret" extras="autocomplete=\"off\" autocapitalize=\"off\" autocorrect=\"off\"
							spellcheck=\"false\""}}
						{{#can "update" "client"}}
						<div class="input-group">
							<span class="input-group-addon">Length</span>
							<input type="number" class="form-control text-right" value="24" />
							<span class="input-group-btn">
								<button class="btn btn-default" type="button" data-trigger="generate">Generate</button>
							</span>
						</div><!-- /input-group -->
						{{/can}}
					</div>
				</div>
			</div>
		</fieldset>
		<fieldset>
			<legend>Permissions</legend>

			<label>Application Permissions</label> <small>(2-legged)</small>
			<div class="well">
				<section name="appPermissions">
					<div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
						<table class="table" style="margin-bottom: 0;">
							<thead>
								<tr>
									<th class="text-center" style="width: 1px;" title="Authorized"><i class="glyphicon glyphicon-ok"></i></th>
									<th class="text-center" style="width: 1px;" title="Create">C</th>
									<th class="text-center" style="width: 1px;" title="Read">R</th>
									<th class="text-center" style="width: 1px;" title="Update">U</th>
									<th class="text-center" style="width: 1px;" title="Delete">D</th>
									<th>Name</th>
									<th>Description</th>
								</tr>
							</thead>
							<tbody>
								{{#each permissions.app}}
								<tr>
									<td>
										{{#ifCond type "===" "action"}}
										<input id="app-perm-{{id}}" type="checkbox" name="{{id}}" {{checkedIfContains ../../clientPermissionIds id}} title="Authorized"/>
										{{/ifCond}}
									</td>
									{{#ifCond type "===" "entity"}}
									<td>
										<input id="app-perm-{{id}}" type="checkbox" name="{{id}}:create" {{permissionChecked ../../clientPermissionIds id "create"}} title="Create" />
									</td>
									<td>
										<input id="app-perm-{{id}}" type="checkbox" name="{{id}}:read" {{permissionChecked ../../clientPermissionIds id "read"}} title="Read"/>
									</td>
									<td>
										<input id="app-perm-{{id}}" type="checkbox" name="{{id}}:update" {{permissionChecked ../../clientPermissionIds id "update"}} title="Update"/>
									</td>
									<td>
										<input id="app-perm-{{id}}" type="checkbox" name="{{id}}:delete" {{permissionChecked ../../clientPermissionIds id "delete"}} title="Delete"/>
									</td>
									{{else}}
									<td></td>
									<td></td>
									<td></td>
									<td></td>
									{{/ifCond}}
									<td><label for="app-perm-{{id}}">{{name}}</label></td>
									<td>{{description}}</td>
								</tr>
								{{/each}}
							</tbody>
						</table>
					</div>
				</section>
			</div>

			<label>Access Delegation</label> <small>(3-legged)</small>
			<div class="well">
				<div class="form-group">
					<label>Authorization scheme</label> <small>(Select then modify below)</small>
					<div id="auth-scheme-picker" class="input-group">
						<input type="text" class="form-control" disabled="disabled" />
						<span class="input-group-btn">
							<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
								<span class="caret"></span>
								<span class="sr-only">Select scheme</span>
							</button>
							<ul class="dropdown-menu pull-right" role="menu">
								{{#each permissionSets}}
								<li><a href="#">{{@key}}</a></li>
								{{/each}}
								{{#can "update" "client"}}
								<li class="divider"></li>
								<li><a href="#" data-trigger="new">New</a></li>
								{{/can}}
							</ul>
						</span>
					</div>
				</div>
				<section name="permissionSet" style="display: none;">
				<hr />
				{{#model "permissionSet"}}
				<div class="row">
					<div class="col-md-6">
						<div class="form-group">
							<div class="checkbox">
								<label><input type="checkbox" {{checked "enabled"}} /> Enabled</label>
							</div>
						</div>
						<div class="row">
							<div class="col-md-6">
								{{input "expiration" "Expiration" class="text-right" caption="in HH:MM:SS" formatter="timespan" placeholder="HH:MM:SS"}}
							</div>
							<div class="col-md-6">
								{{#select "lifespan" "Life Span" caption="if expiration set"}}
									<option value="finite">Finite</option>
									<option value="session">Session</option>
								{{/select}}
							</div>
						</div>
					</div>
					<div class="col-md-6" style="position: relative;">
						{{#can "update" "client"}}
						<button type="button" class="btn btn-default" data-trigger="remove" style="position: absolute; right: 15px; top: 0;"><i class="glyphicon glyphicon-trash"></i> Remove</button>
						{{/can}}

						<label>User Interface Options</label>
						<div class="checkbox">
							<label><input type="checkbox" {{checked "refreshable"}} /> Allow refresh</label>
						</div>
						<div class="checkbox">
							<label><input type="checkbox" {{checked "autoAuthorize"}} /> Auto authorize permissions</label>
						</div>
						<div class="checkbox">
							<label><input type="checkbox" {{checked "allowWebView"}} /> Allow native web view controls</label>
						</div>
						<div class="checkbox">
							<label><input type="checkbox" {{checked "allowPopup"}} /> Allow popup windows and IFrames</label>
						</div>
					</div>
				</div><!-- /.row -->
				<br />

				<div class="clearfix">
					<label class="pull-left">User Permissions</label>
					<div class="checkbox pull-left" style="margin: 0 0 0 20px;">
						<label><input type="checkbox" {{checked "global"}} /> Manage globally</label>
					</div>
				</div>
				<section name="userPermissions">
					<div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
						<table class="table" style="margin-bottom: 0;">
							<thead>
								<tr>
									<th class="text-center" style="width: 1px;" title="Authorized"><i class="glyphicon glyphicon-ok"></i></th>
									<th class="text-center" style="width: 1px;" title="Create">C</th>
									<th class="text-center" style="width: 1px;" title="Read">R</th>
									<th class="text-center" style="width: 1px;" title="Update">U</th>
									<th class="text-center" style="width: 1px;" title="Delete">D</th>
									<th>Name</th>
									<th>Description</th>
								</tr>
							</thead>
							<tbody>
								{{#each permissions.user}}
								<tr>
									<td>
										{{#ifCond type "===" "action"}}
										<input id="user-perm-{{id}}" type="checkbox" name="{{id}}" {{checkedIfContains ../../clientPermissionIds id}} title="Authorized"/>
										{{/ifCond}}
									</td>
									{{#ifCond type "===" "entity"}}
									<td>
										<input id="user-perm-{{id}}" type="checkbox" name="{{id}}:create" {{permissionChecked ../../clientPermissionIds id "create"}} title="Create" />
									</td>
									<td>
										<input id="user-perm-{{id}}" type="checkbox" name="{{id}}:read" {{permissionChecked ../../clientPermissionIds id "read"}} title="Read"/>
									</td>
									<td>
										<input id="user-perm-{{id}}" type="checkbox" name="{{id}}:update" {{permissionChecked ../../clientPermissionIds id "update"}} title="Update"/>
									</td>
									<td>
										<input id="user-perm-{{id}}" type="checkbox" name="{{id}}:delete" {{permissionChecked ../../clientPermissionIds id "delete"}} title="Delete"/>
									</td>
									{{else}}
									<td></td>
									<td></td>
									<td></td>
									<td></td>
									{{/ifCond}}
									<td><label for="user-perm-{{id}}">{{name}}</label></td>
									<td>{{description}}</td>
								</tr>
								{{/each}}
							</tbody>
						</table>
					</div>
				</section>
				{{/model}}
				</section>
			</div>
		</fieldset>
		<fieldset>
			<legend>Security Policies</legend>

			{{include "authenticators-and-policies"}}

			<br />
			<div class="row">
				<div class="col-sm-6 form-group">
					<label>Claim Sources</label>
				</div>
				<div class="col-sm-6 text-right form-group">
					<div class="btn-group">
						<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
						Add Claim Source <span class="caret"></span></button>
						<ul id="add-claim-source-menu" class="dropdown-menu pull-right" role="menu">
							{{#each claimSourceConfigs}}
							<li><a href="#" data-value="{{name}}">{{displayName}}</a></li>
							{{/each}}
						</ul>
					</div>
				</div>
			</div>
			<section name="claim-sources">
				<div class="panel-group add-bottom" id="claim-source-accordion">
				</div>
			</section>
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
			{{#can "delete" "client"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "client"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "client"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>