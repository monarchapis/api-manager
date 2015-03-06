<label>Management Permissions</label>
{{#model "provider"}}
<div class="well form-horizontal">
	<h4>Partners</h4>
	<div class="row">
		<div class="col-md-6">
			<div class="form-group">
				<label for="application" class="col-md-5 control-label">Applications</label>
				<div class="col-md-7 text-right">
					<select id="application" name="application" rv-value="accessLevels:application" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="form-group">
				<label for="client" class="col-md-5 control-label">Clients</label>
				<div class="col-md-7 text-right">
					<select id="client" name="client" rv-value="accessLevels:client" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="redacted">Redacted</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-6">
			<div class="form-group">
				<label for="developer" class="col-md-5 control-label">Developers</label>
				<div class="col-md-7 text-right">
					<select id="developer" name="developer" rv-value="accessLevels:developer" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="form-group">
				<label for="appDeveloper" class="col-md-5 control-label">Developer Team</label>
				<div class="col-md-7 text-right">
					<select id="appDeveloper" name="appDeveloper" rv-value="accessLevels:appDeveloper" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
	</div>
	<hr class="narrow" />
	<h4>APIs</h4>
	<div class="row">
		<div class="col-md-6">
			<div class="form-group">
				<label for="service" class="col-md-5 control-label">Services</label>
				<div class="col-md-7 text-right">
					<select id="service" name="service" rv-value="accessLevels:service" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="form-group">
				<label for="plan" class="col-md-5 control-label">Plans</label>
				<div class="col-md-7 text-right">
					<select id="plan" name="plan" rv-value="accessLevels:plan" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-6">
			<div class="form-group">
				<label for="permission" class="col-md-5 control-label">Permissions</label>
				<div class="col-md-7 text-right">
					<select id="permission" name="permission" rv-value="accessLevels:permission" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="form-group">
				<label for="message" class="col-md-5 control-label">Messages</label>
				<div class="col-md-7 text-right">
					<select id="message" name="message" rv-value="accessLevels:message" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
	</div>
	<hr class="narrow" />
	<h4>Access</h4>
	<div class="row">
		<div class="col-md-6">
			<div class="form-group">
				<label for="provider" class="col-md-5 control-label">Providers</label>
				<div class="col-md-7 text-right">
					<select id="provider" name="provider" rv-value="accessLevels:provider" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="redacted">Redacted</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="form-group">
				<label for="role" class="col-md-5 control-label">Roles</label>
				<div class="col-md-7 text-right">
					<select id="role" name="role" rv-value="accessLevels:role" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-6">
			<div class="form-group">
				<label for="principalProfile" class="col-md-5 control-label">Principal Profiles</label>
				<div class="col-md-7 text-right">
					<select id="principalProfile" name="principalProfile" rv-value="accessLevels:principalProfile" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="redacted">Redacted</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="form-group">
				<label for="principalClaims" class="col-md-5 control-label">Principal Claims</label>
				<div class="col-md-7 text-right">
					<select id="principalClaims" name="principalClaims" rv-value="accessLevels:principalClaims" class="form-control">
						<option value="noaccess">No Access</option>
						<option value="read">Read Only</option>
						<option value="readwrite">Read / Write</option>
						<option value="fullaccess">Full Access</option>
					</select>
				</div>
			</div>
		</div>
	</div>
</div>
{{/model}}