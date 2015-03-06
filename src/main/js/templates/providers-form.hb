<form role="form">
	{{#model "provider"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Provider</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<fieldset>
			<div class="row">
				<div class="col-md-4">
					<div class="form-group">
						<div class="checkbox">
							<label>
								<input type="checkbox" name="enabled" rv-checked="provider:enabled" /> Enabled
							</label>
						</div>
					</div>
					<span class="help-block">If checked, allows requests to the APIs for this provider to be processed.</span>
				</div>
				<div class="col-md-4">
					{{input "label" "Label" required=true help="Please use the label to refer to this provider instead of the API Key."}}
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
						{{#can "update" "provider"}}
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
						{{#can "update" "provider"}}
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

			{{include "service-permissions"}}

			{{include "management-permissions"}}
		</fieldset>
		<fieldset>
			<legend>Security Policies</legend>
			<div class="form-group">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="behindReverseProxy" rv-checked="provider:behindReverseProxy" /> Behind HTTP reverse proxy (e.g. Apache, Nginx, not necessary for AJP).  Enables evaluation of X-Forwarded-For/Proto/Path.
					</label>
				</div>
			</div>

			{{include "authenticators-and-policies"}}
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
			{{#can "delete" "provider"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "provider"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "provider"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>