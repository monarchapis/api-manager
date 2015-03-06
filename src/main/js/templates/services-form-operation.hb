<tr>
	<td><a href="#" role="toggle"><i class="glyphicon glyphicon-chevron-down"></i><span class="sr-only">Toggle</span></a></td>
	<td>
		<div class="form">
			<div class="row">
				<div class="col-md-4 col-sm-4">
					<input type="text" id="operationName" name="operationName" class="form-control input-sm" value="" placeholder="Name" />
				</div>
				<div class="col-md-8 col-sm-8">
					<div class="form-group">
						<div class="input-group">
							<span class="input-group-btn">
								<button id="dropdownMenu2" name="method" class="btn btn-method GET dropdown-toggle btn-sm" type="button" data-toggle="dropdown" value="GET">GET</button>
								<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu2">
									<li role="presentation"><a role="menuitem" tabindex="-1" href="#">GET</a></li>
									<li role="presentation"><a role="menuitem" tabindex="-1" href="#">POST</a></li>
									<li role="presentation"><a role="menuitem" tabindex="-1" href="#">PUT</a></li>
									<li role="presentation"><a role="menuitem" tabindex="-1" href="#">PATCH</a></li>
									<li role="presentation"><a role="menuitem" tabindex="-1" href="#">DELETE</a></li>
									<li role="presentation"><a role="menuitem" tabindex="-1" href="#">OPTIONS</a></li>
								</ul>
							</span>
							<input class="form-control input-sm" name="uriPattern" type="text" placeholder="URI Pattern" />
						</div>
					</div>
				</div>
			</div>
			<div class="row extended">
				<div class="col-md-6 col-sm-6">
					<div class="form-group">
						<div class="input-group">
							<div class="input-group-addon"><small style="display: inline-block; width: 60px;">Client</small></div>
							<input class="form-control input-sm" name="clientPermissionIds" type="text"  placeholder="Client Permissions" />
						</div>
					</div>
					<div class="form-group">
						<div class="input-group">
							<div class="input-group-addon"><small style="display: inline-block; width: 60px;">Delegated</small></div>
							<input class="form-control input-sm" name="delegatedPermissionIds" type="text" placeholder="Delegated Permissions" />
						</div>
					</div>
				</div>
				<div class="col-md-6 col-sm-6">
					<div class="form-group">
						<div class="input-group">
							<div class="input-group-addon"><small>Claims</small></div>
							<textarea class="form-control input-sm autosize" name="claims" placeholder="User claims"></textarea>
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