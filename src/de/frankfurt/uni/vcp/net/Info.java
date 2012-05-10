package de.frankfurt.uni.vcp.net;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 *  <h3> This is the base class, from which all other network relevant information 
 *  datastructures are inherited. </h3>
 *  
 *  <p> To prepare for a more or less easy mapping between network-messages and
 *      corresponding info-datastructures the string value given to the constructor
 *      will be split into "key=value", wich are inserted in a hash-table.
 *  </p>
 */

public class Info {

  HashMap<String,String> tags = new HashMap<String,String>();
  
  /**
   * <h3> Create a new Info instance. </h3>
   * 
   * <p> The string given will be split at spaces. </p>
   * <p> Every part of the form key=value" will be inserted in a hash
   *     map as mapping "key" to "value"
   * 
   * @param string The string, from which to initialize key-value mappings
   */
  public Info (String string) {
 
    for (String part : string.split(" ")){
      String[] s = part.split ("=");

      String key = s[0];
      String value = (s.length > 1)? s[1] : "";
      
      tags.put(key, value);
    }
  }
  
  
  /**
   * Dump the contents of all fields of this class.
   */
  @Override
  public String toString() {
      String s = getClass().getSimpleName() + ":";
      for (Field f : getClass().getDeclaredFields()) {
          try {
              s += " " + f.getName() + "=" + f.get(this);
          } catch (Exception e) { }
      }
      
    return s;
  }
  
}
