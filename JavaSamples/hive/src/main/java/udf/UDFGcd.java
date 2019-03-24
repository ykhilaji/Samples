package udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

@Description(name = "udfGcd", value = "_FUNC_(int a, int b) - GCD(a, b)")
public class UDFGcd extends UDF {
    public long evaluate(long a, long b) {
        if (b == 0) {
            return a;
        }

        return a % b == 0 ? b : evaluate(b, a % b);
    }
}
