package providers;

import java.util.Random;

public class Entity {
    private int randomInt;

    public Entity() {
        randomInt = new Random().nextInt(1000);
    }

    public void doAction() {
        System.out.println(randomInt);
    }
}
