<div id="sticky-wrap">
	<div class="container">
		<form class="form-signin">
			<header>
				<span>Monarch API Manager</span>
			</header>
			<h2>Welcome to Monarch API Manager</h2>
			<div class="row">
				<div class="col-md-6">
					<p>An API management solution:  All the tools you need to quickly deploy, manage, and analyze your APIs.</p>
					<p><a href="http://www.monarchapis.com">Learn more</a></p>
				</div>
				<div class="col-md-6 border-left">
					<div id="initializing-message">
						<h3>Initializing...</h3>
					</div>
					<div id="initial-environment" style="display: none;">
						<h3>There are currently no environments.</h3>

						<h4>You can get started by clicking the "Create environment" button below.</h4>
						<br />
						<p class="text-center">
							<button id="create-initial-environment" data-collection="environments" data-trigger="create" class="btn btn-primary" type="button">Create environment</button>
						</p>
					</div>
					<div id="access-denied" style="display: none;">
						<h3>Access Denied.</h3>
						<h4>An administrator must grant you access to an environment before proceeding.</h4>
					</div>
				</div>
			</div>
		</form>
	</div> <!-- /container -->
</div>

<footer>
	<div id="footer-links">
		<div class="container">
			<div class="row">
				<div class="col-md-3 col-sm-6">
					<span class="fa fa-book"></span> <a href="#" rel="tooltip" title="Documentation">Documentation</a>
				</div>
				<div class="col-md-3 col-sm-6">
					<span class="fa fa-google-plus"></span> <a href="#" rel="tooltip" title="Google Plus">Discuss</a> in Google Groups
				</div>
				<div class="col-md-3 col-sm-6">
					<span class="fa fa-github"></span> <a href="#" rel="tooltip" title="Github" target="_blank">Issue tracker</a> on Github
				</div>
				<div class="col-md-3 col-sm-6">
					<span class="fa fa-twitter"></span> <a href="http://www.twitter.com/monarchapis" rel="tooltip" title="Twitter" target="_blank">@monarchapis</a> on Twitter
				</div>
			</div>
		</div>
	</div> <!-- /#footer-links -->

	<div id="copyright">
		<div class="container">
			<p>&copy; Copyright 2014 by <a href="http://www.captechconsulting.com">CapTech Ventures, Inc.</a>&nbsp; All Rights Reserved.</p>
		</div>
	</div> <!-- /#copyright -->
</footer>