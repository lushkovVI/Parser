import java.io.FileWriter;
import java.io.IOException;

public class SaveToFile {
    private FileWriter writer;
    SaveToFile(String nameFile) throws Exception {
        try{
            writer = new FileWriter(nameFile, false);
        }
        catch (IOException exp){
            System.out.println(exp.getMessage());
        }
    }

    public void save(Node node) throws IOException {
        writer.write( "node_id : "+node.getId()+"\n");
        writer.write( "parent_id : "+node.getParentId()+"\n");
        writer.write( "node_name : "+node.getNodeName()+"\n");
        writer.write( "node_value : "+node.getNodeValue()+"\n");
        writer.write( "\n\n");
        writer.flush();
    }
}
