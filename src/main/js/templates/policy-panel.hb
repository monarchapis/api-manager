<div class="panel panel-default">
	<div class="panel-heading">
		<h4 class="panel-title">
			<i class="fa fa-sort fa-fw dnd-handle"></i> <a class="collapsed" data-toggle="collapse" data-parent="#policy-accordion" href="#policy-{{id}}">
			{{displayName}}
			</a>
			<a href="#" data-trigger="remove" class="pull-right btn btn-default btn-sm"><i class="glyphicon glyphicon-trash"></i><span class="sr-only"> Delete</span></a>
		</h4>
	</div>
	<div id="policy-{{id}}" class="panel-collapse collapse">
		<div class="panel-body">
			<div class="form-horizontal">
				{{#model "policy"}}
				{{#each properties}}
				<div class="form-group">
					{{configProperty this}}
				</div>
				{{/each}}
				{{/model}}
			</div>
		</div>
	</div>
</div>