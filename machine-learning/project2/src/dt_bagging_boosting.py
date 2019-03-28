import random
import math
import numpy as np
import matplotlib.pyplot as plt
from sklearn import tree
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import AdaBoostClassifier

def partition(x):
    """
    Partition the column vector x into subsets indexed by its unique values (v1, ... vk)

    Returns a dictionary of the form
    { v1: indices of x == v1,
      v2: indices of x == v2,
      ...
      vk: indices of x == vk }, where [v1, ... vk] are all the unique values in the vector z.
    """

    # Build our dictionary
    d = {}
    for i, v in enumerate(x):
        # If the dictionary contains a key entry for v, then append the index i to its list of indices
        if v in d:
            d[v].append(i)
        # Otherwise, create a new key entry for v and initialize the index list with i
        else:
            d[v] = [i]
    return d


def entropy(y):
    """
    Compute the entropy of a vector y by considering the counts of the unique values (v1, ... vk), in z

    Returns the entropy of z: H(z) = p(z=v1) log2(p(z=v1)) + ... + p(z=vk) log2(p(z=vk))
    """

    # Initialize the total entropy H to be 0
    H = 0.0

    yPartitioned = partition(y)

    for v in yPartitioned:
        # Compute P(z = v)
        # The probability of z = v is the number of times v occurs in z divided by the length of z
        p = len(yPartitioned[v]) / len(y)

        # Add to the total entropy
        H -= p * np.log2(p)

    return H

def entropy_weighted(y, d):
    """
    Compute the entropy of a vector y by considering the WEIGHTED counts of the unique values (v1, ... vk), in z
    
    For examples which were misclassified, their weights would be increased -> weighted count increases -> p increases -> H decreases
    
    The lower entropy means the criterion becomes a better candidate for the next split, as the greedy ID3 algorithm will select the 
    attribute-value pair that minimizes entropy.
    
    Returns the entropy of z with respect to the weighted examples: H(z) = p(z=v1) log2(p(z=v1)) + ... + p(z=vk) log2(p(z=vk))
    """

    # Initialize the total entropy H to be 0
    H = 0.0

    yPartitioned = partition(y)

    for v in yPartitioned:
        # Compute the WEIGHTED count 
        weighted_count = 0.0
        for example_index in yPartitioned[v]:
            weighted_count += d[example_index]

        # Compute P(z = v)
        # The probability of z = v is the weighted count of v divided by the length of z
        p = weighted_count / len(y)

        # Add to the total entropy
        H -= (p * np.log2(p))

    return H    


def mutual_information(x, y):
    """
    Compute the mutual information between a data column (x) and the labels (y). The data column is a single attribute
    over all the examples (n x 1). Mutual information is the difference between the entropy BEFORE the split set, and
    the weighted-average entropy of EACH possible split.

    Returns the mutual information: I(x, y) = H(y) - H(y | x)
    """
    
    xPartitioned = partition(x)

    # Dictionary which      { v1: P(x=v1),
    # maps the                v2: P(x=v2),    
    # Probabilities of x:     ...
    #                         vk: P(x=vk) }
    probs = {}

    # Dictionary which      { v1: H(Y | x=v1),
    # maps the entropies      v2: H(Y | x=v1),    
    # of y given x = v:       ...
    #                         vk: H(Y | x=v1) }
    hConditionals = {}

    for v in xPartitioned:
        # Compute probabilities P(x = v_i) and save to our probs dictionary
        p = len(xPartitioned[v]) / len(x)
        probs[v] = p

        # Get indices where x = v
        indices = xPartitioned[v]

        # Data vector of y values given x = v
        yGivenX = []

        # Add the values of y given x = v to our vector
        for i in indices:
            yGivenX.append(y[i])

        # Compute H(Y | x = v )
        H_yGivenXequalsV = entropy(yGivenX)

        # Save the conditional entropy to our hConditionals dictionary
        hConditionals[v] = H_yGivenXequalsV

    # compute H(y|x)
    # P(x=v_i) * H(y|x=v_i) for all i
    H_yGivenX = 0.0
    for v in xPartitioned:
        H_yGivenX += probs[v] * hConditionals[v]

    # get entropy of y, H(y)
    H_y = entropy(y)

    # compute I(x, y) = H(y) - H(y | x)
    I_xy = H_y - H_yGivenX

    return I_xy


def id3(x, y, attribute_value_pairs=None, depth=0, max_depth=5):
    """
    Implements the classical ID3 algorithm given training data (x), training labels (y) and an array of
    attribute-value pairs to consider. This is a recursive algorithm that depends on three termination conditions
        1. If the entire set of labels (y) is pure (all y = only 0 or only 1), then return that label
        2. If the set of attribute-value pairs is empty (there is nothing to split on), then return the most common
           value of y (majority label)
        3. If the max_depth is reached (pre-pruning bias), then return the most common value of y (majority label)
    Otherwise the algorithm selects the next best attribute-value pair using INFORMATION GAIN as the splitting criterion
    and partitions the data set based on the values of that attribute before the next recursive call to ID3.

    The tree we learn is a BINARY tree, which means that every node has only two branches. The splitting criterion has
    to be chosen from among all possible attribute-value pairs. That is, for a problem with two features/attributes x1
    (taking values a, b, c) and x2 (taking values d, e), the initial attribute value pair list is a list of all pairs of
    attributes with their corresponding values:
    [(x1, a),
     (x1, b),
     (x1, c),
     (x2, d),
     (x2, e)]
     If we select (x2, d) as the best attribute-value pair, then the new decision node becomes: [ (x2 == d)? ] and
     the attribute-value pair (x2, d) is removed from the list of attribute_value_pairs.

    The tree is stored as a nested dictionary, where each entry is of the form
                    (attribute_index, attribute_value, True/False): subtree
    * The (attribute_index, attribute_value) determines the splitting criterion of the current node. For example, (4, 2)
    indicates that we test if (x4 == 2) at the current node.
    * The subtree itself can be nested dictionary, or a single label (leaf node).
    * Leaf nodes are (majority) class labels

    Returns a decision tree represented as a nested dictionary, for example
    {(4, 1, False):
        {(0, 1, False):
            {(1, 1, False): 1,
             (1, 1, True): 0},
         (0, 1, True):
            {(1, 1, False): 0,
             (1, 1, True): 1}},
     (4, 1, True): 1}
    """

    # Base cases:

    # 0. If the set of labels is empty within this branch, return null (ie: case where all examples are shifted left or right )
    if len(y) == 0:
        return None

    # 1. If the entire set of labels is pure, then return that label (which is the majority label)
    if all(v == 0 for v in y) or all (v == 1 for v in y):
        return y[0]

    # 2. If there is nothing to split on, then return the majority label
    if attribute_value_pairs is not None and len(attribute_value_pairs) == 0:
        return majority_label(y)

    # 3. If the max depth is reached, then return the majority label
    if depth == max_depth:
        return majority_label(y)

    # If attribute_value_pairs is null then initialize with all attribute value pairs
    if attribute_value_pairs is None:
        attribute_value_pairs = get_attribute_value_pairs(x)
    
    # Determine the next best attribute-value pair to split on by selecting the one which yields the most Information Gain
    # First, determine the best feature to split on
    attr_set = set()
    for a, v in attribute_value_pairs:
        attr_set.add(a)
    
    bestAttr, bestAttrIndex = find_best_attribute(x,y, attr_set)

    # Then, determine the best value of the feature to split on
    value_set = set()
    for aIdx, v in attribute_value_pairs:
        if aIdx == bestAttrIndex:
            value_set.add(v)
    
    bestValue = find_best_value(bestAttr, y, value_set)

    # Remove the attribute-value pair from the list of attribute-value pairs
    attribute_value_pairs.remove((bestAttrIndex, bestValue))

    # New data sets for the "Left" branch which denotes (x_bestAttrIndex == bestValue)? -> False
    xNewFalse = []
    yNewFalse = []

    # New data sets for the "Right" branch which denotes (x_bestAttrIndex == bestValue)? -> True
    xNewTrue = []
    yNewTrue = []

    # Remove the examples matching the attribute-value pair from x and y
    for rowIdx, row in enumerate(x):
        addLeft = True
        for i, v in enumerate(row):
            if i == bestAttrIndex and v == bestValue:
                addLeft = False
        if addLeft:
            xNewFalse.append(row)
            yNewFalse.append(y[rowIdx])
        else:
            xNewTrue.append(row)
            yNewTrue.append(y[rowIdx])
    
    # New decision node becomes [ (x_bestAttrIndex == bestValue)? ]
    node = { (bestAttrIndex, bestValue, False) : id3(xNewFalse, yNewFalse, attribute_value_pairs, depth+1),
             (bestAttrIndex, bestValue, True): id3(xNewTrue, yNewTrue, attribute_value_pairs,depth+1) }

    return node


def id3_boosting(x, y, d, attribute_value_pairs=None, depth=0, max_depth=5):
    """
    Implements the same ID3 algorithm as above, only modified to accept an input distribution of the weighted examples
    This is the Learn function which is called in the boosting algorithm

    Returns a decision tree represented as a nested dictionary as before, for example
    {(4, 1, False):
        {(0, 1, False):
            {(1, 1, False): 1,
             (1, 1, True): 0},
         (0, 1, True):
            {(1, 1, False): 0,
             (1, 1, True): 1}},
     (4, 1, True): 1}
    """

    # Base cases:

    # 0. If the set of labels is empty within this branch, return null (ie: case where all examples are shifted left or right )
    if len(y) == 0:
        return None

    # 1. If the entire set of labels is pure, then return that label (which is the majority label)
    if all(v == 0 for v in y) or all (v == 1 for v in y):
        return y[0]

    # 2. If there is nothing to split on, then return the majority label
    if attribute_value_pairs is not None and len(attribute_value_pairs) == 0:
        return majority_label(y)

    # 3. If the max depth is reached, then return the majority label
    if depth == max_depth:
        return majority_label(y)

    # If attribute_value_pairs is null then initialize with all attribute value pairs
    if attribute_value_pairs is None:
        attribute_value_pairs = get_attribute_value_pairs(x)
    
    # Determine the next best attribute-value pair to split on by selecting the one which yields the most Information Gain
    # First, determine the best feature to split on
    attr_set = set()
    for a, v in attribute_value_pairs:
        attr_set.add(a)
    
    bestAttr, bestAttrIndex = find_best_attribute(x,y, attr_set)

    # Then, determine the best value of the feature to split on taking into account the weights of the examples
    value_set = set()
    for aIdx, v in attribute_value_pairs:
        if aIdx == bestAttrIndex:
            value_set.add(v)
    
    bestValue = find_best_value_weighted(bestAttr, y, d, value_set)

    # Remove the attribute-value pair from the list of attribute-value pairs
    attribute_value_pairs.remove((bestAttrIndex, bestValue))

    # New data sets for the "Left" branch which denotes (x_bestAttrIndex == bestValue)? -> False
    xNewFalse = []
    yNewFalse = []

    # New data sets for the "Right" branch which denotes (x_bestAttrIndex == bestValue)? -> True
    xNewTrue = []
    yNewTrue = []

    # Remove the examples matching the attribute-value pair from x and y
    for rowIdx, row in enumerate(x):
        addLeft = True
        for i, v in enumerate(row):
            if i == bestAttrIndex and v == bestValue:
                addLeft = False
        if addLeft:
            xNewFalse.append(row)
            yNewFalse.append(y[rowIdx])
        else:
            xNewTrue.append(row)
            yNewTrue.append(y[rowIdx])
    
    # New decision node becomes [ (x_bestAttrIndex == bestValue)? ]
    node = { (bestAttrIndex, bestValue, False) : id3(xNewFalse, yNewFalse, attribute_value_pairs, depth+1),
             (bestAttrIndex, bestValue, True): id3(xNewTrue, yNewTrue, attribute_value_pairs,depth+1) }

    return node


def find_best_attribute(x, y, attr_set):
    """
    Determines the best feature to split on by selecting the one with the most Mutual Information between x and y 

    Returns the feature x which maximizes Information Gain as a column vector, and its index
    """

    # {attr_index, i : column vector x_i}
    attrs = {}
    for row in x:
        for i, val in enumerate(row):
            if i in attrs:
                attrs[i].append(val)
            else:
                attrs[i] = [val]

    # {attr_index, i : Info Gain with the attribute }
    attrs_InfoGain = {}
    for i in attrs:
        infoGain = mutual_information(attrs[i], y)
        attrs_InfoGain[i] = infoGain

    # Determine which feature/attribute has the most Mutual Information with y
    bestAttr = None
    bestAttrIndex = None
    while True:
        maxInfoGain = None
        for i in attrs_InfoGain:
            infoGainedByAttr = attrs_InfoGain[i]
            if maxInfoGain is None or infoGainedByAttr >= maxInfoGain:
                maxInfoGain = infoGainedByAttr
                bestAttr = attrs[i]
                bestAttrIndex = i

        if bestAttrIndex in attr_set:
            break
        else:
            attrs_InfoGain.pop(bestAttrIndex, None)

        if len(attrs_InfoGain.keys()) == 0:
            return list(attr_set)[0]
    
    return bestAttr, bestAttrIndex


def find_best_value(attr, y, value_set):
    """
    Determines the best value of the given attribute to split on by determining which value of the attribute yields the least entropy with y 

    Returns the value v 
    """

    bestAttrPartitioned = partition(attr)

    # {attr_value, V : H(Y | X = V)}
    vals_entropies = {}
    for v in bestAttrPartitioned:
        # Build the data vector of Y given x = v
        yGivenXequalsV = []
        for i in bestAttrPartitioned[v]:
            yGivenXequalsV.append(y[i])

        # Compute the entropy of Y given x = v and add to our dictionary
        H_yGivenXequalsV = entropy(yGivenXequalsV)
        vals_entropies[v] = H_yGivenXequalsV

    # Determine the value of the attribute which yields the lowest entropy in y
    bestAttrValue = None
    while True:
        minEntropy = None
        for v in vals_entropies:
            if minEntropy is None or vals_entropies[v] <= minEntropy:
                minEntropy = vals_entropies[v]
                bestAttrValue = v
        
        if bestAttrValue in value_set:
            break
        else:
            vals_entropies.pop(bestAttrValue, None)

        if len(vals_entropies.keys()) == 0:
            return list(value_set)[0]

    return bestAttrValue


def find_best_value_weighted(attr, y, d, value_set):
    """
    Determines the best value of the given attribute to split on by determining which value of the attribute yields the least entropy with y 
    AND factoring in the weights of the examples given the distribution D

    Returns the value v 
    """

    bestAttrPartitioned = partition(attr)

    # {attr_value, V : H(Y | X = V)}
    vals_entropies = {}
    for v in bestAttrPartitioned:
        # Build the data vector of Y given x = v
        yGivenXequalsV = []
        for i in bestAttrPartitioned[v]:
            yGivenXequalsV.append(y[i])

        # Compute the entropy of Y given x = v, factoring in the weighted examples
        H_yGivenXequalsV = entropy_weighted(yGivenXequalsV, d)

        # add to our dictionary
        vals_entropies[v] = H_yGivenXequalsV

    # Determine the value of the attribute which yields the lowest entropy in y
    bestAttrValue = None
    while True:
        minEntropy = None
        for v in vals_entropies:
            if minEntropy is None or vals_entropies[v] <= minEntropy:
                minEntropy = vals_entropies[v]
                bestAttrValue = v
        
        if bestAttrValue in value_set:
            break
        else:
            vals_entropies.pop(bestAttrValue, None)

        if len(vals_entropies.keys()) == 0:
            return list(value_set)[0]

    return bestAttrValue


def majority_label(y):
    """
    Determines the majority label for a binary classification of 0 or 1

    Returns 0 if the most common value of y is 0, otherwise return 1
    """

    zero_count = 0
    one_count = 0

    for v in y:
        if v == 0: 
            zero_count += 1
        else:
            one_count += 1

    if zero_count > one_count:
        return 0

    return 1


def get_attribute_value_pairs(x):
    """
    Builds the initial attribute-value pair list for attributes x

    Returns a list of attribute-value pairs in the form [(attr_index, value), ...]
    """

    attribute_value_pairs = []

    # {x_i : set of possible values for x_i}
    attr_values = {}

    # Determine all possible values for all attributes and map them to attr_values
    for row in x:
        for i, val in enumerate(row):
            if i in attr_values:
                attr_values[i].add(val)
            else:
                attr_values[i] = set([val])
    
    # Build attribute_value_pairs list
    for x in attr_values:
        for val in attr_values[x]:
            attribute_value_pairs.append((x, val))

    return attribute_value_pairs


def predict_example(x, tree):
    """
    Predicts the classification label for a single example x using tree by recursively descending the tree until
    a label/leaf node is reached.

    Returns the predicted label of x according to tree
    """

    # If we've reached a null-child then step back
    if tree is None:
        return

    # If we've reached a label
    if type(tree) is not dict:
        return tree

    # Get the left child
    attrIdx, attrVal, decisionTrue = list(tree.keys())[0]

    # Test the decision
    if x[attrIdx] == attrVal and decisionTrue:
        # Go Left
        return predict_example(x, tree[(attrIdx, attrVal, decisionTrue)])
    else:
        # Go Right
        return predict_example(x, tree[(attrIdx, attrVal, not decisionTrue)])


def compute_error(y_true, y_pred):
    """
    Computes the average error between the true labels (y_true) and the predicted labels (y_pred)

    Returns the error = (1/n) * sum(y_true != y_pred)
    """

    total_errors = 0
    n = len(y_true)

    for i, ytrue in enumerate(y_true):
        if ytrue != y_pred[i]:
            total_errors += 1

    return (1 / n) * total_errors


def compute_error_weighted(y_true, y_pred, d):
    """
    Computes the weighted error between the true labels (y_true) and the predicted labels (y_pred)

    Returns the weighted error = (1/sum of weights) * weighted_count(y_true != y_pred)
    """

    weighted_errors = 0
    n = len(y_true)

    for i, ytrue in enumerate(y_true):
        if ytrue != y_pred[i]:
            # Add the weight of the example to the weighted error 
            weighted_errors += d[i]

    return (1 / sum(d)) * weighted_errors


def get_confusion_matrix(y_true, y_pred):
    """
    Generates the confusion matrix given the true labels and predicted labels

    Returns the confusion matrix represented as a 2d array in the form:
                                          |  Col 0: Prediction is Positive   |    Col 1: Prediction is Negative
        Row 0: Actual Value is Positive   |  TP Rate (# TP / # P Examples)   |    FN Rate (# FN / # N Examples)
        Row 1: Actual Value is Negative   |  FP Rate (# FP / # P Examples)   |    TN Rate (# TN / # N Examples)
    """

    matrix = []
    true_positives = 0
    false_positives = 0
    true_negatives = 0
    false_negatives = 0

    for i, ytrue in enumerate(y_true):
        ypred = y_pred[i]
        if ytrue == 1: 
            if ypred == 1:
                true_positives += 1
            else:
                false_negatives += 1

        if ytrue == 0:
            if ypred == 1:
                false_positives += 1
            else:
                true_negatives += 1

    n_positives = true_positives + false_positives
    n_negatives = true_negatives + false_negatives

    tpr = None
    fpr = None
    fnr = None
    tnr = None

    if n_positives != 0:
        tpr = true_positives / n_positives
        fpr = false_positives / n_positives

    if n_negatives != 0:
        fnr = false_negatives / n_negatives
        tnr = true_negatives / n_negatives

    matrix.append([tpr, fnr])
    matrix.append([fpr, tnr])

    return matrix


def visualize(tree, depth=0):
    """
    Pretty prints (kinda ugly, but hey, it's better than nothing) the decision tree to the console. Use print(tree) to
    print the raw nested dictionary representation.
    DO NOT MODIFY THIS FUNCTION!
    """

    if depth == 0:
        print('TREE')

    for index, split_criterion in enumerate(tree):
        sub_trees = tree[split_criterion]

        # Print the current node: split criterion
        print('|\t' * depth, end='')
        print('+-- [SPLIT: x{0} = {1}]'.format(split_criterion[0], split_criterion[1]))

        # Print the children
        if type(sub_trees) is dict:
            visualize(sub_trees, depth + 1)
        else:
            print('|\t' * (depth + 1), end='')
            print('+-- [LABEL = {0}]'.format(sub_trees))


def bagging(x, y, max_depth, num_trees):
    """
    Performs a bagging algorithm on the ID3 learner by generating num_trees samples of size M with replacement

    Returns a list of the hypotheses generated for each of the N samples
    In this case N many ID3 Decision Trees (represented as nested dictionaries) are returned
    """
    
    bags_X, bags_y = get_samples(x,y,num_trees)
    
    trees = []

    # Learn a decision tree for each bag
    for i in range(num_trees):
        dt = id3(bags_X[i], bags_y[i], max_depth=max_depth)
        trees.append(dt)

    return trees


def get_samples(x, y, num_trees):
    """
    Creates num_trees bootstrap samples by randomly drawing examples with replacement 

    Returns two lists: a list for bags of x and a list for bags of y, each bag containing M many samples ie:
    bags_X: [   [x_1,x_2, ... , x_m] (B_1)
                [x_1,x_2, ... , x_m] (B_2)
                          ...
                [x_1,x_2, ... , x_m] (B_N) ]

    bags_y: [   [y_1,y_2, ... , y_m] (B_1)
                [y_1,y_2, ... , y_m] (B_2)
                          ...
                [y_1,y_2, ... , y_m] (B_N) ]
    """

    bags_X = []
    bags_y = []

    # Each bag contains m evenly sized samples. ie each tree receives the same number of examples
    m = len(x) // num_trees

    # For each bag from 0 to num_trees - 1
    for i in range(num_trees):
        samples_X = []
        samples_y = []

        # Randomly select m examples
        while len(samples_X) < m:
            random_row = random.randrange(len(x))
            samples_X.append(x[random_row])
            samples_y.append(y[random_row])

        # Add to the list of bags
        bags_X.append(samples_X)
        bags_y.append(samples_y)

    return bags_X, bags_y


def boosting(x, y, max_depth, num_stumps):
    """
    Performs a AdaBoost algorithm on the ID3 learner, iterating up to num_stumps

    Returns an ensemble of hypotheses with their associated alphas, represented as a list of pairs, ie:
    [ (a_1, h_1), (a_2, h_2), … , (a_L, h_L) ]
    """

    h_ens = []

    # Initialize example weight distribution to be uniform
    d = [1/len(x) for i in range(len(x))]

    for l in range(num_stumps):
        # learn a hypothesis h_l
        h_l = id3_boosting(x, y, d, max_depth=max_depth)

        # generate predictions for the hypothesis
        predictions = [predict_example(ex, h_l) for ex in x]

        # compute the error of the hypothesis on examples and the weighted distribution
        error = compute_error_weighted(y, predictions, d)
        
        # compute alpha
        alpha_l = 0.5 * math.log((1 - error) / error)

        # add the hypothesis and the alpha to our ensemble
        h_ens.append((alpha_l, h_l))

        # update the weighted distribution
        for i in range(len(d)):
            if predictions[i] != y[i]:
                d[i] *= math.exp(alpha_l)
            else:
                d[i] *= math.exp(-alpha_l)

        # normalize
        d = [float(i)/sum(d) for i in d]

    return h_ens


def predict_example_bagging(x, bag):
    """
    Predicts the classification label for a single example x using a bag of trees by recursively descending each tree
    and taking a majority vote of the predicted label

    Returns the predicted label of x according to a majority vote of the bag of trees
    """

    predictions = []

    # Make a prediction using each hypothesis (tree) in the bag
    for h in bag:
        prediction = predict_example(x, h)
        predictions.append(prediction)

    # Make final prediction using majority vote
    positive_votes = sum(predictions)
    if positive_votes > (len(bag) / 2):
        return 1
    if positive_votes < (len(bag) / 2):
        return 0

    # If there is a tie, no prediction can be made; return Null label
    return None


def predict_example_boosting(x, h_ens):
    """
    Predicts the classification label for a single example x using an ensemble of weighted hypotheses and taking
    a weighted vote of the predicted label

    Returns the predicted label of x according to a weighted vote of the ensemble of weighted hypotheses / stumps
    """

    weighted_sum = 0.0

    # Generate predictions for the example x using each of the hypotheses
    for a, h in h_ens:
        prediction = predict_example(x, h)

        # Our original labels are 0 and +1, but we need to change to -1 and +1
        if prediction == 0:
            prediction = -1
            
        # Compute the weighted vote alpha*h(x) and add to the sum
        weighted_vote = a * prediction
        weighted_sum += weighted_vote

    # Final ensemble prediction is the sign of all the votes totaled
    if weighted_sum < 0:
        return 0
    if weighted_sum > 0:
        return 1

    # If the final vote cancels to 0, then return null label (ie: cannot make a prediction)
    return None


if __name__ == '__main__':

    # Load the training data
    M = np.genfromtxt('./mushroom.train', missing_values=0, skip_header=0, delimiter=',', dtype=int)
    ytrn = M[:, 0]
    Xtrn = M[:, 1:]

    # Load the test data
    M = np.genfromtxt('./mushroom.test', missing_values=0, skip_header=0, delimiter=',', dtype=int)
    ytst = M[:, 0]
    Xtst = M[:, 1:]

    # print('---------- PART A ----------')
    for d in [3, 5]:
        for k in [10, 20]:
            # Learn a bagged ensemble of k Decision Trees each of depth d
            bagged_model = bagging(Xtrn, ytrn, d, k)

            y_pred = [predict_example_bagging(x, bagged_model) for x in Xtst]

            conf_matrix = get_confusion_matrix(ytst, y_pred)

            print('Confusion Matrix for Bagged DT: D = ' + str(d) + ' K = ' + str(k))
            print(conf_matrix)

    # print('---------- PART B ----------')
    for d in [1, 2]:
        for k in [20, 40]:
            # Learn a weighted ensemble of k Decision Trees each of depth d
            h_ens = boosting(Xtrn, ytrn, d, k)

            y_pred = [predict_example_boosting(x, h_ens) for x in Xtst]

            conf_matrix = get_confusion_matrix(ytst, y_pred)

            print('Confusion Matrix for Boosted DT: D = ' + str(d) + ' K = ' + str(k))
            print(conf_matrix)

    print('---------- PART C ----------')
    # scikit-learn bagging
    for d in [3, 5]:
        for k in [10, 20]:
            clf_bagging = RandomForestClassifier(n_estimators=k)
            clf_bagging.fit(Xtrn, ytrn)

            y_pred = clf_bagging.predict(Xtst)

            conf_matrix = get_confusion_matrix(ytst, y_pred)

            print('Confusion Matrix for Bagged DT (Random Forest): D = ' + str(d) + ' K = ' + str(k))
            print(conf_matrix)

    # scikit-learn boosting
    for d in [1, 2]:
        for k in [20, 40]:
            clf_boosting = AdaBoostClassifier(n_estimators=k)
            clf_boosting.fit(Xtrn, ytrn)

            y_pred = clf_boosting.predict(Xtst)

            conf_matrix = get_confusion_matrix(ytst, y_pred)

            print('Confusion Matrix for Boosted DT: D = ' + str(d) + ' K = ' + str(k))
            print(conf_matrix)

