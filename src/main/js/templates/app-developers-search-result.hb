<td>
	<div style="margin-bottom: 5px;"><label class="label label-default">{{username}}</label></div>
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
	<div class="form-inline">
		<select name="role" class="form-control" style="width: auto;" data-id="{{id}}">
			<option value="">Not a member</option>
			{{option role "operations" "Operations"}}
			{{option role "developer" "Developer"}}
			{{option role "administrator" "Administrator"}}
		</select>
		<span class="processing"></span>
	</div>
</td>