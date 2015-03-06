Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {
    switch (operator) {
        case '==':
            return (v1 == v2) ? options.fn(this) : options.inverse(this);
        case '===':
            return (v1 === v2) ? options.fn(this) : options.inverse(this);
        case '<':
            return (v1 < v2) ? options.fn(this) : options.inverse(this);
        case '<=':
            return (v1 <= v2) ? options.fn(this) : options.inverse(this);
        case '>':
            return (v1 > v2) ? options.fn(this) : options.inverse(this);
        case '>=':
            return (v1 >= v2) ? options.fn(this) : options.inverse(this);
        case '&&':
            return (v1 && v2) ? options.fn(this) : options.inverse(this);
        case '||':
            return (v1 || v2) ? options.fn(this) : options.inverse(this);
        default:
            return options.inverse(this);
    }
});

Handlebars.registerHelper('include', function(path, block) {
	var result = window.JST[path](this);
	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('eachProperty', function(context, options) {
    var ret = "";
    var first = true;

    for (var prop in context) {
        ret += options.fn({
        	property : prop,
        	value : context[prop],
        	first : first
        });

        first = false;
    }

    return ret;
});

// format an ISO date using Moment.js
// http://momentjs.com/
// moment syntax example: moment(Date("2011-07-18T15:50:52")).format("MMMM YYYY")
// usage: {{dateFormat creation_date format="MMMM YYYY"}}
Handlebars.registerHelper('dateFormat', function(context, block) {
	if (window.moment) {
		var f = block.hash.format || "MMM Do, YYYY";
		return moment(context).format(f);
	} else {
		return context; // moment plugin not available. return data as is.
	};
});

Handlebars.registerHelper('translate', function(value, block) {
	return block.hash[value] || value; 
});

Handlebars.registerHelper('upper', function(value, block) {
	return value != null ? value.toUpperCase() : null;
});

var g_modelNames = ['model'];

Handlebars.registerHelper('model', function(name, block) {
	try {
		g_modelNames.push(name);

		return new Handlebars.SafeString(block.fn(this));
	} finally {
		g_modelNames.pop();
	}
});

Handlebars.registerHelper('action', function(entity, block) {
	if (!block) {
		block = entity;
		entity = null;
	}
	
	entity = entity || g_modelNames[g_modelNames.length - 1];
	var result = this.id ? (can('update', entity) ? 'Edit' : 'View') : 'Create';
	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('checkedIfContains', function(list, value, block) {
	var result = _.contains(list, value) ? ' checked="checked"' : '';
	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('permissionChecked', function(list, value, action, block) {
	var result = _.contains(list, value + ':' + action) ? ' checked="checked"' : '';
	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('checkedIfExists', function(obj, property, block) {
	var result = obj[property] ? ' checked="checked"' : '';
	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('selectedAttr', function(current, value, block) {
	var result = current == value ? ' selected="selected"' : '';
	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('option', function(current, value, text, block) {
	var result = '<option value="' + Handlebars.Utils.escapeExpression(value) + '"' + (current == value ? ' selected="selected"' : '') + '>' + Handlebars.Utils.escapeExpression(text) + '</option>';
	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('bind', function(name, block) {
	var model = block.hash.model || g_modelNames[g_modelNames.length - 1];

	var result = 'id="' + model + '.' + name + '" name="' + name +
		'" rv-value="' + model + ':' + name +
		(block.hash.formatter ? ' | ' + block.hash.formatter : '') + '"' +
		(block.hash.required ? ' required="required"' : '');

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('checked', function(name, block) {
	var model = block.hash.model || g_modelNames[g_modelNames.length - 1];

	var result = 'rv-checked="' + model + ':' + name + '"';

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('input', function(name, label, block) {
	name  = Handlebars.Utils.escapeExpression(name);
	label = Handlebars.Utils.escapeExpression(label);
	var model = block.hash.model || g_modelNames[g_modelNames.length - 1];
	var type = block.hash.type || "text";

	var result = '<div class="form-group">' +
		'<label for="' + model + '.' + name + '">' + label +
		(block.hash.required ? ' <i class="glyphicon glyphicon-asterisk" title="Required"></i>' : '') +
		'</label>' +
		(block.hash.caption ? ' <small>(' + Handlebars.Utils.escapeExpression(block.hash.caption) + ')</small>' : '') +
		'<input type="' + type + '" class="form-control' +
		(block.hash.class ? '  ' + block.hash.class : '') +
		'" id="' + model + '.' + name + '" name="' + name +
		'" rv-value="' + model + ':' + name +
		(block.hash.formatter ? ' | ' + block.hash.formatter : '') + '"' +
		(block.hash.extras ? ' ' + block.hash.extras : '') +
		(block.hash.required ? ' required="required"' : '') +
		(block.hash.placeholder ? ' placeholder="' + Handlebars.Utils.escapeExpression(block.hash.placeholder) + '"' : '') +
		' />' +
		(block.hash.help ? ' <span class="help-block">' + Handlebars.Utils.escapeExpression(block.hash.help) + '</span>' : '') +
		'</div>';

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('select', function(name, label, block) {
	name  = Handlebars.Utils.escapeExpression(name);
	label = Handlebars.Utils.escapeExpression(label);
	var model = block.hash.model || g_modelNames[g_modelNames.length - 1];

	var result = '<div class="form-group">' +
		'<label for="' + model + '.' + name + '">' + label +
		(block.hash.required ? ' <i class="glyphicon glyphicon-asterisk" title="Required"></i>' : '') +
		'</label>' +
		(block.hash.caption ? ' <small>(' + Handlebars.Utils.escapeExpression(block.hash.caption) + ')</small>' : '') +
		'<select class="form-control' + 
		(block.hash.class ? '  ' + block.hash.class : '') +
		'" id="' + model + '.' + name + '" name="' + name + '" rv-value="' + model + ':' + name + 
		(block.hash.formatter ? ' | ' + block.hash.formatter : '') + '"' +
		(block.hash.extras ? ' ' + block.hash.extras : '') +
		(block.hash.required ? ' required="required"' : '') +
		'>' +
		block.fn(this) +
		'</select>' +
		(block.hash.help ? ' <span class="help-block">' + Handlebars.Utils.escapeExpression(block.hash.help) + '</span>' : '') +
		'</div>';

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('link', function(href, block) {
	var result = (href != null ? '<a href="' + href + '"' + (block.hash.newwindow ? ' target="blank"' : '') + '>' : '') +
		block.fn(this) +
		(block.hash.newwindow ? ' <i class="glyphicon glyphicon-new-window"></i>' : '') +
		(href != null ? '</a>' : '');

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('icon', function(icon, block) {
	return new Handlebars.SafeString("glyphicon glyphicon-" + icon);
});

Handlebars.registerHelper('dataType', function(value, block) {
	var result = '<select name="type" class="form-control">';

	result += '<option' + (_.isString(value) ? ' selected="selected"' : '') + '>string</option>';
	result += '<option' + (_.isNumber(value) ? ' selected="selected"' : '') + '>number</option>';
	result += '<option' + (_.isBoolean(value) ? ' selected="selected"' : '') + '>boolean</option>';

	result += '</select>';

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('dataValue', function(value, block) {
	var result;

	if (_.isString(value)) {
		result = '<input type="text" name="value" class="form-control" value="' + Handlebars.Utils.escapeExpression(value) + '" required="required" />';
	} else if (_.isNumber(value)) {
		result = '<input type="number" name="value" class="form-control" value="' + value + '" required="required" />';
	} else if (_.isBoolean(value)) {
		result = '<div class="checkbox"><label><input type="checkbox" name="value" value="true"' + (value ? ' checked="checked"' : '') + ' /> Enabled</label></div>';
	}

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('textarea', function(name, label, block) {
	name  = Handlebars.Utils.escapeExpression(name);
	label = Handlebars.Utils.escapeExpression(label);
	var model = block.hash.model || g_modelNames[g_modelNames.length - 1];

	var result = '<div class="form-group">' +
		'<label for="' + model + '.' + name + '">' + label +
		(block.hash.required ? ' <i class="glyphicon glyphicon-asterisk" title="Required"></i>' : '') +
		'</label>' +
		(block.hash.caption ? ' <small>(' + Handlebars.Utils.escapeExpression(block.hash.caption) + ')</small>' : '') +
		'<textarea class="form-control' +
		(block.hash.class ? '  ' + block.hash.class : '') +
		'" rows="' + (block.hash.rows ? block.hash.rows : 3) + '" id="' + model + '.' + name + '" name="' + name +
		'" rv-value="' + model + ':' + name + (block.hash.formatter ? ' | ' + block.hash.formatter : '') + '"' +
		(block.hash.extras ? ' ' + block.hash.extras : '') +
		(block.hash.required ? ' required="required"' : '') +
		'></textarea>' +
		(block.hash.help ? ' <span class="help-block">' + Handlebars.Utils.escapeExpression(block.hash.help) + '</span>' : '') +
		'</div>';

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('can', function(action, entity, options) {
	if (!options) {
		options = entity;
		entity = null;
	}
	var conditional = can(action, entity);

	if (!conditional) {
		return options.inverse(this);
	} else {
		return options.fn(this);
	}
});

Handlebars.registerHelper('viewedit', function(entity, block) {
	return can("edit", entity) ? "Edit" : "View";
});

Handlebars.registerHelper('viewmanage', function(entity, block) {
	return can("edit", entity) ? "Manage" : "View";
});

Handlebars.registerHelper('configProperty', function(property) {
	var model = g_modelNames[g_modelNames.length - 1];
	var result = '';

	if (property.propertyType == 'boolean') {
		result += '<div class="col-sm-offset-3 col-sm-9">';
		result += '<div class="checkbox">';
		result += '<label>';
		result += '<input type="checkbox" name="' + property.propertyName + '" rv-checked="' + model + ':' + property.propertyName + '"> ' + Handlebars.Utils.escapeExpression(property.displayName);
		result += '</label>';
		result += '</div>';
		result += '</div>';
	} else {
		var type = 'text';
		var size = 9;
		var formatter = null;

		if (property.propertyType == 'integer' || property.propertyType == 'decimal') {
			type = "number";
			formatter = property.propertyType;
			size = 4;
		}

		result += '<label for="authenticator-property-' + property.propertyName + '" class="col-sm-3 control-label">' + Handlebars.Utils.escapeExpression(property.displayName) + (property.required ? ' <i class="glyphicon glyphicon-asterisk" title="Required"></i>' : '') + '</label>';
		result += '<div class="col-sm-' + size + '">';

		if (property.options && property.options.length > 0) {
			var type = property.multi ? 'radio' : 'checkbox';

			for (var i=0; i<property.options.length; i++) {
				result += '<label class="checkbox-inline">';
				result += '<input type="checkbox" name="' + property.propertyName + '" value="' + Handlebars.Utils.escapeExpression(property.options[i].value) + '" data-array="' + property.propertyName + '"> ' + Handlebars.Utils.escapeExpression(property.options[i].label);
				result += '</label>';
			}
		} else {
			if (property.propertyType == 'string' && property.multi) {
				result += '<textarea class="form-control an-' + property.propertyType + '" id="authenticator-property-' + property.propertyName + '" rv-value="' + model + ':' + property.propertyName + ' | stringList"></textarea>';
			} else {
				result += '<input type="' + type + '" class="form-control an-' + property.propertyType + '" id="authenticator-property-' + property.propertyName + '" rv-value="' + model + ':' + property.propertyName + (formatter ? ' | ' + formatter : '') + '" />';
			}
		}

		result += '</div>';
	}

	return new Handlebars.SafeString(result);
});

Handlebars.registerHelper('join', function(values, delim, block) {
	if (values == null) return '';
	if (delim == "\\n") delim = "\n";
	return values.join(delim);
});

Handlebars.registerHelper('joinClaims', function(claims, delim, block) {
	if (claims == null) return '';

	if (delim == "\\n") delim = "\n";
	var ret = '';

	_.each(claims, function(claim) {
		if (ret.length > 0) ret += delim;

		ret += claim.type + '=' + claim.value;
	});
	
	return ret;
});