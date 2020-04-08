import java.util.LinkedList;

public class Node {
    private Integer id;
    private Integer parentId;
    private String nodeName;
    private String nodeValue;
    LinkedList<Node> childList;

    Node(Integer id,Integer parentid,String nodeName,String nodeValue){
        this.id = id;
        this.parentId = parentid;
        this.nodeName = nodeName;
        this.nodeValue = nodeValue;
        this.childList = new LinkedList<>();
    }

    public Integer getId(){
        return id;
    }

    public Integer getParentId(){
        return parentId;
    }

    public String getNodeName(){
        return nodeName;
    }

    public String getNodeValue(){
        return nodeValue;
    }

    public void printNode(){
        System.out.print("id = " + id + " idparent = "+ parentId + "  nodeName = " + nodeName + " nodeValue = " + nodeValue);
    }
}
