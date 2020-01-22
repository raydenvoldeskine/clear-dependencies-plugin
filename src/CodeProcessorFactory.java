import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

public class CodeProcessorFactory {

    public static CodeProcessor createProcessor(PsiFile psiFile, ProjectAnalyser analyser){
        if (psiFile instanceof PsiJavaFile){
            return new CodeProcessorJava((PsiJavaFile)psiFile, analyser);
        }
        return new CodeProcessorUnknown(analyser);
    }
}
