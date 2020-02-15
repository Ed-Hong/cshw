import sys, getopt

# 4 output configurations:
# Unigram (n = 1)
# Bigram (n = 2)
#   No smoothing
#   +1 smoothing
#   Good-Touring smoothing

def main(argv):
    n = 1
    smoothing = 'none'

    if len(sys.argv) < 5:
        print('usage:   -n {1|2} -s {no|+1|gt}')
        print('-n   1 for unigram, 2 for bigram')
        print('-s  +1 for +1 smoothing, gt for Good-Touring, or no for no smoothing')
        sys.exit(2)

    try:
        opts, args = getopt.getopt(argv,'n:s:',['smoothing='])
    except getopt.GetoptError:
        print('usage:   -n {1|2} -s {no|+1|gt}')
        print('-n   1 for unigram, 2 for bigram')
        print('-s   +1 for +1 smoothing, gt for Good-Touring, or no for no smoothing')
        sys.exit(2)

    for opt, arg in opts:
        if opt in ('-n'):
            n = arg
        elif opt in ('-s', '--smoothing'):
            smoothing = arg

    print('n = ',n)
    print('smoothing = ',smoothing)

if __name__ == '__main__':
    main(sys.argv[1:])
