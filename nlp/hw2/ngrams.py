import sys, getopt

# 4 output configurations:
# Unigram (n = 1)
# Bigram (n = 2)
#   No smoothing
#   +1 smoothing
#   Good-Touring smoothing

tokenCount = 0

counts = {}
unigram = {}

bigramCounts = {}
bigram = {}

def buildCounts():
    lines = open("test.txt", "r")
    for line in lines:
        word_tags = line.lower().split()
        for word_tag in word_tags:
            pair = word_tag.split('_')
            word = pair[0]
            count(word)
            global tokenCount
            tokenCount += 1

def count(word):
    if word not in counts.keys():
        counts[word] = 1
    else:
        counts[word] += 1

def countBi(prev, word):
    if prev not in bigramCounts.keys():
        bigramCounts[prev] = {}
        bigramCounts[prev][word] = 1
        bigram[prev] = {}
    else:
        if word not in bigramCounts[prev].keys():
            bigramCounts[prev][word] = 1
        else:
            bigramCounts[prev][word] += 1

def buildUnigram():
    uniqueWords = len(counts)
    for word,count in counts.items():
        unigram[word] = count/tokenCount

def buildBigram():
    prev = None
    lines = open("test.txt", "r")
    for line in lines:
        word_tags = line.lower().split()
        for word_tag in word_tags:
            pair = word_tag.split('_')
            word = pair[0]
            if prev is None:
                prev = word
            else:
                countBi(prev, word)
                prev = word
    for prev in bigramCounts:
        for word in bigramCounts[prev]:
            bigram[prev][word] = bigramCounts[prev][word]/counts[prev]


def main(argv):
    if len(sys.argv) < 5:
        helpmsg()
        sys.exit(2)

    try:
        opts, args = getopt.getopt(argv,'n:s:',['smoothing='])
    except getopt.GetoptError:
        helpmsg()
        sys.exit(2)

    for opt, arg in opts:
        if opt in ('-n'):
            global n
            n = arg
        elif opt in ('-s', '--smoothing'):
            global smoothing
            smoothing = arg

    print('n = ',n)
    print('smoothing = ',smoothing, '\n')

    buildCounts()
    print(counts)
    print(len(counts))

    #buildUnigram()
    #print(unigram)
    #writeToFile("unigram.txt", unigram)

    buildBigram()
    print(bigram)
    writeToFile("bigram.txt", bigram)
    
    try:
        test = bigram["brainpower"][","]
        print(test)
    except KeyError:
        print('nope')



def helpmsg():
    print('usage:   -n {1|2} -s {no|+1|gt}')
    print('-n   1 for unigram, 2 for bigram')
    print('-s  +1 for +1 smoothing, gt for Good-Touring, or no for no smoothing')

def writeToFile(filename, d):
    f = open(filename, "w")
    f.write(str(d))
    f.close()

if __name__ == '__main__':
    main(sys.argv[1:])
