<td><button type="button" class="btn btn-primary" data-trigger="edit" data-id="{{id}}"><i class="glyphicon glyphicon-edit"></i></button></td>
<td>{{#link application.applicationUrl newwindow=true}}{{application.name}}{{/link}}</td>
<td>{{label}}</td>
<td>
	{{#each authenticators}}
	<span class="label label-default">{{@key}}</span>
	{{/each}}
</td>
<td>
	{{#each signaturePolicies}}
	<span class="label label-default">{{name}}</span>
	{{/each}}
</td>
<td>
	{{#each permissionSets}}
	<span class="label label-default">{{@key}}</span>
	{{/each}}
</td>
<td>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>