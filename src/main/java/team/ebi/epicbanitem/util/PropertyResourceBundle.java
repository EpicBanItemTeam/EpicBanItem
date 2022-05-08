package team.ebi.epicbanitem.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ResourceBundle;

public final class PropertyResourceBundle extends java.util.PropertyResourceBundle {

  public PropertyResourceBundle(Reader reader) throws IOException {
    super(reader);
  }

  @Override
  public void setParent(ResourceBundle parent) {
    super.setParent(parent);
  }
}
