import re


# Move level of logs from front to back
# LOG.debug()...  -> LOG....debug();
file_name = "/home/mtoth/Desktop/logy/hadoop-all-prod-log-export.txt"
output_name = "output.log"
f = list(enumerate(open(file_name, "rw+b")))
out_file = open(output_name, "w")

for i, line in f:
	line = line.rstrip()
	match = re.search('LOG\.(trace|debug|info|warn|error|fatal)\(\)', line)

	if match:		
		newline = line[:match.start()+3] + line[match.end():-1] + line[match.start()+3:match.end()] + ";"
		print line
		print 'newline=', newline
		line = newline
	out_file.write(line+"\n")