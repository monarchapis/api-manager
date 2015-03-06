<div class="tabbable">
	<ul class="nav nav-tabs">
		{{#eachProperty tabs}}
		<li{{#if first}} class="active"{{/if}}><a data-toggle="tab" data-name="{{property}}" href="#{{../$cid}}-{{property}}">{{value.text}}</a></li>
		{{/eachProperty}}
	</ul>
	<div class="tab-content {{#if maxHeight}} scrollable{{/if}}"{{#if maxHeight}} style="max-height: {{maxHeight}};"{{/if}}>
		{{#eachProperty tabs}}
		<div id="{{../$cid}}-{{property}}" data-region="{{property}}" class="tab-pane fade{{#if first}} in active{{/if}}">
			<div class="loading">Loading...</div>
		</div>
		{{/eachProperty}}
	</div>
</div>