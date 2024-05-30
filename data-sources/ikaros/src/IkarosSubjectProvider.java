import kotlin.coroutines.Continuation;
import me.him188.ani.datasources.api.paging.PagedSource;
import me.him188.ani.datasources.api.source.ConnectionStatus;
import me.him188.ani.datasources.api.subject.Subject;
import me.him188.ani.datasources.api.subject.SubjectProvider;
import me.him188.ani.datasources.api.subject.SubjectSearchQuery;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IkarosSubjectProvider implements SubjectProvider {
    @NotNull
    @Override
    public String getId() {
        return IkarosMediaSource.ID;
    }

    @Nullable
    @Override
    public Object testConnection(@NotNull Continuation<? super ConnectionStatus> $completion) {
        return null;
    }

    @NotNull
    @Override
    public PagedSource<Subject> startSearch(@NotNull SubjectSearchQuery query) {
        return null;
    }
}
