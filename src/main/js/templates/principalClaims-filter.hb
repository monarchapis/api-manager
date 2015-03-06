<div class="well narrow">
	<div class="row">
		<div class="col-sm-8">
			<div class="form-horizontal">
				<div class="form-group">
					<label for="principalProfileIds" class="col-sm-3 control-label">Profile <i class="glyphicon glyphicon-asterisk" title="Required"></i></label>
					<div class="col-sm-7 col-md-5">
						<select id="principalProfileIds" name="profileId" class="form-control" required="required">
							<option value=""></option>
						</select>
					</div>
					<button type="button" class="btn btn-default" data-trigger="newProfile" data-collection="principalProfiles"><i class="fa fa-plus"></i><span class="sr-only">New</span></button>
					<button type="button" class="btn btn-default" data-trigger="editProfile" data-collection="principalProfiles" style="display: none;"><i class="fa fa-pencil-square-o"></i><span class="sr-only">Edit</span></button>
				</div>
				<div class="form-group">
					<label for="filter" class="col-sm-3 control-label">Filter by name</label>
					<div class="col-sm-7 col-md-5">
						<input type="text" id="filter" name="filter" class="form-control" />
					</div>
				</div>
			</div>
		</div>
		{{#can "create" "principalClaims"}}
		<div class="col-sm-4 text-right sm-add-top">
			<button type="button" class="btn btn-default" data-trigger="create" data-collection="principalClaims" style="display: none;">Create principal</button>
		</div>
		{{/can}}
	</div>
</div>