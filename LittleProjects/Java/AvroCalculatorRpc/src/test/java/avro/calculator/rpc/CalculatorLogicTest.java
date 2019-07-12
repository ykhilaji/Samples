package avro.calculator.rpc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorLogicTest {
    @Test
    public void plus() {
        Double result = CalculatorLogic.execute("1 + 1");
        assertEquals(result, 2);
    }

    @Test
    public void minus() {
        Double result = CalculatorLogic.execute("5 - 2");
        assertEquals(result, 3);
    }

    @Test
    public void mult() {
        Double result = CalculatorLogic.execute("2 * 2");
        assertEquals(result, 4);
    }

    @Test
    public void div() {
        Double result = CalculatorLogic.execute("9 / 3");
        assertEquals(result, 3);
    }

    @Test
    public void pow() {
        Double result = CalculatorLogic.execute("2 ^ 5");
        assertEquals(result, 32);
    }

    @Test
    public void log() {
        Double result = CalculatorLogic.execute("log(10)(100)");
        assertEquals(result, 2);
    }

    @Test
    public void priority() {
        Double result = CalculatorLogic.execute("5 + 2 * 3 - ((3^2) + 2)");
        assertEquals(result, 0);
    }
}
