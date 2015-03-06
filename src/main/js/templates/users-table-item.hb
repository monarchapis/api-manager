<td>
	{{#can "changePassword" "user"}}
	<div class="btn-group">
		<button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
			<i class="glyphicon glyphicon-cog"></i> <span class="caret"></span>
		</button>
		<ul class="dropdown-menu" role="menu">
			<li><a href="#" data-trigger="edit" data-id="{{id}}">{{viewedit "user"}} user</a></li>
			<li role="presentation" class="divider"></li>
			<li><a href="#" data-trigger="changePassword" data-id="{{id}}">Change password</a></li>
		</ul>
	</div>
	{{else}}
	<button type="button" class="btn btn-primary" data-trigger="edit" data-id="{{id}}"><i class="glyphicon glyphicon-edit"></i></button>
	{{/can}}
</td>
<td>{{userName}}</td>
<td>{{firstName}} {{lastName}}</td>
<td>
	<div>
		{{#if role.displayName}}
		{{role.displayName}}
		{{else}}
		No Access
		{{/if}}
	</div>
	{{#if administrator}}
	<label class="label label-default">System Administrator</label>
	{{/if}}
</td>
<td>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>