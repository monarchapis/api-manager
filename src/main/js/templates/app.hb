<div id="sticky-wrap">
	<header>
		<div class="navbar-wrapper">
			<div class="navbar">
				<div class="navbar-inner">
					<div class="container" style="position: relative;">
						<!-- Brand and toggle get grouped for better mobile display -->
						<div class="navbar-header">
							<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navigation">
								<span class="sr-only">Toggle navigation</span>
								<span class="icon-bar"></span>
								<span class="icon-bar"></span>
								<span class="icon-bar"></span>
							</button>
							<span class="navbar-brand brand"><span>Monarch API Manager</span></span>
						</div>
						<div id="navigation" class="collapse navbar-collapse pull-right">
							<ul id="menu">
								<li class="active"><a href="#dashboard"><i class="flaticon-dashboard2"></i><div>Dashboard</div></a></li>
								<li><a href="#partners"><i class="flaticon-hans"></i><div>Partners</div></a></li>
								<li><a href="#apis"><i class="flaticon-cogs3"></i><div>APIs</div></a></li>
								<li><a href="#access"><i class="flaticon-group16"></i><div>Access</div></a></li>
								<li><a href="#analytics"><i class="flaticon-analytics2"></i><div>Analytics</div></a></li>
								<li><a href="#logs"><i class="flaticon-list3"></i><div>Logs</div></a></li>
							</ul>
						</div><!--/.nav-collapse -->
					</div>
				</div>
			</div>
		</div> <!-- /.navbar-wrapper -->
	</header>

	<div id="session-info">
		<div class="container text-right">
			<div style="position: relative;">
				Hello, <span id="user-name">...</span> |
				<label>Environment</label>:
				<span id="environment-name">...</span>
				<a id="settings-menu" href="#settings" class="dropdown-toggle" data-toggle="dropdown"><i class="fa fa-cog"></i></a>
				<ul id="environment-menu" class="dropdown-menu pull-right text-left" role="menu" aria-labelledby="settings-menu"></ul>
			</div>
		</div>
	</div> <!-- /#session-info -->

	<div id="environment-info" class="hidden-xs">
	</div>

	<div id="main" class="container">
	</div> <!-- /#main -->
</div> <!-- /#sticky-wrap -->

<footer>
	<div id="footer-links">
		<div class="container">
			<div class="row">
				<div class="col-md-3 col-xs-6">
					<span class="fa fa-book"></span> <a href="http://www.monarchapis.com/docs" rel="tooltip" title="Documentation" target="_blank">Documentation</a>
				</div>
				<div class="col-md-3 col-xs-6">
					<span class="fa fa-google-plus"></span> <a href="https://groups.google.com/d/forum/monarchapis" rel="tooltip" title="Google Plus" target="_blank">Discuss</a> in Google Groups
				</div>
				<div class="col-md-3 col-xs-6">
					<span class="fa fa-github"></span> <a href="https://github.com/monarchapis/api-manager/issues" rel="tooltip" title="Github" target="_blank">Issue tracker</a> on Github
				</div>
				<div class="col-md-3 col-xs-6">
					<span class="fa fa-twitter"></span> <a href="http://www.twitter.com/monarchapis" rel="tooltip" title="Twitter" target="_blank">@monarchapis</a> on Twitter
				</div>
			</div>
		</div>
	</div> <!-- /#footer-links -->

	<div id="copyright">
		<div class="container">
			<div class="row">
				<div class="col-xs-10">
					<p>&copy; Copyright 2015 by <a href="http://www.captechconsulting.com">CapTech Ventures, Inc.</a>&nbsp; All Rights Reserved.</p>
				</div>
				<div class="col-xs-2">
					<div id="back-to-top"><a href="#"></a></div>
				</div>
			</div>
		</div>
	</div> <!-- /#copyright -->
</footer>