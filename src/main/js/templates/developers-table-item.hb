<td>
	<div class="btn-group">
		<button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
			<i class="glyphicon glyphicon-cog"></i> <span class="caret"></span>
		</button>
		<ul class="dropdown-menu" role="menu">
			<li><a href="#" data-trigger="edit" data-id="{{id}}">{{viewedit "developer"}} developer</a></li>
			{{#can "view" "appDeveloper"}}<li><a href="#" data-trigger="manage-applications" data-id="{{id}}">{{viewmanage "appDeveloper"}} developer teams</a></li>{{/can}}
			{{#can "changePassword" "developer"}}
			<li class="divider"></li>
			<li><a href="#" data-trigger="changePassword" data-id="{{id}}">Change password</a></li>
			{{/can}}
		</ul>
	</div>
</td>
<td>
	<div>{{username}}</div>
	<div>{{firstName}} {{lastName}}</div>
</td>
<td>
	{{#if title}}
	<div>{{title}}</div>
	{{/if}}
	<div><i class="glyphicon glyphicon-envelope"></i> <a href="mailto:{{email}}">{{email}}</a></div>
	{{#if phone}}
	<div><i class="glyphicon glyphicon-earphone"></i> {{phone}}</div>
	{{/if}}
	{{#if mobile}}
	<div><i class="glyphicon glyphicon-phone"></i> {{mobile}}</div>
	{{/if}}
</td>
<td>
	{{#if company}}
	<div>{{company}}</div>
	{{/if}}
	{{#if address1}}
	<div>{{address1}}</div>
	{{/if}}
	{{#if address2}}
	<div>{{address2}}</div>
	{{/if}}
	<div>
	{{locality}} {{region}}{{#if region}},{{/if}} {{portalCode}} {{countryCode}}
	</div>
</td>
<td>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>