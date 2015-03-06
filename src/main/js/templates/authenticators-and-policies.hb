<label>Authenticators</label>
<section name="authenticators">
	<div class="panel-group" id="authenticator-accordion">
		{{#model "authenticator"}}
		{{#each authenticatorConfigs}}
		<div class="panel panel-default">
			<div class="panel-heading">
				<h4 class="panel-title">
					<input type="checkbox" id="authenticator-enabled-{{@index}}" />
					<a data-toggle="collapse" data-parent="#authenticator-accordion" href="#authenticator-{{@index}}">
					{{this.displayName}}
					</a>
				</h4>
			</div>
			<div id="authenticator-{{@index}}" class="panel-collapse collapse">
				<div class="panel-body">
					<div class="form-horizontal">
						{{#each properties}}
						<div class="form-group">
							{{configProperty this}}
						</div>
						{{/each}}
					</div>
				</div>
			</div>
		</div>
		{{/each}}
		{{/model}}
	</div>
</section>
<br />
<div class="row">
	<div class="col-sm-6 form-group">
		<label>Policies</label>
	</div>
	<div class="col-sm-6 text-right form-group">
		<div class="btn-group">
			<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
			Add Policy <span class="caret"></span></button>
			<ul id="add-policy-menu" class="dropdown-menu pull-right" role="menu">
				{{#each policyConfigs}}
				<li><a href="#" data-value="{{name}}">{{displayName}}</a></li>
				{{/each}}
			</ul>
		</div>
	</div>
</div>
<section name="policies">
	<div class="panel-group add-bottom" id="policy-accordion">
	</div>
</section>