<td><button type="button" class="btn btn-primary" data-trigger="edit" data-id="{{id}}"><span class="glyphicon glyphicon-edit"></span></button></td>
<td>{{name}}</td>
<td>{{description}}</td>
<td>{{translate type action="Action" entity="Entity"}}</td>
<td>{{translate scope client="Client only" user="User only" both="Client + User"}}</td>
<td>{{message.key}}</td>
<td>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>