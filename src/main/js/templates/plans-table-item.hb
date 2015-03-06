<td><button type="button" class="btn btn-primary" data-trigger="edit" data-id="{{id}}"><i class="glyphicon glyphicon-edit"></i></button></td>
<td>{{name}}</td>
<td>
	{{#each quotas}}
	<span class="label label-default">{{requestCount}} / {{timeUnit}}</span>
	{{/each}}
</td>
<td>{{priceAmount}} {{priceCurrency}}</td>
<td>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Created:</span> {{createdBy}} @ {{dateFormat createdDate format="MM/DD/YYYY h:mm A"}}</small></div>
	<div class="nowrap"><small><span class="muted text-inline" style="width: 55px;">Modified:</span> {{modifiedBy}} @ {{dateFormat modifiedDate format="MM/DD/YYYY h:mm A"}}</small></div>
</td>