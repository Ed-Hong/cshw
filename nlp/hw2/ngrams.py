import sys, getopt

# 4 output configurations:
# Unigram (n = 1)
# Bigram (n = 2)
#   No smoothing
#   +1 smoothing
#   Good-Touring smoothing

n = 1
smoothing = "no"

tokenCount = 0

counts = {}
unigram = {}

bigramCounts = {}
bigram = {}

histogram = {} # <k,v>: the number of ngrams, v, which occur k times

def count(word):
    if word not in counts.keys():
        counts[word] = 1
    else:
        counts[word] += 1

    if smoothing == "gt":
        if counts[word] not in histogram.keys():
            histogram[counts[word]] = 1
        else:
            histogram[counts[word]] += 1

def buildCounts():
    lines = open("corpus.txt", "r")
    for line in lines:
        word_tags = line.lower().split()
        for word_tag in word_tags:
            pair = word_tag.split('_')
            word = pair[0]
            count(word)
            global tokenCount
            tokenCount += 1

def buildUnigram():
    uniqueWords = len(counts)
    for word,count in counts.items():
        unigram[word] = count/tokenCount

def buildBigramCounts():
    # init bigramCounts with all zeroes (or ones if +1 smoothing)
    for word in counts.keys():
        bigramCounts[word] = {}
        for w in counts.keys():
            if smoothing == "+1":
                bigramCounts[word][w] = 1
            else:
                bigramCounts[word][w] = 0

    # count bigrams
    lines = open("corpus.txt", "r")
    for line in lines:
        word_tags = line.lower().split()
        prev = None
        for word_tag in word_tags:
            pair = word_tag.split('_')
            word = pair[0]
            if prev is None:
                prev = word
            else:
                bigramCounts[prev][word] += 1
                prev = word

    # Good-Touring discounting smoothed counts
    if smoothing == "gt":
        for prev in bigramCounts:
            for word in bigramCounts[prev]:
                c = bigramCounts[prev][word]
                if c > 0:
                    cstar = (c+1) * (histogram[c+1] / histogram[c])
                    bigramCounts[prev][word] = cstar

def buildBigram():
    buildBigramCounts()
    bigram.update(bigramCounts)
    for prev in bigramCounts:
        for word in bigramCounts[prev]:
            if smoothing == "+1":
                bigram[prev][word] = bigramCounts[prev][word] / (counts[prev] + len(counts))
            elif smoothing == "gt":
                c = bigramCounts[prev][word]
                if c == 0:
                    bigram[prev][word] = histogram[1] / tokenCount
                else:
                    bigram[prev][word] = bigramCounts[prev][word] / tokenCount
            else:
                bigram[prev][word] = bigramCounts[prev][word] / counts[prev]

def helpmsg():
    print('usage:   -n {1|2} -s {no|+1|gt}')
    print('-n   1 for unigram, 2 for bigram')
    print('-s  +1 for +1 smoothing, gt for Good-Touring, or no for no smoothing')

def writeToFile(filename):
    f = open(filename, "w")
    if n == 1:
        for word in unigram.keys():
            f.write(word + " " + str(unigram[word]) + "\n")
    else:
        for prev in bigram.keys():
            for word in bigram[prev]:
                f.write(prev + " " + word + " " + str(bigram[prev][word]) + "\n")
    f.close()

def main(argv):
    processArgs(argv)
    print('n =', n, ' smoothing =', smoothing, '\n')

    buildCounts()
    if n == 1:
        buildUnigram()
        writeToFile("unigram.txt")
    else:
        buildBigram()
        writeToFile("bigram_" + smoothing + "_smoothing.txt")
    
    # test debug
    #try:
    #    test = bigram["brainpower"][","]
    #    print(test)
    #except KeyError:
    #    print('nope')

def processArgs(argv):
    if len(sys.argv) < 3:
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
            n = int(arg)
        elif opt in ('-s', '--smoothing'):
            global smoothing
            smoothing = arg

if __name__ == '__main__':
    main(sys.argv[1:])
