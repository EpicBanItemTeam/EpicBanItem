package team.ebi.epicbanitem.util;

import java.text.MessageFormat;
import java.util.regex.Pattern;

public final class Regex {

  private final String regexString;
  private final CharSequence flags;

  public Regex(String regexString, CharSequence flags) {
    this.regexString = regexString;
    this.flags = flags;
  }

  public Regex(String s) {
    String[] strings = s.split("/");
    if (strings.length > 1) {
      if (strings.length == 3) {
        this.regexString = strings[1];
        this.flags = strings[2];
        return;
      }
      this.regexString = s;
      this.flags = null;
    } else {
      throw new IllegalArgumentException(
          MessageFormat.format("input string isn't a regex: {0}", s));
    }
  }

  public static boolean isRegex(String s) {
    int last = s.lastIndexOf('/');
    return s.charAt(0) == '/' && last > 0;
  }

  public Pattern pattern() {
    //noinspection MagicConstant
    return Pattern.compile(regexString, mergeFlags());
  }

  private int mergeFlags() {
    int curr = 0;
    for (int i = 0; i < flags.length(); i++) {
      curr |= flagToInt(flags.charAt(i));
    }
    return curr;
  }

  private int flagToInt(char flag) {
    return switch (flag) {
      case 'i' -> Pattern.CASE_INSENSITIVE;
      case 'm' -> Pattern.MULTILINE;
      case 'x' -> Pattern.COMMENTS;
      case 's' -> Pattern.DOTALL;
      default -> 0;
    };
  }

  @Override
  public String toString() {
    return String.format("/%s/%s", regexString, flags);
  }
}
