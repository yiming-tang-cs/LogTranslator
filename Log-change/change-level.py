import re

#f = list(enumerate(open("change logs", "rw+b")))
f = list(enumerate(open("test-logs", "rw+b")))
out_file = open('output.log', "w")

for i, line in f:
	line = line.rstrip()
	match = re.search('LOG\.(trace|debug|info|warn|error|fatal)\(\)', line)

	if match:
		newline = line[:match.start()+3] + line[match.end():-1] + line[match.start()+3:match.end()] + ";"
#		print line + "\n"
#		print 'newline=', newline
		line = newline
	out_file.write(line+"\n")