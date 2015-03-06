<form role="form" autocomplete="off">
	{{#model "password"}}
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		<h4 class="modal-title">Change Password</h4>
	</div>
	<div class="modal-body">
		<div id="modal-errors"></div>
		{{input "password" "Password" type="password" extras="autocomplete=\"off\" autocapitalize=\"off\" autocorrect=\"off\"
						spellcheck=\"false\"" required=true}}
		{{input "confirm" "Confirm" type="password" extras="autocomplete=\"off\" autocapitalize=\"off\" autocorrect=\"off\"
						spellcheck=\"false\"" required=true}}
	</div>
	<div class="modal-footer">
		<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
		<button type="submit" class="btn btn-primary">Set password</button>
	</div>
	{{/model}}
</form>