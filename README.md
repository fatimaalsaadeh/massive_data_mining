# massive_data_mining
# People You Might Know

 MapReduce Algorithm Recommends to 10 friends sorted by the number of mutual friends
 ```
 ##The Algorithm :
  ###Map Procedure:
   - Loop over all the users list:
   - Pick the user A:
   - Loop over the user A friends:
   - Pair the user A to the already friend users and 0: Key A, Pair (friend1, 0)
   - Loop over each friend of user A friends and Pair them together with 1
     which means they have one mutual friend A: Key: friend1, Pair (friend2,1)

  ###The resulted pairs will be shuffled and sorted, combined by the similar keys and sent to the produce
  ###Produce Procedure:
   - Get a key with all its pairs
   - Loop over these pairs:
   - If the pair with count 0, ignore it because it means the user and suggested are already friends
   - If the pair with count=1, if it was counted before increase the counter, else add it with count 1
   - Will end up for key A : (friend1,1), (friend2,4), (friend3,9), where friend1 is suggested and
     count is how many mutual friends between them
   - Sort the friends by their mutual friends count and pick the top 10
  ```  
  ###Output Examples:
    924	439,2409,6995,11860,15416,43748,45881
    8941	8938,8942,8946,8939,8943,8944,8945,8940
    8942	8938,8939,8941,8945,8946,8940,8943,8944
    9019	320,9018,9016,9017,9020,9021,9022,317,9023
    9020	9021,320,9016,9017,9018,9019,9022,317,9023
    9021	9020,320,9016,9017,9018,9019,9022,317,9023
    9022	9019,9020,9021,317,320,9016,9017,9018,9023
    9990	9987,9988,9989,9993,9994,35667,9991,9992,13134,13478
    9992	9987,9989,9988,9990,9993,9994,35667,9991
    9993	9990,9994,9987,9988,9989,9991,35667,9992,13134,13478
