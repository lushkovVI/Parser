import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private File file;
    private StringBuilder textFromFile;
    private boolean isChange = false;
    private Tree tree;
    private Integer id = 0;
    private StringBuilder compareString;

    Parser(String str) throws Exception {
        this.file = new File(str);
        this.textFromFile = new StringBuilder();
        this.compareString = new StringBuilder();
        tree = new Tree();
    }

    public void readFile() throws IOException {
        FileReader filereader = new FileReader(this.file);
        BufferedReader bufreader = new BufferedReader(filereader);
        while(bufreader.ready()){
            textFromFile.append(bufreader.readLine()).append("\n");
        }
    }

    private void saveToFile() throws IOException {
        tree.saveTreeToFile(tree.root,false);
    }

    private Matcher getMather(String regExp,StringBuilder sourceString){
        Pattern pattern = Pattern.compile(regExp);
        return pattern.matcher(sourceString);
    }

    private void parseFile(StringBuilder str,Integer parentId) throws Exception {
        if(!isChange){
            str = getNodeChild(str,parentId);
            compareString = new StringBuilder(str);
        }
        if(isChange){
            isChange = false;
            str = getNode(str,parentId);
            str = getNodeList(str,parentId);
            parseFile(str,parentId);
            if(compareString.toString().equals(str.toString()) && compareString.length() > 1)
            {
                throw new Exception("Неверный формат данных");
            }
        }
    }

    public void parseFile() throws Exception {
        StringBuilder str = getRootNode();
        if(!isChange){
            throw new Exception("Неверный формат данных");
        }
        parseFile(str,0);
        saveToFile();
    }

    private StringBuilder getRootNode(){
        String regExp = "(^[A-Za-z_]+[A-Za-z0-9_]+\\s?)(=)(\\s*)[{](\\R)";
        Matcher matherName = getMather(regExp,textFromFile);
        if(matherName.find()){
            regExp = "[}]$";
            Matcher mather = getMather(regExp,textFromFile);
            if(mather.find()){
                tree.addNode(id,null,matherName.group(1),textFromFile.substring(matherName.end(),textFromFile.length()-2).replaceAll("\n",""));
                id++;
                isChange = true;
                textFromFile = new StringBuilder(textFromFile.substring(matherName.end(),textFromFile.length()-2));
            }
        }
        return  textFromFile;
    }

    private StringBuilder getNodeChild(StringBuilder tmp,Integer parentId) throws Exception {
        StringBuilder sourceString = new StringBuilder(tmp);
        StringBuilder bufferString = new StringBuilder(tmp);

        String regExp = "(^\\R?[A-Za-z_]+[A-Za-z0-9_]*\\s?)(=)(\\s*)[{](\\R)";
        Matcher matherName = getMather(regExp,sourceString);
        while(matherName.find()){
            String temp = new String(sourceString);
            int end = 0;
            end = findBorders(temp,matherName);
            if(end != 0){
                tree.addNode(id,parentId,matherName.group(1).replaceAll("\n",""),sourceString.substring(matherName.end(),end-1).replaceAll("\n",""));
                isChange = true;
                StringBuilder innerString = new StringBuilder(sourceString.substring(matherName.end(),end-1));
                bufferString = new StringBuilder(sourceString.substring(end, sourceString.length()));
                if(innerString.length() > 2){
                    id++;
                    parseFile(innerString,id-1);
                }
                parseFile(bufferString,parentId);
                isChange = true;
                return bufferString;
            }
        }
        return bufferString;
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



    private StringBuilder getNode(StringBuilder tmp,Integer parent){
        StringBuilder sourceString = new StringBuilder(tmp);
        StringBuilder bufferString = new StringBuilder(tmp);

        String regExp ="([A-Za-z_]+[A-Za-z0-9_]+\\s?)(=)(\\s?“[A-Za-z_]+[A-Za-z0-9_]+”\\R)";
        Matcher mather = getMather(regExp,sourceString);

        while(mather.find()){
            if(checkValidNode(sourceString,bufferString,mather.start(),mather.end())){
                bufferString.delete(mather.start(),mather.end());
                tree.addNode(id,parent,mather.group(1).replaceAll("\n",""),getValue(mather.group(0),'“','”').replaceAll("\n",""));
                id++;
                isChange = true;
                sourceString = new StringBuilder(bufferString);
            }
        }
        return bufferString;
    }

    private StringBuilder getNodeList(StringBuilder tmp,Integer parentId) throws Exception {
        StringBuilder sourceString = new StringBuilder(tmp);
        StringBuilder bufferString = new StringBuilder(tmp);
        String regExp ="(^\\R?[A-Za-z_]+[A-Za-z0-9_]*\\s?)(=)(\\s?)[{][(\\s?\\w+\\s*=\\s*“\\w+”)]+[}](\\R*)";
        Matcher mather = getMather(regExp,sourceString);
        while(mather.find()){
            if(checkValidNode(sourceString,bufferString,mather.start(),mather.end())){
                bufferString.delete(mather.start(),mather.end());
                String values = getValue(mather.group(0),'{','}');
                tree.addNode(id,parentId,mather.group(1).replaceAll("\n",""),values.replaceAll("\n",""));
                id++;
                isChange = true;
                getNodesFromNodeList(values.replaceAll("\n",""),id-1);
            }
        }
        return bufferString;
    }

    private void getNodesFromNodeList(String value,int parentId) throws Exception {
        String regExp ="(\\s?[A-Za-z_]+[A-Za-z0-9_]*\\s?)(=)(\\s*“\\w+”\\s*)";
        Pattern pattern = Pattern.compile(regExp);
        Matcher mather = pattern.matcher(value);
        String stringCheck = new String(value);
        while(mather.find()) {
            stringCheck = stringCheck.replace(mather.group(0),"");
            tree.addNode(id,parentId,mather.group(1).replaceAll("\n",""),getValue(mather.group(3),'“','”').replaceAll("\n",""));
            id++;
        }
        if(stringCheck.length() != 0){
            throw new Exception("Неверный формат данных");
        }
    }

    private String getValue(String value,char c1,char c2){
        int start = value.indexOf(c1);
        int end = value.indexOf(c2);
        return value.substring(start+1,end);
    }

    private boolean checkValidNode(StringBuilder sourceString ,StringBuilder bufferString,int start,int end){
        if(start == 0 || start ==1){
            return true;
        }
        if(end == bufferString.length()){
            return true;
        }
        String str = sourceString.substring(start - 3,start);
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
    }
}


