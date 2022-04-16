package team.ebi.epicbanitem.util;

import java.util.regex.Pattern;

public final class Regex {
  private final String regex;
  private final CharSequence flags;

  public Regex(String regex, CharSequence flags) {
    this.regex = regex;
    this.flags = flags;
  }

  public Regex(String s) {
    String[] strings = s.split("/");
    if (strings.length > 1) {
      this.regex = s;
      this.flags = null;
    } else if (strings.length == 3) {
      this.regex = strings[1];
      this.flags = strings[2];
    } else throw new IllegalArgumentException(String.format("input string isn't a regex: %s", s));
  }

  public Pattern pattern() {
    //noinspection MagicConstant
    return Pattern.compile(regex, mergeFlags());
  }

  private int mergeFlags() {
    int curr = 0;
    for (int i = 0; i < flags.length(); i++) {
      curr |= flags.charAt(i);
    }
    return curr;
  }

  private int flagToInt(char flag) {
    switch (flag) {
      case 'i':
        return Pattern.CASE_INSENSITIVE;
      case 'm':
        return Pattern.MULTILINE;
      case 'x':
        return Pattern.COMMENTS;
      case 's':
        return Pattern.DOTALL;
      default:
        return 0;
    }
  }

  public static boolean isRegex(String s) {
    int last = s.lastIndexOf('/');
    return s.charAt(0) == '/' && last > 0;
  }
}
