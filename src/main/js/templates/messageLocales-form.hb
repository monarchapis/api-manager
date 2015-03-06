<hr />

{{#model "messageLocale"}}

{{#select "format" "Format" required=true}}
	<option value="text">Plain Text</option>
	<option value="html">HTML</option>
	<option value="markdown">Markdown</option>
{{/select}}
{{textarea "content" "Content" required=true}}

{{#can "update" "message"}}
<div class="text-right">
	<button type="button" class="btn btn-default" data-trigger="remove"><i class="glyphicon glyphicon-trash"></i> Remove</button>
</div>
{{/can}}

{{/model}}