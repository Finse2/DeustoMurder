package utils;

import java.awt.Font;

public final class AllFonts {

    private AllFonts() {}


    // Georgia


    // plain
    public static final Font GEORGIA_PLAIN_14 = new Font("Georgia", Font.PLAIN, 14);
    public static final Font GEORGIA_PLAIN_15 = new Font("Georgia", Font.PLAIN, 15);
    public static final Font GEORGIA_PLAIN_17 = new Font("Georgia", Font.PLAIN, 17);

    // bold
    public static final Font GEORGIA_BOLD_14 = new Font("Georgia", Font.BOLD, 14);
    public static final Font GEORGIA_BOLD_15 = new Font("Georgia", Font.BOLD, 15);
    public static final Font GEORGIA_BOLD_16 = new Font("Georgia", Font.BOLD, 16);
    public static final Font GEORGIA_BOLD_17 = new Font("Georgia", Font.BOLD, 17);
    public static final Font GEORGIA_BOLD_18 = new Font("Georgia", Font.BOLD, 18);
    public static final Font GEORGIA_BOLD_19 = new Font("Georgia", Font.BOLD, 19);
    public static final Font GEORGIA_BOLD_21 = new Font("Georgia", Font.BOLD, 21);
    public static final Font GEORGIA_BOLD_24 = new Font("Georgia", Font.BOLD, 24);
    public static final Font GEORGIA_BOLD_28 = new Font("Georgia", Font.BOLD, 28);


    // Sans serif


    // plain
    public static final Font SANS_SERIF_PLAIN_10 = new Font("SansSerif", Font.PLAIN, 10);
    public static final Font SANS_SERIF_PLAIN_11 = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font SANS_SERIF_PLAIN_13 = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font SANS_SERIF_PLAIN_14 = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SANS_SERIF_PLAIN_15 = new Font("SansSerif", Font.PLAIN, 15);

    // bold
    public static final Font SANS_SERIF_BOLD_12 = new Font("SansSerif", Font.BOLD, 12);
    public static final Font SANS_SERIF_BOLD_13 = new Font("SansSerif", Font.BOLD, 13);
    public static final Font SANS_SERIF_BOLD_14 = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SANS_SERIF_BOLD_15 = new Font("SansSerif", Font.BOLD, 15);
    public static final Font SANS_SERIF_BOLD_22 = new Font("SansSerif", Font.BOLD, 22);
    public static final Font SANS_SERIF_BOLD_23 = new Font("SansSerif", Font.BOLD, 23);
    public static final Font SANS_SERIF_BOLD_54 = new Font("SansSerif", Font.BOLD, 54);


    // Arial


    // plain
    public static final Font ARIAL_PLAIN_10 = new Font("Arial", Font.PLAIN, 10);
    public static final Font ARIAL_PLAIN_11 = new Font("Arial", Font.PLAIN, 11);
    public static final Font ARIAL_PLAIN_13 = new Font("Arial", Font.PLAIN, 13);

    // bold
    public static final Font ARIAL_BOLD_12 = new Font("Arial", Font.BOLD, 12);
    public static final Font ARIAL_BOLD_19 = new Font("Arial", Font.BOLD, 19);
    public static final Font ARIAL_BOLD_20 = new Font("Arial", Font.BOLD, 20);
    public static final Font ARIAL_BOLD_22 = new Font("Arial", Font.BOLD, 22);
    public static final Font ARIAL_BOLD_25 = new Font("Arial", Font.BOLD, 25);
    public static final Font ARIAL_BOLD_32 = new Font("Arial", Font.BOLD, 32);


    // Serif

    // plain
    public static final Font SERIF_PLAIN_13 = new Font("Serif", Font.PLAIN, 13);

    // bold
    public static final Font SERIF_BOLD_17 = new Font("Serif", Font.BOLD, 17);
    public static final Font SERIF_BOLD_25 = new Font("Serif", Font.BOLD, 25);

    // bold italic
    public static final Font SERIF_BOLD_ITALIC_20 =
            new Font("Serif", Font.BOLD | Font.ITALIC, 20);


    // Dinamicos


    public static Font georgiaBold(int size) {
        return new Font("Georgia", Font.BOLD, size);
    }

    public static Font georgiaPlain(int size) {
        return new Font("Georgia", Font.PLAIN, size);
    }

    public static Font sansSerifBold(int size) {
        return new Font("SansSerif", Font.BOLD, size);
    }

    public static Font sansSerifPlain(int size) {
        return new Font("SansSerif", Font.PLAIN, size);
    }

    public static Font arialBold(int size) {
        return new Font("Arial", Font.BOLD, size);
    }

    public static Font arialPlain(int size) {
        return new Font("Arial", Font.PLAIN, size);
    }

    public static Font serifBold(int size) {
        return new Font("Serif", Font.BOLD, size);
    }

    public static Font serifPlain(int size) {
        return new Font("Serif", Font.PLAIN, size);
    }

    public static Font serifBoldItalic(int size) {
        return new Font("Serif", Font.BOLD | Font.ITALIC, size);
    }
}
