

import java.io.*;
import java.util.*;

/*
* Fatima AlSaadeh
* Product Recommendation System
*  A-priori algorithm to find products which are frequently browsed together.
*  Fix the support to s = 100 (i.e. product pairs need to occur together at least 100 times to be considered frequent)
*  and find itemsets of size 2 and 3.
* */
public class AssociationRules {

    public static void main(String[] args) {
        new Apriori();
        Apriori.data = args[0];
        System.out.println("First Pass Start:");
        Apriori.pass(1);
        System.out.println("Second Pass Start:");
        Apriori.pass(2);
        System.out.println("Third Pass Start:");
        Apriori.pass(3);
        System.out.println("Get Pairs Rules Start:");
        Apriori.computePairRules();
        System.out.println("Get Triples Rules Start:");
        Apriori.getTriplesRules();
        Apriori.printRules(Apriori.secondPassRulesConf, "PairRule.txt");
        Apriori.printRules(Apriori.thirdPassRulesConf, "TripleRule.txt");
    }

    static class Apriori {
        public static HashMap<String, Double> firstPassCounts = new HashMap<>();
        public static HashMap<HashSet<String>, Double> secondPassCount = new HashMap<>();
        public static HashMap<HashSet<String>, Double> thirdPassCount = new HashMap<>();
        public static String data;
        public static HashMap<List<String>, Double> secondPassRulesConf = new HashMap<>();
        public static HashMap<List<String>, Double> thirdPassRulesConf = new HashMap<>();


        public static void pass(Integer passNum) {
            try {
                Scanner scanner = new Scanner(new File(data));
                while (scanner.hasNextLine()) {
                    switch (passNum) {
                        case 1:
                            firstPass(scanner.nextLine());
                            break;
                        case 2:
                            secondPass(scanner.nextLine());
                            break;
                        case 3:
                            thirdPass(scanner.nextLine());
                            break;
                    }
                }
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Apriori.prune(passNum);
        }

        public static void prune(Integer passNum) {
            switch (passNum) {
                case 1:
                    Iterator<Map.Entry<String, Double>> firstPassIter = firstPassCounts.entrySet().iterator();
                    while (firstPassIter.hasNext()) {
                        Map.Entry<String, Double> entry = firstPassIter.next();
                        if (entry.getValue() < 100.0) {
                            firstPassIter.remove();
                        }
                    }
                    break;
                case 2:
                    Iterator<Map.Entry<HashSet<String>, Double>> secondPassIter = secondPassCount.entrySet().iterator();
                    while (secondPassIter.hasNext()) {
                        if (secondPassIter.next().getValue() < 100.0) {
                            secondPassIter.remove();
                        }
                    }
                    break;
                case 3:
                    Iterator<Map.Entry<HashSet<String>, Double>> thirdPassIter = thirdPassCount.entrySet().iterator();
                    while (thirdPassIter.hasNext()) {
                        Map.Entry<HashSet<String>, Double> entry = thirdPassIter.next();
                        if (entry.getValue() < 100.0) {
                            thirdPassIter.remove();
                        }
                    }
                    break;
            }
        }


        public static void firstPass(String line) {
            String[] sessionItems = line.split("\\s+");
            for (String item: sessionItems) {
                if (firstPassCounts.containsKey(item)) {
                    firstPassCounts.put(item, firstPassCounts.get(item) + 1.0);
                } else {
                    firstPassCounts.put(item, 1.0);
                }
            }
        }


        public static void secondPass(String line) {
            HashSet<HashSet<String>> sessionPairItems = getPairs(line);
            for (HashSet<String> pair : sessionPairItems) {
                if (secondPassCount.containsKey(pair)) {
                    secondPassCount.put(pair, secondPassCount.get(pair) + 1.0);
                } else {
                    secondPassCount.put(pair, 1.0);
                }
            }
        }

        public static void thirdPass(String line) {
            HashSet<HashSet<String>> sessionTripleItems = getTriples(line);
            for (HashSet<String> triple : sessionTripleItems) {
                if (thirdPassCount.containsKey(triple)) {
                    thirdPassCount.put(triple, thirdPassCount.get(triple) + 1.0);
                } else {
                    thirdPassCount.put(triple, 1.0);
                }
            }
        }


        public static HashSet<HashSet<String>> getPairs(String line) {
            String[] sessionItems = line.split("\\s+");
            HashSet<HashSet<String>> sessionPairs = new HashSet<>();
            for (String item1: sessionItems) {
                for (String item2: sessionItems) {
                    if(!item1.equals(item2)) {
                        if (firstPassCounts.containsKey(item1) && firstPassCounts.containsKey(item2)) {
                            HashSet<String> pair = new HashSet<>();
                            pair.add(item1);
                            pair.add(item2);
                            sessionPairs.add(pair);
                        }
                    }
                }
            }
            return sessionPairs;
        }


        public static HashSet<HashSet<String>> getTriples(String value) {
            String[] sessionItems = value.split("\\s+");
            HashSet<HashSet<String>> sessionItemsPairs = getPairs(value);
            HashSet<HashSet<String>> sessionItemsTriples = new HashSet<>();
            for (HashSet<String> pair : sessionItemsPairs) {
                for (String item1: sessionItems) {
                    if (firstPassCounts.containsKey(item1) && !pair.contains(item1)) {
                        HashSet<String> triple = new HashSet<>();
                        triple.add(item1);
                        triple.addAll(pair);
                        sessionItemsTriples.add(triple);
                    }
                }
            }
            return sessionItemsTriples;
        }

        public static void computePairRules() {
            Iterator<Map.Entry<HashSet<String>, Double>> iter = secondPassCount.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<HashSet<String>, Double> entry = iter.next();
                Object[] pair = entry.getKey().toArray();
                if (pair.length == 2) {
                    HashSet<String> c = new HashSet<>();
                    c.addAll(new HashSet<>(Arrays.asList(pair[0].toString())));
                    c.addAll(new HashSet<>(Arrays.asList(pair[1].toString())));
                    HashSet<String> d = new HashSet<>();
                    d.addAll(new HashSet<>(Arrays.asList(pair[1].toString())));
                    d.addAll(new HashSet<>(Arrays.asList(pair[0].toString())));

                    if (secondPassCount.get(c) != null) {
                        double countPair = secondPassCount.get(c);
                        double leftCount = firstPassCounts.get(pair[0]);
                        double confidence = countPair/leftCount;
                        secondPassRulesConf.put(new ArrayList<>(c), confidence);
                    }
                    if (secondPassCount.get(d) != null) {
                        double countPair = secondPassCount.get(d);
                        double leftCount = firstPassCounts.get(pair[1]);
                        double confidence = countPair/leftCount;
                        secondPassRulesConf.put(new ArrayList<>(d), confidence);
                    }
                }


            }
        }

        public static void getRule(String key1, String key2, String key3) {
            HashSet<String> left = new HashSet<>();
            left.add(key1);
            left.add(key2);
            HashSet<String> right = new HashSet<>();
            right.add(key3);
            HashSet<String> triple = new HashSet<>();
            triple.addAll(left);
            triple.addAll(right);
            if (secondPassCount.get(left) != null && thirdPassCount.get(triple) != null) {
                double countPair2 = secondPassCount.get(left);
                double countTriple2 = thirdPassCount.get(triple);
                double confidence2 = countTriple2 / countPair2;
                thirdPassRulesConf.put(new ArrayList<>(triple), confidence2);
            } else {
                thirdPassRulesConf.put(new ArrayList<>(triple), 0.0);
            }
        }

        public static void getTriplesRules() {
            Iterator<Map.Entry<HashSet<String>, Double>> iter = thirdPassCount.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<HashSet<String>, Double> entry = iter.next();
                HashSet<String> triple = entry.getKey();
                for (String key1 : triple) {
                    for (String key2 : triple) {
                        if (!key1.equals(key2)) {
                            for (String key3 : triple) {
                                if (!key2.equals(key3) && !key1.equals(key3)) {
                                    getRule(key1, key2, key3);
                                    getRule(key1, key3, key2);
                                    getRule(key2, key3, key1);
                                }
                            }
                        }

                    }
                }


            }
        }

        public static void printRules(HashMap<List<String>, Double> rules, String outputFile) {
            try {
                FileWriter myWriter = new FileWriter(outputFile);
                Apriori.MyComparator comparator1 = new Apriori.MyComparator(rules);

                java.util.Map<List<String>, Double> sortedPass1 = new TreeMap<>(comparator1);
                sortedPass1.putAll(rules);

                Iterator<Map.Entry<List<String>, Double>> iter1 = sortedPass1.entrySet().iterator();
                Map.Entry<List<String>, Double> entry1;
                while (iter1.hasNext()) {
                    entry1 = iter1.next();
                    myWriter.write(entry1.getKey().toString() + "=" + entry1.getValue().toString()+"\n");
                }
                myWriter.close();
                System.out.println("Writing Finished Successfully.");
            } catch (IOException e) {
                System.out.println("Writing Error.");
                e.printStackTrace();
            }
        }


        static class MyComparator implements Comparator<Object> {

            HashMap<List<String>, Double> map;

            public MyComparator(HashMap<List<String>, Double> map) {
                this.map = map;
            }

            public int compare(Object o1, Object o2) {
                if ((map.get(o1) > map.get(o2)))
                    return -1;
                else
                    return 1;

            }
        }

    }
}
