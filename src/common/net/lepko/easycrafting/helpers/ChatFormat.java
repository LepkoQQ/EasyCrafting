package net.lepko.easycrafting.helpers;

public enum ChatFormat {

    BLACK('0'), DARK_BLUE('1'), DARK_GREEN('2'), DARK_AQUA('3'), DARK_RED('4'), DARK_PURPLE('5'), GOLD('6'), GRAY('7'), DARK_GRAY('8'), BLUE('9'), GREEN('a'), AQUA('b'), RED('c'), LIGHT_PURPLE('d'), YELLOW('e'), WHITE('f'),

    MAGIC('k', true), BOLD('l', true), STRIKETHROUGH('m', true), UNDERLINE('n', true), ITALIC('o', true), RESET('r', true);

    public static final char COLOR_CHAR = '\u00A7';

    private final char code;
    private final boolean isFormat;

    private ChatFormat(char code) {
        this(code, false);
    }

    private ChatFormat(char code, boolean isFormat) {
        this.code = code;
        this.isFormat = isFormat;
    }

    @Override
    public String toString() {
        return String.valueOf(new char[] { COLOR_CHAR, code });
    }
}
