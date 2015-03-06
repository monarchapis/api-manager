<div class="well narrow">
	<div class="row">
		<div class="col-sm-8">
			<label class="no-bottom">
				{{#if label}}
					{{label}}
				{{else}}
					Filter:
				{{/if}}
				&nbsp;
				<input type="text" name="filter" class="form-control" style="width: 250px; display: inline-block;" />
			</label>
		</div>
		{{#can "create" singular}}
		<div class="col-sm-4 text-right sm-add-top">
			<button type="button" class="btn btn-default" data-trigger="create" data-collection="{{plural}}">Create {{singular}}</button>
		</div>
		{{/can}}
	</div>
</div>