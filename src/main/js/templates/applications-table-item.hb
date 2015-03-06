<td>
	<div class="btn-group">
		<button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
			<i class="glyphicon glyphicon-cog"></i> <span class="caret"></span>
		</button>
		<ul class="dropdown-menu" role="menu">
			<li><a href="#" data-trigger="edit" data-id="{{id}}">{{viewedit "application"}} application</a></li>
			{{#can "view" "appDeveloper"}}<li><a href="#" data-trigger="manage-developers" data-id="{{id}}">{{viewmanage "appDeveloper"}} developer teams</a></li>{{/can}}
		</ul>
	</div>
</td>
<td>
	<h3>{{#link applicationUrl newwindow=true}}{{name}}{{/link}}</h3>
	<div><small>by {{#link companyUrl newwindow=true}}{{companyName}}{{/link}}</small></div>
</td>
<td>{{description}}</td>
<td class="text-right">
	<div class="dropdown">
		<button type="button" class="btn btn-default dropdown-toggle" id="client-dd-{{id}}" class="" role="button" data-toggle="dropdown" data-id="{{id}}" data-trigger="clients"><strong>{{clientCount}}</strong> <b class="caret"></b></button>
		<ul class="dropdown-menu pull-right text-left" role="menu" aria-labelledby="client-dd-{{id}}" data-id="{{id}}"></ul>
	</div>
</td>
<td>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>