<form role="form">
	{{#model "service"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Service</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<fieldset>
			<legend>Service Information</legend>
			<div class="row">
				<div class="col-md-6">
					{{input "name" "Name" required=true}}
				</div>
				<div class="col-md-6">
					{{#select "type" "Type"}}
						<option value=""></option>
						<option value="private">Private / Internal</option>
						<option value="partner">Partner</option>
						<option value="public">Public / Open</option>
					{{/select}}
				</div>
			</div>
			{{textarea "description" "Description"}}
		</fieldset>
		<fieldset role="operations">
			<legend>Access Control</legend>
			<div class="form-group">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="accessControl" value="true" {{checked "accessControl"}} /> Enabled
					</label>
				</div>
			</div>
			<div class="row">
				<div class="col-md-7">
					{{input "uriPrefix" "URI Pattern Prefix"}}

					<div class="row">
						<div class="col-md-7">
							{{#select "versionLocation" "Version Location"}}
								<option value="">No version parameter</option>
								<option value="path">Path parameter in URI</option>
								<option value="header">Mime Type (Accept Header)</option>
								<option value="query">Query Parameter</option>
								<option value="header,query">Mime Type, then Query Parameter</option>
								<option value="query,header">Query Parameter, then Mime Type</option>
							{{/select}}
						</div>
						<div class="col-md-5">
							{{input "defaultVersion" "Default/Current Version"}}
						</div>
					</div>
				</div>
				<div class="col-md-5">
					{{textarea "hostnames" "Hostnames" caption="One per line, leave empty for any hostname" formatter="stringList"}}
				</div>
			</div>
			<table class="table table-condensed table-hover last-row-hidden-buttons">
				<thead>
					<tr>
						<th style="width: 1%;"></th>
						<th style="width: 98%;">Operations <small>(Name, URI Pattern, &amp; Permissions)</small></th>
						<th style="width: 1%;"></th>
					</tr>
				</thead>
				<tbody>
					{{#each operations}}
					<tr class="collapsed">
						<td><a href="#" role="toggle"><i class="glyphicon glyphicon-chevron-right"></i><span class="sr-only">Toggle</span></a></td>
						<td>
							<div class="form">
								<div class="row">
									<div class="col-md-4 col-sm-4">
										<div class="form-group">
											<input type="text" id="operationName" name="operationName" class="form-control input-sm" value="{{name}}" placeholder="Name" required="required" />
										</div>
									</div>
									<div class="col-md-8 col-sm-8">
										<div class="form-group">
											<div class="input-group">
												<span class="input-group-btn">
													<button id="dropdownMenu-{{$index}}" name="method" class="btn btn-method {{method}} dropdown-toggle btn-sm" type="button" data-toggle="dropdown" value="{{method}}">{{method}}</button>
													<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu-{{$index}}">
														<li role="presentation"><a role="menuitem" tabindex="-1" href="#">GET</a></li>
														<li role="presentation"><a role="menuitem" tabindex="-1" href="#">POST</a></li>
														<li role="presentation"><a role="menuitem" tabindex="-1" href="#">PUT</a></li>
														<li role="presentation"><a role="menuitem" tabindex="-1" href="#">PATCH</a></li>
														<li role="presentation"><a role="menuitem" tabindex="-1" href="#">DELETE</a></li>
														<li role="presentation"><a role="menuitem" tabindex="-1" href="#">OPTIONS</a></li>
													</ul>
												</span>
												<input class="form-control input-sm" name="uriPattern" type="text" placeholder="URI Pattern" value="{{uriPattern}}" required="required" />
											</div>
										</div>
									</div>
								</div>
								<div class="row extended">
									<div class="col-md-6 col-sm-6">
										<div class="form-group">
											<div class="input-group">
												<div class="input-group-addon"><small style="display: inline-block; width: 60px;">Client</small></div>
												<input class="form-control input-sm" name="clientPermissionIds" type="text" placeholder="Client Permissions" value="{{join clientPermissionIds ","}}" />
											</div>
										</div>
										<div class="form-group">
											<div class="input-group">
												<div class="input-group-addon"><small style="display: inline-block; width: 60px;">Delegated</small></div>
												<input class="form-control input-sm" name="delegatedPermissionIds" type="text" placeholder="Delegated Permissions" value="{{join delegatedPermissionIds ","}}" />
											</div>
										</div>
									</div>
									<div class="col-md-6 col-sm-6">
										<div class="form-group">
											<div class="input-group">
												<div class="input-group-addon"><small>Claims</small></div>
												<textarea class="form-control input-sm autosize" name="claims" placeholder="User claims">{{joinClaims claims "\n"}}</textarea>
											</div>
										</div>
									</div>
								</div>
							</div>
						</td>
						<td>
							<div class="form"><button class="btn btn-default btn-sm" role="delete"><i class="glyphicon glyphicon-trash"></i></button></div>
						</td>
					</tr>
					{{/each}}
					{{include 'services-form-operation'}}
				</tbody>
			</table>
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