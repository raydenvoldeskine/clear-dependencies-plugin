import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

abstract public class ProjectAnalyser {

    private CodeProcessor processor = new CodeProcessorUnknown(this);

    @Nullable  protected Project project;

    ProjectAnalyser(@Nullable  Project project){
        this.project = project;
    }

    abstract Optional<String> getCorrespondingPackageID(String codePackageID);

    public void setCurrentEditor(FileEditor editor){
        if (editor != null && project != null) {
            VirtualFile file = editor.getFile();
            if (file != null) {
                PsiFile currentFile = PsiManager.getInstance(project).findFile(file);
                processor = CodeProcessorFactory.createProcessor(currentFile, this);
            }
        }
   }

    public Optional<ArrayList<Dependency>> getOutgoingList(){
        return processor.getOutgoingList();
    }

    public Optional<ArrayList<Dependency>> getIncomingList(){
        return processor.getIncomingList();
    }


    @Nullable
    public Project getProject() {
        return project;
    }


}
