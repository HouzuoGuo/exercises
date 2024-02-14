#!/usr/bin/env -S awk -f
BEGIN {
	print "start"
}

match($1, /(Ubuntu)/) {
	key = gensub(/^(.*)=.*/, "\\1", "g", $0)
	value = gensub(/^(.*)=(.*)/, "\\2", "g", $0)
	explain = gensub(/^(.*)=(.*)/, "key: \\1, val: \\2", "g", $0)
	print "key is", key
	print "value is", value
	print explain
	kv[key] = value
	count++
}

END {
	print "done"
	print "count: ", count
	for (key in kv) {
		print "key: ", key
		print "value: ", kv[key]
	}
}
