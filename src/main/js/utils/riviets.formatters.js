rivets.formatters.stringList = {
	read: function(value) {
		return value != null ? value.join("\n") : null;
	},
	publish: function(value) {
		if (value != null) {
			value = value.trim();
			return value.length > 0 ? value.split("\n") : [];
		}

		return null;
	}
}

rivets.formatters.integer = {
	read: function(value) {
		return value;
	},
	publish: function(value) {
		return value ? parseInt(value) : null;
	}
}

rivets.formatters.decimal = {
	read: function(value) {
		return value;
	},
	publish: function(value) {
		return value ? parseFloat(value) : null;
	}
}

function formatDuration(sec_num) {
    var hours   = Math.floor(sec_num / 3600);
    var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
    var seconds = sec_num - (hours * 3600) - (minutes * 60);

    if (hours   < 10) {hours   = "0"+hours;}
    if (minutes < 10) {minutes = "0"+minutes;}
    if (seconds < 10) {seconds = "0"+seconds;}
    var time    = hours+':'+minutes+':'+seconds;

    return time;
}

rivets.formatters.timespan = {
	read: function(value) {
		return value != null ? formatDuration(value) : null;
	},
	publish: function(value) {
		if (value) value = value.trim();
		
		if (value != null && value.length > 0) {
			if (value.indexOf(':') != -1) {
				return moment.duration(value).asSeconds();
			} else {
				return parseInt(value);
			}
		}
		
		return null;
	}
}