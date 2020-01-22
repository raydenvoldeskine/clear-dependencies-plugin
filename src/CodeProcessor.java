
import java.util.ArrayList;
import java.util.Optional;

abstract public class CodeProcessor {

    protected ProjectAnalyser analyser;
    CodeProcessor(ProjectAnalyser analyser){
        this.analyser = analyser;
    }

    abstract public Optional<ArrayList<Dependency>> getOutgoingList();
    abstract public Optional<ArrayList<Dependency>> getIncomingList();
}
