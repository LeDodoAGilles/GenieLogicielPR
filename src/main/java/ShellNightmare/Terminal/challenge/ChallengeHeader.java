package ShellNightmare.Terminal.challenge;

import com.mifmif.common.regex.Generex;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

public class ChallengeHeader implements Serializable {
    private static final long serialVersionUID = 174395L;

    public String name = "";
    public String description = "";
    public String difficulty = "";
    public boolean validated = false;

    public String rootPasswordGenerator = "";
    public String hashedRootPassword = "";
    public String hashedMasterPassword = "";
    public int numberCutKey = 1; // nombre de morceaux de la clé

    public static final String SHAPER_USERNAME = "Shaper";

    public ChallengeHeader() {}

    public ChallengeHeader(String name, String description, String difficulty) {
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
    }

    public Map.Entry<String, Long> generatePassword(){
        try {
            Generex generex = new Generex(rootPasswordGenerator);
            return new AbstractMap.SimpleEntry<>(generex.random(8), generex.matchedStringsSize());
        }
        catch (Exception ignore){
            return new AbstractMap.SimpleEntry<>(rootPasswordGenerator, 1L);
        }
    }
}
