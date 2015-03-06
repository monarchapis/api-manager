<label>Service Permissions</label>
<div class="well">
	<section name="permissions">
		<div class="row">
			<div class="col-md-offset-1 col-md-5">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="authenticate" {{checkedIfContains permissions "authenticate"}}/> Authenticate API requests
					</label>
				</div>
			</div>
			<div class="col-md-offset-1 col-md-5">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="events" {{checkedIfContains permissions "events"}}/> Create events
					</label>
				</div>
			</div>
			<div class="col-md-offset-1 col-md-5">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="delegate" {{checkedIfContains permissions "delegate"}}/> Delegate access
					</label>
				</div>
			</div>
			<div class="col-md-offset-1 col-md-5">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="revoke" {{checkedIfContains permissions "revoke"}}/> Revoke access
					</label>
				</div>
			</div>
		</div>
	</section>
</div>