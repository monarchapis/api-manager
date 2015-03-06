<td>
	<h3>{{#link applicationUrl newwindow=true}}{{name}}{{/link}}</h3>
	<div><small>by {{#link companyUrl newwindow=true}}{{companyName}}{{/link}}</small></div>
</td>
<td>{{description}}</td>
<td>
	<div class="form-inline">
		<select name="role" class="form-control" style="width: auto;" data-id="{{id}}">
			{{option role "operations" "Operations"}}
			{{option role "developer" "Developer"}}
			{{option role "administrator" "Administrator"}}
		</select>
		<span class="processing"></span>
	</div>
</td>
<td>
	<button type="button" class="btn btn-default" data-trigger="delete" data-id="{{id}}"><span class="glyphicon glyphicon-trash"></span><span class="sr-only"> Delete</span></button>
</td>