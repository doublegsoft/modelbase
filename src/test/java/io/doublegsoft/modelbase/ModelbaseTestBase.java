package io.doublegsoft.modelbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ModelbaseTestBase {

  public String getText(String resourcePath) throws IOException  {
    InputStreamReader input = new InputStreamReader(getClass().getResourceAsStream(resourcePath));
    try (BufferedReader reader = new BufferedReader(input)) {
      String line;
      String ret = "";
      while ((line = reader.readLine()) != null) {
        ret += line + "\n";
      }
      return ret;
    }
  }

}
