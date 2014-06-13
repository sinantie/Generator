import sys

if len(sys.argv) < 2:
    sys.stderr.write('Usage:' + sys.argv[0] + ' FILE\n')
    sys.exit(1)


with open(sys.argv[1], 'r') as f:
    for line in f:
		print len(line.split())
