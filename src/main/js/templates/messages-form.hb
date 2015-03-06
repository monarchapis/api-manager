<form role="form">
	{{#model "message"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">{{action}} Message</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		<div class="row">
			<div class="col-md-4">
				{{input "key" "Key" required=true}}
			</div>
			<div class="col-md-5">
				{{#select "parentId" "Parent Key"}}
					<option value=""></option>
					{{#each messages}}
					<option value="{{id}}">{{key}}</option>
					{{/each}}
				{{/select}}
			</div>
			<div class="col-md-3">
				{{input "displayOrder" "Display Order" type="number" required=true}}
			</div>
		</div>
		<label>Message Content</label>
		<div class="well">
			<div class="form-group">
				<label>Locales</label> <small>(Select then modify below)</small>
				<div id="locale-picker" class="input-group">
					<input type="text" class="form-control" disabled="disabled" />
					<span class="input-group-btn">
						<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
							<span class="caret"></span>
							<span class="sr-only">Select locale</span>
						</button>
						<ul class="dropdown-menu pull-right" role="menu">
							{{#each locales}}
							<li><a href="#">{{@key}}</a></li>
							{{/each}}
							{{#can "update" "message"}}
							<li class="divider"></li>
							<li><a href="#" data-trigger="new">New</a></li>
							{{/can}}
						</ul>
					</span>
				</div>
			</div>
			<section name="locale" style="display: none;"></section>
		</div>
	</div>
	<div class="modal-footer">
		{{#if id}}
			{{#can "delete" "message"}}
			<div class="pull-left">
				<button type="button" class="btn btn-default" data-trigger="delete">Delete</button>
			</div>
			{{/can}}
		{{/if}}
		<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
		{{#if id}}
			{{#can "update" "message"}}
			<button type="submit" class="btn btn-primary">Save changes</button>
			{{/can}}
		{{else}}
			{{#can "create" "message"}}
			<button type="submit" class="btn btn-primary">Create</button>
			{{/can}}
		{{/if}}
	</div>
	{{/model}}
</form>