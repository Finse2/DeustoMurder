package activation;

public class dice {

    static int min = 1;
    static int max = 6;
    private int roll_output;

    public int getRoll_output() {
        return roll_output;
    }

    public void setRoll_output(int roll_output) {
        this.roll_output = roll_output;
    }

    public int roll() {
        int roll1 = (int) (Math.random() * (max - min + 1) + min);
        int roll2 = (int) (Math.random() * (max - min + 1) + min);
        int roll = roll1 + roll2;
        setRoll_output(roll);
        return roll;
    }
}
