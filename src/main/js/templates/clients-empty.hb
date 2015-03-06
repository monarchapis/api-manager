{{#if filtered}}
<h3 class="text-center">Your search criteria did not match any clients.</h3>
{{else}}
<h3 class="text-center">There are currently no clients.</h3>
{{#can "create" "client"}}
<h4 class="text-center">You can get started by creating a new client for an application from the Applications tab.</h4>
{{/can}}
{{/if}}