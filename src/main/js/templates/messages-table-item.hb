<td><button type="button" class="btn btn-primary" data-trigger="edit" data-id="{{id}}"><span class="glyphicon glyphicon-edit"></span></button></td>
<td>{{key}}</td>
<td>
	<dl style="margin: 0;">
	{{#each locales}}
	<dt>{{@key}}</dt>
	<dd>{{content}}</dd>
	{{/each}}
	</dl>
</td>
<td class="text-center">{{displayOrder}}</td>
<td>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>