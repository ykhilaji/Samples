package lucene;


import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.NGramDistance;

public class LuceneAlgorithms {
    static JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
    static LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    static NGramDistance nGramDistance = new NGramDistance();

    public static float jaroWinklerDistance(String s1, String s2) {
        return jaroWinklerDistance.getDistance(s1, s2);
    }

    public static float levenshteinDistance(String s1, String s2) {
        return levenshteinDistance.getDistance(s1, s2);
    }

    public static float nGramDistance(String s1, String s2) {
        return nGramDistance.getDistance(s1, s2);
    }

    public static void main(String[] args) {
        System.out.println(jaroWinklerDistance("source", "target"));
        System.out.println(levenshteinDistance("source", "target"));
        System.out.println(nGramDistance("source", "target"));
    }
}
