<td><button type="button" class="btn btn-primary" data-trigger="edit" data-id="{{id}}"><i class="glyphicon glyphicon-edit"></i></button></td>
<td>{{roleName}}</td>
<td>{{displayName}}</td>
<td>
	<div><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>