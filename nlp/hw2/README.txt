Prerequisites to running the Program:
    Python 3.8.0


To run the Program execute the following command in a command-line terminal:
    python3 ngrams.py -n {1|2} -s {no|+1|gt}


Available options:
    -n:
        1 for unigram
        2 for bigram

    -s:
        no for no smoothing
        +1 for +1 smoothing
        gt for Good-Touring smoothing


Example usages:
    Compute bigram model with no smoothing
        python3 ngrams.py -n
            OR
        python3 ngrams.py -n 2 -s no

    Compute bigram model with +1 smoothing
        python3 ngrams.py -n 2 -s +1

    Compute bigram model with Good-Touring smoothing
        python3 ngrams.py -n 2 -s gt

    Compute unigram model
        python3 ngrams.py -n 1
    Note that the unigram model does not support smoothing


Output file format:
    The unigram model is output to a file named:
        unigram.txt
    The bigram models are output to a file named with the following structure (not including { }, and | denoting OR):
        bigram_{no|+1|gt}_smoothing.txt

    Each line of the output file represents either a unigram or a bigram, followed by its probability, all separated by a whitespace eg:
        word p(word)
        word1 word2 p(word2 | word1)


To use the output file to calculate probabilities of input sentences:
    I'd recommend using a search function or tool, and to search for the desired unigram or bigram.
    As an example, the grep command to find p(word2 | word1) in the bigram model with no smoothing would be:
        grep 'word1 word2' bigram_no_smoothing.txt
    
    To search for a unigram using grep:
        grep 'word' unigram.txt

Given the input sentence:
    The standard Turbo engine is hard to work

The following grep commands may be useful:

grep -E '(^the )|(^standard )|(^turbo )|(^engine )|(^is )|(^hard )|(^to )|(^work )' unigram.txt

grep -E '(^the standard )|(^standard turbo )|(^turbo engine )|(^engine is )|(^is hard )|(^hard to )|(^to work )' bigram_no_smoothing.txt
