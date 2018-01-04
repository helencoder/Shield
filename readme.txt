The detailed techniques for morph mention extraction is described in detailed in Zhang et al., 2015.
It include two steps:
(1) Potential morph discovery,
(2) Morph mention extraction.
The same data format should be prepared as the example files in the example command lines.

1. Potential morph discovery

1.1 To disocover potential morphs for a test set, first the following command line can be run to extract features:

java -jar ./jar/feature.jar test_id_path test_ann_path test_feature_path mention_outputpath

test_id: the file that contains the tweet ids for the test set (e.g., ./data/exp/tweets/test)
test_ann_path: the file that contains the testing data after preprocessing (e.g., ./data/exp/tweets/preprocess.txt)
test_feature_path: the output file that contains the extracted features
mention_outputpath: the file that contains the set of unique terms in the test set

Example: java -Xmx6g -jar ./jar/feature.jar ./data/exp/tweets/test ./data/exp/tweets/preprocess.txt ./data/exp/discovery/test.feature ./data/exp/discovery/test.mention

1.2 Then the following command line can be used to predict the potential morphs: 

java -jar ./jar/svm_predict.jar test_feature_path mention_path outputpath

test_feature_path: the feature file generated in 1.1,
mention_path: the file that contains the set of unique terms in the test set, which is generated in 1.1

Example: java -jar ./jar/svm_predict.jar ./data/exp/discovery/test/test.feature ./data/exp/discovery/test/test.mention ./data/exp/discovery/test/morphs.output

2. Morph mention extraction

This part mainly contain two steps: (1) Mention graph construction (2) Semi-supervised graph regularization

2.1 To contruct the mention graph based on coreference and correlation relations, the following command line can be run:

java -Xmx6g -jar ./jar/graph.jar labelled_path tweet_path user_path train_mention_path test_rank_path threshold

labelled_path: the file that contains the tweet ids for the training tweets (e.g., ./data/exp/tweets/train)
tweet_path: the file that contains the preprocessed tweets, including both training and testing tweets (e.g., ./data/exp/tweets/preprocess.txt).
user_path: the file that contains the user information for each tweet, the users include auhots and the users mentioned in the tweet (e.g., ./data/exp/tweets/t2u_exp)
train_mention_path: the file that contains the list of morphs in the training set (e.g., ./data/exp/discovery/train.mention)
test_rank_path: the output file generated from step 1.2
threshold: the threhold you considered a term as a potential morph based on the predicted probability from step 1.2 (e.g, 0.1)

Example: java -Xmx6g -jar ./jar/graph.jar ./data/exp/tweets/train ./data/exp/tweets/preprocess.txt ./data/exp/tweets/t2u_exp ./data/exp/discovery/train.mention ./data/exp/discovery/test/morphs.output 0.1

2.2 Semi-supervised graph regularization
This step is to verify whether a morph mention from a specific tweet is used as a true morph mention or not.

java -Xmx6g -jar ./jar/regu.jar outputpath

Example: java -Xmx6g -jar ./jar/regu.jar ./data/exp/verification/verification.output2

