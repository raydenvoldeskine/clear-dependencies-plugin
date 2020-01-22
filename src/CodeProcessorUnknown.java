
import java.util.ArrayList;
import java.util.Optional;

public class CodeProcessorUnknown extends CodeProcessor {
    CodeProcessorUnknown(ProjectAnalyser analyser) {
        super(analyser);
    }

    @Override
    public Optional<ArrayList<Dependency>> getOutgoingList() {
        return Optional.empty();
    }

    @Override
    public Optional<ArrayList<Dependency>> getIncomingList() {
        return Optional.empty();
    }
}
