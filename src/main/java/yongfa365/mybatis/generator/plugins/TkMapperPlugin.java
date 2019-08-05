package yongfa365.mybatis.generator.plugins;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ReferenceType;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


//代码摘自：https://github.com/abel533/Mapper/blob/master/generator/src/main/java/tk/mybatis/mapper/generator/FalseMethodPlugin.java
public class TkMapperPlugin extends PluginAdapter {
    private Set<String> mappers = new HashSet<String>();
    ShellCallback shellCallback = new DefaultShellCallback(false);
    org.mybatis.generator.api.dom.xml.Document document;

    private Regex regex = new Regex("^Optional\\[(.*?)\\]$");

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String mappers = properties.getProperty("mappers");
        if (StringUtility.stringHasValue(mappers)) {
            this.mappers.addAll(Arrays.asList(mappers.split(",")));
        } else {
            throw new RuntimeException("Mapper插件缺少必要的mappers属性!");
        }
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String targetPackage = super.context.getJavaModelGeneratorConfiguration().getTargetPackage();
        String targetProject = super.context.getJavaModelGeneratorConfiguration().getTargetProject();
        try {
            File directory = shellCallback.getDirectory(targetProject, targetPackage);
            String fileName = introspectedTable.getTableConfiguration().getDomainObjectName() + ".java";
            File javaFile = new File(directory, fileName);

            CompilationUnit oldCU = JavaParser.parse(javaFile);
            NodeList<ImportDeclaration> oldImports = oldCU.getImports();

            // imports
            AddImports(oldImports, topLevelClass);

            NodeList<TypeDeclaration<?>> oldTypes = oldCU.getTypes();

            // types
            for (int i=0; i<oldTypes.size();i++){
                TypeDeclaration oldType = oldTypes.get(i);

                // class annotations
                NodeList<AnnotationExpr> oldTypeAnn = oldType.getAnnotations();
                AddAnnotations(oldTypeAnn, topLevelClass);

                // fields
                List<FieldDeclaration> oldTypeFields = oldType.getFields();
                List<Field> newFields = topLevelClass.getFields();
                AddFields(oldTypeFields, newFields, topLevelClass);

                // methos
                List<MethodDeclaration> oldTypeMethods = oldType.getMethods();
                AddMethods(oldTypeMethods, topLevelClass);

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //获取实体类
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        //import接口
        for (String mapper : mappers) {
            interfaze.addImportedType(new FullyQualifiedJavaType(mapper));
            interfaze.addSuperInterface(new FullyQualifiedJavaType(mapper + "<" + entityType.getShortName() + ">"));
        }
        //import实体类
        interfaze.addImportedType(entityType);

        String targetPackage = super.context.getJavaClientGeneratorConfiguration().getTargetPackage();
        String targetProject = super.context.getJavaClientGeneratorConfiguration().getTargetProject();
        try {
            File directory = shellCallback.getDirectory(targetProject, targetPackage);
            String fileName = introspectedTable.getTableConfiguration().getDomainObjectName() + "Mapper.java";
            File javaFile = new File(directory, fileName);

            CompilationUnit oldCU = JavaParser.parse(javaFile);
            NodeList<ImportDeclaration> oldImports = oldCU.getImports();

            // imports
            AddImports(oldImports, interfaze);

            NodeList<TypeDeclaration<?>> oldTypes = oldCU.getTypes();
            // types
            for (int i=0; i<oldTypes.size();i++){
                TypeDeclaration oldType = oldTypes.get(i);

                // annotations
                NodeList<AnnotationExpr> oldTypeAnn = oldType.getAnnotations();
                AddAnnotations(oldTypeAnn, interfaze);

                // fields
                List<FieldDeclaration> oldTypeFields = oldType.getFields();
                List<Field> newFields = interfaze.getFields();
                AddFields(oldTypeFields, newFields, interfaze);

                // methos
                List<MethodDeclaration> oldTypeMethods = oldType.getMethods();
                AddMethods(oldTypeMethods, interfaze);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

            return true;
    }

    private void SetModifiers(EnumSet<Modifier> modifiers, JavaElement element){
        Iterator<Modifier> it = modifiers.iterator();
        while (it.hasNext()) {
            Modifier modifier = it.next();
            switch (modifier){
                case FINAL:
                    element.setFinal(true);
                    break;
                case STATIC:
                    element.setStatic(true);
                    break;
                case VOLATILE:
                    if(element instanceof Field){
                        Field field = (Field)element;
                        field.setVolatile(true);
                    }
                    break;
                case TRANSIENT:
                    if(element instanceof Field){
                        Field field = (Field)element;
                        field.setTransient(true);
                    }
                    break;
                case NATIVE:
                    if(element instanceof Method){
                        Method method = (Method)element;
                        method.setNative(true);
                    }
                    break;
                case SYNCHRONIZED:
                    if(element instanceof Method){
                        Method method = (Method)element;
                        method.setSynchronized(true);
                    }
                    break;
                case PUBLIC:
                    element.setVisibility(JavaVisibility.PUBLIC);
                    break;
                case DEFAULT:
                    if(element instanceof Method){
                        Method method = (Method)element;
                        method.setDefault(true);
                    }
                    break;
                case PRIVATE:
                    element.setVisibility(JavaVisibility.PRIVATE);
                    break;
                case PROTECTED:
                    element.setVisibility(JavaVisibility.PROTECTED);
                    break;
            }
        }
    }

    private void AddAnnotations( NodeList<AnnotationExpr> oldTypeAnn, JavaElement element){
        if(oldTypeAnn == null || oldTypeAnn.size() < 1){
            return;
        }

        boolean isExist = false;
        for(int j = 0; j < oldTypeAnn.size(); j++){
            AnnotationExpr ota = oldTypeAnn.get(j);
            isExist = false;
            for(java.lang.String na : element.getAnnotations()){
                if(ota.toString().equals(na)) {
                    isExist = true;
                    break;
                }
            }

            if(!isExist){
                element.addAnnotation(ota.toString());
            }
        }
    }

    private void AddFields(List<FieldDeclaration> oldTypeFields, List<Field> newFields, org.mybatis.generator.api.dom.java.CompilationUnit cu){
        boolean isExist = false;
        for(int j =0;j<oldTypeFields.size();j++){
            FieldDeclaration of = oldTypeFields.get(j);
            isExist = false;
            if(of.getVariables().get(0).getName().toString().equals("serialVersionUID")){
                continue;
            }
            for (Field f : newFields){
                if(of.getVariables().get(0).getName().toString().equals(f.getName())) {
                    isExist = true;
                    break;
                }
            }

            if(!isExist){
                Field nf = new Field();
                nf.setName(of.getVariables().get(0).getName().toString());
                nf.setType(new FullyQualifiedJavaType(of.getElementType().toString()));
                /* javaParser's  Type can not get the FullyQualified Name, so it must import the type in import part, and use the type shortname in code.*/
                SetModifiers(of.getModifiers(), nf);
                if(of.getVariables().get(0).getInitializer().isPresent()) {
                    nf.setInitializationString(of.getVariables().get(0).getInitializer().get().toString());
                }

                NodeList<AnnotationExpr> fas = of.getAnnotations();
                for (int k = 0; k < fas.size();k++){
                    AnnotationExpr ae = fas.get(k);
                    nf.addAnnotation(ae.toString());
                }

                if(cu instanceof Interface ){
                    Interface interfaze = (Interface)cu;
                    interfaze.addField(nf);
                }
                else if (cu instanceof  TopLevelClass){
                    TopLevelClass topLevelClass = (TopLevelClass)cu;
                    topLevelClass.addField(nf);
                }
            }
        }
    }

    private void AddImports(NodeList<ImportDeclaration> oldImports, org.mybatis.generator.api.dom.java.CompilationUnit cu){
        boolean isExist = false;
        Set<String> staticImports = cu.getStaticImports();
        Set<FullyQualifiedJavaType> imports = cu.getImportedTypes();

        for(int i =0; i < oldImports.size();i++){
            ImportDeclaration oldImport = oldImports.get(i);
            String oi = oldImport.getNameAsString() + (oldImport.isAsterisk() ? ".*" :"");
            isExist = false;

            if(oldImport.isStatic()){
                for(String nt: staticImports){
                    if (oi.equals(nt)){
                        isExist = true;
                        break;
                    }
                }
            }else {
                for(FullyQualifiedJavaType nt: imports){
                    if (oi.equals(nt.getFullyQualifiedName())){
                        isExist = true;
                        break;
                    }
                }
            }

            if(!isExist){
                if(oldImport.isStatic()){
                    cu.addStaticImport(oi);
                } else {
                    cu.addImportedType(new FullyQualifiedJavaType(oi));
                }
            }
        }
    }

    private void AddMethods(List<MethodDeclaration> oldTypeMethods, org.mybatis.generator.api.dom.java.CompilationUnit cu){
        for (int j=0;j<oldTypeMethods.size();j++){
            MethodDeclaration omd = oldTypeMethods.get(j);
            Method method = new Method(omd.getNameAsString()); // method name
            SetModifiers(omd.getModifiers(), method);  // method modifiers
            method.setReturnType(new FullyQualifiedJavaType(omd.getType().toString())); // method return type
            // method body
            if(omd.getBody().isPresent()) {
                String[] body = omd.getBody().get().toString().split("\r\n");
                for (int k = 1; k < body.length - 1; k++){
                    body[k] = body[k].substring(4);
                }
                method.addBodyLines(Arrays.asList(Arrays.copyOfRange(body, 1, body.length -1)));
            }

            // method parameters
            NodeList<Parameter> ops = omd.getParameters();
            for(int k = 0; k < ops.size(); k ++){
                Parameter op = ops.get(k);
                org.mybatis.generator.api.dom.java.Parameter parameter = new org.mybatis.generator.api.dom.java.Parameter(
                        new FullyQualifiedJavaType(op.getType().toString()),
                        op.getName().toString());
                NodeList<AnnotationExpr> opAn = op.getAnnotations();
                for(int n = 0; n < opAn.size(); n++){
                    parameter.addAnnotation(opAn.get(n).toString());
                }
                method.addParameter(parameter);
            }

            // method annotations
            NodeList<AnnotationExpr> mae = omd.getAnnotations();
            for (int k = 0; k < mae.size();k++){
                method.addAnnotation(mae.get(k).toString());
            }

            // method exceptions
            NodeList<ReferenceType> exceptions = omd.getThrownExceptions();
            for(int k = 0; k < exceptions.size(); k++){
                method.addException(new FullyQualifiedJavaType(exceptions.get(k).toString()));
            }

            if(cu instanceof Interface){
                ((Interface)cu).addMethod(method);
            }
            else if(cu instanceof TopLevelClass){
                ((TopLevelClass)cu).addMethod(method);
            }
        }
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(org.mybatis.generator.api.dom.xml.Document document,
                                           IntrospectedTable introspectedTable) {

        this.document = document;
        return true;
        /*
        String targetPackage = super.context.getSqlMapGeneratorConfiguration().getTargetPackage();
        String targetProject = super.context.getSqlMapGeneratorConfiguration().getTargetProject();
        try
        {
            File directory = shellCallback.getDirectory(targetProject, targetPackage);
            String fileName = introspectedTable.getTableConfiguration().getDomainObjectName() + "Mapper.xml";
            File xmlFile = new File(directory, fileName);
            if (!directory.exists() || !xmlFile.exists()) {
                return true;
            }

            // old exist xml nodes
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new FileInputStream(xmlFile));
            org.w3c.dom.Element rootElement = doc.getDocumentElement();
            NodeList list = rootElement.getChildNodes();

            // new xml nodes
            List<Element> elements = document.getRootElement().getElements();

            // match pattern
            Pattern p = Pattern.compile("<(\\w+)\\s+id=\"(\\w+)\"");

            List<Node> oldDocumentWillRemoveElements = new ArrayList<Node>();

            boolean findSameNode = false;
            // traverse new nodes to compare old nodes to filter
            for (Iterator<Element> elementIt = elements.iterator(); elementIt.hasNext(); ) {
                findSameNode = false;
                String newNodeName = "";
                String NewIdValue = "";
                Element element = elementIt.next();
                Matcher m = p.matcher(element.getFormattedContent(0));
                if (m.find()) {
                    newNodeName = m.group(1);
                    NewIdValue = m.group(2);
                }

                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        if (newNodeName.equals(node.getNodeName())) {
                            NamedNodeMap attr = node.getAttributes();
                            for (int j = 0; j < attr.getLength(); j++) {
                                Node attrNode = attr.item(j);
                                if (attrNode.getNodeName().equals("id") && attrNode.getNodeValue().equals(NewIdValue)) {
                                    //elementIt.remove();
                                    oldDocumentWillRemoveElements.add(node);
                                    findSameNode = true;
                                    break;
                                }
                            }
                            if (findSameNode == true)
                                break;
                        }
                    }
                }
            }

            // remove old xml nodes that exist in new xml
            for(Node n : oldDocumentWillRemoveElements){
                rootElement.removeChild(n);
            }
            System.out.println("deleted same nodes from old document:" + oldDocumentWillRemoveElements.size());

            // save old xml document
//            TransformerFactory tff = TransformerFactory.newInstance();
//            Transformer tf = tff.newTransformer();
//            tf.setOutputProperty(OutputKeys.INDENT, "no");
//            tf.transform(new DOMSource(doc), new StreamResult(xmlFile));

            DomWriter dw = new DomWriter();
            String s = dw.toString(doc);
            FileOutputStream fos = new FileOutputStream(xmlFile, false);
            OutputStreamWriter osw;

                osw = new OutputStreamWriter(fos, "UTF-8");


            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(s);
            bw.close();
            System.out.println("saved old document");
        }
        catch (Exception ex){
            System.out.println("【Error】");
            ex.printStackTrace();
        }


        return true;
        */
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap,
                                   IntrospectedTable introspectedTable) {

        return true;
    }

    // region false returns

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectAllMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    @Override
    public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        return false;
    }

    // endregion

}
