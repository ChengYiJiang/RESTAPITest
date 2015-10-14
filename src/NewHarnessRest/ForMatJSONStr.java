package NewHarnessRest;

public class ForMatJSONStr {
	  String seperator = System.getProperty("line.separator");
	  /**
	   * formatting json data
	   * This class is used in report not in the JSON data viewer.
	   */
	  public String format(String jsonStr) {
	    int level = 0;
	    StringBuffer jsonForMatStr = new StringBuffer();
	    for(int i=0;i<jsonStr.length();i++){
	      char c = jsonStr.charAt(i);   
	      if (level > 0 && ('\n'== jsonForMatStr.charAt(jsonForMatStr.length()-1))){  //|| ('\r'== jsonForMatStr.charAt(jsonForMatStr.length()-1))
	        jsonForMatStr.append(getLevelStr(level));
	      }
	      switch (c) {
	      case '{': 
	      case '[':
	        jsonForMatStr.append(c+seperator);
	        level++;
	        break;
	      case ',': 
	        jsonForMatStr.append(c+seperator);
	        break;
	      case '}':
	      case ']':
	        jsonForMatStr.append(seperator);
	        level--;
	        jsonForMatStr.append(getLevelStr(level));
	        jsonForMatStr.append(c);
	        break;
	      default:
	        jsonForMatStr.append(c);
	        break;
	      }
	    }
	    
	    return jsonForMatStr.toString();

	  }
	  
	  private String getLevelStr(int level){
	    StringBuffer levelStr = new StringBuffer();
	    for(int levelI = 0;levelI<level ; levelI++){
	      levelStr.append("\t");
	    }
	    return levelStr.toString();
	  }

	}