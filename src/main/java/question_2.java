

import java.io.*;
import java.util.*;

/*
 * Fatima AlSaadeh
 * Product Recommendation System
 *  A-priori algorithm to find products which are frequently browsed together.
 *  Fix the support to s = 100 (i.e. product pairs need to occur together at least 100 times to be considered frequent)
 *  and find itemsets of size 2 and 3.
 * */
public class question_2 {

    public static void main(String[] args) {
        new Apriori();
        Apriori.data = "src/main/resources/data/browsing.txt";
        Apriori.pass(1);
        Apriori.pass(2);
        Apriori.pass(3);
        Apriori.pairConfidence();
        Apriori.tripleConfidence();
        Apriori.printRules(Apriori.secondPassConf, "PairRule.txt");
        Apriori.printRules(Apriori.thirdPassConf, "TripleRule.txt");
    }

    static class Apriori {
        public static HashMap<String, Double> firstPassCounts = new HashMap<>();
        public static HashMap<List<String>, Double> secondPassCount = new HashMap<>();
        public static HashMap<List<String>, Double> thirdPassCount = new HashMap<>();
        public static String data;
        public static HashMap<List<String>, Double> secondPassConf = new HashMap<>();
        public static HashMap<List<String>, Double> thirdPassConf = new HashMap<>();


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
                    Iterator<Map.Entry<List<String>, Double>> secondPassIter = secondPassCount.entrySet().iterator();
                    while (secondPassIter.hasNext()) {
                        if (secondPassIter.next().getValue() < 100.0) {
                            secondPassIter.remove();
                        }
                    }
                    break;
                case 3:
                    Iterator<Map.Entry<List<String>, Double>> thirdPassIter = thirdPassCount.entrySet().iterator();
                    while (thirdPassIter.hasNext()) {
                        Map.Entry<List<String>, Double> entry = thirdPassIter.next();
                        if (entry.getValue() < 100.0) {
                            thirdPassIter.remove();
                        }
                    }
                    break;
            }
        }


        public static void firstPass(String line) {
            String[] sessionItems = line.split("\\s+");
            for (String item : sessionItems) {
                if (firstPassCounts.containsKey(item)) {
                    firstPassCounts.put(item, firstPassCounts.get(item) + 1.0);
                } else {
                    firstPassCounts.put(item, 1.0);
                }
            }
        }


        public static void secondPass(String line) {
            String[] sessionItems = line.split("\\s+");
            for (String item1 : sessionItems) {
                for (String item2 : sessionItems) {
                    if (!item1.equals(item2)) {
                        if (firstPassCounts.containsKey(item1) && firstPassCounts.containsKey(item2)) {
                            List<String> pair = new ArrayList<>();
                            pair.add(item1);
                            pair.add(item2);
                            if (secondPassCount.containsKey(pair)) {
                                secondPassCount.put(pair, secondPassCount.get(pair) + 1.0);
                            } else {
                                secondPassCount.put(pair, 1.0);
                            }
                        }
                    }
                }
            }

        }

        public static void thirdPass(String line) {
            String[] sessionItems = line.split("\\s+");
            for (String item1 : sessionItems) {
                for (String item2 : sessionItems) {
                    if (!item1.equals(item2)) {
                        if (firstPassCounts.containsKey(item1) && firstPassCounts.containsKey(item2)) {
                            List<String> sessionPairs = new ArrayList<>();
                            sessionPairs.add(item1);
                            sessionPairs.add(item2);
                            for (String item3 : sessionItems) {
                                if (secondPassCount.containsKey(sessionPairs) && firstPassCounts.containsKey(item3)
                                        && !item1.equals(item3) && !item2.equals(item3)) {
                                    List<String> triple = new ArrayList<>();
                                    triple.addAll(sessionPairs);
                                    triple.add(item3);
                                    if (thirdPassCount.containsKey(triple)) {
                                        thirdPassCount.put(triple, thirdPassCount.get(triple) + 1.0);
                                    } else {
                                        thirdPassCount.put(triple, 1.0);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        public static void pairConfidence() {
            for (List<String> sc : secondPassCount.keySet()) {
                List<String> pair = sc;
                for (String key1 : pair) {
                    for (String key2 : pair) {
                        if (!key1.equals(key2)) {
                            List<String> pair1 = new ArrayList<>();
                            pair1.add(key1);
                            pair1.add(key2);
                            List<String> pair2 = new ArrayList<>();
                            pair2.add(key2);
                            pair2.add(key1);
                            if (secondPassCount.get(pair1) != null) {
                                double countPair = secondPassCount.get(pair1);
                                double leftCount = firstPassCounts.get(key1);
                                double confidence = countPair / leftCount;
                                secondPassConf.put(pair1, confidence);
                            }
                            if (secondPassCount.get(pair2) != null) {
                                double countPair = secondPassCount.get(pair2);
                                double leftCount = firstPassCounts.get(key2);
                                double confidence = countPair / leftCount;
                                secondPassConf.put(pair2, confidence);
                            }

                        }
                    }
                }


            }
        }

        public static void getRule(String key1, String key2, String key3) {
            List<String> left = new ArrayList<>();
            left.add(key1);
            left.add(key2);
            List<String> triple = new ArrayList<>();
            triple.addAll(left);
            triple.add(key3);
            if (secondPassCount.get(left) != null && thirdPassCount.get(triple) != null) {
                double countPair2 = secondPassCount.get(left);
                double countTriple2 = thirdPassCount.get(triple);
                double confidence2 = countTriple2 / countPair2;
                thirdPassConf.put(triple, confidence2);
            } else {
                thirdPassConf.put(triple, 0.0);
            }
        }

        public static void tripleConfidence() {
            for (List<String> tp : thirdPassCount.keySet()) {
                List<String> triple = tp;
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
                    myWriter.write(entry1.getKey().toString() + "=" + entry1.getValue().toString() + "\n");
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
