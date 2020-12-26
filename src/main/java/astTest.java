/*     */ import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;

import javax.swing.*;


public class astTest
{

    public static ArrayList<String> actList = new ArrayList<String>();
    public static ArrayList<String> stateList = new ArrayList<String>();
    public static ArrayList<String> stateTable = new ArrayList<String>();

    public static boolean actFlag = true;
    public static boolean stateFlag = false;
    public static int tableFlag = 0;
    public static int count = 0;
    public static void main(String[] args) throws Exception
    {
//        FileContent fileContent = FileContent.createForExternalFileLocation("E:\\test_work\\efsm.c");
        FileContent fileContent = FileContent.createForExternalFileLocation("D:\\collage\\test\\efsm.c");

        Map definedSymbols = new HashMap();
        String[] includePaths = new String[0];
        IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
        IParserLogService log = new DefaultLogService();

        IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();

        int opts = 8;
        IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info, emptyIncludes, null, opts, log);
//            获取include部分
        IASTPreprocessorIncludeStatement[] includes = translationUnit.getIncludeDirectives();
        for (IASTPreprocessorIncludeStatement include : includes) {
            System.out.println("include - " + include.getName());
        }
        printTree(translationUnit, 1);
        System.out.println("1111111111111111111111111");
        System.out.println(actList);
        System.out.println(stateList);
        System.out.println(stateTable);

        System.out.println("-----------------------------------------------------");
        int length = actList.size() - 1;
        int width = stateTable.size() / length;
        String [][]action = new String[length][width];
        int n = 0;
        for (int i = 0; i < length; i++) {
            for(int j = 0; j < width; j++) {
                action[i][j] = stateTable.get(n);
                n++;
            }
        }
        System.out.println("-----------------------------------------------------");
        for (int i = 0; i < length; i++) {
            for(int j = 0; j < width; j++) {
                System.out.print(action[i][j] + "  ");
            }
            System.out.println();
        }
        System.out.println("-----------------------------------------------------");

        ASTVisitor visitor = new ASTVisitor()
        {
            public int visit(IASTName name)
            {
                if ((name.getParent() instanceof CPPASTFunctionDeclarator)) {
                    System.out.println("IASTName: " + name.getClass().getSimpleName() + "(" + name.getRawSignature() + ") - > parent: " + name.getParent().getClass().getSimpleName());
                    System.out.println("-- isVisible: " + ParserExample.isVisible(name));
                }
                return 3;
            }

            public int visit(IASTDeclaration declaration)
            {
                System.out.println("declaration: " + declaration + " ->  " + declaration.getRawSignature());

                if ((declaration instanceof IASTSimpleDeclaration)) {
                    IASTSimpleDeclaration ast = (IASTSimpleDeclaration)declaration;
                    try
                    {
                        System.out.println("--- type: " + ast.getSyntax() + " (childs: " + ast.getChildren().length + ")");
                        IASTNode typedef = ast.getChildren().length == 1 ? ast.getChildren()[0] : ast.getChildren()[1];
                        System.out.println("------- typedef: " + typedef);
                        IASTNode[] children = typedef.getChildren();
                        if ((children != null) && (children.length > 0))
                            System.out.println("------- typedef-name: " + children[0].getRawSignature());
                    }
                    catch (ExpansionOverlapsBoundaryException e)
                    {
                        e.printStackTrace();
                    }

                    IASTDeclarator[] declarators = ast.getDeclarators();
                    for (IASTDeclarator iastDeclarator : declarators) {
                        System.out.println("iastDeclarator > " + iastDeclarator.getName());
                    }

                    IASTAttribute[] attributes = ast.getAttributes();
                    for (IASTAttribute iastAttribute : attributes) {
                        System.out.println("iastAttribute > " + iastAttribute);
                    }

                }

                if ((declaration instanceof IASTFunctionDefinition)) {
                    IASTFunctionDefinition ast = (IASTFunctionDefinition)declaration;
                    IScope scope = ast.getScope();
                    try
                    {
                        System.out.println("### function() - Parent = " + scope.getParent().getScopeName());
                        System.out.println("### function() - Syntax = " + ast.getSyntax());
                    }
                    catch (DOMException e) {
                        e.printStackTrace();
                    } catch (ExpansionOverlapsBoundaryException e) {
                        e.printStackTrace();
                    }
                    ICPPASTFunctionDeclarator typedef = (ICPPASTFunctionDeclarator)ast.getDeclarator();
                    System.out.println("------- typedef: " + typedef.getName());
                }

                return 3;
            }

            public int visit(IASTTypeId typeId)
            {
                System.out.println("typeId: " + typeId.getRawSignature());
                return 3;
            }

            public int visit(IASTStatement statement)
            {
                System.out.println("statement: " + statement.getRawSignature());
                return 3;
            }

            public int visit(IASTAttribute attribute)
            {
                return 3;
            }
        };
        visitor.shouldVisitNames = true;
        visitor.shouldVisitDeclarations = false;

        visitor.shouldVisitDeclarators = true;
        visitor.shouldVisitAttributes = true;
        visitor.shouldVisitStatements = false;
        visitor.shouldVisitTypeIds = true;
///*     */ typeId: eKEY_ACT
        translationUnit.accept(visitor);
    }

    private static void printTree(IASTNode node, int index) {

        IASTNode[] children = node.getChildren();

        boolean printContents = true;

        if ((node instanceof CPPASTTranslationUnit)) {
            printContents = false;
        }

        String offset = "";
        try {
            offset = node.getSyntax() != null ? " (offset: " + node.getFileLocation().getNodeOffset() + "," + node.getFileLocation().getNodeLength() + ")" : "";
            printContents = node.getFileLocation().getNodeLength() < 30;
        } catch (ExpansionOverlapsBoundaryException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            offset = "UnsupportedOperationException";
        }
        System.out.println(String.format(new StringBuilder("%1$").append(index * 2).append("s").toString(), new Object[] { "-" }) + node.getClass().getSimpleName() + offset + " -> " + (printContents ? node.getRawSignature().replaceAll("\n", " \\ ") : node.getRawSignature().subSequence(0, 5)));

        String value = node.getClass().getSimpleName();

        if (value.equals("CPPASTArrayDeclarator")) {
            count++;
        }
        if(value.equals("CPPASTArrayModifier")) {
            tableFlag++;
        }
        System.out.println("count: " + count + " tableFlag: " + tableFlag);
        if (value.equals("CPPASTDeclarator")){
            actFlag = false;
            stateFlag = true;
        }
        if (value.equals("CPPASTEnumerator")){
            String k = (printContents ? node.getRawSignature().replaceAll("\n", " \\ ") : (String) node.getRawSignature().subSequence(0, 5));
            if (actFlag){
                actList.add(k);
            }
            if (stateFlag){
                stateList.add(k);
            }
        }
        if(value.equals("CPPASTIdExpression") && count == 3 && tableFlag == 4){
            String k = (printContents ? node.getRawSignature().replaceAll("\n", " \\ ") : (String) node.getRawSignature().subSequence(0, 5));
            stateTable.add(k);
        }


        for (IASTNode iastNode : children){
            printTree(iastNode, index + 1);
        }

    }

    public static boolean isVisible(IASTNode current)
    {
        IASTNode declator = current.getParent().getParent();
        IASTNode[] children = declator.getChildren();

        for (IASTNode iastNode : children) {
            if ((iastNode instanceof ICPPASTVisibilityLabel)) {
                return 1 == ((ICPPASTVisibilityLabel)iastNode).getVisibility();
            }
        }

        return false;
    }

}

/* Location:           /media/Dados/Codigos/C_Plus/Projetos/eclipse-cdt-standalone-astparser/bin/
 * Qualified Name:     ParserExample
 * JD-Core Version:    0.6.0
 */