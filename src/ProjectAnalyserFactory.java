import com.intellij.openapi.project.Project;

public class ProjectAnalyserFactory {

    public static ProjectAnalyser createAnalyser(Project project){
        return new ProjectAnalyserAndroid(project);
    }
}
