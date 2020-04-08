import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private File file;
    private StringBuilder strbuilder;
    private boolean isChange = false;
    public Tree tree;
    private Integer id = 0;


    Parser(String str) throws Exception {
        this.file = new File(str);
        this.strbuilder = new StringBuilder();
        tree = new Tree();
    }

    void readFile() throws IOException {
        FileReader filereader = new FileReader(this.file);
        BufferedReader bufreader = new BufferedReader(filereader);
        while(bufreader.ready()){
            strbuilder.append(bufreader.readLine()+"\n");
        }
    }


    private void saveToFile() throws IOException {
        tree.saveTreeToFile(tree.root,false);
    }

    private StringBuilder getRootNode(){
        int endIndex=0;
        String regExp = "(\\w+)(\\s*)(=)(\\s*)[{](\\R)";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matherName = pattern.matcher(strbuilder);
        if(matherName.find()){
            endIndex = matherName.end();
            regExp = "[}]$";
            pattern = Pattern.compile(regExp);
            Matcher mather = pattern.matcher(strbuilder);
            if(mather.find()){
                tree.addNode(id,0,matherName.group(1),strbuilder.substring(endIndex,strbuilder.length()-2));
                id++;
                isChange = true;
                strbuilder = new StringBuilder(strbuilder.substring(endIndex,strbuilder.length()-2));
                System.out.println(strbuilder);
            }
        }
        return  strbuilder;
    }



    private void parseFile(StringBuilder str,Integer parentId){
        if(!isChange){
            str = getNodeListOfLists(str,parentId);
        }
        if(isChange){
            isChange = false;
            str = getNode(str,parentId);
            str = getNodeList(str,parentId);
            parseFile(str,parentId);
        }
    }

    public void parseFile() throws Exception {
        StringBuilder str = getRootNode();
        if(!isChange){
            throw new Exception("Неверный формат данных");
        }
        parseFile(str,0);
    }

    private StringBuilder getNodeListOfLists(StringBuilder buffer,Integer parentId){
        StringBuilder tempStrB = new StringBuilder(buffer);
        StringBuilder tempStrB2 = new StringBuilder(buffer);

        String regExp = "(\\w+)(\\s*)(=)(\\s*)[{](\\R)";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matherName = pattern.matcher(tempStrB);
        while(matherName.find()){
            String temp = new String(tempStrB);
            int end = 0;
            end = findBorders(temp,matherName);

            System.out.println(temp);
            System.out.println(tempStrB);
            if(end != 0){
                tree.addNode(id,parentId,matherName.group(1),tempStrB.substring(matherName.end(),end-1));
                isChange = true;
                StringBuilder outerString = new StringBuilder(tempStrB.substring(matherName.end(),end-1));
                tempStrB2 = new StringBuilder(tempStrB.substring(end, tempStrB.length()));
                if(outerString.length() > 2){
                    id++;
                    parseFile(outerString,id-1);
                }
                parseFile(tempStrB2,parentId);
                return tempStrB2;
            }
        }
        return tempStrB2;
    }

    private int findBorders(String temp,Matcher matherName ){
        int count1 = 1;
        int count2 = 0;
        int end = 0;
        for(int i=matherName.end() ; i<= temp.length();i++){
            if(count1 == count2){
                end = i;
                break;
            }
            if(temp.charAt(i) == '{'){
                count1 ++;
            }
            if(temp.charAt(i) == '}'){
                count2 ++;
            }
        }
        return end;

    }

    private StringBuilder getNodeList(StringBuilder buffer,Integer parentId){
        StringBuilder tempStrB = new StringBuilder(buffer);
        StringBuilder tempStrB2 = new StringBuilder(buffer);
        String regExp ="(^[A-Za-z_]+[A-Za-z0-9_]+\\s?)(=)(\\s?)[{][(\\s?\\w+\\s*=\\s*“\\w+”)]+[}](\\R*)";
        Pattern pattern = Pattern.compile(regExp);
        Matcher mather = pattern.matcher(tempStrB);
        while(mather.find()){
            if(checkValidNode(tempStrB,tempStrB2,mather.start(),mather.end())){
                tempStrB2.delete(mather.start(),mather.end());
                String values = getValue(mather.group(0),'{','}');
                tree.addNode(id,parentId,mather.group(1),values);
                id++;
                isChange = true;
                getNodesFromNodeList(values,id-1);
            }
        }
        return new StringBuilder(tempStrB2);
    }
    private StringBuilder getNode(StringBuilder buffer,Integer parent){
        StringBuilder tempStrB = new StringBuilder(buffer);
        StringBuilder tempStrB2 = new StringBuilder(buffer);

        String regExp ="(^[A-Za-z_]+[A-Za-z0-9_]+\\s*)(=)(\\s?“[A-Za-z_]+[A-Za-z0-9_]+”\\R)";
        Pattern pattern = Pattern.compile(regExp);
        Matcher mather = pattern.matcher(tempStrB);

        while(mather.find()){
            if(checkValidNode(tempStrB,tempStrB2,mather.start(),mather.end())){
                tempStrB2.delete(mather.start(),mather.end());
                tree.addNode(id,parent,mather.group(1),getValue(mather.group(0),'“','”'));
                id++;
                isChange = true;
                tempStrB = new StringBuilder(tempStrB2);
            }
        }
        return tempStrB2;
    }

    private void getNodesFromNodeList(String value,int parentId){
        String regExp ="(\\s*\\w+\\s*)(=)(\\s*“\\w+”\\s*)";
        Pattern pattern = Pattern.compile(regExp);
        Matcher mather = pattern.matcher(value);
        while(mather.find()) {
            tree.addNode(id,parentId,mather.group(1),getValue(mather.group(3),'“','”'));
            id++;
        }
    }

    private String getValue(String value,char c1,char c2){
        int start = value.indexOf(c1);
        int end = value.indexOf(c2);
        return value.substring(start+1,end);
    }



    private boolean checkValidNode(StringBuilder tempB ,StringBuilder tempB2,int start,int end){
        if(start == 0 || start ==1){
            return true;
        }
        if(end == tempB2.length()){
            return true;
        }
        String str = tempB.substring(start - 3,start);
        if( str.charAt(str.length()-1) == ('{')){
            return false;
        }
        if( str.charAt(str.length()-1) == ('}')){
            return true;
        }
        return false;
    }



    public static void main(String[] args) throws Exception {
         Parser prs = new Parser("source.txt");
         prs.readFile();
         prs.parseFile();
         prs.tree.saveTreeToFile(prs.tree.root,false);
    }
}


