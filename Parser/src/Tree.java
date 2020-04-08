import java.io.IOException;
import java.util.Iterator;

public class Tree {
    public Node root;
    private SaveToFile savetofile;
    Tree() throws Exception {
         savetofile = new SaveToFile("parsed.txt");
    }

    public void saveTreeToFile(Node node,boolean rootIsAdd) throws IOException {
        if(!rootIsAdd){
            savetofile.save(node);
        }
        for (Node temp : node.childList) {
            savetofile.save(temp);
            saveTreeToFile(temp,true);
        }
    }


    public void addNode(Integer id,Integer parentid,String name,String value) {
        Node node = new Node(id,parentid,name,value);
        if (root == null) {
            root = node;
        } else {
            Node current = root;
            Node prev = null;
            prev = current;

            if (parentid.equals(prev.getId())) {
                prev.childList.add(node);
            }
            else {
                findParentNode(current,node, parentid,false);
            }
        }
    }

    private void findParentNode(Node root ,Node node,int parentid,boolean isFind){
        if(!isFind){
            for (Node temp : root.childList) {
                if (temp.getId() == parentid) {
                    temp.childList.add(node);
                    isFind = true;
                }
                findParentNode(temp,node,parentid,isFind);
            }
        }
    }
}
