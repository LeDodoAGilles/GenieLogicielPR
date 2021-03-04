package ShellNightmare.Terminal;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ShellNightmare.Terminal.DocTopic.DESCRIPTION;
import static ShellNightmare.Terminal.DocTopic.SYNOPSIS;

/**
 * classe représentant la génération des ressources pour la commande man
 * @author Gaëtan Lounes
 */
public class DocTemplate {
    public Map<String, HashMap<String,String>> doc = new HashMap<>();
    public void registerNewCommandDoc(String command){
        if (!doc.containsKey(command))
            doc.put(command,new HashMap<>());
    }

    public void registerCommandTopicData(String command, DocTopic topic, String data) {
        var h = doc.get(command);
        if (!h.containsKey(topic.toString()))
            h.put(topic.toString(), data);

        if (topic == DESCRIPTION) {
            var e = MetaContext.registerC.optionnalArgs.get(command);
            var f = e.keys();
            var g = MetaContext.registerC.getLongOpt(command);

            while (f.hasMoreElements()) {
                Character v = f.nextElement();
                String nextE = v.toString();
                for (int i = 0; i < g.length; i++) {
                    if (g[i].getVal() == v)
                        nextE = nextE + " (--" + g[i].getName() + ')';

                    if (!h.containsKey(nextE))
                        h.put(nextE, data);
                }


            }

        }
    }

    public void initDocTemplate(){
        for (String command : MetaContext.registerC.getCommandsString()){
            registerNewCommandDoc(command);
            for (DocTopic dt : DocTopic.values())
                registerCommandTopicData(command,dt,"WIP");
        }
    }
    public String formatCommand(String command){
        StringBuilder sb = new StringBuilder();
        HashMap<String,String> commandData = doc.get(command);
        for (DocTopic dt : DocTopic.values()){
            String data = commandData.get(dt.name());
            if (data.equals("WIP"))
                continue;
            sb.append("\n");
            sb.append(dt.name().replace("_"," "));
            sb.append(":\n");
            sb.append(data);

            if (dt==SYNOPSIS){
                for (String opt : commandData.keySet()){
                    if (Arrays.stream(DocTopic.values()).anyMatch(s->s.name().equals(opt)))
                        continue;
                    data = commandData.get(opt);

                    if (data.equals("WIP"))
                        continue;

                    sb.append("-");
                    sb.append(opt);
                    sb.append(":\n");
                    sb.append(data);
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    public static DocTemplate loadFromJson(){
        DocTemplate dt = new DocTemplate();

        try {

            File doc = new File("doc.json");
            ObjectMapper om = new ObjectMapper();
            if (doc.exists())
                dt = om.readValue(doc, DocTemplate.class);

                //dt.initDocTemplate();
                //om.writeValue(doc, dt);

                // TODO String content = Files.readString(Paths.get(dt.getClass().getResource("/Texts/doc.json").toExternalForm()), StandardCharsets.UTF_8);
                //String content = Files.readString(Paths.get("doc.json"), StandardCharsets.UTF_8);
                //content = content.replace("\",","\",\n").replace(",\"","\n,\"");
                //Writer writer;
                //writer = new FileWriter(new File("doc.json"));
                //writer.write(content);
                //writer.close();

            }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        return dt;
    }

}
