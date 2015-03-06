{{#if filtered}}
<h3 class="text-center">Your search criteria did not match any {{plural}}.</h3>
{{else}}
<h3 class="text-center">There are currently no {{plural}}.</h3>
{{#can "create" singular}}
<h4 class="text-center">You can get started by clicking the "Create {{singular}}" button above.</h4>
{{/can}}
{{/if}}