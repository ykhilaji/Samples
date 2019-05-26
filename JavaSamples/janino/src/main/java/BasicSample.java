import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class BasicSample {
    static IExpressionEvaluator singleExpression;
    static IExpressionEvaluator multipleExpressions;

    static {
        try {
            singleExpression = CompilerFactoryFactory.getDefaultCompilerFactory().newExpressionEvaluator();
            singleExpression.setExpressionType(double.class); // return type
            singleExpression.setParameters(new String[]{"a", "b"}, new Class[]{double.class, double.class}); // input types

            singleExpression.cook("b != 0 ? a / b : 0");

            multipleExpressions = CompilerFactoryFactory.getDefaultCompilerFactory().newExpressionEvaluator();
            multipleExpressions.setExpressionTypes(new Class[]{String.class, int.class, A.class, Object.class});
            multipleExpressions.setParameters(new String[][]{
                    {"a"},
                    {"a", "step"},
                    {"key", "value"},
                    {"map", "a"}
            }, new Class[][]{
                    {int.class},
                    {int.class, int.class},
                    {int.class, int.class},
                    {Map.class, A.class}
            });

            multipleExpressions.cook(new String[] {
                    "\"Result: \" + a",
                    "a + step",
                    "new A(key, value)",
                    "map.put(a.k, a.v)"
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double calculate(Object[] arguments) throws InvocationTargetException {
        return (double) singleExpression.evaluate(arguments);
    }

    // implicitly choose method
    public static Object selectMethod(Object[] arguments) throws InvocationTargetException {
        return multipleExpressions.evaluate(arguments);
    }

    // explicitly choose method
    public static Object selectMethod(int idx, Object[] arguments) throws InvocationTargetException {
        return multipleExpressions.evaluate(idx, arguments);
    }

    public static void main(String[] args) throws InvocationTargetException {
        System.out.println(calculate(new Object[]{5.0, 2.0}));
        System.out.println(calculate(new Object[]{5.0, 0.0}));

        System.out.println(selectMethod(new Object[] {10}));
        System.out.println(selectMethod(1, new Object[] {1, 2}));

        A a = (A) selectMethod(2, new Object[] {1, 2});
        Map<Integer, Integer> map = new HashMap<>();
        selectMethod(3, new Object[] {map, a});

        System.out.println(a);
        System.out.println(map);
    }
}
