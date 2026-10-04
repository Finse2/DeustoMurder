package activation;

import java.util.Random;

class dice {

    private int roll_output;

    int getRoll_output() {
        return roll_output;
    }

    void setRoll_output(int roll_output) {
        this.roll_output = roll_output;
    }

    int roll() {
        int roll1 = new Random().nextInt(0, 7);
        int roll2 = new Random().nextInt(6);
        int roll = roll1 + roll2;
        setRoll_output(roll);
        return roll;
    }
}
